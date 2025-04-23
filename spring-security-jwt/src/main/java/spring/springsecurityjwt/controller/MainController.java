package spring.springsecurityjwt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;

@RestController
public class MainController {

    @GetMapping("/")
    public String main() {

        // 세션 현재 사용자 아이디 추출 방법
        String name = SecurityContextHolder.getContext().getAuthentication().getName();


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        // 세션 현재 사용자 ROLE 추출 방법
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority();

        return "Main Controller : "+name;
    }
}
