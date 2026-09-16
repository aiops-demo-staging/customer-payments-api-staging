'use strict';

const express = require('express');
const domainController = require('../controllers/domainController');

const router = express.Router();

router.post('/payment-requests', domainController.submit);
router.get('/payment-requests/:paymentRequestId', domainController.getPaymentRequest);
router.get('/payment-requests/:paymentRequestId/receipt', domainController.getReceipt);
router.get('/customers/:customerId/payment-requests', domainController.listForCustomer);

module.exports = router;
