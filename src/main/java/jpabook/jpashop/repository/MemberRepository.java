package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // select m from Member m where m.name 을 알아서 jpql로 짜준다
    // 기본적인 CRUD 기능이 모두 제공됨
    List<Member> findByName(String name);

}
