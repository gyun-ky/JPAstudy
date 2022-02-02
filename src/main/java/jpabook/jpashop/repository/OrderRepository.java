package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderSearch;
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

    public List<Order> findAllWithMemberDelivery(){
        return em.createQuery(
                "select o from Order o"+
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();  // 한방 쿼리로 order랑 member랑 delivery를 join 후에 select 절에 다 넣은 후 땡겨오기/. LAZY 무시
    } // 재사용성이 높다


    // repository는 순수한 entity를 조회하는 데에 사용

    // / 의존 관계는 controller -> repository 한 방향으로 흘러야

    //동적 쿼리를 위해서는 QueryDSL 사용 - 검색 조건에 따라 SQL이 변화

}
