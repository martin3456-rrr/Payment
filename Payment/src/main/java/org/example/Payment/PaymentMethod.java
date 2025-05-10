package org.example.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentMethod {
    private String id;
    private int discount; // Percentage
    private BigDecimal limit;
    private BigDecimal currentLimit;

    public PaymentMethod() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(String discountStr) {
        if (discountStr != null) {
            this.discount = Integer.parseInt(discountStr);
        }
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setLimit(String limitStr) {
        if (limitStr != null) {
            this.limit = new BigDecimal(limitStr).setScale(2, RoundingMode.HALF_UP);
            this.currentLimit = this.limit;
        } else {
            this.limit = null;
            this.currentLimit = null;
        }
    }

    public void setLimit(BigDecimal limit) {
        if (limit != null) {
            this.limit = limit.setScale(2, RoundingMode.HALF_UP);
            this.currentLimit = this.limit;
        } else {
            this.limit = null;
            this.currentLimit = null;
        }
    }

    public BigDecimal getCurrentLimit() {
        if (this.currentLimit == null && this.limit != null) {
            this.currentLimit = this.limit;
        }
        return currentLimit;
    }

    public void setCurrentLimit(BigDecimal currentLimit) {
        if (currentLimit != null) {
            this.currentLimit = currentLimit.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.currentLimit = null;
        }
    }

    public void decreaseLimit(BigDecimal amount) {
        if (this.currentLimit != null && amount != null) {
            this.currentLimit = this.currentLimit.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public PaymentMethod copy() {
        PaymentMethod copy = new PaymentMethod();
        copy.setId(this.id);
        copy.setDiscount(this.discount);
        if (this.limit != null) {
            copy.setLimit(this.limit);
        }
        if (this.currentLimit != null) {
            copy.setCurrentLimit(this.currentLimit);
        } else if (this.limit != null) {
            copy.setCurrentLimit(this.limit);
        }
        return copy;
    }

    @Override
    public String toString() {
        return "PaymentMethod{id='" + id + "', discount=" + discount + "%, limit=" + limit + ", currentLimit=" + currentLimit + "}";
    }
}