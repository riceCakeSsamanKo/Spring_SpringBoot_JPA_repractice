package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order ->Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * 주의: 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭!
     * 한곳을 @JsonIgnore 처리 해야 한다.
     * 안그러면 양쪽을 서로 호출하면서 무한 루프가 걸린다.
     */
    @GetMapping("api/v1/simple-orders")
    public List<Order> ordersV1() {  // 엔티티를 직접 노
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();  // Lazy 강제 초기화. Proxy 객체를 사용해서 정보 땡겨
            order.getDelivery().getAddress(); //Proxy: Lazy 강제 초기화
        }
        return all;
        // Order에는 member가 있고 Member에는 orders가 있다.
        // 따라서 Order를 조회해 오면 종속된 member를 가져오고, 또 해당 member 내부에 있는 orders를 또 가져오고 orders의 각 order에 있는 member를 또 가져오고.....
        // 이런식으로 무한 루프가 발생하게 된다.

        // Member.orders에 @JsonIgnore로 해서 가져온다고 해도, Order.member가 FetchType.LAZY이므로
        // 가져온 객체의 타입이 불분명해서 또 에러가 발생한다.
        // (Type definition error: [simple type, class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor];)
    }

    @GetMapping("api/v2/simple-orders")
    public List<SimpleOrderDTO> ordersV2() {
        // N+1 문제
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDTO> result
                = orders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    private class SimpleOrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        SimpleOrderDTO(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // LAZY 초기화
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();  // LAZY 초기화
        }
    }
}
