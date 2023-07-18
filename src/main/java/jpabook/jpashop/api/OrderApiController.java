package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

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
    private final OrderQueryRepository orderQueryRepository;

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
            orderItems.stream().forEach(o -> o.getItem().getName());  // OrderItem, Item 초기화(1+N 문제 발생)
        }
        List<Result<Order>> results =
                all.stream().map(o -> new Result<Order>(o)).collect(toList());

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
                all.stream().map(o -> new OrderDto(o)).collect(toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> all = orderRepository.findAllWithItem();
        for (Order order : all) {
            System.out.println("order = " + order + "orderId = "+order.getId());
        }
        List<OrderDto> collect =
                all.stream().map(o -> new OrderDto(o)).collect(toList());

        return collect;
    }
 
    @GetMapping("/api/v3.1/orders")
    /** @RequestParnam
     *
     * @RequestParam(value = "offset", defaultValue = "0") int offset 설명
     * : "localhost:8080/api/v3.1/orders?offset=value" 도메인의 offset 값을 가져와서 int offset에 넘김.
     * 이때 offset의 값이 없는 경우 에러가 발생하므로 defaultValue를 지정한다.
     * 이 경우는 offset값이 넘어오지 않는 경우 0 int offset에 대입.
     */
    public List<OrderDto> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "100") int limit) {
        // 컬렉션이 아닌 엔티티만 페치 조인으로 가져오고, 컬렉션은 페치조인으로 가져오지 않고, 대신 지연로딩으로 가져와 페이징이 가능
        List<Order> orders =
                orderRepository.findAllWithMemberDelivery(offset,limit);// ToOne 관계인 엔티티(member, delivery)만 페치 조인으로 조회 해오는 메서드
        // 지연로딩 시 1(Order) + N(OrderItem) + N(Item) 문제 발생.
        List<OrderDto> result =
                orders.stream().map(o -> new OrderDto(o)).collect(toList());
        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    /**
     * 장점: Query: 1번
     * 단점: 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로
     * 상황에 따라 V5 보다 더 느릴 수 도 있다.
     * 애플리케이션에서 추가 작업이 크다.
     * 페이징 불가능
     */
    public List<OrderQueryDto> orderV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream() // OrderFlatDto -> OrderQueryDto
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());

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
                    .collect(toList());  // 루프 돌면서 지연 로딩
        }

        @Getter
        static class OrderItemDto {
            private String itemName;
            private int orderPrice;
            private int count;

            private OrderItemDto(OrderItem orderItem) {
                this.itemName = orderItem.getItem().getName();
                this.orderPrice = orderItem.getOrderPrice();
                this.count = orderItem.getCount();
            }
        }
    }
}
