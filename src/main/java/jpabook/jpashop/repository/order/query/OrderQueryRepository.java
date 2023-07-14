package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // query: 루트 1번, 컬렉션 1번
    // ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId들로 ToMany 관계인 OrderItem을 한꺼번에 조회 (in 사용)
    // 한꺼번에 조회된 OrderItem들은 각자에게 맞는 OrderId를 갖는 order에게 매핑해줌
    // Map을 사용해서 매칭 성능 향상(O(1))
    public List<OrderQueryDto> findAllByDto_optimization() {
        // member와 delivery를 join한 OrderQueryDto 리스트 조회
        List<OrderQueryDto> result = findOrders();  // 1) Order 엔티티 조회 -> 쿼리 1번

        // result의 모든 원소의 orderId로 이루어진 List orderIds 생성
        List<Long> orderIds = toOrderIds(result);

        // orderId 값을 key로 가지고, 값이 해당 orderId 값을 OrderItemQueryDto.orderID로 가지는 List<OrderItemQueryDto>
        // Map 생성
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        // orderItemMap에서 result의 각 order와 orderId가 일치하는 List<OrderItemQueryDto>를 조회해서
        // order의 orderItems로 set 해줌.
        result.stream().forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        // orderIds 내부의 값에 포함된 order.id를 가지는 OrderItem 엔티티들을 조회해서 OrderQueryDto로 변환 후 리스트의 형태로 반환
        List<OrderItemQueryDto> orderItems = em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id,oi.item.name,oi.orderPrice,oi.count)" +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();  // 2) OrderItem 엔티티 조회 -> 쿼리 1번

        // orderItems를 orderItem.orderId를 기준으로 Map으로 변환.
        // key: orderItemQueryDto.getOrderId(), value: List<OrderItemQueryDto>
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds =
                result.stream().map(o -> o.getOrderId()).collect(Collectors.toList());
        return orderIds;
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


}
