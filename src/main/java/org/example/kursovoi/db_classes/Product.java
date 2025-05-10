package org.example.kursovoi.db_classes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long id;
    private String productName;
    private Long idCompany;
    private String properties;
    private JsonNode propertiesNode;
    private Long cost;
    private String photo;
    private List<Category> categories;
    private Long remain;

    private static List<Product> cachedProducts = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public Product(Long id, String productName, Long cost) {
        this.id = id;
        this.productName = productName;
        this.cost = cost;
        this.idCompany = 1L;
    }




    public static void initializeProducts(String token) {
        if (token == null) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/products";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                cachedProducts = objectMapper.readValue(response.body(), new TypeReference<>() {});
            } else {
                showError("Failed to fetch products. Status: " + response.statusCode());
                cachedProducts = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Error fetching products: " + e.getMessage());
            cachedProducts = new ArrayList<>();
        }
    }
    public static Long postProduct(String token, String productName, Double cost, Long idCompany, String properties, String photo) {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/products";

        try {
            // Create the request body matching the Postman example
            ObjectMapper mapper = new ObjectMapper();
            var requestBody = new java.util.HashMap<String, Object>();
            requestBody.put("productName", productName);
            requestBody.put("cost", cost);
            requestBody.put("idCompany", idCompany);
            requestBody.put("properties", properties != null ? properties : "{}");
            requestBody.put("photo", photo);

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // The API returns the product ID as a Long
                return mapper.readValue(response.body(), Long.class);
            } else {
                showError("Failed to add product. Status: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            showError("Error adding product: " + e.getMessage());
            return null;
        }
    }

    public static List<Product> getProducts() {
        return cachedProducts;
    }

    public String getCategory() {
        if (categories == null || categories.isEmpty()) {
            return "No categories";
        }
        return categories.stream()
                .map(category -> category.getName() != null ? category.getName() : "Unnamed")
                .collect(Collectors.joining(", "));
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}