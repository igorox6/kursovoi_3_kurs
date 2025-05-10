package org.example.kursovoi.interfases;

import org.example.kursovoi.classes.UserSession;

import java.util.Map;

public interface User {
    Long getId();
    void setId(Long id);
    String getEmail();
    void setEmail(String email);
    String getPhone();
    void setPhone(String phone);
    String getPassword();
    void setPassword(String password);
    String getNickname();
    void setNickname(String nickname);
    Long getIdRole();
    void setIdRole(Long idRole);

    public Map<String, Object> login(UserSession userSession);
}
