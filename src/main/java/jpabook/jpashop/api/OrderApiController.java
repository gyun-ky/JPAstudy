package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    // N:1 이나 1:1은 fetch join 하거나 DTO 로 불러와도 성능적으로 문제 없으나
    // N을 가져오는 것은 카티션 프로덕트가 되어서 row 개수가 늘어나서 성능 최적화하기 어렵다
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllWithMemberDelivery(1,100);
        for (Order order : all) {
            order.getMember().getName();
            order.getMember().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName()); //강제 초기화 - Hibernate5 모듈을 통해서 proxy를 넣어놓았기 때문에
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery(1, 100);
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o-> new OrderDto(o))
                .collect(Collectors.toList()); // 객체 그래포로 담아야 함 - 이 과정에서 중복되는 order에 대해서는 고려하지 않는다

        return result; // DB 입장에서 조인을 하면서 같은 order가 중복되어서 나타나게 된다
        // OrderItem 이 많으면 뻥튀기가 되어버린다
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value="offset", defaultValue = "0") int offset,
                                        @RequestParam(value="limit", defaultValue = "100") int limit){
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // Member와 Delivery fetch join
        // 정규화된 상태로 3개의 쿼리로 전송되어서 받아온다
        // 네트워크 호출하는 횟수와 전송하는 데이터 양 사이에서의 trade off가 있음 -> 데이터가 많으면 fetch join으로 한번에 가져오는 것보다 위와 같이 나눠서 가져오는 것이 좋다

        // 1. yml에 global하게 default_batch_fetch_size 적용
        // 2. entity collection에 @BatchSize(size=) 적용
        List<OrderDto> result = orders.stream()
                .map(o-> new OrderDto(o))
                .collect(Collectors.toList()); // 객체 그래포로 담아야 함 - 이 과정에서 중복되는 order에 대해서는 고려하지 않는다

        return result; // DB 입장에서 조인을 하면서 같은 order가 중복되어서 나타나게 된다
        // OrderItem 이 많으면 뻥튀기가 되어버린다
    }

    @Getter // property가 없다는 오류면 대부분 Getter Setter
    static class OrderDto{

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

    @Getter
    static class OrderItemDto{

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem){
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }





}
