# 📅 Quartz Scheduler Test (Spring Boot 기반)

Java 17과 Spring Boot 3.4.4, Quartz Scheduler를 기반으로 한 배치 스케줄러 예제 프로젝트입니다.  
간단한 Job 등록 및 실행을 통해 Quartz의 동작을 실습할 수 있도록 구성되어 있습니다.

---

## 🔧 기술 스택
 
| 기술        | 설명 |
|-------------|------|
| Java 17     | JDK 17 사용 |
| Spring Boot | 3.4.4 |
| Quartz      | 2.3.2, 스케줄링 엔진 |
| H2 Database | In-Memory 및 TCP 기반 DB (개발용) |
| JPA         | Spring Data JPA 사용 |
| Lombok      | 보일러플레이트 코드 제거 |
| Gradle      | 빌드 도구 |

---

## 🚀 실행 방법

### 1. 의존성 설치

```bash
./gradlew clean build
```

2. 실행

```bash
./gradlew bootRun
```
또는 IDE에서 QuartzSchedulerTestApplicationRunner가 포함된 Application 클래스 실행


⚙️ 주요 설정 요약
📌 application.yml
```bash
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/quartz-test
    driver-class-name: org.h2.Driver
    username: sa
    password:
  sql:
    init:
      mode: always
      schema-locations: classpath:tables_h2.sql
setting:
  quartz:
    file-path: quartz.properties
```

📌 quartz.properties
```bash
# Scheduler 설정
org.quartz.scheduler.instanceName=QuartzScheduler
org.quartz.scheduler.instanceId=AUTO

# ThreadPool 설정
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount=10
org.quartz.threadPool.threadPriority=5

# JobStore 설정
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
org.quartz.jobStore.tablePrefix=QRTZ_
org.quartz.jobStore.isClustered=true

# 🟡 [중요] DataSource 명시
org.quartz.jobStore.dataSource=quartz

# 🟢 DataSource 정의
org.quartz.dataSource.quartz.provider=hikaricp
org.quartz.dataSource.quartz.driver=org.h2.Driver
org.quartz.dataSource.quartz.URL=jdbc:h2:tcp://localhost/~/quartz-test
org.quartz.dataSource.quartz.user=sa
org.quartz.dataSource.quartz.password=
org.quartz.dataSource.quartz.maxConnections=5

```

⏰ 동작 방식 요약
1. AbstractSchedulerJob: Job 및 Trigger의 공통 로직 추상화
2. TestSimpleJob: 매 분 10초에 실행되는 샘플 Job
3. TestSimpleProcessor: 실제 작업 수행 (콘솔 출력)
4. QuartzSchedulerTestApplicationRunner: 앱 실행 시 Job 등록 트리거