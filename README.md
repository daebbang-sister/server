# daebbang-sister Backend Server

> 쇼핑 커머스 플랫폼으로, '대빵언니'의 백엔드 시스템입니다.
> 실제 운영 중인 서비스로, 안정성과 확장성을 최우선으로 설계하였습니다.

---
## 🛠 Tech Stack
- **Backend**: Java 17, Spring boot 4.0.2
- **Database**: MySQL, Redis
- **ORM**: JPA(Hibernate), QueryDSL
- **Infra**: AWS EC2, AWS S3, Github Actions(CI/CD), Docker

## 🏗 Project Structure
- `daebbang-api`: 사용자 서비스 관련 API 모듈
- `daebbang-admin`: 관리자 시스템 및 백오피스 API 모듈
- `daebbang-core`: 비즈니스 핵심 도메인 로직 및 인프라스트럭처 (Infra/Domain 분리)
- `daebbang-common`: 공용 유틸리티 및 예외 처리 클래스

## 🚀 Key Features & Engineering
- **Security**: JWT 기반 인증/인가 시스템 구축 및 Redis를 활용한 보안 고도화
- **Architecture**: Domain-Driven Design 아키텍처를 지향하여 비즈니스 로직과 외부 의존성을 엄격히 분리
- **CI/CD**: GitHub Actions를 통한 자동 배포 환경 구축으로 개발 생산성 향상

---
© 2026 youngho. All rights reserved.
