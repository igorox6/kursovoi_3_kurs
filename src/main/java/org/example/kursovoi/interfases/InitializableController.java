package org.example.kursovoi.interfases;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.example.kursovoi.Application;
import org.example.kursovoi.classes.UserSession;

import java.net.URL;
import java.util.ResourceBundle;

public interface InitializableController extends Initializable {
    void setMainApp(Application app, UserSession userSession);
    void setMainAppWithObject(Application app, UserSession userSession, Object data);
}