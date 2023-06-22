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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    @Rollback
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("엄준식");

        //when
        memberService.join(member);
        //then

        Member findMember = memberService.findOne(member.getId());
        assertEquals(member,findMember);
    }

    @Test(expected = IllegalStateException.class)  //IllegalStateException이 반환되길 기대함
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("엄준식");

        Member member2 = new Member();
        member2.setName("엄준식");

        //when
        memberService.join(member1);
        memberService.join(member2); // 중복 회원: IllegalStateException 발생

        //then
        fail("예외가 발생해야 한다"); //만일 여기 도달하면 테스트가 실패한 것
    }
}