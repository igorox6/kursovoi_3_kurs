package org.example.kursovoi.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Category;
import org.example.kursovoi.db_classes.Company;
import org.example.kursovoi.db_classes.Country;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.db_classes.User;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProductsController implements InitializableController, Initializable {

    @FXML
    private HBox productsOptions;

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
            Product.initializeProducts(token);
            Category.initializeCategories(token);
            User.initializeUsers(token);
            Country.initializeCountries(token);
            Company.initializeCompanies(token);
            displayCounts();
        } else {
            System.out.println("Token is null, cannot initialize data.");
        }
    }

    private void displayCounts() {
        if (productsOptions == null) {
            System.out.println("productsOptions is null, skipping displayCounts.");
            return;
        }
        productsOptions.getChildren().clear();

        int productCount = Product.getProducts().size();
        Button productBox = new Button("Продукты: " + productCount);
        productBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10 20; -fx-min-width: 150;");
        productBox.setOnAction(e -> onProductsManageClick(e));
        productsOptions.getChildren().add(productBox);

        int categoryCount = Category.getCategories().size();
        Button categoryBox = new Button("Категории: " + categoryCount);
        categoryBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10 20; -fx-min-width: 150;");
        categoryBox.setOnAction(e -> onCategoriesManageClick(e));
        productsOptions.getChildren().add(categoryBox);

        int userCount = User.getUsers().size();
        Button userBox = new Button("Пользователи: " + userCount);
        userBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10 20; -fx-min-width: 150;");
        userBox.setOnAction(e -> onUsersClick(e));
        productsOptions.getChildren().add(userBox);

        int countryCount = Country.getCountries().size();
        Button countryBox = new Button("Страны: " + countryCount);
        countryBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10 20; -fx-min-width: 150;");
        countryBox.setOnAction(e -> onCountriesClick(e));
        productsOptions.getChildren().add(countryBox);

        int companyCount = Company.getCompanies().size();
        Button companyBox = new Button("Компании: " + companyCount);
        companyBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10 20; -fx-min-width: 150;");
        companyBox.setOnAction(e -> onCompaniesClick(e));
        productsOptions.getChildren().add(companyBox);
    }

    @FXML
    public void onProductsManageClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/products-manage-view.fxml");
    }

    @FXML
    public void onCategoriesManageClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/categories-manage-view.fxml");
    }

    @FXML
    public void onProductsClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/products-view.fxml");
    }

    @FXML
    public void onAnalyticsClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/analytics-view.fxml");
    }

    @FXML
    public void onUsersClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/users-view.fxml");
    }

    @FXML
    public void onCountriesClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/countries-view.fxml");
    }

    @FXML
    public void onCompaniesClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/companies-view.fxml");
    }
}
