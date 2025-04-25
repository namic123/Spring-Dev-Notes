## Spring Security 기반 JWT(JSON Web Token) 개념 및 구현 (2) - 프로젝트 환경 구성 및 기본 도메인 예제

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EA%B8%B0%EB%B0%98-JWTJSON-Web-Token-%EA%B0%9C%EB%85%90-%EB%B0%8F-%EA%B5%AC%ED%98%84-2-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%ED%99%98%EA%B2%BD-%EA%B5%AC%EC%84%B1-%EB%B0%8F-%EA%B8%B0%EB%B3%B8-%EB%8F%84%EB%A9%94%EC%9D%B8-%EC%98%88%EC%A0%9C

## 1\. 핵심 의존성 구성

```
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    runtimeOnly 'com.h2database:h2'

    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.3'
}
```

-   spring-boot-starter-security는 인증 및 인가를 위한 필터 체인과 설정을 자동 구성한다.
-   jjwt는 JWT 생성, 서명, 파싱 등의 기능을 담당한다.
-   h2database는 인메모리 테스트 환경에서 유용하며, 빠른 개발 및 테스트에 적합하다.

**JWT 의존성**

[https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api/0.12.3](https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api/0.12.3)

```
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/test
    username: sa
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        show_sql: true
        format_sql: true

  jwt:
    secret: [생략된 비밀키]
```

위 yml 설정에서 jwt secret은 Spring Boot 애플리케이션에서 JWT 서명(Signature)에 사용할 비밀 키(secret key)를 지정하는 설정이다. 

**JPA 및 H2 Database 관련 설정은 아래 글을 참고**

[2025.04.18 - \[Backend/JPA\] - 테스트를 위한 H2 Database + Spring Data JPA 구성 방법](https://pjs-world.tistory.com/entry/%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%A5%BC-%EC%9C%84%ED%95%9C-H2-Database-Spring-Data-JPA-%EA%B5%AC%EC%84%B1-%EB%B0%A9%EB%B2%95)

**JWT 구조**

-   **Header**: 알고리즘, 타입
-   **Payload**: 클레임 정보 (username, role 등)
-   **Signature**: 위변조 방지를 위한 서명

서명을 생성할 때 사용되는 것이 바로 **secret key**다. 예를 들어 **HMAC-SHA256** 알고리즘을 사용할 경우, 다음과 같은 방식으로 서명이 생성된다.

```
Signature = HMACSHA256(
    base64UrlEncode(header) + "." + base64UrlEncode(payload),
    secret
)
```

즉, 이 secret이 없으면 서버는 클라이언트가 보낸 JWT의 무결성(위변조 여부)을 확인할 수 없다.

**Entity 설계**

**UserEntity**

```
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role;
}
```

**RefreshEntity**

```
@Entity
@Getter @Setter
public class RefreshEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String refresh;
    private String expiration;
}
```

UserEntity는 인증 대상 사용자의 정보를 담으며, RefreshEntity는 Refresh Token을 저장해서 사용자의 Refresh Token을 검증한다.

**Repository 구성**

**UserRepository**

```
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByUsername(String username);	// User 존재 여부 
    UserEntity findByUsername(String username);	// User Entity 조회
}
```

**RefreshRepository**

```
public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Boolean existsByRefresh(String refresh);  // Refresh 존재 여부
    @Transactional
    void deleteByRefresh(String refresh);	// Refresh 삭제
}
```

**초기 데이터 삽입**

```
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
```

해당 코드는 애플리케이션 실행 시점(@Component 애너테이션에 의해 자동으로 빈 등록, @PostConstruct는 Bean 초기화 직후 실행되는 메서드)에 테스트용 사용자를 DB에 자동으로 저장하는 초기화 클래스다. 개발 또는 테스트 환경에서 **로그인 기능**을 구현하거나 **권한 테스트**를 하기 위해 자주 사용하는 패턴이다.

## 3\. 사용자 인증 구현 – UserDetails 및 UserDetailsService

**UserDetails란?**

**UserDetails**는 Spring Security에서 사용자의 **인증 정보를 담는 객체**를 의미한다.

-   사용자의 **아이디, 비밀번호, 권한(역할)** 등의 정보를 포함한다.
-   Spring Security 내부에서 인증이 완료된 사용자를 표현할 때 이 인터페이스를 사용한다.

쉽게 말해, **로그인한 사용자 한 명의 정보**를 담는 표준 틀이다.

**UserDetailsService란?**

**UserDetailsService**는 사용자 이름(보통 아이디)을 받아서 UserDetails 객체를 반환하는 인터페이스다.

-   로그인 요청 시, 사용자가 입력한 username으로 DB에서 사용자 정보를 조회하고, 이를 UserDetails로 감싸 반환한다.
-   반환된 UserDetails 객체는 내부적으로 AuthenticationManager에 의해 비밀번호 확인 및 인증 처리에 사용된다.

쉽게 말해, **사용자 정보를 데이터베이스에서 불러오는 서비스**다.

**요약**

| **항목** | **설명** |
| --- | --- |
| **UserDetails** | 사용자 1명의 로그인 정보(아이디, 비밀번호, 권한 등)를 담는 객체 |
| **UserDetailsService** | 사용자 이름으로 UserDetails를 조회해주는 서비스 |

**CustomUserDetails 구현**

```
public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity;

    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> userEntity.getRole());
    }

    @Override public String getPassword() { return userEntity.getPassword(); }
    @Override public String getUsername() { return userEntity.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
```

**주요 메서드**

| **메서드명** | **설명** |
| --- | --- |
| **getUsername()** | 사용자 식별자 (일반적으로 ID 혹은 이메일) |
| **getPassword()** | 암호화된 비밀번호 |
| **getAuthorities()** | 사용자의 권한 목록 (ROLE\_USER, ROLE\_ADMIN 등) |
| **isAccountNonExpired()** | 계정 만료 여부 (true: 유효) |
| **isAccountNonLocked()** | 계정 잠김 여부 |
| **isCredentialsNonExpired()** | 비밀번호 만료 여부 |
| **isEnabled()** | 계정 활성화 여부 |

**CustomUserDetailsService 구현**

```
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username);
        if (user != null) return new CustomUserDetails(user);
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }
}
```

loadUserByUsername을 이용해 DB에 접근한다.