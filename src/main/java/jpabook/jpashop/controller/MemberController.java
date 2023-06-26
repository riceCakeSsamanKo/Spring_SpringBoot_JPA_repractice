package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.form.MemberForm;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    // memberForm을 통해서 "members/new로 부터 회원 가입 데이터를 가져와 Member 객체에 집어넣어
    // 새로운 member 객체를 생성하고, 그 객체를 persist 하여 데이터를 서버에 저장
    private final MemberService memberService;


    // Get: 클라이언트에서 서버로 어떠한 리소스로부터 정보를 요청하는 메서드
    @GetMapping("/members/new") //@RequestMapping(value = "/members/new", method = "RequestMethod.GET)
    public String createForm(Model model) {
        // model 객체: Controller에서 생성한 데이터를 담아서 View로 전달할 때 사용하는 객체.
        model.addAttribute("memberForm", new MemberForm());
        // memberForm이라는 이름으로 MemberForm 객체가 view로 전달됨
        // 전달된 memberForm에 데이터를 담음

        log.info("Create Member");
        return "members/createMemberForm.html";
    }

    // Post: 리소스를 생성/업데이트하기 위해 서버에 데이터를 보내는 데 사용됩니다.
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {  //MemberForm의 @NotEmpty 어노테이션을 사용하기 위해서 @Vaild 어노테이션 붙임
        // GetMapping에서 데이터를 담은 MemberForm 객체를 가져옴
        if (result.hasErrors()) {  // 에러 발생시 (MemberForm의 name이 null인 경우)
            return "members/createMemberForm";
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        // memberForm이 가져온 데이터를 바탕으로 Member member 객체를 만들고 이를 서버에 저장
        memberService.join(member);
        return "redirect:/"; // 메인화면으로 돌아감
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members); //조회한 members를 model에 담아("members"에 담아) html에 전달
        log.info("Member List");
        return "members/memberList.html";
    }
}
