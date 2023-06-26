package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.form.BookForm;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {

        model.addAttribute("form", new BookForm());
        log.info("create book");

        return "items/createItemForm.html";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = Book.createBook(form.getName(), form.getPrice(), form.getStockQuantity(), form.getAuthor(), form.getAuthor());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items); //조회한 members를 model에 담아("members"에 담아) html에 전달
        log.info("Item List");

        return "items/itemList.html";
    }

    @GetMapping("/items/{itemId}/edit")  //@PathVariable로 {itemId}와 매핑
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = BookForm.createBookForm(item.getId(), item.getName(), item.getPrice(), item.getStockQuantity(), item.getAuthor(), item.getIsbn());
        model.addAttribute("form", form);

        return "items/updateItemForm.html";
    }

    @PostMapping("/items/{itemId}/edit")  //@PathVariable로 {itemId}와 매핑
    public String updateItem(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form) { // @ModelAttribute: items/updateItemForm.html의 "form"에 대한 데이터를 BookForm form에 대입함
//        Book item = (Book) itemService.findOne(form.getId());
//
//        item.setName(form.getName());
//        item.setPrice(form.getPrice());
//        item.setStockQuantity(form.getStockQuantity());
//        item.setAuthor(form.getAuthor());
//        item.setIsbn(form.getIsbn());
//        itemService.saveItem(item);  //이미 id가 존재하는 경우: merge 처리 되어 있음

        itemService.updateItem(itemId, form);
        return "redirect:/items"; // localhost:8080/items로 재접속
    }
}
