## 🔐 1. 현재 사용자 아이디 조회

```
String username = SecurityContextHolder.getContext().getAuthentication().getName();
```

### ✅ 설명

-   SecurityContextHolder는 Spring Security에서 **현재 인증된 사용자의 보안 컨텍스트를 저장하는 정적 클래스**입니다.
-   getContext().getAuthentication()을 통해 현재 사용자의 인증 정보를 가져올 수 있습니다.
-   .getName()은 기본적으로 로그인 시 입력한 **username 값을 반환**합니다.
-   이는 내부적으로 Authentication.getPrincipal()이 String일 경우, 해당 값을 name으로 처리합니다.

---

## 🛡️ 2. 현재 사용자 권한(Role) 조회

```
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
Iterator<? extends GrantedAuthority> iter = authorities.iterator();
GrantedAuthority auth = iter.next();
String role = auth.getAuthority();
```

### ✅ 설명

-   Authentication.getAuthorities()는 사용자의 **역할(ROLE\_\*)이나 권한**을 나타내는 GrantedAuthority 목록을 반환합니다.
-   Spring Security에서는 ROLE\_ADMIN, ROLE\_USER와 같은 문자열이 GrantedAuthority 형태로 저장되어 있습니다.
-   이 코드는 authorities 컬렉션에서 첫 번째 권한을 꺼내서 해당 문자열 값을 가져오는 방식입니다.
    -   즉, auth.getAuthority()는 "ROLE\_ADMIN" 또는 "ROLE\_USER" 같은 값을 반환합니다.
-   여러 권한이 있을 수 있으므로, 실제 서비스에서는 for-each나 stream을 사용하는 방식이 더 안전할 수 있습니다.

---

## ✅ 예시 출력

```
Username: user1
Role: ROLE_ADMIN
```