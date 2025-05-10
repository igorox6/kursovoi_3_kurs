package org.example.kursovoi.db_classes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private Long id;
    private Long id_parent_category;
    private String name;

    private static List<Category> cachedCategories = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void initializeCategories(String token) {
        if (token == null) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/categories";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                cachedCategories = objectMapper.readValue(response.body(), new TypeReference<>() {});
            } else {
                showError("Failed to fetch categories. Status: " + response.statusCode());
                cachedCategories = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Error fetching categories: " + e.getMessage());
            cachedCategories = new ArrayList<>();
        }
    }

    public static List<Category> getCategories() {
        return cachedCategories;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
