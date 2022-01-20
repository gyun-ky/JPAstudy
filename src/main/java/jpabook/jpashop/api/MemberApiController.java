package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){  // javax validation 관련되어있는 것이 알아서 valid
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 검증 로직이 entity에 모두 들어가있으면 어떤 entity는 안필요한 경우 곤란
    // api 스펙 자체(필드 이름)이 바뀌면 서비스 로직이 바뀌면 안된다 -> DTO를 만들어야 함
    // api 요청 스펙에 맞추어서 별도의 Data Transfer Object를 만들어야함
    // 엔티티를 웹에 노출해서도 안된다

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 누군가 필드 이름을 바꾸면 controller에서만 setUsername으로 바꾸어주면 된다.
    // request에서 넘어온 것이 무엇인지 명확하게 정리할 수 있다
    // -> entity와 presentation 계층을 분리 가능
    // -> entity와 api스펙을 명확하게 분리 가능
    // ----> entity가 변경되어도 api스펙에 영향을 주지 않는다
    @Data
    static class CreateMemberRequest{
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
