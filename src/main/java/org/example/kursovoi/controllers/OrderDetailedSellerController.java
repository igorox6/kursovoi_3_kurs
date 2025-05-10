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
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Order;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.db_classes.Worker;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrderDetailedSellerController implements InitializableController {

    @FXML
    private Button backButton;
    @FXML
    private Label orderInfoLabel;
    @FXML
    private ScrollPane productsScrollPane;
    @FXML
    private VBox productsList;

    @FXML
    private ImageView iconOrders;
    @FXML
    private ImageView iconBasket;
    @FXML
    private Button ordersButton;
    @FXML
    private Button basketButton;

    @FXML
    private Button payButton;

    private Application app;
    private UserSession userSession;
    private Order currentOrder;

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
        if (data instanceof Order) {
            this.currentOrder = (Order) data;
        }
        initialize(null, null);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIcons();
        displayOrderDetails();
    }

    private void loadIcons() {
        iconOrders.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/package.png")).toExternalForm()));
        iconBasket.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/shopping-basket.png")).toExternalForm()));
    }

    private void displayOrderDetails() {
        if (currentOrder == null) {
            orderInfoLabel.setText("Заказ не найден");
            return;
        }

        // Формат даты
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

        String buyerInfo = "Покупатель: Неизвестен";
        List<Buyer> buyers = Buyer.getBuyers();
        if (currentOrder.getIdBuyer() != null) {
            Buyer buyer = buyers.stream()
                    .filter(b -> b.getId_buyer() != null && b.getId_buyer().equals(currentOrder.getIdBuyer().longValue()))
                    .findFirst()
                    .orElse(null);
            if (buyer != null) {
                buyerInfo = "Покупатель: " + buyer.getName() + " " + buyer.getLastname();
            } else {
                System.out.println("Buyer not found for ID: " + currentOrder.getIdBuyer());
            }
        }

        // Собираем строку заказа
        String orderInfo = String.format(
                "Заказ #%d - Дата: %s - %s - Сумма: %.2f руб.",
                currentOrder.getId(),
                dateFormat.format(currentOrder.getDateReceipt()),
                buyerInfo,
                currentOrder.getTotalSum()
        );

        orderInfoLabel.setText(orderInfo);

        java.util.List<Product> products = currentOrder.getProducts();
        if (products == null || products.isEmpty()) {
            Label emptyLabel = new Label("Нет продуктов в заказе");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-padding: 20;");
            productsList.getChildren().add(emptyLabel);
            payButton.setDisable(true);
            return;
        }

        payButton.setDisable(false);
        productsList.getChildren().clear();

        Map<Long, java.util.List<Product>> groupedProducts = products.stream()
                .collect(Collectors.groupingBy(Product::getId));

        for (Map.Entry<Long, java.util.List<Product>> entry : groupedProducts.entrySet()) {
            Product product = entry.getValue().get(0);
            int quantity = entry.getValue().size();
            long totalCost = product.getCost() * quantity;

            HBox productBox = new HBox(10);
            productBox.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

            Label nameLabel = new Label(product.getProductName() + " (x" + quantity + ")");
            nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            nameLabel.setPrefWidth(300);

            Label totalCostLabel = new Label(totalCost + " руб.");
            totalCostLabel.setStyle("-fx-font-size: 14;");
            totalCostLabel.setPrefWidth(100);

            productBox.getChildren().addAll(nameLabel, totalCostLabel);
            productsList.getChildren().add(productBox);
        }
    }


    @FXML
    private void handlePayment() {
        if (currentOrder == null || currentOrder.getId() == null || currentOrder.getDateReceipt() == null) {
            System.out.println("Cannot proceed with payment: order data is incomplete");
            return;
        }

        Worker worker = userSession.getWorker();
        if (worker == null || userSession.getToken() == null) {
            System.out.println("Cannot proceed with payment: worker or token is missing");
            return;
        }

        java.util.Date newDate = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(newDate);


        worker.makePurchase(
                userSession.getToken(),
                currentOrder.getId(),
                worker.getId(),
                formattedDate  // передаём строку с датой в формате yyyy-MM-dd
        );

        System.out.println("Payment processed for order #" + currentOrder.getId());
        goBack();
    }




    @FXML
    private void goBack() {
        app.switchScene("/org/example/kursovoi/orders-seller-view.fxml");
    }


}