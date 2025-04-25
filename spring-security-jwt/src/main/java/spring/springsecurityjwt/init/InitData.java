package spring.springsecurityjwt.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import spring.springsecurityjwt.entity.UserEntity;
import spring.springsecurityjwt.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class InitData {

    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        userRepository.save(UserEntity.builder().username("admin").role("ADMIN").build());
        userRepository.save(UserEntity.builder().username("user").role("USER").build());
    }
}
