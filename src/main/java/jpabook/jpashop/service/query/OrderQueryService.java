package jpabook.jpashop.service.query;

// Open-Session_In-View off 한 상태에서 지연로딩을 하기 위한 해결방법
// 쿼리용 서비스를 별도로 분리 -

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    // controller 단에 있는 변환 로직을 모두 여기서 돌려버린다 transaction 안에서

    public List<OrderDto> orderV32(){
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o-> new OrderDto(o))
                .collect(Collectors.toList()); // 객체 그래포로 담아야 함 - 이 과정에서 중복되는 order에 대해서는 고려하지 않는다

        return result;
    }




}
