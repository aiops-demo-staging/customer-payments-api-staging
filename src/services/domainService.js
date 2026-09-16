'use strict';

const { domainRepository } = require('../repositories/domainRepository');
const { billingClient } = require('../clients/billingClient');

/**
 * Submits a customer's payment for an invoice.
 *
 * The customer-facing contract is deliberately thin: this service owns the request record
 * and its lifecycle, while Billing owns whether the money actually moves. A submission
 * carrying a client reference that has already been seen returns the original record, so a
 * customer retrying on a flaky connection does not pay twice.
 */
function submitPaymentRequest(request) {
  const existing = domainRepository.findByClientReference(request.clientReference);
  if (existing) return existing;

  const outcome = billingClient.submitInvoicePayment({
    invoiceId: request.invoiceId,
    amountMinor: request.amountMinor,
    currency: request.currency,
    network: request.network,
  });

  process.stdout.write(
    JSON.stringify({
      level: 'info',
      event: 'payment_request.submitted',
      invoiceId: request.invoiceId,
      billingReference: outcome.billingReference,
      accepted: outcome.accepted,
    }) + '\n',
  );

  return domainRepository.save({
    customerId: request.customerId,
    invoiceId: request.invoiceId,
    amountMinor: request.amountMinor,
    currency: request.currency,
    network: request.network,
    paymentMethodId: request.paymentMethodId || null,
    clientReference: request.clientReference || null,
    status: outcome.unavailable ? 'pending' : outcome.accepted ? 'submitted' : 'rejected',
    billingReference: outcome.billingReference,
  });
}

module.exports = { submitPaymentRequest };
