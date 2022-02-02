package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * X To One 관계에서의 성능 최적화
 * Order
 * Order -> Member (M : 1)
 * Order -> Delivery (1 : 1)
 * Order -> OrderItem (1 : N)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());

        for (Order order : all){
            order.getMember().getName();
            // getMember까지는 프록시 객체, getName 하는 순간부터 쿼리가 날라감 - LAZY 강제 초기화
            order.getDelivery().getAddress();
        }
        // 그렇다고 fetch type을 EAGER로 바꾸면
        // em.find()는 JPA가 알아서 성능 최적화된 쿼리를 보내주지만, 직접 만든 메소드 같은 경우 JPQL이 날라감 -> 그대로 SQL로 변환 (Order만 가져오고 가져오고 보니 Member를 안가져와서 단건으로 조회가 나감 N+1문제)
        // -> 다른 API가 필요없다하더라도 가져옴
        return all;
    }
    // [문제1] 무한루프에 빠짐 - member로 가고 order로 가고 member로 가고 ..... @JSONIGNORE로 끊어주어도
    // [문제2] ByteBuddyInterceptor - fetchType LAZY로 db에서 가져오지 않고 member = new ProxyMember() (프록시 객체) (ByteBuddyInterceptor())를 임의로 넣어주는데 그때 쓰는 프록시 기술이 bytebuddy
    //-> Hibernate5 module을 등록해야

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAll(new OrderSearch());
//        List<SimpleOrderDto> result = orders.stream()
//                .map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());
        List<SimpleOrderDto> result =orderRepository.findAll(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        // lambda 를 적용한 경
        return result;
    }

    // ORDER -> SQL 1번 -> 결과 주문수 2개
    // 첫번째 주문에 해당하는 member, delivery 조회 쿼리 나감
    // 두번째 주문에 해당하는 member, delivery 조회 쿼리 나감
    // N + 1 -> 1 + N 한번의 쿼리가 N개의 추가 실행을 불러옴
    // 1 + member N + delivery N

    // 지연 로딩 같은 경우에는 처음에는 쿼리를 날리고 불러온 것이라면 영속성 컨텍스트에 있는 것을 가져다 쓰기 때문에 모두 member1의 주문이라면 1 + 1 + N이 된다

    // order table을 1차로 조회
    // member table을 2차로 조회
    // delivery table을 3차로 조회
    // member table 4차로 조회
    // delivery table을 5차로 조회

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o-> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    @RequiredArgsConstructor
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }

}
