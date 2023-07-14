package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.swing.*;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderRepositoryTest {


    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderService orderService;
    @Autowired
    EntityManager em;

    @Test
    public void 조인_패치_주문_조회() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("member1");
        Member member2 = new Member();
        member2.setName("member2");

        Delivery delivery1 = new Delivery();
        Delivery delivery2 = new Delivery();

        Book book1 = Book.createBook("book1", 10000, 100, "asdf", "1234asdf");
        Book book2 = Book.createBook("book1", 10000, 100, "asdf", "1234asdf");
        Book book3 = Book.createBook("book1", 10000, 100, "asdf", "1234asdf");

        OrderItem orderItem1 = OrderItem.createOrderItem(book1,10000,100);
        OrderItem orderItem2 = OrderItem.createOrderItem(book2,10000,100);
        OrderItem orderItem3 = OrderItem.createOrderItem(book3,10000,100);

        Order order1 = Order.createOrder(member1,delivery1,orderItem1,orderItem2);
        Order order2 = Order.createOrder(member1,delivery2,orderItem3);

        orderRepository.save(order1);
        orderRepository.save(order2);

        //when
        List<Order> joinFetchOrders = orderRepository.findAllWithMemberDelivery(0,0);

        //then
        for (Order order : joinFetchOrders) {
            System.out.println("order.getClass() = " + order.getClass());

            Assert.assertEquals(order.getClass(), Order.class);
        }
    }
}