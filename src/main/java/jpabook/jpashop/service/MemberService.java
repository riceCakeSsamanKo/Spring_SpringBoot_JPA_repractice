package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//트랜잭션 상황에서는 꼭 @Transactional 어노테이션 필요
//클래스 범위에서 @Transactional 어노테이션이 있으면 메서드들도 모두 트랜잭션 안에서 작동
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 가입 방지
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    //회원 전체 조회
    @Transactional(readOnly = true)
    // 조회 같이 데이터 변경이 되지 않는곳에서 readOnly = true를 하면 성능이 좀더 좋음.
    // 만일 데이터 변경이 되는 곳에서 readOnly = true하면 데이터 변경이 안되서 망함.
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    //회원 단건 조회
    @Transactional(readOnly = true)
    public Member findOne(Long memberId) {
        return memberRepository.find(memberId);
    }
}
