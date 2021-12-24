package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@Transactional(readOnly = true)
//@RequiredArgsConstructor  // final 에 해당하는 필드에 대한 contructor를 만들어준다 -> 최신버전에서는 어노테이션 안써도 인젝션됨
public class MemberService {

//    @Autowired // spring bean에 등록되어있는 memberRepository를 인젝션해준다 -> 이 방식 사용시 중간에 바꾸기 repository 바꾸기 어려움
    private final MemberRepository memberRepository;
    //final을 하면 컴파일 지점에서 체크 가능

    //setter injection -> 테스트 코드 작성시 mock 같은 것을 직접 주입 가능
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }

    //constructor injection -> 어플리케이션이 동작중에 바꾸는 것이 적기 때문에 스프링이 뜰때 인젝션해주는 것이 좋음
    @Autowired
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    /**
     * 회원가입
     */
    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member); // 중복회원검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member){
        List<Member> findMembers = memberRepository.findByName(member.getName()); // 멀티스레드를 고려하여 사실 db에 유니크 제약조건 걸자
        if(findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
//    @Transactional(readOnly = true) // JPA가 조회하는 것에서는 성능을 최적화 readOnly true - 영속성 컨텍스트 flush 안하고 dirty check 안함, 읽기 전용이니 리소스 많이 쓰지 말고 db야 읽어라
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

//    @Transactional(readOnly = true)
    public Member findOne(Long memberId){
        return memberRepository.findById(memberId);
    }
}
