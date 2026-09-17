package com.beaconstone.paymentapi;

import java.util.Map;
import java.util.UUID;

/**
 * Billing client.
 *
 * Beaconstone's demo estate runs each service in isolation, so this stands in for the
 * synchronous call to Billing. The request and response shapes match the real contract,
 * which is what makes the dependency legible in the service graph.
 */
final class BillingClient {
    static Map<String, Object> submitInvoicePayment(String invoiceId, int amountMinor, String network) {
        return Map.of(
            "accepted", true,
            "unavailable", false,
            "billingReference", UUID.randomUUID().toString(),
            "invoiceId", invoiceId,
            "network", network == null ? "" : network
        );
    }

    private BillingClient() {}
}
