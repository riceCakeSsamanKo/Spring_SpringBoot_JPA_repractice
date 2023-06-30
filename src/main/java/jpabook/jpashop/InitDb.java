package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

// 프로그램 실행시 DB에 미리 생성해둔 데이터를 저장해주는 클래스

@Component  //컴포넌트 스캔 대상(빈 등록 대상)
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct  // 빈으로 등록될 시 자동으로 실행됨
    public void init(){
        initService.dbInit1();
        initService.dbInit2();
    }


    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{

        private final EntityManager em;
        public void dbInit1() {
            Member member = initMember("userA", "Seoul", "Seoul_street", "11111");
            em.persist(member);

            Book book1 = initBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = initBook("JPA2 BOOK", 10000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 1);


            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {
            Member member = initMember("userB", "Seongnam", "Seongnam_street", "22222");
            em.persist(member);

            Book book1 = initBook("Spring BOOK1", 20000, 200);
            em.persist(book1);

            Book book2 = initBook("Spring BOOK2", 40000, 400);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 2);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 3);


            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private static Book initBook(String name, int price, int stockQuantity) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);
            return book;
        }

        private static Member initMember(String user, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(user);
            member.setAddress(city, street, zipcode);
            return member;
        }
    }
}
