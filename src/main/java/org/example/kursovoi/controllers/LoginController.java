package org.example.kursovoi.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Worker;
import org.example.kursovoi.interfases.InitializableController;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginController implements InitializableController {
    private Application app;
    private UserSession userSession;

    @FXML
    private TextField login_mail_phone;
    @FXML
    private PasswordField login_password;
    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private ImageView iconMailLogin;
    @FXML
    private ImageView iconLockLogin;
    @FXML
    private ImageView iconArrowRightLogin;

    private static final String CONFIG_FILE = "config.properties";

    @Override
    public void setMainApp(Application app, UserSession userSession) {
        this.app = app;
        this.userSession = UserSession.getInstance();
    }

    @Override
    public void setMainAppWithObject(Application app, UserSession userSession, Object data) {
        this.app = app;
        this.userSession = UserSession.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIcons();

        Properties props = loadProperties();
        String mailOrPhone = props.getProperty("mailOrPhone");
        String password = props.getProperty("password");
        if (mailOrPhone != null && password != null) {
            login_mail_phone.setText(mailOrPhone);
            login_password.setText(password);
            rememberMeCheckBox.setSelected(true);
        }
    }

    private void loadIcons() {
        iconMailLogin.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/mail.png")).toExternalForm()));
        iconLockLogin.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/lock.png")).toExternalForm()));
        iconArrowRightLogin.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/keyboard_arrow_left.png")).toExternalForm()));
    }

    public void go_to_registration(MouseEvent mouseEvent) throws IOException {
        app.switchScene("registration-view.fxml");
    }

    @FXML
    public void login_next(MouseEvent mouseEvent) {
        String mailOrPhone = login_mail_phone.getText().trim();
        String password = login_password.getText().trim();

        if (mailOrPhone.isEmpty() || password.isEmpty()) {
            System.out.println("Ошибка: Все поля должны быть заполнены");
            return;
        }

        boolean isEmail = mailOrPhone.contains("@");

        // Если это телефон — оставляем только последние 10 цифр
        if (!isEmail) {
            // Удаляем всё, кроме цифр
            String digitsOnly = mailOrPhone.replaceAll("\\D", "");

            // Если номер длиннее 10 цифр, берём последние 10
            if (digitsOnly.length() > 10) {
                mailOrPhone = digitsOnly.substring(digitsOnly.length() - 10);
            } else {
                mailOrPhone = digitsOnly; // или можно ругнуться, если меньше 10
            }
        }

        try {
            Buyer buyer = new Buyer("", "", isEmail ? mailOrPhone : null, !isEmail ? mailOrPhone : null, password, null, 1L);
            Map<String, Object> loginResponse = buyer.login(userSession);

            if (rememberMeCheckBox.isSelected()) {
                saveCredentials(mailOrPhone, password);
            } else {
                clearSavedCredentials();
            }

            Long userRole = ((Number) loginResponse.get("idRole")).longValue();
            Long id_user = ((Number) loginResponse.get("id")).longValue();
            userSession.setId_user(id_user);

            if (userRole == 1L) {
                System.out.println("Переход на filter-catalog-view.fxml с id_user: " + id_user);
                app.switchScene("filter-catalog-view.fxml");
            } else if (userRole == 2L) {
                System.out.println("Переход на admin-panel-view.fxml с id_user: " + id_user);
                app.switchScene("admin-panel-view.fxml");
            } else if (userRole == 3L) {
                Worker worker = new Worker(isEmail ? mailOrPhone : null, !isEmail ? mailOrPhone : null, password, null, 3L);
                loginResponse = worker.login(userSession);
                System.out.println("Переход на seller-view.fxml с id_worker: " + id_user);
                app.switchScene("seller-view.fxml");
            } else {
                throw new RuntimeException("Неизвестная роль пользователя: " + userRole);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при авторизации: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void saveCredentials(String mailOrPhone, String password) {
        Properties props = new Properties();
        props.setProperty("mailOrPhone", mailOrPhone);
        props.setProperty("password", password);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Login credentials");
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    private void clearSavedCredentials() {
        try {
            Files.deleteIfExists(Paths.get(CONFIG_FILE));
        } catch (IOException e) {
            System.out.println("Ошибка при удалении файла: " + e.getMessage());
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                props.load(input);
            } catch (IOException e) {
                System.out.println("Ошибка при загрузке данных: " + e.getMessage());
            }
        }
        return props;
    }
}