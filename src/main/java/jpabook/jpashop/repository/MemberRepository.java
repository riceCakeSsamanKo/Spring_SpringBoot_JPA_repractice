package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//스프링 데이터 JPA는 JpaRepository 라는 인터페이스를 제공하는데, 여기에 기본적인 CRUD 기능이 모두
//제공된다. (일반적으로 상상할 수 있는 모든 기능이 다 포함되어 있다.)
//findByName 처럼 일반화 하기 어려운 기능도 메서드 이름으로 정확한 JPQL 쿼리를 실행한다.
//select m from Member m where m.name = :name
//개발자는 인터페이스만 만들면 된다. 구현체는 스프링 데이터 JPA가 애플리케이션 실행시점에 주입해준다.


// JpaRepository: findById, findAll, save, saveAll... 등등 Repository 기능이 이미 어느정도 구현되어 있다
public interface MemberRepository extends JpaRepository<Member, Long> {
    // ... extends JpaRepository<엔티티,Id의 자료형(= Long)>

    // findByName과 같이 JpaRepository에는 없는 기능들은 따로 선언을 해야함.
    // 근데 findByxxx 라고 메서드를 선언만 하면 자동으로 "select m from Member m where m.xxx = :xxx"
    // 와 같은 쿼리를 생성해서 메서드를 구현할 필요가 없음

    List<Member> findByName(String name); // 몸체부를 구현하지 않아도 SpringDataJpa가 자동으로 구현해줌

}
