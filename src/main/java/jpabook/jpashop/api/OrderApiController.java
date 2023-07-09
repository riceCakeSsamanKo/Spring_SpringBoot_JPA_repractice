package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제
 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 *
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경
 가능)
 *
 * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 * - 페이징 가능
 * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능...
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Result<Order>> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // LAZY 로딩 강제 초기화
        for (Order order : all) {
            order.getMember().getName();  // Member LAZY 초기화
            order.getDelivery().getAddress();  // Delivery LAZY 초기화

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());  // OrderItem, Item 초기화
        }
        List<Result<Order>> results =
                all.stream().map(o -> new Result<Order>(o)).collect(Collectors.toList());

        return results;
    }
    @Data
    @AllArgsConstructor
    private static class Result<T>{
        T Order;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> all =
                orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect =
                all.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> all = orderRepository.findAllWithItem();
        for (Order order : all) {
            System.out.println("order = " + order + "orderId = "+order.getId());
        }
        List<OrderDto> collect =
                all.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());

        return collect;
    }
    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // 엔티티를 Dto로 감쌈

        /*
        private List<OrderItem> orderItems; : Dto 내부에 엔티티가 존재한다.
        => 엔티티에 의존하므로 orderItems도 별도의 Dto를 만들어서 반환해야 한다.
        */

        OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            // OrderItem 엔티티를 그대로 반환하는 것이 아닌 별도의 Dto에 담아서 반환함으로써
            // Dto가 내부적으로 엔티티에 의존하는 것을 방지할 수 있다.
            orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderItemDto(o))
                    .collect(Collectors.toList());
        }

        @Getter
        static class OrderItemDto {
            private String itemName;
            private int orderPrice;
            private int count;

            OrderItemDto(OrderItem orderItem){
                this.itemName = orderItem.getItem().getName();
                this.orderPrice = orderItem.getOrderPrice();
                this.count = orderItem.getCount();
            }
        }
    }
}
