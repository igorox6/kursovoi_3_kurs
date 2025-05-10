package org.example.kursovoi.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Order;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class OrderUserController implements InitializableController {

    @FXML
    private Button backButton;
    @FXML
    private ScrollPane ordersScrollPane;
    @FXML
    private VBox ordersList;

    private Application app;
    private UserSession userSession;
    private List<Order> userOrders;

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = userSession;

        String token = userSession.getToken();
        if (token != null && !token.isEmpty()) {
            // Загружаем только заказы текущего пользователя
            Order.initializeUserOrders(token, userSession);
            // Получаем и сохраняем в поле
            this.userOrders = Order.getOrders();
            displayOrders();
        } else {
            System.err.println("Token is null or empty, cannot fetch orders.");
        }
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        setMainApp(app, userSession);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Заказы будут загружены в setMainApp
    }

    private void displayOrders() {
        ordersList.getChildren().clear();

        if (userOrders == null || userOrders.isEmpty()) {
            ordersList.getChildren().add(new Label("У вас пока нет заказов."));
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

        for (Order order : userOrders) {
            if (order == null) continue;

            HBox orderBox = new HBox(20);
            orderBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

            VBox detailsBox = new VBox(5);
            Label dateLabel = new Label("Дата: " + dateFormat.format(order.getDateReceipt()));
            dateLabel.setStyle("-fx-font-weight: bold;");
            Label orderLabel = new Label(String.format("Заказ #%d - Сумма: %.2f руб.", order.getId(), order.getTotalSum()));
            Label paidLabel = new Label("Оплачен: " + (order.isPaid() ? "Да" : "Нет"));

            detailsBox.getChildren().addAll(dateLabel, orderLabel, paidLabel);
            orderBox.getChildren().add(detailsBox);

            ordersList.getChildren().add(orderBox);
        }
    }

    @FXML
    public void onBackClick(ActionEvent actionEvent) {
        app.switchScene("/org/example/kursovoi/filter-catalog-view.fxml");
    }
}