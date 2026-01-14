# Phraiz - AI 기반 통합 글쓰기 지원 플랫폼 (Backend)

Spring Boot 기반 AI 글쓰기 지원 서비스 백엔드

## 기술 스택

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: MySQL, Redis
- **Security**: Spring Security, JWT, OAuth2
- **AI Integration**: OpenAI GPT API
- **Build Tool**: Gradle

## 프로젝트 소개

Phraiz는 대학생과 작성자를 위한 AI 기반 통합 글쓰기 지원 플랫폼입니다.
패러프레이징, 요약, 인용 생성 등 학술 글쓰기에 필요한 모든 기능을 하나의 플랫폼에서 제공합니다.

## 주요 기능

### AI 패러프레이징
- 6가지 문체 모드 지원 (표준, 학술, 창의적, 유창형, 문학적, 사용자 지정)
- 강도 조절 (0-100 스케일)
- GPT API 프롬프트 최적화 (Tone Blend Slider)

### AI 요약
- 6가지 요약 모드 (한줄, 전체, 문단별, 핵심, 질문 기반, 타겟)
- 상황별 맞춤 요약 제공

### 인용 생성
- URL, DOI 자동 파싱
- APA, MLA, Chicago 등 형식 자동 변환
- 인용 히스토리 관리

### 회원 관리
- 자체 회원가입/로그인 (이메일 인증)
- OAuth2 소셜 로그인 (Google, Kakao, Naver)
- JWT 기반 인증/인가

## 프로젝트 구조

```
src/main/java/com/phraiz/back/
├── common/              # 공통 모듈
│   ├── config/         # 설정 (Security, Redis, GPT)
│   ├── security/       # JWT, OAuth2 인증
│   ├── exception/      # 예외 처리
│   └── service/        # 공통 서비스
├── member/             # 회원 관리
│   ├── controller/
│   ├── service/
│   └── repository/
├── paraphrase/         # 패러프레이징
├── summary/            # 요약
└── cite/               # 인용 생성
```

## 실행 방법

### 1. 사전 요구사항
- Java 17
- MySQL 8.0+
- Redis

### 2. 환경 변수 설정
`.env` 파일 생성:
```properties
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/phraiz
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# JWT
JWT_SECRET=your_jwt_secret_key

# OpenAI
OPENAI_API_KEY=your_openai_api_key

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# Email
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_USERNAME=your_email
SPRING_MAIL_PASSWORD=your_app_password
```

### 3. 빌드 및 실행
```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 4. Docker 실행
```bash
docker build -t phraiz-backend .
docker run -p 8080:8080 phraiz-backend
```

## 핵심 기술

### GPT API 프롬프트 최적화
- **구조화된 템플릿**: 역할-목표-규칙-지침-출력의 5단계 구조
- **Tone Blend Slider**: 0-100 스케일로 문체 강도 조절
- **동적 파라미터**: 모드와 강도에 따라 temperature, top_p, presence_penalty 자동 조정

### 인용 처리
- **다양한 소스 파싱**: URL, DOI, PDF에서 메타데이터 추출
- **CSL Processor**: 표준 Citation Style Language 사용
- **한국 학술지 지원**: DBpia, KISS, 교보문고 등 파싱

## 개발 팀

**Codiva**
- 조은빈 (팀장) - Backend Developer, Infrastructure
- 김희서 - Backend Developer
- 김현진 - Frontend Developer, Infrastructure
- 안선아 - Frontend Developer

## 성과

- 📊 **총 활성 사용자**: 93명 (2025.11.01~11.09)
- 📈 **로그인 전환율**: 32.3%
- 🔧 **핵심 기능 시작 전환율**: 17.2%
- ⏱️ **평균 체류 시간**: 2분 47초

## CI/CD

GitHub Actions를 통한 자동화:
- 빌드 및 테스트
- Docker 이미지 생성
- 배포 자동화

## 라이선스

제 3회 2025 IT 프로젝트 공모전 출품작
