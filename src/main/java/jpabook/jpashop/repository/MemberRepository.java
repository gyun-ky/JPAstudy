package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Repository // component annotation으로 인해 spring의 component 스캔의 대상이 되어서
public class MemberRepository {

    @PersistenceContext
    private EntityManager em; // spring이 entity manager를 생성하여서 주입해준다

    //@PersistenceUnit
    //private EntityManagerFactory emf;
    // 직접 주입 받을 수도 있음

    public void save(Member member){
        em.persist(member);
    }

    public Member findById(Long id){
        return em.find(Member.class, id);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class).getResultList();
        // entity 객체를 대상으로 조회
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class).setParameter("name", name).getResultList();
    }

}
