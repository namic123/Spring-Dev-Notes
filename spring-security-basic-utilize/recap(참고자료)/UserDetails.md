## 1\. 전체 개요

이 코드는 **Spring Security의 인증 처리 흐름 중 사용자 인증(User Authentication)** 을 담당하는 커스텀 구현입니다.  
Spring Security는 사용자가 로그인할 때 UserDetailsService를 통해 사용자 정보를 조회하고, 이를 UserDetails 객체로 감싼 후 인증 처리를 수행합니다.

---

## 🧩 2. 클래스 구조와 역할



| 클래스명 | 역할 |
| --- | --- |
| CustomUserDetailsService | DB에서 사용자 정보를 조회하는 서비스 (UserDetailsService 구현체) |
| CustomUserDetails | DB에서 조회한 사용자 정보를 Spring Security의 인증 객체로 감싸는 클래스 |
| UserRepository | 사용자 엔티티(UserEntity)를 DB에서 조회하는 JPA 레포지토리 |

---

## 🔍 3. 핵심 클래스 설명

### ✅ CustomUserDetailsService implements UserDetailsService

Spring Security에서 인증을 처리할 때 반드시 필요한 인터페이스입니다.

```
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity userData = userRepository.findByUsername(username);

    if (userData != null) {
        return new CustomUserDetails(userData); // UserDetails로 감싸 반환
    }

    return null; // 사용자 없으면 인증 실패
}
```

-   loadUserByUsername() 메서드는 Spring Security가 내부적으로 로그인 시 호출하는 메서드입니다.
-   username에 해당하는 UserEntity를 조회하고, 있으면 CustomUserDetails로 감싸 반환합니다.
-   반환된 객체는 AuthenticationManager 내부 로직에서 인증 처리에 사용됩니다.

### ✅ CustomUserDetails implements UserDetails

Spring Security의 인증 처리에서 사용되는 사용자 정보 인터페이스입니다.

```
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> collection = new ArrayList<>();
    collection.add(() -> userEntity.getRole());
    return collection;
}
```

-   getAuthorities()는 사용자의 권한(예: ROLE\_USER, ROLE\_ADMIN)을 반환합니다.
-   getPassword()와 getUsername()은 인증 과정에서 비교할 실제 데이터입니다.
-   나머지 4개의 boolean 메서드는 계정 상태(잠김 여부, 만료 여부 등)를 판단합니다. 현재는 모두 true로 설정되어 있어 계정이 항상 유효한 상태로 간주됩니다.

### ✅ UserRepository

```
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    boolean existsByUsername(String username);
    UserEntity findByUsername(String username);
}
```

-   existsByUsername() : 중복 회원가입 방지에 사용됩니다.
-   findByUsername() : 로그인 시 사용자 정보를 조회하기 위해 사용됩니다.
-   Spring Data JPA가 이 메서드 이름을 기반으로 SQL 쿼리를 자동 생성합니다.

---

## 🧠 4. 인증 처리 흐름

아래는 로그인 요청 시 실제 동작 흐름입니다:

1.  사용자가 /loginProc (Spring Security 기본 로그인 처리 경로)로 username/password를 전송합니다.
2.  UsernamePasswordAuthenticationFilter가 요청을 가로채고 내부적으로 loadUserByUsername()을 호출합니다.
3.  CustomUserDetailsService가 DB에서 사용자를 조회한 뒤 CustomUserDetails 객체를 생성합니다.
4.  Spring Security는 CustomUserDetails에서 getPassword() 값과 사용자가 입력한 패스워드를 비교(BCrypt 등 인코딩 포함)합니다.
5.  인증 성공 시 SecurityContextHolder에 인증 객체가 저장되어 이후 요청부터는 인증된 사용자로 처리됩니다.

---

## 🧾 예시 출력 (로그인 성공 시)

```
Username: user1
Password: $2a$10$HnY... (BCrypt 해시)
Role: ROLE_ADMIN
Authorities: [ROLE_ADMIN]
```