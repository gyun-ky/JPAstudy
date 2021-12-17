package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // JOINED - 가장 정규화 된 스타일로 상속 / TABLE_PER_CLASS - 하위 클래스가 각각의 테이블로 나누어짐 / SINGLE_TABLE - 하나의 테이블에 다 넣음
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stackQuantity;


}
