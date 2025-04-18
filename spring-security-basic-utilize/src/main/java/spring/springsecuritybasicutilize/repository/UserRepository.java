package spring.springsecuritybasicutilize.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.springsecuritybasicutilize.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

}