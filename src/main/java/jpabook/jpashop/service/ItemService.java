package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.Movie;
import jpabook.jpashop.form.AlbumForm;
import jpabook.jpashop.form.BookForm;
import jpabook.jpashop.form.ItemForm;
import jpabook.jpashop.form.MovieForm;
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

    public void updateItem(Long itemId, ItemForm param) {

        // 영속성 엔티티 findItem 조회(변경 감지 지원)
        Item findItem = em.find(Item.class, itemId);

        findItem.setName(param.getName());
        findItem.setPrice(param.getPrice());
        findItem.setStockQuantity(param.getStockQuantity());

        // 트랜잭션 커밋
        if (param instanceof BookForm) {

            ((Book) findItem).setAuthor(((BookForm) param).getAuthor());
            ((Book) findItem).setIsbn(((BookForm) param).getIsbn());
        } else if (param instanceof AlbumForm) {

            ((Album) findItem).setArtist(((AlbumForm) param).getArtist());
            ((Album) findItem).setEtc(((AlbumForm) param).getEtc());
        } else if (param instanceof MovieForm) {

            ((Movie) findItem).setActor(((MovieForm) param).getActor());
            ((Movie) findItem).setDirector(((MovieForm) param).getDirector());
        }
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
