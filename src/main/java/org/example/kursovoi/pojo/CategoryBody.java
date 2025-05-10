package org.example.kursovoi.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryBody {
    private String name;
    private Long id_parent_category;

    public CategoryBody(String name, Long id_parent_category) {
        this.name = name;
        this.id_parent_category = id_parent_category;
    }

}