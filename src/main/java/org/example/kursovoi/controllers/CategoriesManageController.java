package org.example.kursovoi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Category;
import org.example.kursovoi.interfases.InitializableController;
import org.example.kursovoi.pojo.CategoryBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class CategoriesManageController implements InitializableController, Initializable {

    @FXML
    private VBox categoriesList;

    @FXML
    private TextField inputName;
    @FXML
    private TextField inputParentId;

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
            Category.initializeCategories(token);
            displayCategories();
        } else {
            System.out.println("Token is null, cannot initialize data.");
        }
    }

    private void displayCategories() {
        if (categoriesList == null) {
            System.out.println("categoriesList is null, skipping displayCategories.");
            return;
        }
        categoriesList.getChildren().clear();

        // Заголовок с названиями полей
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        header.getChildren().addAll(
                new Label("ID") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 50;"); }},
                new Label("Название") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }},
                new Label("ID родительской категории") {{ setStyle("-fx-font-weight: bold; -fx-min-width: 200;"); }}
        );
        categoriesList.getChildren().add(header);

        // Список категорий
        for (Category category : Category.getCategories()) {
            HBox categoryRow = new HBox(20);
            categoryRow.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.3, 0, 1);");
            categoryRow.getChildren().addAll(
                    new Label(String.valueOf(category.getId())) {{ setStyle("-fx-min-width: 50;"); }},
                    new Label(category.getName()) {{ setStyle("-fx-min-width: 200;"); }},
                    new Label(category.getId_parent_category() != null ? String.valueOf(category.getId_parent_category()) : "Нет") {{ setStyle("-fx-min-width: 200;"); }}
            );
            categoriesList.getChildren().add(categoryRow);
        }
    }

    @FXML
    public void onAddCategory(ActionEvent event) {
        try {
            // Собираем данные из полей ввода
            String name = inputName.getText();
            Long idParentCategory = inputParentId.getText().isEmpty() ? null : Long.parseLong(inputParentId.getText());

            // Создаём объект CategoryBody
            CategoryBody categoryBody = new CategoryBody(name, idParentCategory);

            // Сериализуем объект в JSON
            String jsonBody = objectMapper.writeValueAsString(categoryBody);

            // Отправляем POST-запрос
            URL url = new URL("http://localhost:8080/api/categories");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + userSession.getToken());
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Проверяем код ответа
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Читаем ответ
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Обновляем список категорий после успешного добавления
                Category.initializeCategories(userSession.getToken());
                displayCategories();

                // Очистка полей после добавления
                inputName.clear();
                inputParentId.clear();
            } else {
                System.out.println("Ошибка добавления категории. Код ответа: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении категории: " + e.getMessage());
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