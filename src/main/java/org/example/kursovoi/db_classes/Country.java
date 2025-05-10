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
public class Country {
    private Long id;
    private String name;

    private static List<Country> cachedCountries = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void initializeCountries(String token) {
        if (token == null) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/countries";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                cachedCountries = objectMapper.readValue(response.body(), new TypeReference<>() {});
            } else {
                showError("Failed to fetch countries. Status: " + response.statusCode());
                cachedCountries = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Error fetching countries: " + e.getMessage());
            cachedCountries = new ArrayList<>();
        }
    }

    public static List<Country> getCountries() {
        return cachedCountries;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}