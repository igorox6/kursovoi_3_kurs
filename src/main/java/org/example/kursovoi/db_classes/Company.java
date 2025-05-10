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
public class Company {
    private Long id;
    private String name;
    private Long idCountry;

    private static List<Company> cachedCompanies = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void initializeCompanies(String token) {
        if (token == null || !cachedCompanies.isEmpty()) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/companies";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                cachedCompanies = objectMapper.readValue(response.body(), new TypeReference<>() {});
            } else {
                showError("Failed to fetch companies. Status: " + response.statusCode());
                cachedCompanies = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Error fetching companies: " + e.getMessage());
            cachedCompanies = new ArrayList<>();
        }
    }

    public static List<Company> getCompanies() {
        return cachedCompanies;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}