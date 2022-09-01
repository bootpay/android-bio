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
   