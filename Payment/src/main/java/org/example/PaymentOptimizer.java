package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Payment.Order;
import org.example.Payment.PaymentMethod;
import org.example.Payment.PotentialPayment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentOptimizer {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal TEN_PERCENT = new BigDecimal("0.10");
    private static final BigDecimal NINETY_PERCENT = new BigDecimal("0.90");

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar app.jar <orders.json_path> <paymentmethods.json_path>");
            return;
        }

        String ordersFilePath = args[0];
        String paymentMethodsFilePath = args[1];
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Order> orders = objectMapper.readValue(new File(ordersFilePath), new TypeReference<>() {
            });
            List<PaymentMethod> pmList = objectMapper.readValue(new File(paymentMethodsFilePath), new TypeReference<>() {
            });

            Map<String, PaymentMethod> paymentMethods = pmList.stream()
                    .collect(Collectors.toMap(PaymentMethod::getId, PaymentMethod::copy));

            Map<String, BigDecimal> spent = optimizePayments(new ArrayList<>(orders), paymentMethods);

            spent.forEach((pmId, amount) -> {
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    System.out.printf("%s %.2f%n", pmId, amount.doubleValue());
                }
            });

        } catch (IOException e) {
            System.err.println("Error reading JSON files: " + e.getMessage());
        }
    }

    public static Map<String, BigDecimal> optimizePayments(List<Order> orders, Map<String, PaymentMethod> paymentMethods) {
        Map<String, BigDecimal> spentAmounts = new HashMap<>();
        paymentMethods.keySet().forEach(pmId -> spentAmounts.put(pmId, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));

        Set<String> paidOrderIds = new HashSet<>();
        PaymentMethod punktyMethod = paymentMethods.get("PUNKTY");

        List<PotentialPayment> singlePaymentOpportunities = new ArrayList<>();
        for (Order order : orders) {
            for (String promoId : order.getPromotions()) {
                PaymentMethod card = paymentMethods.get(promoId);
                if (card != null && !card.getId().equals("PUNKTY")) {
                    BigDecimal discountRate = new BigDecimal(card.getDiscount()).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
                    BigDecimal discountAbs = order.getValue().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal cost = order.getValue().subtract(discountAbs);
                    singlePaymentOpportunities.add(new PotentialPayment(order, card, discountAbs, cost));
                }
            }

            if (punktyMethod != null && punktyMethod.getDiscount() > 0) {
                BigDecimal discountRate = new BigDecimal(punktyMethod.getDiscount()).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
                BigDecimal discountAbs = order.getValue().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal cost = order.getValue().subtract(discountAbs);
                singlePaymentOpportunities.add(new PotentialPayment(order, punktyMethod, discountAbs, cost, "PUNKTY_FULL_RULE4"));
            }
        }

        singlePaymentOpportunities.sort(
                Comparator.comparing(PotentialPayment::getDiscountAmount).reversed()
                        .thenComparing(pp -> "PUNKTY_FULL_RULE4".equals(pp.getType()) ? 0 : 1)
                        .thenComparing(pp -> pp.getOrder().getValue().negate())
        );

        for (PotentialPayment pp : singlePaymentOpportunities) {
            Order order = pp.getOrder();
            PaymentMethod pmToUse = pp.getPaymentMethod();

            if (!paidOrderIds.contains(order.getId()) &&
                    pmToUse.getCurrentLimit() != null &&
                    pmToUse.getCurrentLimit().compareTo(pp.getFinalCost()) >= 0) {

                spentAmounts.put(pmToUse.getId(), spentAmounts.get(pmToUse.getId()).add(pp.getFinalCost()));
                pmToUse.decreaseLimit(pp.getFinalCost());
                paidOrderIds.add(order.getId());
            }
        }

        List<Order> remainingOrdersForRule3 = orders.stream()
                .filter(o -> !paidOrderIds.contains(o.getId()))
                .sorted(Comparator.comparing(Order::getValue).reversed())
                .toList();

        if (punktyMethod != null) {
            for (Order order : remainingOrdersForRule3) {
                if (paidOrderIds.contains(order.getId())) continue;

                BigDecimal originalValue = order.getValue();
                BigDecimal minPointsForDiscount = originalValue.multiply(TEN_PERCENT).setScale(2, RoundingMode.HALF_UP);
                BigDecimal costAfterRule3Discount = originalValue.multiply(NINETY_PERCENT).setScale(2, RoundingMode.HALF_UP);

                if (punktyMethod.getCurrentLimit() != null && punktyMethod.getCurrentLimit().compareTo(minPointsForDiscount) >= 0) {
                    BigDecimal pointsToPay = costAfterRule3Discount.min(punktyMethod.getCurrentLimit());

                    if (pointsToPay.compareTo(minPointsForDiscount) >= 0) {
                        BigDecimal cardAmountNeeded = costAfterRule3Discount.subtract(pointsToPay);

                        Optional<PaymentMethod> cardOpt = paymentMethods.values().stream()
                                .filter(pm -> !pm.getId().equals("PUNKTY") &&
                                        pm.getCurrentLimit() != null &&
                                        pm.getCurrentLimit().compareTo(cardAmountNeeded) >= 0)
                                .max(Comparator.comparing(PaymentMethod::getCurrentLimit));

                        if (cardOpt.isPresent()) {
                            PaymentMethod chosenCard = cardOpt.get();

                            spentAmounts.put(punktyMethod.getId(), spentAmounts.get(punktyMethod.getId()).add(pointsToPay));
                            punktyMethod.decreaseLimit(pointsToPay);

                            spentAmounts.put(chosenCard.getId(), spentAmounts.get(chosenCard.getId()).add(cardAmountNeeded));
                            chosenCard.decreaseLimit(cardAmountNeeded);

                            paidOrderIds.add(order.getId());
                        }
                    }
                }
            }
        }
        List<Order> ordersToFallbackPay = orders.stream()
                .filter(o -> !paidOrderIds.contains(o.getId()))
                .sorted(Comparator.comparing(Order::getValue).reversed())
                .toList();

        for (Order order : ordersToFallbackPay) {
            BigDecimal remainingValueToPay = order.getValue();

            if (punktyMethod != null && punktyMethod.getCurrentLimit() != null && punktyMethod.getCurrentLimit().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal amountByPoints = remainingValueToPay.min(punktyMethod.getCurrentLimit());
                spentAmounts.put(punktyMethod.getId(), spentAmounts.get(punktyMethod.getId()).add(amountByPoints));
                punktyMethod.decreaseLimit(amountByPoints);
                remainingValueToPay = remainingValueToPay.subtract(amountByPoints);
            }

            if (remainingValueToPay.compareTo(BigDecimal.ZERO) > 0) {
                List<PaymentMethod> availableCards = paymentMethods.values().stream()
                        .filter(pm -> !pm.getId().equals("PUNKTY") && pm.getCurrentLimit() != null && pm.getCurrentLimit().compareTo(BigDecimal.ZERO) > 0)
                        .sorted(Comparator.comparing(PaymentMethod::getCurrentLimit).reversed())
                        .toList();

                for (PaymentMethod card : availableCards) {
                    if (remainingValueToPay.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal amountByCard = remainingValueToPay.min(card.getCurrentLimit());
                    spentAmounts.put(card.getId(), spentAmounts.get(card.getId()).add(amountByCard));
                    card.decreaseLimit(amountByCard);
                    remainingValueToPay = remainingValueToPay.subtract(amountByCard);
                }
            }

            if (remainingValueToPay.compareTo(BigDecimal.ZERO) > 0) {
                System.err.printf("Warning: Could not fully pay order %s. Remaining: %.2f%n", order.getId(), remainingValueToPay.doubleValue());
            } else {
                paidOrderIds.add(order.getId());
            }
        }
        return spentAmounts;
    }
}