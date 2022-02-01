package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    } // entity 직접 노출시에, entity 수정시 -> api 스펙이 변경됨 / 같은 엔티티에 대해서 여러 용도의 api가 만들어질 때 문제
    // 번외 문제 : array에 count 값을 넣어준다고 할 때, JSON의 기본틀이 깨짐

    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();

        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect); // List를 바로 내보내면 배열 상태로 JSON 변환되기 때문에 유연성을 떨어진다
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }


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

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
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

    @Data
    static class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor // dto 는 크게 logic이 있는 것이 아니라서 막 쓴다
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }
}
