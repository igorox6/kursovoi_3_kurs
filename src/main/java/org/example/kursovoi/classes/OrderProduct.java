package org.example.kursovoi.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.kursovoi.db_classes.Product;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderProduct {
    private Product product;
    private int quantity;
}