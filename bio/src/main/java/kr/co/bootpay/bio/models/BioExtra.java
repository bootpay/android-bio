package kr.co.bootpay.bio.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootExtraCardEasyOption;

public class BioExtra {

    private String cardQuota; //카드 결제시 할부 기간 설정 (5만원 이상 구매시)
    private String sellerName; //노출되는 판매자명 설정
    private int deliveryDay = 1; //배송일자
    private String locale = "ko"; //결제창 언어지원
    private String offerPeriod; //결제창 제공기간에 해당하는 string 값, 지원하는 PG만 적용됨
    private boolean displayCashReceipt = true; // 현금영수증 보일지 말지.. 가상계좌 KCP 옵션
    private String depositExpiration; //가상계좌 입금 만료일자 설정, yyyy-MM-dd

    private String appScheme; //모바일 앱에서 결제 완료 후 돌아오는 옵션 ( 아이폰만 적용 )
    private boolean useCardPoint = true; //카드 포인트 사용 여부 (토스만 가능)
    private String directCard = ""; //해당 카드로 바로 결제창 (토스만 가능)

    private boolean useOrderId = false; //가맹점 order_id로 PG로 전송
    private boolean internationalCardOnly = false; //해외 결제카드 선택 여부 (토스만 가능)
    private String phoneCarrier;  //본인인증 시 고정할 통신사명, SKT,KT,LGT 중 1개만 가능
    private boolean directAppCard = false; //카드사앱으로 direct 호출
    private boolean directSamsungpay = false; //삼성페이 바로 띄우기
    private boolean testDeposit = false;  //가상계좌 모의 입금
    private boolean enableErrorWebhook = false;  //결제 오류시 Feedback URL로 webhook
    private boolean separatelyConfirmed = true; // confirm 이벤트를 호출할지 말지, false일 경우 자동승인, 간편결제에선 적용되지 않음
    private boolean separatelyConfirmedBio = false; // 중요 - 간편결제에서 true면 무조건 서버 승인(분리승인), false면 바로 승인

    private boolean confirmOnlyRestApi = false; // REST API로만 승인 처리
    private String openType = "redirect"; //페이지 오픈 type [iframe, popup, redirect] 중 택 1
    private boolean useBootpayInappSdk = true; //native app에서는 redirect를 완성도있게 지원하기 위한 옵션
    private String redirectUrl = "https://api.bootpay.co.kr/v2"; //open_type이 redirect일 경우 페이지 이동할 URL ( 오류 및 결제 완료 모두 수신 가능 )
    private boolean displaySuccessResult = false; // 결제 완료되면 부트페이가 제공하는 완료창으로 보여주기 ( open_type이 iframe, popup 일때만 가능 )
    private boolean displayErrorResult = true; // 결제가 실패하면 부트페이가 제공하는 실패창으로 보여주기 ( open_type이 iframe, popup 일때만 가능 )
    private int disposableCupDeposit = 0; //배달대행 플랫폼을 위한 컵 보증급 가격
    private BootExtraCardEasyOption cardEasyOption = new BootExtraCardEasyOption();
    //    private List<BrowserOpenType> browserOpenType = new ArrayList<>();
    private boolean useWelcomepayment = false; //웰컴 재판모듈 진행시 true

    private int timeout = 30; //배달대행 플랫폼을 위한 컵 보증급 가격
    private boolean commonEventWebhook = false; //창닫기, 결제만료 웹훅 추가
    private List<String> enableCardCompanies = new ArrayList<>(); //https://developers.nicepay.co.kr/manual-code-partner.php '01,02,03,04,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,31,32,33,34,35,36,37,38,39,40,41,42'
    private List<String> exceptCardCompanies = new ArrayList<>(); //제외할 카드사 리스트 ( enable_card_companies가 우선순위를 갖는다 )
    private List<String> enableEasyPayments = new ArrayList<>(); //노출될 간편결제 리스트
    private String firstSubscriptionComment = ""; //자동결제 price > 0 조건일 때 첫 결제 관련 메세지
    private int confirmGraceSeconds = 10; ////결제승인 유예시간 ( 승인 요청을 여러번하더라도 승인 이후 특정 시간동안 계속해서 결제 response_data 를 리턴한다 )

    public String getCardQuota() {
        return cardQuota;
    }

    public BioExtra setCardQuota(String cardQuota) {
        this.cardQuota = cardQuota;
        return this;
    }

    public String getSellerName() {
        return sellerName;
    }

    public BioExtra setSellerName(String sellerName) {
        this.sellerName = sellerName;
        return this;
    }

    public int getDeliveryDay() {
        return deliveryDay;
    }

    public BioExtra setDeliveryDay(int deliveryDay) {
        this.deliveryDay = deliveryDay;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public BioExtra setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public String getOfferPeriod() {
        return offerPeriod;
    }

    public BioExtra setOfferPeriod(String offerPeriod) {
        this.offerPeriod = offerPeriod;
        return this;
    }

    public boolean isDisplayCashReceipt() {
        return displayCashReceipt;
    }

    public BioExtra setDisplayCashReceipt(boolean displayCashReceipt) {
        this.displayCashReceipt = displayCashReceipt;
        return this;
    }

    public String getDepositExpiration() {
        return depositExpiration;
    }

    public BioExtra setDepositExpiration(String depositExpiration) {
        this.depositExpiration = depositExpiration;
        return this;
    }

    public String getAppScheme() {
        return appScheme;
    }

    public BioExtra setAppScheme(String appScheme) {
        this.appScheme = appScheme;
        return this;
    }

    public boolean isUseCardPoint() {
        return useCardPoint;
    }

    public BioExtra setUseCardPoint(boolean useCardPoint) {
        this.useCardPoint = useCardPoint;
        return this;
    }

    public String getDirectCard() {
        return directCard;
    }

    public BioExtra setDirectCard(String directCard) {
        this.directCard = directCard;
        return this;
    }

    public boolean isUseOrderId() {
        return useOrderId;
    }

    public BioExtra setUseOrderId(boolean useOrderId) {
        this.useOrderId = useOrderId;
        return this;
    }

    public boolean isInternationalCardOnly() {
        return internationalCardOnly;
    }

    public BioExtra setInternationalCardOnly(boolean internationalCardOnly) {
        this.internationalCardOnly = internationalCardOnly;
        return this;
    }

    public String getPhoneCarrier() {
        return phoneCarrier;
    }

    public BioExtra setPhoneCarrier(String phoneCarrier) {
        this.phoneCarrier = phoneCarrier;
        return this;
    }

    public boolean getDirectAppCard() {
        return directAppCard;
    }

    public BioExtra setDirectAppCard(boolean directAppCard) {
        this.directAppCard = directAppCard;
        return this;
    }

    public boolean getDirectSamsungpay() {
        return directSamsungpay;
    }

    public BioExtra setDirectSamsungpay(boolean directSamsungpay) {
        this.directSamsungpay = directSamsungpay;
        return this;
    }

    public boolean getTestDeposit() {
        return testDeposit;
    }

    public BioExtra setTestDeposit(boolean testDeposit) {
        this.testDeposit = testDeposit;
        return this;
    }

    public boolean getEnableErrorWebhook() {
        return enableErrorWebhook;
    }

    public BioExtra setEnableErrorWebhook(boolean enableErrorWebhook) {
        this.enableErrorWebhook = enableErrorWebhook;
        return this;
    }

    public boolean isSeparatelyConfirmed() {
        return separatelyConfirmed;
    }

    public BioExtra setSeparatelyConfirmed(boolean separatelyConfirmed) {
        this.separatelyConfirmed = separatelyConfirmed;
        return this;
    }

    public boolean isSeparatelyConfirmedBio() {
        return separatelyConfirmedBio;
    }

    public BioExtra setSeparatelyConfirmedBio(boolean separatelyConfirmedBio) {
        this.separatelyConfirmedBio = separatelyConfirmedBio;
        return this;
    }

    public boolean isConfirmOnlyRestApi() {
        return confirmOnlyRestApi;
    }

    public BioExtra setConfirmOnlyRestApi(boolean confirmOnlyRestApi) {
        this.confirmOnlyRestApi = confirmOnlyRestApi;
        return this;
    }

    public String getOpenType() {
        return openType;
    }

    public BioExtra setOpenType(String openType) {
        this.openType = openType;
        return this;
    }

    public boolean getUseBootpayInappSdk() { return useBootpayInappSdk; }

    public BioExtra setUseBootpayInappSdk(boolean useBootpayInappSdk) {
        this.useBootpayInappSdk = useBootpayInappSdk;
        return this;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public BioExtra setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public boolean isDisplaySuccessResult() {
        return displaySuccessResult;
    }

    public BioExtra setDisplaySuccessResult(boolean displaySuccessResult) {
        this.displaySuccessResult = displaySuccessResult;
        return this;
    }

    public boolean isDisplayErrorResult() {
        return displayErrorResult;
    }

    public BioExtra setDisplayErrorResult(boolean displayErrorResult) {
        this.displayErrorResult = displayErrorResult;
        return this;
    }

    public int getDisposableCupDeposit() {
        return disposableCupDeposit;
    }

    public BioExtra setDisposableCupDeposit(int disposableCupDeposit) {
        this.disposableCupDeposit = disposableCupDeposit;
        return this;
    }

    public BootExtraCardEasyOption getCardEasyOption() {
        return cardEasyOption;
    }

    public BioExtra setCardEasyOption(BootExtraCardEasyOption cardEasyOption) {
        this.cardEasyOption = cardEasyOption;
        return this;
    }


    public boolean getUseWelcomepayment() {
        return useWelcomepayment;
    }

    public BioExtra setUseWelcomepayment(boolean useWelcomepayment) {
        this.useWelcomepayment = useWelcomepayment;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public BioExtra setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isCommonEventWebhook() {
        return commonEventWebhook;
    }

    public BioExtra setCommonEventWebhook(boolean commonEventWebhook) {
        this.commonEventWebhook = commonEventWebhook;
        return this;
    }

    public List<String> getEnableCardCompanies() {
        return enableCardCompanies;
    }

    public BioExtra setEnableCardCompanies(List<String> enableCardCompanies) {
        this.enableCardCompanies = enableCardCompanies;
        return this;
    }

    public List<String> getExceptCardCompanies() {
        return exceptCardCompanies;
    }

    public BioExtra setExceptCardCompanies(List<String> exceptCardCompanies) {
        this.exceptCardCompanies = exceptCardCompanies;
        return this;
    }

    public List<String> getEnableEasyPayments() {
        return enableEasyPayments;
    }

    public BioExtra setEnableEasyPayments(List<String> enableEasyPayments) {
        this.enableEasyPayments = enableEasyPayments;
        return this;
    }

    public String getFirstSubscriptionComment() {
        return firstSubscriptionComment;
    }

    public BioExtra setFirstSubscriptionComment(String firstSubscriptionComment) {
        this.firstSubscriptionComment = firstSubscriptionComment;
        return this;
    }

    public int getConfirmGraceSeconds() {
        return confirmGraceSeconds;
    }

    public BioExtra setConfirmGraceSeconds(int confirmGraceSeconds) {
        this.confirmGraceSeconds = confirmGraceSeconds;
        return this;
    }


    public JSONObject toJsonObject() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("card_quota", cardQuota);
            jsonObject.put("seller_name", sellerName);
            jsonObject.put("delivery_day", deliveryDay);
            jsonObject.put("locale", locale);
            jsonObject.put("offer_period", offerPeriod);
            jsonObject.put("display_cash_receipt", displayCashReceipt);
            jsonObject.put("deposit_expiration", depositExpiration);

            jsonObject.put("app_scheme", appScheme);
            jsonObject.put("use_card_point", useCardPoint);
            jsonObject.put("direct_card", directCard);

            jsonObject.put("use_order_id", useOrderId);
            jsonObject.put("international_card_only", internationalCardOnly);
            jsonObject.put("phone_carrier", phoneCarrier);

            jsonObject.put("direct_app_card", directAppCard);
            jsonObject.put("direct_samsungpay", directSamsungpay);
            jsonObject.put("test_deposit", testDeposit);

            jsonObject.put("enable_error_webhook", enableErrorWebhook);
            jsonObject.put("separately_confirmed", separatelyConfirmed);
            jsonObject.put("confirm_only_rest_api", confirmOnlyRestApi);

            jsonObject.put("open_type", openType);
            jsonObject.put("use_bootpay_inapp_sdk", useBootpayInappSdk);
            jsonObject.put("redirect_url", redirectUrl);

            jsonObject.put("display_success_result", displaySuccessResult);
            jsonObject.put("display_error_result", displayErrorResult);
            jsonObject.put("disposable_cup_deposit", disposableCupDeposit);

            jsonObject.put("card_easy_option", cardEasyOption);
            jsonObject.put("use_welcomepayment", useWelcomepayment);

            jsonObject.put("timeout", timeout);
            jsonObject.put("common_event_webhook", commonEventWebhook);

            jsonObject.put("enable_card_companies", new JSONArray(enableCardCompanies));
            jsonObject.put("except_card_companies", new JSONArray(exceptCardCompanies));
            jsonObject.put("enable_easy_payments", new JSONArray(enableEasyPayments));

            jsonObject.put("first_subscription_comment", firstSubscriptionComment);
            jsonObject.put("confirm_grace_seconds", confirmGraceSeconds);

        } catch (JSONException e) {
            e.printStackTrace();

        }
        return jsonObject;
    }

    public JSONObject toJsonObjectEasyPay() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("card_quota", cardQuota);
            jsonObject.put("seller_name", sellerName);
            jsonObject.put("delivery_day", deliveryDay);
            jsonObject.put("locale", locale);
            jsonObject.put("offer_period", offerPeriod);
            jsonObject.put("display_cash_receipt", displayCashReceipt);
            jsonObject.put("deposit_expiration", depositExpiration);

            jsonObject.put("app_scheme", appScheme);
            jsonObject.put("use_card_point", useCardPoint);
            jsonObject.put("direct_card", directCard);

            jsonObject.put("use_order_id", useOrderId);
            jsonObject.put("international_card_only", internationalCardOnly);
            jsonObject.put("phone_carrier", phoneCarrier);

            jsonObject.put("direct_app_card", directAppCard);
            jsonObject.put("direct_samsungpay", directSamsungpay);
            jsonObject.put("test_deposit", testDeposit);

            jsonObject.put("enable_error_webhook", enableErrorWebhook);
            jsonObject.put("separately_confirmed", separatelyConfirmedBio);
            jsonObject.put("confirm_only_rest_api", confirmOnlyRestApi);

            jsonObject.put("open_type", openType);
            jsonObject.put("use_bootpay_inapp_sdk", useBootpayInappSdk);
            jsonObject.put("redirect_url", redirectUrl);

            jsonObject.put("display_success_result", displaySuccessResult);
            jsonObject.put("display_error_result", displayErrorResult);
            jsonObject.put("disposable_cup_deposit", disposableCupDeposit);

            jsonObject.put("card_easy_option", cardEasyOption);
            jsonObject.put("use_welcomepayment", useWelcomepayment);

            jsonObject.put("timeout", timeout);
            jsonObject.put("common_event_webhook", commonEventWebhook);

            jsonObject.put("enable_card_companies", new JSONArray(enableCardCompanies));
            jsonObject.put("except_card_companies", new JSONArray(exceptCardCompanies));
            jsonObject.put("enable_easy_payments", new JSONArray(enableEasyPayments));

            jsonObject.put("first_subscription_comment", firstSubscriptionComment);
            jsonObject.put("confirm_grace_seconds", confirmGraceSeconds);

        } catch (JSONException e) {
            e.printStackTrace();

        }
        return jsonObject;
    }
}
