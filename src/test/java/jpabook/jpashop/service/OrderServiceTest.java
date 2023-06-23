package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

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


    @Test
    public void 상품_주문() throws Exception{
        //given
        Member member = new Member();
        member.setName("Jeon");
        memberService.join(member);

        Item book = new Book();
        book.setName("book");
        book.setPrice(5000);
        book.setStockQuantity(10);
        itemService.saveItem(book);

        int count = 5;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), count);
        
        //then


    }

}