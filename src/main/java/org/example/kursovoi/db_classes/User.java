package org.example.kursovoi.db_classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User {
    private String password;
    private String phone;
    private String nickname;
    private String email;

    @JsonProperty("idRole")
    private int idRole;

    private int id;

    private Role role;

    private static List<User> cachedUsers = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();



    public static void initializeUsers(String token) {
        if (token == null || !cachedUsers.isEmpty()) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/users";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                cachedUsers = objectMapper.readValue(response.body(), new TypeReference<>() {});
            } else {
                System.err.println("Failed to fetch users. Status: " + response.statusCode());
                cachedUsers = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            cachedUsers = new ArrayList<>();
        }
    }

    public static List<User> getUsers() {
        return cachedUsers;
    }


    public static class Role {
        private int id;
        private String name;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
