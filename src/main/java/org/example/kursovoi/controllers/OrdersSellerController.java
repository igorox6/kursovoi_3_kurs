package org.example.kursovoi.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Order;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrdersSellerController implements InitializableController {

    @FXML
    private Button menuButton;
    @FXML
    private ScrollPane ordersScrollPane;
    @FXML
    private VBox ordersList;
    @FXML
    private ImageView penIcon;
    @FXML
    private ImageView iconOrders;
    @FXML
    private ImageView iconBasket;
    @FXML
    private Button ordersButton;
    @FXML
    private Button basketButton;
    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;
    @FXML
    private TextField minSumField;
    @FXML
    private TextField maxSumField;
    @FXML
    private ComboBox<String> paymentFilter;

    private Application app;
    private UserSession userSession;
    private List<Order> allOrders;

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = userSession;
        String token = userSession.getToken();
        if (token != null && !token.isEmpty()) {
            Order.initializeAllOrders(token);
        } else {
            System.err.println("Token is null or empty, cannot fetch buyers or orders.");
        }
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = userSession;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIcons();
        allOrders = Order.getOrders();
        setupFilters();
        displayOrders();
    }

    private void loadIcons() {
        Image penImage = new Image(Objects.requireNonNull(getClass().getResource("/img/pen.png")).toExternalForm());
        if (penIcon != null) penIcon.setImage(penImage);

        if (iconOrders != null) iconOrders.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/package.png")).toExternalForm()));
        if (iconBasket != null) iconBasket.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/shopping-basket.png")).toExternalForm()));
    }

    private void setupFilters() {
        startDateField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayOrders());
        endDateField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayOrders());
        minSumField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayOrders());
        maxSumField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayOrders());
        paymentFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayOrders());
        paymentFilter.getSelectionModel().select("Все");
    }

    private void filterAndDisplayOrders() {
        if (allOrders == null || allOrders.isEmpty()) {
            ordersList.getChildren().clear();
            ordersList.getChildren().add(new Label("Нет заказов для отображения."));
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Date startDate = null, endDate = null;
        Double minSum = null, maxSum = null;
        String paymentStatus = paymentFilter.getSelectionModel().getSelectedItem();

        try {
            if (!startDateField.getText().trim().isEmpty()) {
                startDate = dateFormat.parse(startDateField.getText().trim());
            }
            if (!endDateField.getText().trim().isEmpty()) {
                endDate = dateFormat.parse(endDateField.getText().trim());
            }
        } catch (ParseException e) {
            // Invalid date format, proceed with null
        }

        try {
            if (!minSumField.getText().trim().isEmpty()) {
                minSum = Double.parseDouble(minSumField.getText().trim());
            }
            if (!maxSumField.getText().trim().isEmpty()) {
                maxSum = Double.parseDouble(maxSumField.getText().trim());
            }
        } catch (NumberFormatException e) {
            // Invalid number format, proceed with null
        }

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        Double finalMinSum = minSum;
        Double finalMaxSum = maxSum;

        List<Order> filteredOrders = allOrders.stream()
                .filter(order -> {
                    if (order == null) return false;

                    if (finalStartDate != null && order.getDateReceipt().before(finalStartDate)) return false;
                    if (finalEndDate != null && order.getDateReceipt().after(finalEndDate)) return false;

                    if (finalMinSum != null && order.getTotalSum() < finalMinSum) return false;
                    if (finalMaxSum != null && order.getTotalSum() > finalMaxSum) return false;

                    if ("Оплачено".equals(paymentStatus) && !order.isPaid()) return false;
                    if ("Не оплачено".equals(paymentStatus) && order.isPaid()) return false;

                    return true;
                })
                .collect(Collectors.toList());

        displayOrders(filteredOrders);
    }

    private void displayOrders() {
        displayOrders(allOrders);
    }

    private void displayOrders(List<Order> orders) {
        ordersList.getChildren().clear();
        if (orders == null || orders.isEmpty()) {
            ordersList.getChildren().add(new Label("Нет заказов для отображения."));
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        List<Buyer> buyers = Buyer.getBuyers();

        for (Order order : orders) {
            if (order == null) continue;

            HBox orderBox = new HBox(20);
            orderBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

            VBox detailsBox = new VBox(5);
            Label dateLabel = new Label("Дата: " + dateFormat.format(order.getDateReceipt()));
            dateLabel.setStyle("-fx-font-weight: bold;");
            Label orderLabel = new Label(String.format("Заказ #%d - Сумма: %.2f руб.", order.getId(), order.getTotalSum()));
            orderLabel.setStyle("-fx-font-weight: bold;");
            Label paidLabel = new Label("Оплачен: " + (order.isPaid() ? "Да" : "Нет"));
            paidLabel.setStyle("-fx-italic: true;");

            String buyerInfo = "Покупатель: Неизвестен";
            if (order.getIdBuyer() != null) {
                Buyer buyer = buyers.stream()
                        .filter(b -> b.getId_buyer() != null && b.getId_buyer().equals(order.getIdBuyer().longValue()))
                        .findFirst()
                        .orElse(null);
                if (buyer != null) {
                    buyerInfo = "Покупатель: " + buyer.getName() + " " + buyer.getLastname();
                } else {
                    System.out.println("Buyer not found for ID: " + order.getIdBuyer());
                }
            }
            Label buyerLabel = new Label(buyerInfo);
            buyerLabel.setStyle("-fx-font-size: 12px;");

            detailsBox.getChildren().addAll(dateLabel, orderLabel, paidLabel, buyerLabel);

            Button editButton = new Button();
            if (penIcon != null) {
                editButton.setGraphic(new ImageView(penIcon.getImage()));
            }
            editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            editButton.setOnAction(e -> openOrderDetails(order));

            orderBox.getChildren().addAll(detailsBox, editButton);
            ordersList.getChildren().add(orderBox);
        }
    }

    private void openOrderDetails(Order order) {
        app.switchSceneWithObject("/org/example/kursovoi/order-detailed-seller-view.fxml", order);
    }

    @FXML
    public void onMenuClick(ActionEvent actionEvent) {
        app.switchScene("/org/example/kursovoi/seller-view.fxml");
    }

    @FXML
    public void onOrdersClick(ActionEvent actionEvent) {
        // No action needed here as this is the orders view
    }

    @FXML
    public void onBasketClick(ActionEvent actionEvent) {
        System.out.println("Переход на вкладку заказа продавца");
    }
}