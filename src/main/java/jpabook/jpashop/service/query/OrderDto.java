package jpabook.jpashop.service.query;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter // property가 없다는 오류면 대부분 Getter Setter
public class OrderDto{

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    //        private List<OrderItem> orderItems; // DTO 안에 Entity가 있어서도 안된다. 외부에 스펙이 노출이 된다
    private List<OrderItemDto> orderItems;

    public OrderDto(Order o){
        orderId = o.getId();
        name = o.getMember().getName();
        orderDate = o.getOrderDate();
        orderStatus = o.getStatus();
        address = o.getDelivery().getAddress();
//            o.getOrderItems().stream().forEach(o->o.getItem().getName()); // 프록시 초기화
//            orderItems = o.getOrderItems();
        orderItems = o.getOrderItems().stream()
                .map(orderItem->new OrderItemDto(orderItem))
                .collect(Collectors.toList());
    }
}
