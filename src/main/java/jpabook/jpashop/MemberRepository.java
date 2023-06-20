package jpabook.jpashop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository //Member 엔티티를 관리하는 다양한 메서드 구현(저장, 조회 등등...)
public class MemberRepository {

    @PersistenceContext
    // EntityManger 생성 어노테이션!
    // EntityMangerFactory 만들고 주입하는 대신 @PersistenceContext 하나면 EntityManger를 만들어줌.
    // 단 사용하기 위해선 build.gradle에 dependencies로
    // "implementation 'org.springframework.boot:spring-boot-starter-data-jpa'" 입력 해줘야함
    private EntityManager em;

    // persist 함수 (id를 반환한다)
    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }

    // Member 엔티티 조회 후 반환 함수
    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
