package org.example.kursovoi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Category;
import org.example.kursovoi.db_classes.Product;
import org.example.kursovoi.interfases.InitializableController;

import java.net.URL;
import java.util.*;

public class RegController implements InitializableController {

    @FXML
    public ImageView iconUser;
    @FXML
    public ImageView iconUser2;
    @FXML
    public ImageView iconMail;
    @FXML
    public ImageView iconLock1;
    @FXML
    public ImageView iconLock2;
    @FXML
    public ImageView iconArrowLeft;

    @FXML
    public TextField enter_name;
    @FXML
    public TextField enter_lastname;
    @FXML
    public TextField enter_mail_phone;
    @FXML
    public PasswordField enter_password;
    @FXML
    public PasswordField enter_repeit_password;

    @FXML
    public TreeView treeView;

    @FXML
    public Text button_go_to_authorization;
    @FXML
    public Group button_go_to_next;

    private Application app;
    private List<Category> categoriesList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private HashMap<Long, TreeItem<String>> categoriesMap = new HashMap<>();
    private UserSession userSession;

    @FXML
    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = userSession;
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
    }


    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image userIcon = new Image(Objects.requireNonNull(getClass().getResource("/img/user.png")).toExternalForm());
        iconUser.setImage(userIcon);
        iconUser2.setImage(userIcon);

        Image mailIcon = new Image(Objects.requireNonNull(getClass().getResource("/img/mail.png")).toExternalForm());
        iconMail.setImage(mailIcon);

        Image lockIcon = new Image(Objects.requireNonNull(getClass().getResource("/img/lock.png")).toExternalForm());
        iconLock1.setImage(lockIcon);
        iconLock2.setImage(lockIcon);

        Image arrowIcon = new Image(Objects.requireNonNull(getClass().getResource("/img/keyboard_arrow_left.png")).toExternalForm());
        iconArrowLeft.setImage(arrowIcon);
    }

    @FXML
    public void go_to_authorization(MouseEvent mouseEvent) {
        app.switchScene("auth-view.fxml");
    }

    @FXML
    public void go_to_next(MouseEvent mouseEvent) {
        /* Регистрация */
        String name = enter_name.getText().trim();
        String lastname = enter_lastname.getText().trim();
        String mailOrPhone = enter_mail_phone.getText().trim();
        String password = enter_password.getText().trim();
        String repeatPassword = enter_repeit_password.getText().trim();

        if (name.isEmpty() || lastname.isEmpty() || mailOrPhone.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            System.out.println("Ошибка: Все поля должны быть заполнены");
            return;
        }

        if (!password.equals(repeatPassword)) {
            System.out.println("Ошибка: Пароли не совпадают");
            return;
        }

        try {
            // Создаём объект Buyer для отправки запроса
            Buyer buyer = new Buyer(
                    name,
                    lastname,
                    mailOrPhone.contains("@") ? mailOrPhone : null,
                    !mailOrPhone.contains("@") ? mailOrPhone : null,
                    password,
                    name + " " + lastname,
                    1L
            );

            // Отправляем запрос на регистрацию
            Map<String, Object> response = buyer.register();
            int status = ((Number) response.get("status")).intValue();

            if (status == 200) {
                Long buyerId = ((Number) response.get("id_buyer")).longValue();
                buyer.setId(buyerId);
                userSession.setBuyer(buyer, null);

                System.out.println("Регистрация успешна. ID покупателя: " + buyerId);
                app.switchScene("auth-view.fxml"); // Переход на экран авторизации после успешной регистрации
            } else {
                System.out.println("Ошибка регистрации: " + response.get("error"));
            }
        } catch (Exception e) {
            System.out.println("Ошибка при регистрации: " + e.getMessage());
        }
    }
}