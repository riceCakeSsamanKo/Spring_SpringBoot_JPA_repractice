package jpabook.jpashop.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수입니다.")  // 값이 없는 경우 에러발생함
    private String name;

    private String city;
    private String street;
    private String zipcode;
}