package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable  // 불변 객체
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    // 값 타입은 기본생성자를 protected로 하는 것이 안전
    protected Address() {}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
