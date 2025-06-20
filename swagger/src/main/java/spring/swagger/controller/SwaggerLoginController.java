package spring.swagger.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class SwaggerLoginController {

    @GetMapping("/swagger-ui/login")
    public String login() {
        return "swagger/swagger-login";
    }

}
