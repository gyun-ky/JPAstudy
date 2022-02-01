package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model){
        log.info("createForm controller");
        model.addAttribute("memberForm", new MemberForm()); // Model을 통해 html로 넘겨서 앞에서 객체에 접근할 수 있도록 함
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result){ // valid후에 오류가 있는 경우 오류가 result에 실림
        //NotEmty 어노테이션을 이용하여 valid적용 -> javax.validation

        if(result.hasErrors()){
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; // 첫번째 페이지로 넘어감
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members); // member entity를 그래도 뿌리는 것 보다는 dto를 만들어서 뿌리는 것이 좋음
        return "members/memberList"; // api를 만들 때에는 이유를 불문하고 dto를 만들어서 반환해야 -> entity에 로직을 추가하면 api의 스펙이 변경되기 떄문
    }

}
