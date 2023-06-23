package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final EntityManager em;
    private final ItemRepository itemRepository;

    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    // 읽기 전용 메서드
    @Transactional(readOnly = true)
    public List<Item> findItems() {
        return itemRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Item findOne(Long itemId) {
        return itemRepository.find(itemId);
    }
}
