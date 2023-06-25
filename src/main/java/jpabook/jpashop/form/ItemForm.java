package jpabook.jpashop.form;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class ItemForm {

    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
}
