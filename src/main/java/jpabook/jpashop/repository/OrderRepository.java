package jpabook.jpashop.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }
    public void save(Order order) {
        em.persist(order);
    }

    public Order find(Long orderId) {
        return em.find(Order.class, orderId);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /*public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o join o.member m" +
                        " where o.status = :status" +
                        " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name",orderSearch.getMemberName())
                .getResultList();
    }*/

    public List<Order> findAll(OrderSearch orderSearch) {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return null;
        }
        return QMember.member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o " +
                "join fetch o.member m " +
                "join fetch o.delivery d", Order.class)
                .getResultList();
    }
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        // join fetch: member, delivery가 Lazy로 fetchType이 설정되어 있는데,
        // join fetch 하면 프록시 무시하고 실제 엔티티를 다 넣어서 가져온다.
        // 즉, 지연로딩도 즉시로딩으로 가져온다.

        // 여기서 주의해야 할 점은 fetch join으로 가져오는 경우 Order와 연관된 Member, Delivery가 모두
        // join된 테이블을 가져오게 되는 것이다. 따라서 동일한 엔티티가 여러번 조회되는 중복 조회도 가능하다.
        List<Order> result = em.createQuery("select o from Order o " +
                "join fetch o.member m " +
                "join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return result;
    }


    public List<Order> findAllWithItem() {
        // orderItems의 item까지 join fetch로 한번의 쿼리만으로 다 끌고 옴.
        // distinct를 통해 Order(one)가 OrderItems(many)에 의해 데이터 뻥튀기 되는 것을 방지
        // distinct: 동일한 엔티티가 조회시 중복 엔티티를 버림. (== id 값이 동일한 엔티티가 존재시 중복 제거)
        // 이 경우 동일한 Order가 두 개씩 총 4개가 조회되는데
        // distinct에 의해서 중복 조회된 엔티티들이 제거되어 두 개만 조회됨.

        // distinct 없을 시: order(id=4), order(id=4), order(id=11), order(id=11)
        // distinct 있을 시: order(id=4), order(id=11) 중복 제거

        List<Order> result = em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.item i", Order.class)
                .getResultList();

        return result;
    }
}


// --컬렉션 페치 조인의 문제점, 페이징과 한계 돌파-- //
// 문제점:
// 특정 엔티티에 컬렉션 엔티티를 조인하여 조회하게 되면 해당 엔티티의 row 수가 컬렉션에 의해서 뻥튀기 되는 문제 발생 -> row수가 복사되어 페이징이 불가능.

// 문제 해결:
// xxxToOne 관계인 엔티티는 페치 조인으로 가져오고, 컬렉션은 지연로딩으로 가져온다
// 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size , @BatchSize 를 적용