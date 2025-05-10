package org.example.kursovoi.db_classes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import org.example.kursovoi.classes.BuyerDTO;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.interfases.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Buyer implements User {
    private Long id;
    private String email;
    private String phone;
    private String password;
    private String nickname;
    private Long idRole;
    private HttpClient client;
    private Gson gson;

    private String name;
    private String lastname;
    @JsonProperty("id_buyer")
    private Long id_buyer;
    private List<Product> cart;
    private static List<Buyer> cachedBuyers = new ArrayList<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Buyer(String name, String lastname, String email, String phone, String password, String nickname, Long idRole) {
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.nickname = nickname;
        this.idRole = idRole;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.name = name;
        this.lastname = lastname;
        this.cart = new ArrayList<>();
    }

    public Buyer(Long id_buyer, String name, String lastname) {
        this.email = null;
        this.phone = null;
        this.password = null;
        this.nickname = null;
        this.idRole = null;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.id_buyer = id_buyer;
        this.name = name;
        this.lastname = lastname;
        this.cart = new ArrayList<>();
    }

    public Buyer() {
        this.cart = new ArrayList<>();
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

        System.out.println("Login response: " + response.body());

        String token = (String) responseBody.get("token");
        if (token == null) {
            throw new RuntimeException("Login failed: No token received");
        }

        Number roleNumber = (Number) responseBody.get("idRole");
        Long userIdRole = (roleNumber != null) ? roleNumber.longValue() : 1L;
        Number idNumber = (Number) responseBody.get("id");
        Long id_user = (idNumber != null) ? idNumber.longValue() : null;

        String nickname = (String) responseBody.get("nickname");
        if (nickname == null) {
            nickname = email != null ? email : phone;
            System.out.println("Nickname not found in response, using email/phone: " + nickname);
        } else {
            System.out.println("Nickname found in response: " + nickname);
        }

        userSession.setToken(token);
        if (userIdRole == 1L) {
            userSession.setBuyer(this, token);
            idRole = 1L;
        } else if (userIdRole == 2L) {
            userSession.setId_user(id_user);
        } else if (userIdRole == 3L) {
            Worker worker = new Worker(email, phone, password, nickname, 3L);
            userSession.setWorker(worker, token);
        } else {
            System.out.println("Предупреждение: Неизвестная роль пользователя: " + userIdRole);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", id_user);
        result.put("token", token);
        result.put("idRole", userIdRole);
        return result;
    }

    protected HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                System.out.println("HTTP Status Code: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
                throw new RuntimeException("Request failed with status: " + response.statusCode());
            }
            return response;
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка при отправке запроса: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public int checkInputType(String input) {
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

    public Map<String, Object> register() {
        String url = "http://localhost:8080/buyers/add";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bName", name);
        requestBody.put("bLastname", lastname);
        requestBody.put("uPhoneEmail", email != null ? email : phone);
        requestBody.put("uPassword", password);
        requestBody.put("uBoolPhone", email == null);

        try {
            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = sendRequest(request);
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<>() {});

            Number idBuyerNumber = (Number) responseBody.get("id_buyer");
            if (idBuyerNumber != null) {
                this.id_buyer = idBuyerNumber.longValue();
            }

            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getUserInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", name);
        info.put("lastname", lastname);
        info.put("email", email);
        info.put("phone", phone);
        info.put("id_buyer", id_buyer);
        return info;
    }

    public void makePurchase(String token, Long id_buyer) {
        if (cart == null || cart.isEmpty()) {
            return;
        }

        try {
            Map<Long, List<Product>> groupedProducts = cart.stream()
                    .collect(Collectors.groupingBy(Product::getId));

            List<Map<String, Object>> products = new ArrayList<>();
            for (Map.Entry<Long, List<Product>> entry : groupedProducts.entrySet()) {
                Map<String, Object> productEntry = new HashMap<>();
                productEntry.put("id_product", entry.getKey());
                productEntry.put("quantity", entry.getValue().size());
                products.add(productEntry);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = dateFormat.format(new Date());

            Map<String, Object> receiptBody = new HashMap<>();
            receiptBody.put("total_sum", calculateTotalSum());
            receiptBody.put("date_receipt", formattedDate);
            receiptBody.put("id_salesman", null);
            receiptBody.put("id_buyer", id_buyer.intValue());
            receiptBody.put("paid", false);
            receiptBody.put("products", products);

            String json = objectMapper.writeValueAsString(receiptBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/receipts"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = sendRequest(request);

            if (response.statusCode() == 200) {
                cart.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long calculateTotalSum() {
        return cart.stream().mapToLong(Product::getCost).sum();
    }

    public static void initializeBuyersFromList(String token) {
        if (token == null || !cachedBuyers.isEmpty()) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/buyers/list";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<BuyerDTO> buyerDTOs = objectMapper.readValue(response.body(), new TypeReference<>() {});
                cachedBuyers = buyerDTOs.stream()
                        .map(dto -> new Buyer(dto.getId(), dto.getName(), dto.getLastname()))
                        .collect(Collectors.toList());
                System.out.println("Cached buyers: " + cachedBuyers);
            } else {
                showError("Failed to fetch buyers from list. Status: " + response.statusCode());
                cachedBuyers = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Error fetching buyers from list: " + e.getMessage());
            cachedBuyers = new ArrayList<>();
        }
    }

    public static List<Buyer> getBuyers() {
        return cachedBuyers;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void clearCart(){
        this.cart.clear();
    }

    @Override
    public String toString() {
        return "Buyer{id_buyer=" + id_buyer + ", name='" + name + "', lastname='" + lastname + "'}";
    }
}