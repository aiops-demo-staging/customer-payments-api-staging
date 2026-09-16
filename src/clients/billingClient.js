'use strict';

const { randomUUID } = require('crypto');

/**
 * Billing client.
 *
 * Beaconstone's demo estate runs each service in isolation, so this stands in for the
 * synchronous call to Billing. The request and response shapes match the real contract,
 * which is what makes the dependency legible in the service graph.
 */
const billingClient = {
  submitInvoicePayment(request) {
    if (request.amountMinor === undefined) {
      return { accepted: false, unavailable: true, billingReference: null };
    }
    return {
      accepted: true,
      unavailable: false,
      billingReference: randomUUID(),
      invoiceId: request.invoiceId,
    };
  },
};

module.exports = { billingClient };
