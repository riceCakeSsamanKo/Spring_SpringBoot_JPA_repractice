package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j // logger 생성: Logger log = (Logger) LoggerFactory.getLogger(getClass());

public class HomeController {

    @RequestMapping("/")
    public String home(){
        log.info("Home Page");
        return "home";
    }
}
