## Unreleased
- example: BioPayload 예제를 client_key 기준으로 전환하고 local.properties production fallback을 유지
- legacy application_id/private_key 토큰 헬퍼는 호환용으로 유지

### 5.1.2
- core `io.github.bootpay:android` 의존성 5.0.0 → 5.1.1 업데이트
  - 나이스페이 앱카드 URL 라우팅 픽스 반영 (Monimo 무반응 / KakaoBank 다운로드 링크 이슈 수정)

### 5.1.1
- Android `<queries>` 패키지 목록 보강 (Android 11+ package visibility)
  - 삼성 모니모(`net.ib.android.smcard`), 카카오뱅크(`com.kakaobank.channel`) 누락으로
    나이스페이 앱카드 결제 시 삼성카드→모니모 선택 무반응 / 카카오뱅크 다운로드 링크 이슈 수정
  - 추가 패키지: KB 스타뱅킹, 신한 쏠/SOL 뱅크, 트레블월렛, 안심클릭 백신, PASS(SKT/LGU+), KCB(SKT),
    티머니, 캐시비, 원스토어, 알리페이
  - core `android` SDK 매니페스트와 `<queries>` 동기화

### 5.1.0
- webview CDN URL을 5.3.0으로 업데이트
- client_key 인증 방식 추가
- minSdk 23 → 24로 상향 (Android 7.0+)
- CDN_URL 끝 슬래시(/) 추가 (URL 결합 버그 수정)
- Java 호환성 1.8 → 11로 업데이트
- Android Gradle Plugin 8.2.2 → 8.5.0 업데이트
- Gradle 8.2 → 8.7 업데이트
- androidx.appcompat alpha → 1.7.0 stable로 변경

## 5.0.0
* android 35 support
* js version update

## 4.2.5 
* 농협 카드 스타일 변경 

## 4.2.41
* 생체인증 정보가 없을 경우 비밀번호 결제로 진행되도록 수정

## 4.2.4
* 생체인증 결제진행시 close가 먼저오는 간헐적 버그 개선 

## 4.2.3
* close에 더 이상 data 를 전달하지 않도록 수정 
* 간편결제는 생체인증, 통합결제 2가지를 사용중
* 간편결제시 서버 분리 승인 로직을 추가하면서 사이드 이펙트가 생긴것을 확인하여 아래와 같이 수정함
    * 발견된 사이드 이펙트는 서버 승인 로직이 추가되면서 클라이언트에서 간편결제 승인이 진행되지 않는 것임 (separatelyConfirmed 옵션 기본값이 true)
    * 수정사항) extra -> bio_extra 모델로 대체
        * bio_extra.separatelyConfirmed 옵션은 통합결제에만 적용되도록 수정
        * bio_extra.separatelyConfirmedBio 옵션은 간편결제에만 적용되도록 수정
            - 단 이 옵션이 true일 경우 특성상 서버승인으로만 진행을 해야함

## 4.2.2
* 등록된 결제수단 편집모드 제공 

## 4.2.1
* bootpay 4.2.1 적용
* bio 커스텀 테마 적용  

## 4.2.0
* 버전 재배포

## 4.1.1
* bootpay 4.2.0 적용
* js 4.2.0 적용
* 생체인증 로직 개선 및 버그 수정 

## 4.1.0
* metadata type 수정 

## 4.0.9
* intent new task 적용 

## 4.0.8
* 비밀번호 간편결제 지원

## 4.0.7
* bootpay 4.0.7 적용

## 4.0.1
* bootpay 4.0.1 적용 

## 4.0.0
* release mode로 배포
   