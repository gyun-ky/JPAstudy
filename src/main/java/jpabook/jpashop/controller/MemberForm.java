package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberForm {
    @NotEmpty(message = "회원 이름은 필수 입니다")
    private String name;
    private String city;
    private String street;
    private String zipcode;
}
//화면을 만드는 요소로는 form 객체나 dto(Data Transform Object)를 사용하여야 함
    // --> Entity를 최대한 순수하게 유지하도록 함


