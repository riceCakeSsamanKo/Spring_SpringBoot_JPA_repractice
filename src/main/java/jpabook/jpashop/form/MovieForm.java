package jpabook.jpashop.form;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MovieForm extends ItemForm {

    private String director;
    private String actor;
}
