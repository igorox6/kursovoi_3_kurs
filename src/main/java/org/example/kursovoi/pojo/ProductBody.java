package org.example.kursovoi.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductBody {
    private String productName;
    private double cost;
    private Long idCompany;
    private String properties;
    private String photo;

    public ProductBody() {
    }

    public ProductBody(String productName, double cost, Long idCompany, String properties, String photo) {
        this.productName = productName;
        this.cost = cost;
        this.idCompany = idCompany;
        this.properties = properties;
        this.photo = photo;
    }
}