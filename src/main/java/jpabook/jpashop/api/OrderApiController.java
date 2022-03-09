package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    // N:1 이나 1:1은 fetch join 하거나 DTO 로 불러와도 성능적으로 문제 없으나
    // N을 가져오는 것은 카티션 프로덕트가 되어서 row 개수가 늘어나서 성능 최적화하기 어렵다
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

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

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        //장점 : 한방 쿼리로 해결 가능
        //단점 : 카티션 프로덕트로 인해서 중복 발생 -> paging 기준이 order가 아닌 orderItems이므로 기준 애매해져서 paging 불가

        // 중복되는 row들을 직접 발라 주어야 한다
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());

        // 쿼리가 한번이지만 결국 DB에서 중복데이터가 넘어오는 것이기 때문에 데이터가 많은 경우 v5보다 느릴 수 있다
        // 에플리케이션에서 중복 제거하는 추가 작업이 크다
        // 페이징이 불가능

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


    // 정리
    // To One은 페이조인으로 쿼리수 최적화 가능
    // To Many 는 페치조인 대신 지연 로딩 유지
    // -> 지연 로딩 하는 시점에 가져오는 size를 조절 but hibernate.default_batch_fetch_size / @BatchSize 사용하게 되면
    // --> 한번에 100개 혹은 1000개씩 땡겨와서 대부분 한번에 조회 가능

    // 정리2
    // To Many는 컬렉션에서는 IN 절 사용하여 미리 조회해서 최적화
    // To Many는 플랫데이터 최적화 -> DB에서 메모리에 올린 후에 application에서 발라냄

    // 권장 순서
    // 엔티티로 조회후 -> DTO로 변환하자 - 코드를 수정하지 않고 옵션만 변경하여 다양한 성능 최적화 시도 가능
    // 1. 페치조인 쿼리수 최적화
    // 2. 컬렉션 최적화
    //// 페이징 필요시(data의 수가 엄청 많다면) > fetch size 설정
    //// 페이징 필요 x (data 수가 작다면) > 페치조인 사용

    // DTO 조회 방식으로 사용  -->>여기 까지도 왔는데 성능 최적화가 안된다??? 그건 cache를 써야지 (redis)

   // DTO도 안된다면 NativeSQL or 스프링 jdbc Template




    // entity르 cache 관리 하면 안됨??
    //// entity는 영속성 컨텍스트에 의해 관리되고 상태가 있기 때문에 cache에 잘못 올라가면 굉장히 곤란해짐
    //// 영속성 컨텍스트가 관리하고 있는데 cache에 있으면 안지워지니까 꼬인다 -> redis 등의 cache에 넣을 때는 DTO를 cache 해야함
    //// hibernate 2차 캐시가 있기는 하지만 이는 굉장히 실무적으로 적용하기 어려움
    /// redis 혹은 local 의 메모리에 cache

    //

}
