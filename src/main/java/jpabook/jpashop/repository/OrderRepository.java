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

    //동적 쿼리를 위해서는 QueryDSL 사용 - 검색 조건에 따라 SQL이 변화

}
