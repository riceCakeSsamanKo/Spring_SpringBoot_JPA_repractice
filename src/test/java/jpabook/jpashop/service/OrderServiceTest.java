package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;


    @Test
    public void 내가만든_상품_주문() throws Exception{
        //given
        Member member = new Member();
        member.setName("Jeon");
        memberService.join(member);

        Item book = new Book();
        int initialStockQuantity = 10;
        book.setName("book");
        book.setPrice(5000);
        book.setStockQuantity(initialStockQuantity);
        itemService.saveItem(book);

        int orderCount = 5;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        Item orderItem = orderRepository.find(orderId).getOrderItems().get(0).getItem();

        //then
        Assertions.assertThat(book.getStockQuantity()).isEqualTo(initialStockQuantity - orderCount); // 주문시 재고가 줄었는지 확인
        Assertions.assertThat(orderItem).isEqualTo(book); // 주문 상품이 book이 맞는지 확인
    }

    @Autowired
    EntityManager em;
    @Test
    public void 상품_주문() throws Exception{
        //given
        Member member = createMember("회원1", "서울", "강가", "123-123");
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.find(orderId);
        Assert.assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        Assert.assertEquals("주문한 상품 종류 수가 정확해야 한다", 1, getOrder.getOrderItems().size());
        Assert.assertEquals("주문 가격은 가격 * 수량이다", 10000 * orderCount, getOrder.getTotalPrice());
        Assert.assertEquals("주문 수량만큼 재고가 줄어야 한다.",8,book.getStockQuantity());
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(city, street, zipcode);
        em.persist(member);
        return member;
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception{
        //given
        Member member = createMember("회원1", "서울", "강가", "123-123");
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        orderService.order(member.getId(), book.getId(), orderCount);  // NotEnoughStockException 발생

        //then
        fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    @Test
    public void 주문_취소() throws Exception{
        //given
        Member member = createMember("회원1", "서울", "강가", "123-123");
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), 2);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.find(orderId);
        assertEquals("주문 상태가 CANCEL", OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals("상품 재고가 원상복구", 10, book.getStockQuantity());

    }
}