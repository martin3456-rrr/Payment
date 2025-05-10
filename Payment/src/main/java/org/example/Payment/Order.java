package org.example.Payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String id;
    private BigDecimal value;
    private final List<String> promotions;

    @JsonCreator
    public Order(@JsonProperty("id") String id,
                 @JsonProperty("value") String valueStr,
                 @JsonProperty("promotions") List<String> promotions) {
        this.id = id;
        this.setValue(valueStr);
        this.promotions = promotions;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public BigDecimal getValue() {
        return value;
    }

    // Setter that takes String, ensures scale and rounding
    public void setValue(String valueStr) {
        if (valueStr != null) {
            this.value = new BigDecimal(valueStr).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.value = null;
        }
    }

    public List<String> getPromotions() {
        return promotions == null ? Collections.emptyList() : promotions;
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', value=" + value + ", promotions=" + promotions + "}";
    }
}