package jpabook.jpashop.form;

import jpabook.jpashop.domain.item.Book;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookForm extends ItemForm{

    private String author;
    private String isbn;

    public static BookForm createBookForm(Long id, String name, int price, int stockQuantity, String author, String isbn) {

        BookForm form = new BookForm();

        form.setId(id);
        form.setName(name);
        form.setPrice(price);
        form.setStockQuantity(stockQuantity);
        form.setAuthor(author);
        form.setIsbn(isbn);

        return form;
    }
}
