package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos(){
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o"+
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    } // 재사용성이 낮 - dto로 조회한 것이기 때문에 변경이 불가 - 성능 개선이 생각보다 미비하다
    // 대부분 성능을 먹어버리는 것은 join 혹은 indexing
    // 리포지토리 재사용성이 떨어짐 api 스펙에 맞춰서 repository가
    // repository는 entity에 대한 개체그래프를 조회하는데에 사용해야 하는데 repository에 api 스펙이 들어오면 안된다
    // 성능 최적화된 쿼리용 패키지를 repository 하위 패키지로 뽑는다 order.simplequery
}
