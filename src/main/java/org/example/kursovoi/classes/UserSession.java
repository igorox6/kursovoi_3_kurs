package org.example.kursovoi.classes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.kursovoi.db_classes.Buyer;
import org.example.kursovoi.db_classes.Worker;

@Getter
@Setter
@NoArgsConstructor
public class UserSession {
    private static UserSession instance;
    private Buyer buyer;
    private Worker worker;
    private String token;
    private Long id_user;

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setBuyer(Buyer buyer, String token) {
        this.buyer = buyer;
        this.worker = null;
        this.token = token;
        System.out.println("UserSession: Buyer set with nickname: " + (buyer != null ? buyer.getNickname() : "null"));
    }

    public boolean isLoggedIn() {
        return buyer != null && token != null;
    }

    public void setWorker(Worker worker, String token) {
        this.worker = worker;
        this.buyer = null;
        this.token = token;
        System.out.println("UserSession: Worker set with nickname: " + (worker != null ? worker.getNickname() : "null"));
    }
}