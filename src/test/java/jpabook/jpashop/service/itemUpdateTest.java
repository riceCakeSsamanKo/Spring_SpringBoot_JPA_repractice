package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class itemUpdateTest {
    @Autowired
    EntityManager em;
    
    @Test
    public void updateTest () throws Exception{
        Book book = em.find(Book.class, 1L);

        // TX
        book.setName("asdf"); // dirty checking


    }
}
