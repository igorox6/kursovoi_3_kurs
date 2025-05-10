package org.example.kursovoi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Country;
import org.example.kursovoi.interfases.InitializableController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class CountriesController implements InitializableController, Initializable {

    @FXML
    private VBox countriesList;

    @FXML
    private TextField inputName;

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
            Country.initializeCountries(token);
            displayCountries();
        } else {
            System.out.println("Token is null, cannot initialize data.");
        }
    }

    private void displayCountries() {
        if (countriesList == null) {
            System.out.println("countriesList is null, skipping displayCountries.");
            return;
        }
        countriesList.getChildren().clear();

        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        header.getChildren().addAll(
                new Label("ID") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 50;"); }},
                new Label("Название") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }}
        );
        countriesList.getChildren().add(header);

        for (Country country : Country.getCountries()) {
            HBox countryRow = new HBox(20);
            countryRow.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.3, 0, 1);");
            countryRow.getChildren().addAll(
                    new Label(String.valueOf(country.getId())) {{ setStyle("-fx-min-width: 50;"); }},
                    new Label(country.getName() != null ? country.getName() : "N/A") {{ setStyle("-fx-min-width: 200;"); }}
            );
            countriesList.getChildren().add(countryRow);
        }
    }

    @FXML
    public void onAddCountry(ActionEvent event) {
        try {
            String token = userSession.getToken();
            if (token == null || token.isEmpty()) {
                System.out.println("Токен отсутствует. Пожалуйста, авторизуйтесь.");
                return;
            }
            System.out.println("Используемый токен: " + token);

            String name = inputName.getText();
            if (name == null || name.trim().isEmpty()) {
                System.out.println("Ошибка: Название страны не может быть пустым.");
                return;
            }

            // Создаем объект для отправки в API
            var countryBody = new java.util.HashMap<String, Object>();
            countryBody.put("name", name);

            String jsonBody = objectMapper.writeValueAsString(countryBody);
            System.out.println("Отправляемый JSON: " + jsonBody);

            URL url = new URL("http://localhost:8080/countries");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            BufferedReader in;
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
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

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                Country.initializeCountries(userSession.getToken());
                displayCountries();

                inputName.clear();
                System.out.println("Страна успешно добавлена.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Страна успешно добавлена!");
                alert.showAndWait();
            } else {
                String errorMessage = response.toString();
                System.out.println("Код ответа сервера: " + responseCode);
                System.out.println("Тело ответа сервера: " + errorMessage);
                if (errorMessage.isEmpty()) {
                    System.out.println("Ошибка добавления страны. Код ответа: " + responseCode + ", Тело ответа пустое.");
                } else {
                    try {
                        Map<String, Object> jsonResponse = objectMapper.readValue(errorMessage, Map.class);
                        String error = (String) jsonResponse.getOrDefault("error", "Неизвестная ошибка");
                        System.out.println("Ошибка добавления страны. Код ответа: " + responseCode + ", Сообщение: " + error);
                    } catch (Exception e) {
                        System.out.println("Ошибка добавления страны. Код ответа: " + responseCode + ", Тело ответа: " + errorMessage);
                    }
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении страны: " + e.getMessage());
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