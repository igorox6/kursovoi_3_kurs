package org.example.kursovoi.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Category;
import org.example.kursovoi.db_classes.Company;
import org.example.kursovoi.db_classes.Country;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductDetailsSellerController implements InitializableController {

    @FXML
    private ImageView productImage;

    @FXML
    private Label productNameLabel;

    @FXML
    private Label countryLabel;

    @FXML
    private Label companyLabel;

    @FXML
    private Label categoriesLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label propertiesLabel;

    @FXML
    private Button addToCartButton;

    @FXML
    private Button backButton;

    @FXML
    private ImageView iconUser;

    @FXML
    private ImageView iconOrders;

    @FXML
    private ImageView iconBasket;

    @FXML
    private Button profileButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button basketButton;

    private Application app;
    private UserSession userSession;
    private Product product;
    private Map<Long, String> companyNames;
    private Map<Long, String> countryNames;

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = userSession;
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = userSession;
        System.out.println("id_buyer: " + (userSession.getBuyer() != null ? userSession.getBuyer().getId_buyer() : "null"));
        if (data instanceof Product) {
            this.product = (Product) data;
            System.out.println("Product received: " + (product != null ? product.getProductName() : "null"));
            initialize(null, null);
        } else {
            System.out.println("Data is not a Product: " + (data != null ? data.getClass().getName() : "null"));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing ProductDetailsController...");

        if (product == null) {
            System.out.println("Product is null during initialization.");
            return;
        }

        // Загрузка данных компаний и стран
        if (userSession != null && userSession.getToken() != null) {
            Company.initializeCompanies(userSession.getToken());
            Country.initializeCountries(userSession.getToken());
            companyNames = Company.getCompanies().stream()
                    .collect(Collectors.toMap(Company::getId, Company::getName));
            countryNames = Country.getCountries().stream()
                    .collect(Collectors.toMap(Country::getId, Country::getName));
        } else {
            companyNames = new java.util.HashMap<>();
            countryNames = new java.util.HashMap<>();
            System.out.println("Token is null, cannot initialize companies or countries.");
        }

        // Загрузка иконок
        loadIcons();

        // Установка ника пользователя
        if (userSession != null && userSession.getBuyer() != null && userSession.getBuyer().getNickname() != null) {
            String nickname = userSession.getBuyer().getNickname();
            System.out.println("Setting nickname on profile button: " + nickname);
            profileButton.setText(nickname);
        } else {
            System.out.println("Nickname not available, using default.");
            profileButton.setText("Имя");
        }

        // Заполнение данных продукта
        System.out.println("Product name: " + product.getProductName());
        productNameLabel.setText(product.getProductName().toUpperCase());

        if (product.getPhoto() != null && !product.getPhoto().isEmpty()) {
            try {
                productImage.setImage(new Image(product.getPhoto()));
            } catch (Exception e) {
                System.out.println("Error loading product image: " + e.getMessage());
                productImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/placeholder.png")).toExternalForm()));
            }
        } else {
            productImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/placeholder.png")).toExternalForm()));
        }

        // Получение названия компании
        String companyName = companyNames.getOrDefault(product.getIdCompany(), "Не указана");
        companyLabel.setText("Компания: " + companyName);

        // Получение названия страны через idCountry компании
        String countryName = "Не указана";
        Company company = Company.getCompanies().stream()
                .filter(c -> c.getId().equals(product.getIdCompany()))
                .findFirst()
                .orElse(null);
        if (company != null && company.getIdCountry() != null) {
            countryName = countryNames.getOrDefault(company.getIdCountry(), "Не указана");
        }
        countryLabel.setText("Страна производства: " + countryName);

        categoriesLabel.setText("Категории: " + (product.getCategories() != null && !product.getCategories().isEmpty()
                ? String.join(", ", product.getCategories().stream().map(Category::getName).collect(Collectors.toList()))
                : "Не указаны"));
        priceLabel.setText("Цена: " + (product.getCost() != null ? product.getCost() + " руб." : "Не указана"));
        propertiesLabel.setText("Характеристики: " + (product.getProperties() != null ? product.getProperties().toString() : "Не указаны"));

        updateAddToCartButtonVisibility();
    }

    private void loadIcons() {
        iconUser.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/user.png")).toExternalForm()));
        iconOrders.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/package.png")).toExternalForm()));
        iconBasket.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/shopping-basket.png")).toExternalForm()));
    }

    @FXML
    private void addToCart() {
        if (userSession != null && userSession.getBuyer() != null) {
            Buyer buyer = userSession.getBuyer();
            if (buyer.getCart() == null) {
                buyer.setCart(new ArrayList<>());
            }
            buyer.getCart().add(product);
            System.out.println("Продукт добавлен в корзину: " + product.getProductName());
        } else {
            System.out.println("Ошибка: Пользователь не авторизован как Buyer.");
        }
    }

    @FXML
    private void goBack() {
        app.switchScene("/org/example/kursovoi/seller-view.fxml");
    }


    @FXML
    public void onProfileClick() {
        System.out.println("Профиль");
    }

    @FXML
    public void onOrdersClick() {
        app.switchScene("/org/example/kursovoi/order-seller-view.fxml");
    }

    @FXML
    public void onBasketClick() {
        System.out.println("Корзина продавца");
        //app.switchScene("/org/example/kursovoi/cart-view.fxml");
    }

    private void updateAddToCartButtonVisibility() {
        if (userSession != null && userSession.getBuyer() != null) {
            addToCartButton.setVisible(true);
        } else {
            addToCartButton.setVisible(false);
        }
    }

    public void onMenuClick(ActionEvent actionEvent) {
        app.switchScene("/org/example/kursovoi/seller-view.fxml");
    }
}