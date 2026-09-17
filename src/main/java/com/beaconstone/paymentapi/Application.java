package com.beaconstone.paymentapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Customer Payments API. Owns the customer-facing payment-request record and forwards
 * the submission to Billing. Beaconstone's demo estate runs each service in isolation,
 * so {@link BillingClient} stands in for that synchronous call.
 */
public final class Application {
    private static final String SERVICE = JsonHttp.serviceName("customer-payments-api");
    private static final String RELEASE = JsonHttp.release("payment-api-3.5.0");
    private static final Map<String, Map<String, Object>> STORE = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = JsonHttp.server(JsonHttp.port());
        server.createContext("/health", Application::health);
        server.createContext("/ready", ex -> JsonHttp.json(ex, 200, Map.of("status", "ready")));
        server.createContext("/metrics", Application::metrics);
        server.createContext("/payment-requests", Application::paymentRequests);
        server.createContext("/customers/", Application::customerRequests);
        server.start();
        System.out.println("{\"level\":\"info\",\"message\":\"customer-payments-api started\",\"service\":\""
            + SERVICE + "\",\"release\":\"" + RELEASE + "\",\"port\":" + JsonHttp.port() + "}");
    }

    private static void health(HttpExchange exchange) throws java.io.IOException {
        JsonHttp.json(exchange, 200, Map.of("status", "ok", "service", SERVICE, "release", RELEASE));
    }

    private static void metrics(HttpExchange exchange) throws java.io.IOException {
        JsonHttp.json(exchange, 200, Map.of(
            "service", SERVICE,
            "release", RELEASE,
            "paymentRequests", STORE.size()
        ));
    }

    private static void paymentRequests(HttpExchange exchange) throws java.io.IOException {
        String path = exchange.getRequestURI().getPath();
        if ("POST".equals(exchange.getRequestMethod()) && "/payment-requests".equals(path)) {
            submit(exchange);
            return;
        }
        if ("GET".equals(exchange.getRequestMethod()) && path.startsWith("/payment-requests/")) {
            String id = path.substring("/payment-requests/".length());
            Map<String, Object> record = STORE.get(id);
            if (record == null) {
                JsonHttp.json(exchange, 404, Map.of("error", "PaymentRequestNotFound"));
                return;
            }
            JsonHttp.json(exchange, 200, record);
            return;
        }
        JsonHttp.json(exchange, 404, Map.of("error", "NotFound", "path", path));
    }

    private static void customerRequests(HttpExchange exchange) throws java.io.IOException {
        var match = java.util.regex.Pattern.compile("^/customers/([^/]+)/payment-requests$")
            .matcher(exchange.getRequestURI().getPath());
        if (!match.matches()) {
            JsonHttp.json(exchange, 404, Map.of("error", "NotFound"));
            return;
        }
        String customerId = match.group(1);
        List<Map<String, Object>> found = new ArrayList<>();
        for (Map<String, Object> record : STORE.values()) {
            if (customerId.equals(record.get("customerId"))) {
                found.add(record);
            }
        }
        JsonHttp.json(exchange, 200, Map.of("customerId", customerId, "paymentRequests", found));
    }

    private static void submit(HttpExchange exchange) throws java.io.IOException {
        Map<String, Object> body = JsonHttp.readJson(exchange);
        String customerId = str(body.get("customerId"));
        String invoiceId = str(body.get("invoiceId"));
        int amountMinor = num(body.get("amountMinor"));
        if (customerId == null || invoiceId == null) {
            JsonHttp.json(exchange, 422, Map.of("error", "MissingIdentifiers", "requestId", JsonHttp.requestId(exchange)));
            return;
        }
        if (amountMinor <= 0) {
            JsonHttp.json(exchange, 422, Map.of("error", "InvalidAmount", "requestId", JsonHttp.requestId(exchange)));
            return;
        }

        String clientReference = str(body.get("clientReference"));
        if (clientReference != null) {
            for (Map<String, Object> existing : STORE.values()) {
                if (clientReference.equals(existing.get("clientReference"))) {
                    JsonHttp.json(exchange, 201, existing);
                    return;
                }
            }
        }

        Map<String, Object> billing = BillingClient.submitInvoicePayment(invoiceId, amountMinor, str(body.get("network")));
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("paymentRequestId", UUID.randomUUID().toString());
        record.put("submittedAt", Instant.now().toString());
        record.put("customerId", customerId);
        record.put("invoiceId", invoiceId);
        record.put("amountMinor", amountMinor);
        record.put("currency", body.getOrDefault("currency", "USD"));
        record.put("network", body.get("network"));
        record.put("clientReference", clientReference);
        record.put("status", Boolean.TRUE.equals(billing.get("accepted")) ? "submitted" : "rejected");
        record.put("billingReference", billing.get("billingReference"));
        STORE.put((String) record.get("paymentRequestId"), record);
        JsonHttp.json(exchange, 201, Map.of(
            "paymentRequestId", record.get("paymentRequestId"),
            "invoiceId", invoiceId,
            "status", record.get("status"),
            "billingReference", record.get("billingReference"),
            "requestId", JsonHttp.requestId(exchange)
        ));
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static int num(Object value) {
        return value instanceof Number n ? n.intValue() : 0;
    }

    private Application() {}
}
