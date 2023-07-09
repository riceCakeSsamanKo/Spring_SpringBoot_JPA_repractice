package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
// @RestController = @Controller + @ResponseBody
// @ResponseBody: 자바객체를 json, xml로 변환해서 보낼 때 사용
@RequiredArgsConstructor
public class MemberApiController {


    private final MemberService memberService;
    /**
     * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의
     API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성
     으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     */
    // 조회 V1: 안 좋은 버전, 모든 엔티티가 노출

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        // 이 함수로 orders에 관한 내용없이 Member에 관한 내용만 반환하기 위해선
        // Member.orders에 @JsonIgnore를 붙여서 orders가 Json으로
        // 전달되지 못하게 강제하는 방법밖에 없다.
        // @JsonIgnore -> 모든 API에 영향을 주기 때문에 절대 사용해서는 안된다!!!
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();

        List<MemberDTO> collect = findMembers.stream()
                .map(m -> new MemberDTO(m.getName()))
                .collect(Collectors.toList());

        return new Result<>(collect);
    }
    @Data
    @AllArgsConstructor
    private class Result<T> {

        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDTO{
        private String name;
    }

    @PostMapping("/api/v1/members")
    // @Valid: member를 검증할 때 사용, @RequestBody: json으로 온 내용을 member에 매핑
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        // api 통신에 이런식으로 엔티티를 파라미터로 직접 받는 경우
        // 엔티티의 필드가 바뀌는 경우 더 이상 통신이 불가능한 문제등이 발생 가능.
        // 엔티티의 변화가 api 스펙에 영향을 주는 식으로 설계하면 안된다.
        // 이는 잘못된 설계임.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
     * - 엔티티를 DTO로 변환해서 반환한다.
     * - 엔티티가 변해도 API 스펙이 변경되지 않는다.
     * - 추가로 Result 클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가할 수 있다
     */

    @PostMapping("/api/v2/members")  // @RequestBody: 이 주소로 json, xml 데이터가 전달되면 이를 자바객체로 변환해서 매핑해줌
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        //@Valid 어노테이션 안 붙이면 에러메시지 안뜸
        Member member = new Member();
        member.setName(request.getName());
        member.setAddress(request.getCity(),request.getStreet(),request.getZipcode());
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    // PUT은 "전체 업데이트"를 할 때 사용하는 것이 맞다. 부분 업데이트를 하려면 PATCH를 사용하거나
    // POST를 사용하는 것이 REST 스타일에 맞다.
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class CreateMemberRequest {
        // 엔티티의 변화에도 대응할 수 있도록 값을 받는

        // Request 파라미터를 별도로 생성
        // 클라이언트 요청이 여기로 들어옴
        @NotEmpty(message = "이름은 필수 값 입니다")
        private String name;

        // Address
        private String city;
        private String street;
        private String zipcode;

        private Address address = new Address(city, street, zipcode);
    }
    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        // 클라이언트에게 반환 값. Json의 형태로 클라이언트에게 반환됨
        private Long id;
    }
    @Data
    static class UpdateMemberRequest {
        // update 내용으로 id와 name을 받음
        @NotEmpty(message = "이름은 필수 값 입니다")
        private String name;
    }
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        // 클라이언트에게 반환
        private Long id;
        private String name;

    }
}
