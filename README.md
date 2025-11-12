# 모뉴 (MONEW) - 마음대로 골라보는 모든 뉴스

<img width="1024" height="300" alt="monew_for_readme" src="https://github.com/user-attachments/assets/43093480-c5c7-4494-9213-c9871067671a" />

### 관련 자료

> 협업 문서: [Notion 팀 문서 바로가기](https://www.notion.so/2-29228815f6f280ca8005d12bc9670225?pvs=21)

<br>

## 📘 프로젝트 소개

**MONEW**는 사용자의 관심사에 맞춰 뉴스를 자동으로 수집하고, 댓글·좋아요·알림 등 **소셜 기능을 통해 뉴스 경험을 확장하는 개인화 뉴스 플랫폼**입니다.

* **프로젝트 기간:** 2025.10.17 ~ 2025.11.07
* **기획 의도:** 넘쳐나는 뉴스 속에서 ‘나에게 맞는 뉴스’만 보고 싶은 사용자의 니즈 해결
* **핵심 목표:**

  * 관심사 기반 뉴스 큐레이션 및 자동 수집
  * 사용자 활동(댓글, 좋아요, 알림) 통합 관리
  * 스케줄러 기반 기사 동기화 및 S3 백업 복구 기능
  * Prometheus + Grafana 기반 실시간 관찰성 확보

---

### 🎥 시연 영상
https://github.com/user-attachments/assets/e1d18b5b-4470-41db-b318-1a91ae83fef4

<br>

## 🛠 기술 스택

| **구분**                            | **사용 기술**                                                                                           |
| :-------------------------------- | :-------------------------------------------------------------------------------------------------- |
| **Language & Core**               | Java 17                                                                                             |
| **Framework / Runtime**           | Spring Boot 3.x, Spring Batch                                                                       |
| **Database & ORM**                | PostgreSQL, MongoDB, H2, Spring Data JPA, QueryDSL                                                  |
| **Build & Dependency Management** | Gradle                                                                                              |
| **Infra & DevOps**                | AWS (ECS Fargate, ECR, RDS, S3), Docker, GitHub Actions                                             |
| **Monitoring & Metrics**          | Spring Actuator, Prometheus, Grafana                                                                |
| **API & Docs**                    | Spring REST Docs, Swagger (OpenAPI 3)                                                               |
| **Utilities & Others**            | Lombok, MapStruct, Jakarta Validation, Logback (MDC Logging), BCryptPasswordEncoder, JUnit, Postman |
| **Collaboration Tools**           | GitHub, Discord, Figma, Notion                                                                      |

<br>

## 💡 팀원 소개 및 주요 구현 기능

### 🧾 임재혁

* NAVER, 조선일보, 한경, 연합뉴스 API 기반 뉴스 자동 수집
* Spring Batch 기반 수집 → 정제 → 저장 3단계 Job 구성
* 기사 수집, 백업, 삭제 및 메트릭 수집 로직 구현
* AWS S3 기반 JSON 백업/복구 기능 구축
* Jacoco, Codecov 기반 CI 환경 구성 및 커버리지 측정 자동화

> GitHub: [🔗 JaehyeokLim](https://github.com/JaehyeokLim)

<br>

### 🔒 김찬혁

* 댓글 CRUD 및 좋아요 기능 구현
* Docker & AWS ECS 배포 환경 구성
* GitHub Actions 통한 자동 배포(CI/CD) 파이프라인 구축
* Prometheus + Grafana 기반 모니터링 환경 구성

> GitHub: [🔗 chanhyeok0201](https://github.com/chanhyeok0201)

<br>

### 🧩 강문영

* 알림 CRUD 및 이벤트 기반 알림 시스템 구현
* 스케줄링 기반 알림 삭제 로직 구현
* JobExecutionListener를 사용하여 구독 뉴스 기사 수집 알림 기능 구현

> GitHub: [🔗 truuuely](https://github.com/truuuely)

<br>

### 🎓 이예림 

* 관심사 CRUD 및 구독 로직 구현
* TDD 기반 테스트 코드 작성 및 검증 체계 구축

> GitHub: [🔗 yeahlimm](https://github.com/yeahlimm)

<br>

### ⏰ 정영진

* 사용자 활동 내역(댓글, 좋아요, 뉴스 열람) 통합 조회 기능 구현
* PostgreSQL → MongoDB 캐시 구조로 전환하여 조회 성능 개선
* Docker & AWS ECS 배포 환경 구성
* GitHub Actions 통한 자동 배포(CI/CD) 파이프라인 구축
* Prometheus + Grafana 기반 모니터링 환경 구성

> GitHub: [🔗 userjin2123](https://github.com/userjin2123)

<br>

### 👤 최도한

* 회원가입, 로그인, 탈퇴 기능 구현
* BCrypt 기반 비밀번호 암호화 처리
* 사용자 배치 및 메트릭 수집 로직 구성

> GitHub: [🔗 DoHanChoi](https://github.com/DoHanChoi)

<br>

### ERD

<img width="1693" height="1091" alt="Monew (2) 1" src="https://github.com/user-attachments/assets/87700e1e-6519-4c0a-ae02-3ed22d39431d" />

<br>

## ⚙️ 시스템 아키텍처

**데이터 성격에 따라 저장소를 분리하고, 클라우드 기반 자동 배포 및 모니터링 환경 구축**
<img width="1500" height="836" alt="스크린샷 2025-11-12 오후 3 35 30" src="https://github.com/user-attachments/assets/adfe8af9-3e00-437f-94c2-aa7d65f95623" />

<br>

### 📁 파일 구조

```plaintext
monew-backend/
├── .github/
│   └── workflows/
│       ├── deploy-api.yml
│       ├── deploy-batch.yml
│       └── deploy-monitor.yml
│
├── .gradle/
├── .idea/
├── build/
├── gradle/
├── logs/
│
├── monew-api/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/monew/monew_api/
│   │   │   │   ├── article/
│   │   │   │   ├── comments/
│   │   │   │   ├── common/
│   │   │   │   ├── interest/
│   │   │   │   ├── notification/
│   │   │   │   ├── subscribe/
│   │   │   │   ├── user/
│   │   │   │   ├── useractivity/
│   │   │   │   └── MonewApiApplication.java
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   ├── data/
│   │   │       │   └── schema.sql
│   │   │       ├── static/
│   │   │       │   ├── api/
│   │   │       │   ├── assets/
│   │   │       │   └── index.html
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── logback-spring.xml
│   │   └── test/
│   │       ├── java/com/monew/monew_api/
│   │       │   ├── Comment/
│   │       │   └── Notification/
│   │       └── resources/application-test.yml
│   └── build.gradle
│
├── monew-batch/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/monew/monew_batch/
│   │   │   │   ├── article/
│   │   │   │   │   ├── config/
│   │   │   │   │   ├── dto/
│   │   │   │   │   ├── enums/
│   │   │   │   │   ├── job/
│   │   │   │   │   ├── matric/
│   │   │   │   │   ├── properties/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── scheduler/
│   │   │   │   │   └── service/
│   │   │   │   ├── common/
│   │   │   │   ├── notification/
│   │   │   │   ├── user/
│   │   │   │   └── MonewBatchApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── schema-batch.sql
│   │   └── test/
│   │       ├── java/com/monew/monew_batch/s3/AWS3Test.java
│   │       └── resources/application-test.yml
│   └── build.gradle
│
├── monew-monitor/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/monew/monew_monitor/
│   │   │   │   └── MonewMonitorApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── prometheus.yml
│   │   └── test/
│   │       └── java/com/monew/monew_monitor/
│   └── build.gradle
│
├── .dockerignore
├── .env
├── .env.example
├── .gitignore
├── build.gradle
├── docker-compose.prod.yml
├── Dockerfile.api
├── Dockerfile.batch
├── Dockerfile.monitor
├── Dockerfile.multi
├── gradlew
├── gradlew.bat
├── README.md
└── settings.gradle
```

