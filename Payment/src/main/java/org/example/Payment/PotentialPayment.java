package org.example.Payment;

import java.math.BigDecimal;

public class PotentialPayment {
    Order order;
    PaymentMethod paymentMethod;
    BigDecimal discountAmount;
    BigDecimal finalCost;
    String type; // "CARD_PROMO", "PUNKTY_FULL_RULE4", "PUNKTY_PARTIAL_RULE3"

    public PotentialPayment(Order order, PaymentMethod paymentMethod, BigDecimal discountAmount, BigDecimal finalCost) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.discountAmount = discountAmount;
        this.finalCost = finalCost;
        if (paymentMethod != null && "PUNKTY".equals(paymentMethod.getId())) {
            this.type = "PUNKTY_FULL_RULE4";
        } else {
            this.type = "CARD_PROMO";
        }
    }

    public PotentialPayment(Order order, PaymentMethod punktyAsPaymentMethod, BigDecimal discountAmount, BigDecimal finalCost, String type) {
        this.order = order;
        this.paymentMethod = punktyAsPaymentMethod;
        this.discountAmount = discountAmount;
        this.finalCost = finalCost;
        this.type = type;
    }

    public Order getOrder() { return order; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getFinalCost() { return finalCost; }
    public String getType() { return type; }
}