package org.example.kursovoi.db_classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.interfases.User;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Worker implements User {
    private Long id;
    private String email;
    private String phone;
    private String password;
    private String nickname;
    private Long idRole;
    private HttpClient client;
    private Gson gson;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Worker(String email, String phone, String password, String nickname, Long idRole) {
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.nickname = nickname;
        this.idRole = idRole;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public Map<String, Object> login(UserSession userSession) {
        String url = "http://localhost:8080/users/login";
        String phoneEmail = email != null && checkInputType(email) == 0 ? email : phone;
        if (phoneEmail == null || checkInputType(phoneEmail) == -1) {
            throw new RuntimeException("Invalid email or phone provided for login");
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phoneEmail", phoneEmail);
        requestBody.put("password", password);

        String json = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = sendRequest(request);
        Map<String, Object> responseBody = gson.fromJson(response.body(), Map.class);

        String token = (String) responseBody.get("token");
        if (token == null) {
            throw new RuntimeException("Login failed: No token received");
        }

        Number roleNumber = (Number) responseBody.get("idRole");
        Long userIdRole = (roleNumber != null) ? roleNumber.longValue() : 3L;
        Number idNumber = (Number) responseBody.get("id");
        this.id = (idNumber != null) ? idNumber.longValue() : null;

        userSession.setWorker(this, token);
        Map<String, Object> result = new HashMap<>();
        result.put("id", this.id);
        result.put("token", token);
        result.put("idRole", userIdRole);
        return result;
    }

    protected HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Request failed with status: " + response.statusCode());
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error sending request: " + e.getMessage());
        }
    }

    protected int checkInputType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1;
        }
        String phoneRegex = "^\\+?\\d{1,3}[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$|^\\d{10}$";
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (input.matches(phoneRegex)) {
            return 1;
        } else if (input.matches(emailRegex)) {
            return 0;
        } else {
            return -1;
        }
    }

    public void makePurchase(String token, Long idReceipt, Long idSalesman, String newDateReceipt) {
        try {
            URL url = new URL("http://localhost:8080/receipts/" + idReceipt + "/purchase");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            // Тело запроса
            String payload = String.format(
                    "{\"idSalesman\": %d, \"paid\": true, \"dateReceipt\": \"%s\"}",
                    idSalesman, newDateReceipt
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Purchase completed successfully.");
            } else {
                System.out.println("Failed to complete purchase. HTTP Code: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}