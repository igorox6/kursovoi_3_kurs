package org.example.kursovoi.classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerDTO {
    private Long id;
    private String name;
    private String lastname;

    public BuyerDTO() {
    }

    public BuyerDTO(Long id, String name, String lastname) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
    }
}