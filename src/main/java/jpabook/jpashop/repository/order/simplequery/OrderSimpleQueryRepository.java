package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {  //API 스펙에 맞춘 코드가 리포지토리에 들어간다는 단점 존재
        //jpa로 엔티티를 전달할 수는 없고, 식별자만 반환할 수 있기 때문에 Order 자체를 넘길 수는 없고 order의 필드값을 각각 따로 넘겨줘야한다.
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name,o.orderDate,o.status,d.address) " +
                                "from Order o " +
                                "join o.member m " +
                                "join o.delivery d",
                        OrderSimpleQueryDto.class)
                .getResultList();
    }
}
