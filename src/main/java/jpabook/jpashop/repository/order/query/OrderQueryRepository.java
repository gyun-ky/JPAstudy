package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    // entity가 아닌 특정 화면에 pick한 쿼리들을 찾을 때에 사용
    // 화면과 관련된 것들은 여기서 하고 핵심 비즈니스 로직 처리는 OrderRepository에서 처리

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(){
        //controller에 있는 OrderDto 를 재사용하지 않는 이유 : repository가 controller를 참조하는 역 의존관계가 생긴다
        List<OrderQueryDto> result = findOrders();

        result.forEach(o ->{
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
   }

   private List<OrderItemQueryDto> findOrderItems(Long orderId){
        // oi.order.id 같은 경우에는 order를 참조하는 것이 아니라 실제로는 외래키로 orderItem 테이블에 있기 때문에 참조하지 않는다
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
   }

   private List<OrderQueryDto> findOrders(){
       return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                       " join o.member m" +
                       " join o.delivery d", OrderQueryDto.class)
               .getResultList();
   }

   //-> 결과적으로 1+N = N+1 문제가 생겼다



   public List<OrderQueryDto> findAllByDto_optimization(){
        List<OrderQueryDto> result = findOrders();

       List<Long> orderIds = toOrderIds(result);

       Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);
       result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                 "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                         " from OrderItem oi" +
                         " join oi.item i" +
                         " where oi.order.id in :orderIds"
                 , OrderItemQueryDto.class).setParameter("orderIds", orderIds).getResultList();
        //쿼리를 한번 날리고
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream().collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        //메모리에서 매칭해주는 방법
        //매칭 성능을 O(1)로 단축
        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream().map(o -> o.getOrderId()).collect(Collectors.toList());
        return orderIds;
    }


    /// fetch join 보다 장점은 필요한 table에 attribute가 많은 경우 필요한 것만 가져올 수 있다.

    public List<OrderFlatDto> findAllByDto_flat(){
        return em.createQuery(
                "select new"+
                        " jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o " +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi"+
                        " join oi.item i", OrderFlatDto.class).getResultList();

    }



}
