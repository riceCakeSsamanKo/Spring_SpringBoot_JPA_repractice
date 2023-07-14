package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(){
        List<OrderQueryDto> result = findOrders(); // query 1번

        result.stream().forEach(o->{
            //컬렉션인 OrderItem에 대한 orderItemQueryDto는 지연로딩으로 따로 가져온다
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());  // 루프에서 orderItem 조회하는 query 2번
            o.setOrderItems(orderItems);
        });

        return result;
    }

    public List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id = :orderId", OrderItemQueryDto.class)
                        .setParameter("orderId",orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findOrders(){
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id,m.name,o.orderDate,o.status,d.address)" +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {

    }
}
