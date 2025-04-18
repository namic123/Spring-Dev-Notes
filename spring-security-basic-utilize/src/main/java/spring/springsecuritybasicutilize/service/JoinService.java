package spring.springsecuritybasicutilize.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import spring.springsecuritybasicutilize.dto.JoinDTO;
import spring.springsecuritybasicutilize.entity.UserEntity;
import spring.springsecuritybasicutilize.repository.UserRepository;

@Service
public class JoinService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    public void joinProcess(JoinDTO joinDTO) {


        //db에 이미 동일한 username을 가진 회원이 존재하는지?


        UserEntity data = new UserEntity();

        data.setUsername(joinDTO.getUsername());
        data.setPassword(bCryptPasswordEncoder.encode(joinDTO.getPassword()));
        data.setRole("ROLE_USER");


        userRepository.save(data);
    }
}
