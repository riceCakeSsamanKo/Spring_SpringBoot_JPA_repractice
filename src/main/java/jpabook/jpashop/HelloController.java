package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello") // localhost:8080/hello
    public String hello(Model model) {
        model.addAttribute("data", "hello!");
        return "hello";  // 결과로 hello.html이 반환됨.
    }
}
