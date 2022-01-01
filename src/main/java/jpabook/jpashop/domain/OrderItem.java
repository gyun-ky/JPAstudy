package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED) // 매개변수 없는 protected 생성자와 동일
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    protected OrderItem(){

    } //다른 곳에서 createOrderItem 메서드를 사용하지 않고 직접 생성자를 통해 생성하려하는 경우 막기

    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    /**
     * 주문 취소 로직
     */
    public void cancel(){
        getItem().addStock(count);
    }

    /**
     * 조회 로직
     * @return
     */
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
