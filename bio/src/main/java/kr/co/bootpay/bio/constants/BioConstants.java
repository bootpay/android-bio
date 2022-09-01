package kr.co.bootpay.bio.constants;

import static kr.co.bootpay.android.constants.BootpayBuildConfig.VERSION;

import android.content.Context;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import kr.co.bootpay.android.constants.BootpayBuildConfig;
import kr.co.bootpay.android.constants.BootpayConstant;
import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.bio.helper.SharedPreferenceHelper;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioExtra;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.BiometricAuthenticate;
import kr.co.bootpay.bio.models.DestroyWallet;
import kr.co.bootpay.bio.models.OTPPayload;

public class BioConstants extends BootpayConstant {

    public static final String CDN_URL = "https://webview.bootpay.co.kr/4.2.0";

    public static final int REQUEST_TYPE_NONE = -1;
    public static final int REQUEST_PASSWORD_TOKEN = 10; //최초요청시 - 비밀번호 설정하기
    public static final int REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD = 11; //카드 등록 전 토큰이 없을 때
    public static final int REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY = 12; //생체인증 결제 전 토큰이 없을 때
    public static final int REQUEST_BIOAUTH_FOR_BIO_FOR_PAY = 13; //생체인증 결제 전 기기등록이 안 됬을때

    public static final int REQUEST_ADD_BIOMETRIC = 15; //생체인식 정보등록
    public static final int REQUEST_ADD_BIOMETRIC_FOR_PAY = 16; //결제 전 생체인식 정보등록

    public static final int REQUEST_ADD_BIOMETRIC_NONE = 17; ////생체인식 정보등록 수행 후 NONE 처리 (이벤트가 재귀함수 호출되지 않도록)
    public static final int REQUEST_ADD_CARD = 20; //카드 등록

    public static final int REQUEST_ADD_CARD_NONE = 21;  //카드 등록 수행 후 NONE 처리 (이벤트가 재귀함수 호출되지 않도록)
    public static final int REQUEST_BIO_FOR_PAY = 30; //결제를 위해 생체인증 진행
    public static final int REQUEST_PASSWORD_FOR_PAY = 40; //비밀번호로 결제 진행
    public static final int REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY = 41; //비밀번호 결제 전 토큰이 없을 때
    public static final int REQUEST_TOTAL_PAY = 50; //통합결제

    public static final int REQUEST_PASSWORD_TOKEN_DELETE_CARD = 60; //카드 삭제
    public static final int REQUEST_DELETE_CARD = 61; //카드 삭제

    public static final int NEXT_JOB_RETRY_PAY = 100;
    public static final int NEXT_JOB_ADD_NEW_CARD = 101;
    public static final int NEXT_JOB_ADD_DELETE_CARD = 102;
    public static final int NEXT_JOB_GET_WALLET_LIST = 103;


    public static String getJSPasswordToken(BioPayload payload) {
        return loadParams(
                "BootpaySDK.requestPasswordToken('",
                payload.getUserToken(),
                "')",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }

    public static String getJSChangePassword(Context context, BioPayload payload) {
        return loadParams(
                "BootpaySDK.requestChangePassword('",
                payload.getUserToken(),
                "')",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }

    public static String getJSAddCard(BioPayload payload) {
        return loadParams(
                "BootpaySDK.requestAddCard('",
                payload.getUserToken(),
                "')",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }

    public static String getJSPasswordPay(Context context, BioPayload payload) {
        payload.setAuthenticateType("token");
        if(payload.getToken() == null || payload.getToken().length() == 0) {
            payload.setToken(SharedPreferenceHelper.getValue(context, "password_token"));
        }
        if(payload.getPrice() < 50000) {
            if(payload.getExtra() == null) payload.setExtra(new BioExtra());
            payload.getExtra().setCardQuota("0");
        }

        Log.d("bootpay", payload.toJsonUnderscoreEasyPay());

        return loadParams(
                "BootpaySDK.requestWalletPayment(",
                payload.toJsonUnderscoreEasyPay(),
                ")",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
//                "Android.easyError(JSON.stringify(res));",
                cancel(),
                easyError(),
                "})"
        );
    }

    public static String getJSBioOTPPay(BioPayload payload) {
        if(CurrentBioRequest.getInstance().otp != null) payload.setToken(CurrentBioRequest.getInstance().otp);
        payload.setAuthenticateType("otp");
        if(payload.getExtra() == null) payload.setExtra(new BioExtra());
        if(payload.getPrice() >= 50000) {
            BioExtra extra = payload.getExtra();
            extra.setCardQuota(CurrentBioRequest.getInstance().selectedQuota);
            payload.setExtra(extra);
        } else {
            payload.getExtra().setCardQuota("0");
        }

        Log.d("bootpay", payload.toJsonUnderscoreEasyPay());

        return loadParams(
                "BootpaySDK.requestWalletPayment(",
                payload.toJsonUnderscoreEasyPay(),
                ")",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
//                "Android.easyError(JSON.stringify(res));",
                cancel(),
                easyError(),
                "})"
        );
    }

//    public static String getJSAddBiometricData( BioPayload payload, String passwordToken) {
//        BiometricAuthenticate biometricAuthenticate = new BiometricAuthenticate();
//        biometricAuthenticate.userToken = payload.getUserToken();
//        biometricAuthenticate.os = "android";
//        biometricAuthenticate.token = passwordToken.replaceAll("\n", "");
//
//        return loadParams(
//                "BootpaySDK.createBiometricAuthenticate(",
//                new Gson().toJson(biometricAuthenticate),
//                ")",
//                ".then( function (res) {",
//                easySuccess(),
//                "}, function (res) {",
//                cancel(),
//                easyError(),
//                "})"
//        );
//    }


    public static String getJSDestroyBiometricData(BioPayload payload, String passwordToken) {
        BiometricAuthenticate biometricAuthenticate = new BiometricAuthenticate();
        biometricAuthenticate.userToken = payload.getUserToken();
        biometricAuthenticate.os = "android";
        biometricAuthenticate.token = passwordToken;

        return loadParams(
                "BootpaySDK.destroyBiometricAuthenticate(",
                new Gson().toJson(biometricAuthenticate),
                ")",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }


    public static String getJSBiometricAuthenticate(BioPayload payload) {
        OTPPayload otpPayload = new OTPPayload();
        otpPayload.userToken = payload.getUserToken();
        otpPayload.os = "android";
        otpPayload.token = payload.getToken();

        return loadParams(
                "BootpaySDK.createBiometricAuthenticate(",
                new Gson().toJson(otpPayload),
                ")",
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }

    public static String getJSTotalPay(BioPayload payload) {

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        return loadParams(
                "Bootpay.requestPayment(",
                gson.toJson(payload),
                ")",
                ".then( function (res) {",
                confirm(),
                issued(),
                "else { Android.easySuccess(JSON.stringify(res)); }",
//                done(),
//                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }

    public static String getJSDestroyWallet(BioPayload payload) {
        DestroyWallet wallet = new DestroyWallet();
        wallet.authenticate_type = "password";
        wallet.user_token = payload.getUserToken();
        wallet.wallet_id = payload.getWalletId();
        wallet.token = payload.getToken();

        return loadParams(
                "BootpaySDK.destroyWallet(",
                new Gson().toJson(wallet),
                ")",
//                ".then((response) => { alert(response); Android.easySuccess(JSON.stringify(res)); }, (error) => { alert(error); Android.easyError(JSON.stringify(res)); })"
                ".then( function (res) {",
                easySuccess(),
                "}, function (res) {",
                cancel(),
                easyError(),
                "})"
        );
    }


    public static String confirm() { return "if (res.event === 'confirm') { " + BootpayBuildConfig.JSInterfaceBridgeName + ".confirm(JSON.stringify(res)); }"; }

    public static String done()  { return "else if (res.event === 'done') { " + BootpayBuildConfig.JSInterfaceBridgeName + ".done(JSON.stringify(res)); }"; }

    public static String issued() { return "else if (res.event === 'issued') { " + BootpayBuildConfig.JSInterfaceBridgeName + ".issued(JSON.stringify(res)); }"; }

    public static String error() { return "if (res.event === 'error') { " + BootpayBuildConfig.JSInterfaceBridgeName + ".error(JSON.stringify(res)); }"; }

    public static String cancel() { return  "if (res.event === 'cancel') { " + BootpayBuildConfig.JSInterfaceBridgeName + ".cancel(JSON.stringify(res)); }"; }

    public static String close() { return  "document.addEventListener('bootpayclose', function (e) { Android.close('결제창이 닫혔습니다'); });"; }

    public static final List<String> getJSBeforePayStart(String uuid) {
        List<String> scripts = new ArrayList<>();
        scripts.add("Bootpay.setVersion('" + VERSION + "', 'android');");
        scripts.add("BootpaySDK.setDevice('ANDROID');");

        if(BioBuildConfig.DEBUG) {
            scripts.add("Bootpay.setEnvironmentMode('development', 'gosomi.bootpay.co.kr');");
            scripts.add("BootpaySDK.setEnvironmentMode('development', 'gosomi.bootpay.co.kr');");
        }
        scripts.add("Bootpay.setDevice('ANDROID');");
        scripts.add("Bootpay.setLogLevel(4);");
        scripts.add("BootpaySDK.setUUID('" + uuid + "');");
        scripts.add(close());

//        scripts.add(getAnalyticsData(context));
//        if(quickPopup) scripts.add("BootPay.startQuickPopup();");
        return scripts;
    }

//    private static String loadParams(String... script) {
//        StringBuilder builder = new StringBuilder();
//        for (String s : script) builder.append(s);
//        builder.append(";");
//        return builder.toString();
//    }
    private static String easySuccess() { return "Android.easySuccess(JSON.stringify(res));"; } //{event:"confirmAddCard”, message:"카드 등록이 완료되었습니다.”}

    private static String easyError() { return " else { Android.easyError(JSON.stringify(res)); }"; }

//    public static String cancel() { return  "else if (res.event === 'cancel') { Android.cancel(JSON.stringify(res)); }"; }

}
