package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // 메모리 모드로 엮는다 - spring과 integration 해줄것
@SpringBootTest //spring container 안에서 test 돌리기 위함
@Transactional //있어야 롤백이됨
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired
    EntityManager em; // insert문 나가는 것 보고 싶으면
    //live template - intelliJ
    @Test
//    @Rollback(value = false) //롤백 안하고 직접 들어간것 확인하고 싶으면 해당 false해주어야
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);
        //테스트이기 때문에 insert query 조차 나가지 않는다 -> 영속성 컨텍스트가 flush 안해버린다

        //then
        em.flush(); // insert문 나가는거 보고 싶고 rollback 도 시킬래!
        assertEquals(member, memberRepository.findById(savedId));
    }
    
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");
        //when
        memberService.join(member1);
//        try{
//            memberService.join(member2);
//        }catch (IllegalStateException e){
//            return;
//        } // 위의 어노테이션으로 커버 가능
        memberService.join(member2);

        //then
        fail("예외가 발생해야 한다");
    }
        
        

}