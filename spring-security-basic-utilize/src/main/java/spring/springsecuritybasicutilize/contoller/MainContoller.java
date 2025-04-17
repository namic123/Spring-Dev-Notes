package spring.springsecuritybasicutilize.contoller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainContoller {

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }
}
