## BCryptPasswordEncoder란?

BCryptPasswordEncoder는 Spring Security에서 제공하는 **비밀번호 해시 암호화** 도구로, 사용자의 비밀번호를 **단방향 해시 처리**하기 위해 사용됩니다.

BCrypt는 단방향 해시 알고리즘 중 하나로, 같은 입력값이라도 매번 **다른 결과값(해시)** 를 생성하고, 암호화된 값을 역으로 복호화할 수 없습니다.

---

## 📌 핵심 특징 요약

| 항목 | 설명 |
| --- | --- |
| 🔁 단방향성 | 암호화된 값은 복호화 불가. 비교는 matches() 메서드로만 가능 |
| 🔑 내부 솔트(salt) 포함 | 자동으로 salt를 포함하여 저장. 동일한 비밀번호라도 결과값이 다름 |
| ⏳ 느린 해시(비용 factor) | 공격자가 빠르게 대입하지 못하도록 연산 비용을 조절 가능 (strength) |
| 🔒 안전성 | Rainbow Table 공격 및 Brute Force 공격에 강함 |
| 🧩 스프링 통합 | PasswordEncoder 인터페이스를 구현, Spring Security에서 바로 사용 가능 |

---

## 🧪 내부 동작 방식

```
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// 암호화
String hashed = encoder.encode("myPassword");

// 비교
boolean match = encoder.matches("myPassword", hashed);
```

-   **encode(plainText)**  
    → 내부적으로 salt를 생성하고, 복잡한 계산을 통해 해시값 생성  
    → 결과는 다음과 같은 문자열 형태:

```
$2a$10$xjqZj4hk6/NOJxEOI7Ilt.bD2vQoXEdOBodWmVRRIP9z6KPzBi3yq
```

-   **matches(rawPassword, encodedPassword)**  
    → 기존에 암호화된 문자열에서 salt 값을 추출하여 동일한 방식으로 다시 해시하고, 비교

---

## 🔍 구성 요소 설명

```
$2a$10$xjqZj4hk6/NOJxEOI7Ilt.bD2vQoXEdOBodWmVRRIP9z6KPzBi3yq
|  |  |  |
|  |  |  └── 해시 결과
|  |  └────── 솔트 값 (22자)
|  └───────── 작업 인자(cost factor, 10)
└──────────── 알고리즘 (2a: BCrypt)
```

-   **2a** : 알고리즘 버전
-   **10** : 비용 factor (높을수록 연산량 증가 → 보안↑, 성능↓)
-   **22자리 salt** : 매번 새로 생성됨
-   **31자리 해시값** : 실제 암호화된 결과

---

## ⚙️ 생성자 옵션

```
// 기본 생성자: cost = 10
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// 커스텀 cost 설정 (4~31 범위 추천)
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
```

-   **비용 인자(strength)**: 기본값은 10 → 숫자가 높을수록 보안은 높아지지만 성능은 느려짐
-   실무에서는 **10 ~ 12** 정도가 일반적

---

## 💡 실무 적용 예시

### 1\. 회원가입 시 암호화

```
user.setPassword(encoder.encode(signUpDto.getPassword()));
```

### 2\. 로그인 시 비교

Spring Security 내부에서 자동으로 처리됨  
→ UserDetailsService에서 DB에서 비밀번호를 가져오기만 하면 됨

---

## 🛡️ 보안적 장점

| 공격 유형 | 대응 방식 |
| --- | --- |
| 📃 Rainbow Table | salt로 인해 동일한 입력값도 결과가 다르므로 무효화 |
| 🧠 Brute-force | 비용 인자가 높을수록 시간 오래 걸림 |
| 🔄 동일 해시값 반복 | salt가 포함되어 있으므로 같은 비밀번호라도 결과값이 매번 다름 |
| 📂 DB 유출 시 | 해시값만 저장되므로 원문 유추 어려움 |

---

## 🔚 요약 정리



| 항목 | 내용 |
| --- | --- |
| 클래스명 | org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder |
| 목적 | 사용자 비밀번호 단방향 해시 암호화 |
| 내부 특징 | 솔트 자동 포함, 비용 인자 조절 가능 |
| 장점 | 높은 보안성, Spring과의 통합성 |
| 메서드 | encode(), matches() |
| 추천 비용 인자 | 10 ~ 12 (기본: 10) |