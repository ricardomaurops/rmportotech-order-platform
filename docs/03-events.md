# Event Contracts

All events follow this base structure:

{
  "eventId": "UUID",
  "eventType": "string",
  "eventVersion": "v1",
  "occurredAt": "timestamp",
  "correlationId": "UUID",
  "payload": {}
}

---

## OrderCreated

Topic:
rmpt.orders.v1.order-created

Payload:
- orderId
- items
- totalAmount
- createdAt

---

## PaymentApproved

Topic:
rmpt.payments.v1.payment-approved

Payload:
- orderId
- paymentId
- approvedAt

---

## PaymentRejected

Topic:
rmpt.payments.v1.payment-rejected

Payload:
- orderId
- reason

---

## StockReserved

Topic:
rmpt.inventory.v1.stock-reserved

Payload:
- orderId
- reservationId

---

## StockFailed

Topic:
rmpt.inventory.v1.stock-failed

Payload:
- orderId
- reason
