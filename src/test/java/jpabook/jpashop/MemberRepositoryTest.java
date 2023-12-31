package jpabook.jpashop;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.MemberRepositoryOld;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

// 실제 테스트가 아닌 테스트 연습을 위해 만든 테스트 케이스
@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {
    @Autowired
    EntityManager em;
    @Autowired
    MemberRepositoryOld memberRepository;

    @Test
    @Transactional  // @Transactional 어노테이션이 테스트 케이스에 존재하는 경우 테이스 종료후 롤백이 디폴트
    @Rollback(value = false)
    public void testMember() throws Exception {

        //given
        Member member = new Member();
        member.setName("memberA");
        em.flush();
        em.clear();
        //when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());
        Assertions.assertThat(findMember).isEqualTo(member); // 영속성 컨텍스트에서 조회한 경우 동일한 객체
    }
}