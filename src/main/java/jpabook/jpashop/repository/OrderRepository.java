package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch){

        return em.createQuery("select o from Order o join o.member m "
                + "where o.status = :status "
                + "and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
//                .setFirstResult(100) //paging 하고 싶은 때
                .setMaxResults(1000)
                .getResultList();

    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit){
        return em.createQuery(
                "select o from Order o"+
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).setFirstResult(offset).setMaxResults(limit).getResultList();  // 한방 쿼리로 order랑 member랑 delivery를 join 후에 select 절에 다 넣은 후 땡겨오기/. LAZY 무시
    } // 재사용성이 높다
    // fetch join 최적화 yml에서 설정

    public List<Order> findAllWithItem() {
        return em.createQuery("" +
                "select distinct o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" + // 1:N이므로 불가능해버린다
                " join fetch oi.item i", Order.class).getResultList(); // order가 orderItem과 조인 하면서 2개였던 order가 OrderItem수에 따라 4개가 된다
                // -> JPA에서 data를 가져올 때 row가 너무 많아진다
                // 그래서 JPQL distinct 추가 - 1. db의 distinct 역할 but 이 기능은 한 row가 정확히 똑같아야 함 -> db쿼리에서는 제거가 안됨
                // 2. JPA 영속성 컨텍스트에서 자체적으로 Order가 같은 id 값이면 제거해준다

                // 어마어마한 단점이 하나 있다!!!!! 단점은 페이징이 불가능하다는 것!!!!!! 1:N을 fetch join 하는 순간 안나가아아
                // .setFirstResult(0).setMaxResults(100) 적용이 안됨 limit 100 OFFSET 1이 안됨
                // -> error collection fetch; applying in memory -> memory로 다 가져와서 memory에서 paging 처리를 해버려서 경고가 나옴 out of memory -> cpu가 작살난다
                // 일단 paging도 order에 대한 기준이 fetch join으로 틀어져 버려서 paging 자체도 불가능
                // collection fetch join은 컬렉션 둘 이상에 적용하면 row가 많아지고 (1:N:M) JPA 자체가 누구를 기준으로 가져와야 하는지 혼란스러워져서 정합성이 안맞거나 데이터 개수가 안맞을 수 있다
                // 1을 기준으로 페이징 하는 것인데 N를 기준으로 row가 생성되는 것이 문제다 문제
    }
    // 해결책
    // 1. ToOne 관계는 모두 fetch join 건다
    // 2. 컬렉션은 지연 로딩으로 조회한다
    // 3. hibernate.default_batch_fetch_size , @BatchSize -> 보통 N+1문제가 터지면 하나씩 가져오는데, 설정해준 개수만큼 미리 땡겨옴


    // repository는 순수한 entity를 조회하는 데에 사용

    // / 의존 관계는 controller -> repository 한 방향으로 흘러야

    //동적 쿼리를 위해서는 QueryDSL 사용 - 검색 조건에 따라 SQL이 변화

}
