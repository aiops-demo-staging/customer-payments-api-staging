# customer-payments-api

Customer-facing entry point for paying invoices; submits invoice payments to Billing.

Owned by **Digital Channels**. Java 21. Registered as **Customer Payments API**.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/payment-requests` | Submit a customer payment for an invoice |
| `GET` | `/payment-requests/:paymentRequestId` | Read a single payment request |
| `GET` | `/customers/:customerId/payment-requests` | List a customer's payment requests |
| `GET` | `/health` | Liveness |

Calls **Billing** synchronously to submit an invoice payment.

```bash
mvn -q -DskipTests package
java -jar target/customer-payments-api-3.5.0.jar
```
