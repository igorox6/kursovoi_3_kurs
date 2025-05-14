package org.example.kursovoi.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminPanelController implements InitializableController, Initializable {

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

    @FXML
    public void onProductsClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/products-view.fxml");
    }

    @FXML
    public void onAnalyticsClick(ActionEvent event) {
        app.switchScene("/org/example/kursovoi/analytics-view.fxml");
    }
}