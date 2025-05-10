package org.example.kursovoi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.User;
import org.example.kursovoi.interfases.InitializableController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class UsersController implements InitializableController, Initializable {

    @FXML
    private VBox usersList;

    @FXML
    private TextField inputName;
    @FXML
    private TextField inputLastname;
    @FXML
    private TextField inputEmail;
    @FXML
    private TextField inputPhone;
    @FXML
    private PasswordField inputPassword;
    @FXML
    private TextField inputNickname;

    @FXML
    private Button productsButton;
    @FXML
    private Button analyticsButton;

    @FXML
    private ImageView iconProducts;
    @FXML
    private ImageView iconAnalytics;

    private Application app;
    private UserSession userSession;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = userSession;
        initializeData();
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = userSession;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Инициализация картинок с проверкой на null
        if (iconProducts != null) {
            try {
                Image productsImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/products.png")));
                iconProducts.setImage(productsImage);
            } catch (NullPointerException e) {
                System.out.println("Не удалось загрузить изображение: /img/products.png. Проверьте путь к файлу.");
            }
        }
        if (iconAnalytics != null) {
            try {
                Image analyticsImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/analys.png")));
                iconAnalytics.setImage(analyticsImage);
            } catch (NullPointerException e) {
                System.out.println("Не удалось загрузить изображение: /img/analys.png. Проверьте путь к файлу.");
            }
        }
    }

    private void initializeData() {
        if (userSession == null) {
            System.out.println("UserSession is null, cannot initialize data.");
            return;
        }

        String token = userSession.getToken();
        if (token != null) {
            User.initializeUsers(token);
            displayUsers();
        } else {
            System.out.println("Token is null, cannot initialize data.");
        }
    }

    private void displayUsers() {
        if (usersList == null) {
            System.out.println("usersList is null, skipping displayUsers.");
            return;
        }
        usersList.getChildren().clear();

        // Заголовок с названиями полей
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        header.getChildren().addAll(
                new Label("ID") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 50;"); }},
                new Label("Ник") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 150;"); }},
                new Label("Email") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }},
                new Label("Телефон") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 150;"); }}
        );
        usersList.getChildren().add(header);

        // Список пользователей
        for (User user : User.getUsers()) {
            HBox userRow = new HBox(20);
            userRow.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.3, 0, 1);");
            userRow.getChildren().addAll(
                    new Label(String.valueOf(user.getId())) {{ setStyle("-fx-min-width: 50;"); }},
                    new Label(user.getNickname() != null ? user.getNickname() : "N/A") {{ setStyle("-fx-min-width: 150;"); }},
                    new Label(user.getEmail() != null ? user.getEmail() : "N/A") {{ setStyle("-fx-min-width: 200;"); }},
                    new Label(user.getPhone() != null ? user.getPhone() : "N/A") {{ setStyle("-fx-min-width: 150;"); }}
            );
            usersList.getChildren().add(userRow);
        }
    }

    @FXML
    public void onRegister(ActionEvent event) {
        try {
            // Собираем данные из полей ввода
            String name = inputName.getText();
            String lastname = inputLastname.getText();
            String email = inputEmail.getText();
            String phone = inputPhone.getText();
            String password = inputPassword.getText();
            String nickname = inputNickname.getText();

            // Формируем тело запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bName", name);
            requestBody.put("bLastname", lastname);
            requestBody.put("uPhoneEmail", email.isEmpty() ? phone : email);
            requestBody.put("uPassword", password);
            requestBody.put("uBoolPhone", email.isEmpty());
            requestBody.put("uNickname", nickname);

            // Сериализуем объект в JSON
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // Отправляем POST-запрос
            URL url = new URL("http://localhost:8080/buyer/add");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Проверяем код ответа
            int responseCode = conn.getResponseCode();
            BufferedReader in;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Десериализуем ответ
            Map<String, Object> jsonResponse = objectMapper.readValue(response.toString(), Map.class);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Успешная регистрация, обновляем список пользователей
                User.initializeUsers(userSession.getToken());
                displayUsers();

                // Очистка полей после успешной регистрации
                inputName.clear();
                inputLastname.clear();
                inputEmail.clear();
                inputPhone.clear();
                inputPassword.clear();
                inputNickname.clear();
            } else {
                // Ошибка регистрации
                String errorMessage = (String) jsonResponse.getOrDefault("error", "Неизвестная ошибка");
                System.out.println("Ошибка регистрации: " + errorMessage);
            }

            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Ошибка при регистрации: " + e.getMessage());
        }
    }

    @FXML
    public void onProductsClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/products-view.fxml");
    }

    @FXML
    public void onAnalyticsClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/analytics-view.fxml");
    }
}