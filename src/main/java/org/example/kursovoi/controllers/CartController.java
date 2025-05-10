package org.example.kursovoi.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CartController implements InitializableController {

    @FXML
    private ScrollPane cartScrollPane;

    @FXML
    private VBox cartItemsContainer;



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

    @FXML
    private Label totalSumLabel;

    @FXML
    private Button payButton;

    private Application app;
    private UserSession userSession;

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = UserSession.getInstance();
        initialize(null, null);
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = UserSession.getInstance();
        initialize(null, null);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIcons();
        updateProfileButton();
        displayCartItems();
    }

    private void loadIcons() {
        iconUser.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/user.png")).toExternalForm()));
        iconOrders.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/package.png")).toExternalForm()));
        iconBasket.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/shopping-basket.png")).toExternalForm()));
    }

    private void updateProfileButton() {
        if (userSession != null && userSession.getBuyer() != null && userSession.getBuyer().getNickname() != null) {
            String nickname = userSession.getBuyer().getNickname();
            System.out.println("Setting nickname on profile button: " + nickname);
            profileButton.setText(nickname);
        } else {
            System.out.println("Nickname not available, using default.");
            profileButton.setText("Имя");
        }
    }

    private void displayCartItems() {
        cartItemsContainer.getChildren().clear();

        if (userSession == null || userSession.getBuyer() == null || userSession.getBuyer().getCart() == null || userSession.getBuyer().getCart().isEmpty()) {
            Label emptyLabel = new Label("Корзина пуста");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-padding: 20;");
            cartItemsContainer.getChildren().add(emptyLabel);
            totalSumLabel.setText("Итого: 0 руб.");
            payButton.setDisable(true);
            return;
        }

        payButton.setDisable(false);

        // Группируем товары по ID, чтобы посчитать количество каждого товара
        Map<Long, List<Product>> groupedProducts = userSession.getBuyer().getCart().stream()
                .collect(Collectors.groupingBy(Product::getId));

        long totalSum = 0;

        for (Map.Entry<Long, List<Product>> entry : groupedProducts.entrySet()) {
            Product product = entry.getValue().get(0); // Первый экземпляр товара
            int quantity = entry.getValue().size(); // Количество товара
            long totalCost = product.getCost() * quantity; // Общая стоимость
            totalSum += totalCost;

            HBox productBox = new HBox(10);
            productBox.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

            // Название товара
            Label nameLabel = new Label(product.getProductName());
            nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            nameLabel.setPrefWidth(200);

            // Кнопка "−"
            Button decreaseButton = new Button("−");
            decreaseButton.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
            decreaseButton.setOnAction(e -> {
                decreaseProductQuantity(product);
                displayCartItems(); // Обновляем отображение
            });

            // Количество
            Label quantityLabel = new Label(String.valueOf(quantity));
            quantityLabel.setStyle("-fx-font-size: 14; -fx-padding: 0 10 0 10;");

            // Кнопка "+"
            Button increaseButton = new Button("+");
            increaseButton.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
            increaseButton.setOnAction(e -> {
                increaseProductQuantity(product);
                displayCartItems(); // Обновляем отображение
            });

            // Общая стоимость
            Label totalCostLabel = new Label(totalCost + " руб.");
            totalCostLabel.setStyle("-fx-font-size: 14;");
            totalCostLabel.setPrefWidth(100);

            // Кнопка удаления
            Button deleteButton = new Button("✖");
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
            deleteButton.setOnAction(e -> {
                removeProductFromCart(product);
                displayCartItems(); // Обновляем отображение
            });

            productBox.getChildren().addAll(nameLabel, decreaseButton, quantityLabel, increaseButton, totalCostLabel, deleteButton);
            cartItemsContainer.getChildren().add(productBox);
        }

        totalSumLabel.setText("Итого: " + totalSum + " руб.");
    }

    private void increaseProductQuantity(Product product) {
        if (userSession != null && userSession.getBuyer() != null) {
            userSession.getBuyer().getCart().add(product);
            System.out.println("Увеличено количество товара: " + product.getProductName());
        }
    }

    private void decreaseProductQuantity(Product product) {
        if (userSession != null && userSession.getBuyer() != null) {
            List<Product> cart = userSession.getBuyer().getCart();
            Optional<Product> productToRemove = cart.stream()
                    .filter(p -> p.getId().equals(product.getId()))
                    .findFirst();
            productToRemove.ifPresent(cart::remove);
            System.out.println("Уменьшено количество товара: " + product.getProductName());
        }
    }

    private void removeProductFromCart(Product product) {
        if (userSession != null && userSession.getBuyer() != null) {
            List<Product> cart = userSession.getBuyer().getCart();
            cart.removeIf(p -> p.getId().equals(product.getId()));
            System.out.println("Товар удалён из корзины: " + product.getProductName());
        }
    }

    @FXML
    private void handlePayment() {
        if (userSession.getBuyer().getId_buyer() == null) {
            System.out.println("Cannot proceed with payment: id_buyer is null");
            return;
        }

        userSession.getBuyer().makePurchase(userSession.getToken(), userSession.getBuyer().getId_buyer());
        displayCartItems(); // Обновляем отображение после покупки
    }

    @FXML
    private void goBack() {
        app.switchScene("/org/example/kursovoi/filter-catalog-view.fxml");
    }

    @FXML
    public void onProfileClick() {
        System.out.println("Переход в профиль пользователя");
    }

    @FXML
    public void onOrdersClick() {
        app.switchScene("/org/example/kursovoi/order-user-view.fxml");
    }

    @FXML
    public void onBasketClick() {
        System.out.println("Уже в корзине");
    }
}