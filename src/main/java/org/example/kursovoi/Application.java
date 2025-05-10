package org.example.kursovoi;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.controllers.ProductDetailsController;
import org.example.kursovoi.db_classes.Category;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.interfases.InitializableController;

import java.io.IOException;
import java.net.URL;

public class Application extends javafx.application.Application {
    private Stage primaryStage;
    private StackPane rootLayout;
    public APIClient api;
    private UserSession userSession;
    @Getter
    private Object currentController;

    @Override
    public void start(Stage stage) throws IOException {

        userSession = UserSession.getInstance();

        this.primaryStage = stage;
        rootLayout = new StackPane();
        primaryStage.setTitle("Приложение");
        primaryStage.setScene(new Scene(rootLayout, 1000, 600));

        switchScene("auth-view.fxml");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    public void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent newScene = loader.load();

            Object controller = loader.getController();

            if (controller instanceof InitializableController) {
                ((InitializableController) controller).setMainApp(this, userSession);
            } else {
                System.out.println("Контроллер не реализует InitializableController: " + controller.getClass().getName());
            }

            rootLayout.getChildren().setAll(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void switchSceneWithObject(String fxmlFile, Object data) {
        try {
            System.out.println("Switching to: " + fxmlFile + " with data: " + (data != null ? data.toString() : "null"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent newScene = loader.load();

            currentController = loader.getController();
            System.out.println("Loaded controller: " + (currentController != null ? currentController.getClass().getName() : "null"));

            if (currentController instanceof InitializableController) {
                ((InitializableController) currentController).setMainAppWithObject(this, userSession, data);
            } else {
                System.out.println("Контроллер не реализует InitializableController: " + (currentController != null ? currentController.getClass().getName() : "null"));
            }

            rootLayout.getChildren().setAll(newScene);
        } catch (IOException e) {
            System.out.println("Ошибка при переходе к сцене: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Stage getStage() {
        return primaryStage;
    }

}