package org.example.Test;

import org.example.Payment.Order;
import org.example.Payment.PaymentMethod;
import org.example.PaymentOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PaymentOptimizerTest {
    private Map<String, PaymentMethod> paymentMethods;
    private PaymentMethod punktyPm;
    private PaymentMethod mZyskPm;
    private PaymentMethod bosBankrutPm;

    private BigDecimal bd(String val) {
        return new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
    }

    @BeforeEach
    void setUp() {
        paymentMethods = new HashMap<>();

        punktyPm = new PaymentMethod();
        punktyPm.setId("PUNKTY");
        punktyPm.setDiscount("15");
        punktyPm.setLimit("1000.00");
        paymentMethods.put(punktyPm.getId(), punktyPm);

        mZyskPm = new PaymentMethod();
        mZyskPm.setId("mZysk");
        mZyskPm.setDiscount("10");
        mZyskPm.setLimit("1000.00");
        paymentMethods.put(mZyskPm.getId(), mZyskPm);

        bosBankrutPm = new PaymentMethod();
        bosBankrutPm.setId("BosBankrut");
        bosBankrutPm.setDiscount("5");
        bosBankrutPm.setLimit("1000.00");
        paymentMethods.put(bosBankrutPm.getId(), bosBankrutPm);
    }

    @Test
    @DisplayName("Test 1: Proste zamówienie, płatność jedną kartą bez promocji")
    void testSimpleOrderNoPromo() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_SIMPLE", "50.00", Collections.emptyList())
        );

        paymentMethods.get("PUNKTY").setLimit("0.00");
        paymentMethods.get("mZysk").setLimit("100.00");
        paymentMethods.get("BosBankrut").setLimit("0.00");


        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        assertEquals(bd("50.00"), result.get("mZysk"), "mZysk powinno pokryć 50.00");
        assertEquals(bd("0.00"), result.get("PUNKTY"), "PUNKTY nie powinny być użyte");
        assertEquals(bd("0.00"), result.get("BosBankrut"), "BosBankrut nie powinien być użyty");
    }

    @Test
    @DisplayName("Test 2: Zamówienie z promocją karty (Zasada 2)")
    void testCardSpecificPromotion() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_CARDPROMO", "100.00", List.of("mZysk"))
        );
        paymentMethods.get("PUNKTY").setLimit("0.00");

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        assertEquals(bd("90.00"), result.get("mZysk"), "mZysk powinno pokryć 90.00 po zniżce");
        assertEquals(bd("0.00"), result.get("PUNKTY"));
    }

    @Test
    @DisplayName("Test 3: Pełna płatność PUNKTY ze zniżką (Zasada 4)")
    void testFullPaymentWithPunktyDiscount() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_PUNKTY_FULL", "100.00", Collections.emptyList())
        );
        paymentMethods.get("PUNKTY").setLimit("100.00");
        paymentMethods.get("PUNKTY").setDiscount("15");
        paymentMethods.get("mZysk").setLimit("0.00");

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);
        assertEquals(bd("85.00"), result.get("PUNKTY"), "PUNKTY powinny pokryć 85.00 po zniżce 15%");
    }

    @Test
    @DisplayName("Test 4: Częściowa płatność PUNKTY (>=10%) + Karta (Zasada 3)")
    void testPartialPunktyPaymentWithGeneralDiscount() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_PART_PUNKTY", "200.00", Collections.emptyList())
        );
        paymentMethods.get("PUNKTY").setLimit("50.00");
        paymentMethods.get("mZysk").setLimit("200.00");

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        assertEquals(bd("50.00"), result.get("PUNKTY"), "PUNKTY powinny pokryć 50.00");
        assertEquals(bd("130.00"), result.get("mZysk"), "mZysk powinno pokryć resztę 130.00");
    }

    @Test
    @DisplayName("Test 5: Częściowa płatność PUNKTY (>=10%) - punkty pokrywają całość po zniżce 10% (Rule 3)")
    void testPartialPunktyCoversAllAfterRule3Discount() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_PUNKTY_COVERS_ALL_RULE3", "100.00", Collections.emptyList())
        );
        paymentMethods.get("PUNKTY").setLimit("100.00");
        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);
        assertEquals(bd("85.00"), result.get("PUNKTY"), "PUNKTY powinny pokryć 85.00 (Rule 4, 15% disc)");
        assertEquals(bd("0.00"), result.get("mZysk"));
    }

    @Test
    @DisplayName("Test 6: Za mało PUNKTY (<10%) do zniżki Rule 3, płatność PUNKTY + Karta bez zniżki 10% (Fallback)")
    void testNotEnoughPunktyForDiscount_Fallback() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_FEW_PUNKTY", "100.00", Collections.emptyList())
        );
        paymentMethods.get("PUNKTY").setLimit("5.00");
        paymentMethods.get("mZysk").setLimit("100.00");

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        assertEquals(bd("5.00"), result.get("PUNKTY"), "PUNKTY powinny pokryć 5.00");
        assertEquals(bd("95.00"), result.get("mZysk"), "mZysk powinno pokryć resztę 95.00");
    }

    @Test
    @DisplayName("Test 7: Złożony scenariusz - przykład z zadania")
    void testComplexScenarioFromExample() {
        List<Order> orders = Arrays.asList(
                new Order("ORDER1", "100.00", List.of("mZysk")),
                new Order("ORDER2", "200.00", List.of("BosBankrut")),
                new Order("ORDER3", "150.00", Arrays.asList("mZysk", "BosBankrut")),
                new Order("ORDER4", "50.00", Collections.emptyList())
        );

        paymentMethods.clear();

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount("15");
        points.setLimit("100.00");
        paymentMethods.put(points.getId(), points);

        PaymentMethod mZysk = new PaymentMethod();
        mZysk.setId("mZysk");
        mZysk.setDiscount("10");
        mZysk.setLimit("180.00");
        paymentMethods.put(mZysk.getId(), mZysk);

        PaymentMethod bosBankrut = new PaymentMethod();
        bosBankrut.setId("BosBankrut");
        bosBankrut.setDiscount("5");
        bosBankrut.setLimit("200.00");
        paymentMethods.put(bosBankrut.getId(), bosBankrut);

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        System.out.println("Wyniki testu złożonego (Test 7):");
        result.forEach((key, value) -> {
            if (value.compareTo(BigDecimal.ZERO)>0) System.out.println(key + " " + value);
        });

        assertEquals(bd("165.00"), result.getOrDefault("mZysk", BigDecimal.ZERO), "mZysk powinno być 165.00");
        assertEquals(bd("190.00"), result.getOrDefault("BosBankrut", BigDecimal.ZERO), "BosBankrut powinno być 190.00");
        assertEquals(bd("100.00"), result.getOrDefault("PUNKTY", BigDecimal.ZERO), "PUNKTY powinny być 100.00");
    }

    @Test
    @DisplayName("Test 8: Dwa zamówienia, jedno z promocją karty (Rule 2), drugie pełna płatność PUNKTY (Rule 4)")
    void testTwoOrdersCardPromoAndFullPunkty() {
        List<Order> orders = Arrays.asList(
                new Order("ORDER_A", "100.00", List.of("mZysk")),
                new Order("ORDER_B", "50.00", Collections.emptyList())
        );

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        assertEquals(bd("90.00"), result.get("mZysk"));
        assertEquals(bd("42.50"), result.get("PUNKTY"));
    }

    @Test
    @DisplayName("Test 9: Kolejność promocji - lepsza promocja karty wybrana dla zamówienia")
    void testCardPromoChoice() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_CHOICE", "200.00", Arrays.asList("mZysk", "BosBankrut"))
        );
        paymentMethods.get("PUNKTY").setLimit("0.00");

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);
        assertEquals(bd("180.00"), result.get("mZysk"));
        assertEquals(bd("0.00"), result.get("BosBankrut"));
        assertEquals(bd("0.00"), result.get("PUNKTY"));
    }

    @Test
    @DisplayName("Test 10: Brak środków na karcie promocyjnej, fallback na PUNKTY (Rule 4)")
    void testCardPromoLimitExceededFallbackToPunktyRule4() {
        List<Order> orders = Collections.singletonList(
                new Order("ORDER_LIMIT", "100.00", List.of("mZysk"))
        );
        paymentMethods.get("mZysk").setLimit("50.00");
        paymentMethods.get("PUNKTY").setLimit("100.00");
        paymentMethods.get("PUNKTY").setDiscount("15");

        Map<String, BigDecimal> result = PaymentOptimizer.optimizePayments(orders, paymentMethods);

        assertEquals(bd("85.00"), result.get("PUNKTY"));
        assertEquals(bd("0.00"), result.get("mZysk"));
    }
}