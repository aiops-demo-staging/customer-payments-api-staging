# customer-payments-api

Customer-facing entry point for paying invoices; submits invoice payments to Billing.

Owned by **Digital Channels**. Registered in the Atlassian Service Registry as **Customer Payments API**.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/payment-requests` | Submit a customer payment for an invoice |
| `GET` | `/payment-requests/:paymentRequestId` | Read a single payment request |
| `GET` | `/customers/:customerId/payment-requests` | List a customer's payment requests |
| `GET` | `/health` | Liveness |
| `GET` | `/ready` | Readiness |
| `GET` | `/metrics` | Counts by status |

## Quick start

```bash
npm install
npm start
```

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `3000` | HTTP listen port |
| `SERVICE_NAME` | `customer-payments-api` | Service name in logs and telemetry |
| `RELEASE` | `payment-api-<package version>` | Release identifier in logs and telemetry |
| `API_KEY` | unset | When set, requests must send `x-api-key` |
| `RATE_LIMIT_WINDOW_MS` | `60000` | Rate-limit window |
| `RATE_LIMIT_MAX` | `100` | Requests per window per client |

## Architecture

Routes parse and validate, controllers map to HTTP, the service layer holds the business
rules, and the repository keeps state in an in-memory `Map`. There is no external database.

## Dependencies

Calls **Billing** synchronously to submit an invoice payment.
