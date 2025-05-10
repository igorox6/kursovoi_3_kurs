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
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.interfases.InitializableController;
import org.example.kursovoi.pojo.ProductBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProductsManageController implements InitializableController, Initializable {

    @FXML
    private VBox productsList;

    @FXML
    private TextField inputName;
    @FXML
    private TextField inputCost;
    @FXML
    private TextField inputIdCompany;
    @FXML
    private TextField inputProperties;
    @FXML
    private TextField inputPhoto;

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
            Product.initializeProducts(token);
            displayProducts();
        } else {
            System.out.println("Token is null, cannot initialize data.");
        }
    }

    private void displayProducts() {
        if (productsList == null) {
            System.out.println("productsList is null, skipping displayProducts.");
            return;
        }
        productsList.getChildren().clear();

        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        header.getChildren().addAll(
                new Label("ID") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 50;"); }},
                new Label("Название") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }},
                new Label("Цена") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 100;"); }},
                new Label("ID компании") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 100;"); }},
                new Label("Свойства") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }},
                new Label("Фото") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 150;"); }},
                new Label("Категории") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }}
        );
        productsList.getChildren().add(header);

        for (Product product : Product.getProducts()) {
            HBox productRow = new HBox(20);
            productRow.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.3, 0, 1);");
            productRow.getChildren().addAll(
                    new Label(String.valueOf(product.getId())) {{ setStyle("-fx-min-width: 50;"); }},
                    new Label(product.getProductName() != null ? product.getProductName() : "N/A") {{ setStyle("-fx-min-width: 200;"); }},
                    new Label(String.valueOf(product.getCost())) {{ setStyle("-fx-min-width: 100;"); }},
                    new Label(String.valueOf(product.getIdCompany())) {{ setStyle("-fx-min-width: 100;"); }},
                    new Label(product.getProperties() != null ? product.getProperties().toString() : "N/A") {{ setStyle("-fx-min-width: 200; -fx-wrap-text: true;"); }},
                    new Label(product.getPhoto() != null ? product.getPhoto() : "N/A") {{ setStyle("-fx-min-width: 150;"); }},
                    new Label(product.getCategory() != null ? product.getCategory() : "N/A") {{ setStyle("-fx-min-width: 200; -fx-wrap-text: true;"); }}
            );
            productsList.getChildren().add(productRow);
        }
    }

    @FXML
    public void onAddProduct(ActionEvent event) {
        try {
            String token = userSession.getToken();
            if (token == null || token.isEmpty()) {
                System.out.println("Токен отсутствует. Пожалуйста, авторизуйтесь.");
                return;
            }
            System.out.println("Используемый токен: " + token);

            String productName = inputName.getText();
            if (productName == null || productName.trim().isEmpty()) {
                System.out.println("Ошибка: Название продукта не может быть пустым.");
                return;
            }

            String costText = inputCost.getText();
            if (costText == null || costText.trim().isEmpty()) {
                System.out.println("Ошибка: Стоимость продукта не может быть пустой.");
                return;
            }
            double cost;
            try {
                cost = Double.parseDouble(costText);
                if (cost < 0) {
                    System.out.println("Ошибка: Стоимость продукта не может быть отрицательной.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Стоимость продукта должна быть числом.");
                return;
            }

            Long idCompany = null;
            String idCompanyText = inputIdCompany.getText();
            if (idCompanyText != null && !idCompanyText.trim().isEmpty()) {
                try {
                    idCompany = Long.parseLong(idCompanyText);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: ID компании должен быть числом.");
                    return;
                }
            } else {
                System.out.println("Ошибка: ID компании не может быть пустым.");
                return;
            }

            String properties = inputProperties.getText();
            if (properties == null || properties.trim().isEmpty()) {
                properties = "{}";
            } else {
                try {
                    objectMapper.readTree(properties);
                } catch (Exception e) {
                    System.out.println("Ошибка: Свойства должны быть в формате JSON (например, {\"key\": \"value\"}).");
                    return;
                }
            }

            String photo = inputPhoto != null ? inputPhoto.getText() : null;
            if (photo != null && photo.trim().isEmpty()) {
                photo = null;
            }

            ProductBody productBody = new ProductBody();
            productBody.setProductName(productName);
            productBody.setCost(cost);
            productBody.setIdCompany(idCompany);
            productBody.setProperties(properties);
            productBody.setPhoto(photo);

            String jsonBody = objectMapper.writeValueAsString(productBody);
            System.out.println("Отправляемый JSON: " + jsonBody);

            URL url = new URL("http://localhost:8080/products");
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
                Product.initializeProducts(userSession.getToken());
                displayProducts();

                inputName.clear();
                inputCost.clear();
                inputIdCompany.clear();
                inputProperties.clear();
                if (inputPhoto != null) inputPhoto.clear();
                System.out.println("Продукт успешно добавлен.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Продукт успешно добавлен!");
                alert.showAndWait();
            } else {
                String errorMessage = response.toString();
                System.out.println("Код ответа сервера: " + responseCode);
                System.out.println("Тело ответа сервера: " + errorMessage);
                if (errorMessage.isEmpty()) {
                    System.out.println("Ошибка добавления продукта. Код ответа: " + responseCode + ", Тело ответа пустое.");
                } else {
                    try {
                        Map<String, Object> jsonResponse = objectMapper.readValue(errorMessage, Map.class);
                        String error = (String) jsonResponse.getOrDefault("error", "Неизвестная ошибка");
                        System.out.println("Ошибка добавления продукта. Код ответа: " + responseCode + ", Сообщение: " + error);
                    } catch (Exception e) {
                        System.out.println("Ошибка добавления продукта. Код ответа: " + responseCode + ", Тело ответа: " + errorMessage);
                    }
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении продукта: " + e.getMessage());
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