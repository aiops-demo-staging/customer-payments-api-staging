'use strict';

const { randomUUID } = require('crypto');

/**
 * In-memory payment-request store. The demo service keeps no external database.
 */
const store = new Map();

const domainRepository = {
  save(record) {
    const saved = {
      paymentRequestId: randomUUID(),
      submittedAt: new Date().toISOString(),
      ...record,
    };
    store.set(saved.paymentRequestId, saved);
    return saved;
  },

  findById(paymentRequestId) {
    return store.get(paymentRequestId) || null;
  },

  findByClientReference(clientReference) {
    if (!clientReference) return null;
    return [...store.values()].find((r) => r.clientReference === clientReference) || null;
  },

  findByCustomer(customerId) {
    return [...store.values()].filter((record) => record.customerId === customerId);
  },

  countByStatus() {
    return [...store.values()].reduce((counts, record) => {
      counts[record.status] = (counts[record.status] || 0) + 1;
      return counts;
    }, {});
  },

  size() {
    return store.size;
  },
};

module.exports = { domainRepository };
