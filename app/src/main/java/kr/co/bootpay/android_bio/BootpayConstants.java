package kr.co.bootpay.android_bio;

/**
 * Bootpay 환경 상수 (예제 전용)
 *
 * 우선순위: local.properties → BuildConfig 주입 → production fallback
 *
 * 환경 전환 (로컬 테스트): local.properties 에 `BOOTPAY_ENV=development` 추가 후 build
 * 키 오버라이드: local.properties 에 `BOOTPAY_ANDROID_APPLICATION_ID_DEV=...` 등 추가
 *
 * 배포 기본값은 항상 production. local.properties는 .gitignore 처리됨.
 */
public class BootpayConstants {
    public static final boolean IS_DEBUG = "development".equals(BuildConfig.BOOTPAY_ENV);

    // PG API
    public static String application_id = BuildConfig.BOOTPAY_APPLICATION_ID;

    // PG REST API (deprecated - EasyPay 용)
    public static String rest_application_id = BuildConfig.BOOTPAY_REST_APPLICATION_ID;
    public static String private_key = BuildConfig.BOOTPAY_PRIVATE_KEY;

    // Commerce API
    public static String client_key = BuildConfig.BOOTPAY_CLIENT_KEY;

    // 주의: server_key (secret) 는 클라이언트에 절대 포함하지 말 것 — 서버 SDK 에서만 사용
}
