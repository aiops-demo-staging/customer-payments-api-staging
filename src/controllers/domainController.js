'use strict';

const { submitPaymentRequest } = require('../services/domainService');
const { domainRepository } = require('../repositories/domainRepository');

function submit(req, res) {
  const request = {
    customerId: req.body.customerId,
    invoiceId: req.body.invoiceId,
    amountMinor: req.body.amountMinor,
    currency: req.body.currency || 'USD',
    network: req.body.network,
    paymentMethodId: req.body.paymentMethodId || null,
    clientReference: req.body.clientReference || null,
  };

  if (!request.customerId || !request.invoiceId) {
    return res.status(422).json({ error: 'MissingIdentifiers', requestId: req.requestId });
  }
  if (!Number.isInteger(request.amountMinor) || request.amountMinor <= 0) {
    return res.status(422).json({ error: 'InvalidAmount', requestId: req.requestId });
  }

  const record = submitPaymentRequest(request);

  return res.status(201).json({
    paymentRequestId: record.paymentRequestId,
    invoiceId: record.invoiceId,
    status: record.status,
    billingReference: record.billingReference,
    requestId: req.requestId,
  });
}

function getPaymentRequest(req, res) {
  const record = domainRepository.findById(req.params.paymentRequestId);
  if (!record) {
    return res.status(404).json({ error: 'PaymentRequestNotFound', requestId: req.requestId });
  }
  return res.json(record);
}

function getReceipt(req, res) {
  const record = domainRepository.findById(req.params.paymentRequestId);
  if (!record) {
    return res.status(404).json({ error: 'PaymentRequestNotFound', requestId: req.requestId });
  }
  return res.json({
    paymentRequestId: record.paymentRequestId,
    invoiceId: record.invoiceId,
    amountMinor: record.amountMinor,
    currency: record.currency,
    status: record.status,
    billingReference: record.billingReference,
    issuedAt: record.submittedAt,
  });
}

function listForCustomer(req, res) {
  return res.json({
    customerId: req.params.customerId,
    paymentRequests: domainRepository.findByCustomer(req.params.customerId),
  });
}

module.exports = { submit, getPaymentRequest, getReceipt, listForCustomer };
