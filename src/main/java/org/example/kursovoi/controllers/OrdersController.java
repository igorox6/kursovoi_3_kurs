package org.example.kursovoi.controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class OrdersController implements InitializableController {

    @FXML
    private VBox ordersContainer;

    @FXML
    private Button profileButton;
    @FXML
    private Button ordersButton;
    @FXML
    private Button basketButton;

    private Application app;
    private UserSession userSession;
    private HttpClient httpClient;
    private Gson gson;

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = UserSession.getInstance();
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        loadOrders();
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = UserSession.getInstance();
    }

    private void loadOrders() {
        Long idBuyer = userSession.getBuyer() != null ? userSession.getBuyer().getId_buyer() : null;
        if (idBuyer == null) {
            System.out.println("Cannot load orders: idBuyer is null");
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/receipts/byBuyer/" + idBuyer))
                    .header("Authorization", "Bearer " + userSession.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> responseBody = gson.fromJson(response.body(), Map.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> receipts = (List<Map<String, Object>>) responseBody.get("data");
                ordersContainer.getChildren().clear();
                int index = 1;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (Map<String, Object> receipt : receipts) {
                    String formattedDate = dateFormat.format(receipt.get("date_receipt"));
                    Button orderButton = new Button();
                    orderButton.setText(String.format("%d", index++));
                    orderButton.setGraphic(new HBox(10, new Label(formattedDate),
                            new Label(String.valueOf(receipt.get("total_sum")) + " руб."),
                            new Label((Boolean) receipt.get("paid") ? "Да" : "Нет")));
                    orderButton.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-cursor: hand; -fx-max-width: 900; -fx-alignment: CENTER-LEFT;");
                    orderButton.setOnAction(event -> showOrderDetails(((Number) receipt.get("id")).longValue()));
                    ordersContainer.getChildren().add(orderButton);
                }
            } else {
                System.out.println("Failed to load orders, status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Error loading orders: " + e.getMessage());
        }
    }

    private void showOrderDetails(Long receiptId) {
        app.switchSceneWithObject("/org/example/kursovoi/order-details-view.fxml", receiptId);
    }

    @FXML
    public void onProfileClick(ActionEvent event) {
        //app.switchScene("/org/example/kursovoi/profile-view.fxml");
    }

    @FXML
    public void onOrdersClick(ActionEvent event) {
        // Уже на этой странице
    }

    @FXML
    public void onBasketClick(ActionEvent event) {
        //app.switchScene("/org/example/kursovoi/cart-view.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}