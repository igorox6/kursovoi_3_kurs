package org.example.kursovoi.db_classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.kursovoi.classes.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private Long id;

    @JsonProperty("total_sum")
    private Double totalSum;

    @JsonProperty("date_receipt")
    private Date dateReceipt;

    private Boolean paid;

    @JsonProperty("id_salesman")
    private Integer idSalesman;

    @JsonProperty("id_buyer")
    private Integer idBuyer;

    // Поле для сырых JSON продуктов
    @JsonProperty("products")
    private ProductsWrapper productsWrapper;


    // Поле для продуктов
    @JsonIgnore
    private List<OrderProduct> products = new ArrayList<>();

    private static List<Order> cachedOrders = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductsWrapper {
        private String type;
        private String value;
        @JsonProperty("null")
        private boolean isNull;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReceiptProduct {
        private Long id_receipt;
        private Long id_product;
        private Double subtotal;
        private String date_receipt;
        private Integer quantity;
        private Long id_row;
    }

    public static void initializeAllOrders(String token) {
        if (token == null) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/receipts";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Order> rawOrders = objectMapper.readValue(response.body(), new TypeReference<>() {});

                List<Product> allProducts = Product.getProducts();
                for (Order order : rawOrders) {
                    ProductsWrapper wrapper = order.getProductsWrapper();
                    if (wrapper != null && wrapper.getValue() != null && !wrapper.isNull()) {
                        List<ReceiptProduct> receiptProducts = objectMapper.readValue(
                                wrapper.getValue(),
                                new TypeReference<>() {}
                        );

                        List<OrderProduct> orderProducts = receiptProducts.stream()
                                .map(rp -> {
                                    Product product = allProducts.stream()
                                            .filter(p -> p.getId().equals(rp.getId_product()))
                                            .findFirst()
                                            .orElse(null);
                                    if (product != null) {
                                        return new OrderProduct(product, rp.getQuantity());
                                    }
                                    return null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        order.setProducts(orderProducts);
                    }
                }

                cachedOrders = rawOrders;
            } else {
                showError("Не удалось загрузить заказы. Статус: " + response.statusCode());
                cachedOrders = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Ошибка при загрузке заказов: " + e.getMessage());
            cachedOrders = new ArrayList<>();
        }
    }

    public static void initializeUserOrders(String token, UserSession userSession) {
        if (token == null) return;

        Integer userId = Math.toIntExact(userSession.getId_user());
        if (userId == null) {
            showError("Не удалось получить ID пользователя из сессии.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/users/" + userId + "/receipts";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                showError("Не удалось загрузить заказы пользователя. Статус: " + response.statusCode());
                cachedOrders = new ArrayList<>();
                return;
            }

            List<Order> rawOrders = objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<Order>>() {}
            );

            cachedOrders = rawOrders;

        } catch (Exception e) {
            showError("Ошибка при загрузке заказов пользователя: " + e.getMessage());
            cachedOrders = new ArrayList<>();
        }
    }






    public static List<Order> getOrders() {
        return cachedOrders;
    }

    public boolean isPaid() {
        return paid;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}