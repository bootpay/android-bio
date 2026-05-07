package kr.co.bootpay.android_bio;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kr.co.bootpay.android_bio.deprecated.BootpayRest;
import kr.co.bootpay.android_bio.deprecated.BootpayRestImplement;
import kr.co.bootpay.android_bio.deprecated.EasyPayUserTokenData;
import kr.co.bootpay.android_bio.deprecated.TokenData;
import kr.co.bootpay.bio.BootpayBio;
import kr.co.bootpay.bio.models.BioExtra;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.BioPrice;
import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.android.models.BootUser;
import kr.co.bootpay.bio.models.BioThemeData;

public class MainActivity extends AppCompatActivity implements BootpayRestImplement {
    private enum AuthMode {
        CLIENT_KEY,
        LEGACY_APPLICATION_ID,
        MISSING_KEY
    }

    String clientKey = BootpayConstants.client_key;

    @Deprecated
    String restApplicationId = BootpayConstants.rest_application_id;

    // 주의: server_key (secret) 는 클라이언트에 절대 포함하지 말 것 — 서버 SDK 에서만 사용. 아래 호출은 서버에서 받은 토큰을 직접 주입하도록 변경하세요.
    String serverKey = "";

    @Deprecated
    String privateKey = BootpayConstants.private_key;

    AuthMode authMode = AuthMode.CLIENT_KEY;
    boolean isPasswordMode = false;
    boolean isEditdMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goBioPay(View v) {
        authMode = AuthMode.CLIENT_KEY;
        isPasswordMode = false;
        isEditdMode = false;
        requestRestToken();
    }

    public void goBioPayLegacy(View v) {
        authMode = AuthMode.LEGACY_APPLICATION_ID;
        isPasswordMode = false;
        isEditdMode = false;
        requestRestToken();
    }

    public void goBioPayMissingKey(View v) {
        authMode = AuthMode.MISSING_KEY;
        isPasswordMode = false;
        isEditdMode = false;
        requestRestToken();
    }

    public void goPasswordPay(View v) {
        authMode = AuthMode.CLIENT_KEY;
        isPasswordMode = true;
        isEditdMode = false;
        requestRestToken();
    }

    public void goEditPayment(View v) {
        authMode = AuthMode.CLIENT_KEY;
        isPasswordMode = false;
        isEditdMode = true;
        requestRestToken();
    }

    private void requestRestToken() {
        if(authMode == AuthMode.LEGACY_APPLICATION_ID) {
            BootpayRest.getRestToken(this, this, restApplicationId, privateKey);
        } else {
            BootpayRest.getRestTokenWithClientKey(this, this, clientKey, serverKey);
        }
    }


    @Override
    public void callbackRestToken(TokenData token) {
        Log.d("bootpay", token.access_token);

//        String userId = UserInfo.getInstance(this).getBootpayUuid(); // 실제값을 적용하실 때에는, 관리하시는 user_id를 입력해주세요.
        String userId = "123411aaaaaaaaaaaabd4ss11234";



        BootUser user = new BootUser();
        user.setId(userId);
        user.setArea("서울");
        user.setGender(1); //1: 남자, 0: 여자
        user.setEmail("test1234@gmail.com");
        user.setPhone("010-1234-4567");
        user.setBirth("1988-06-10");
        user.setUsername("홍길동");

        BootpayRest.getEasyPayUserToken(this, this, token.access_token, user);
    }

    @Override
    public void callbackEasyPayUserToken(EasyPayUserTokenData userToken) {
        String easyUserToken = userToken.user_token; //example. 621ef840ec81b404d7c6fe83
        BootUser user = new BootUser().setPhone("010-1234-5678");
        BioExtra extra = new BioExtra().setCardQuota("6");
//        extra.

        BioPayload bioPayload = new BioPayload();


        Map<String, Object> map = new HashMap<>();
        map.put("1", "abcdef");
        map.put("2", "abcdef55");
        map.put("3", 1234);
        bioPayload.setMetadata(map);

        bioPayload.setPg("nicepay")
                .setOrderName("bootpay test")
                .setUserToken(easyUserToken)
                .setPrice(1000.0) //최종 결제 금액
                .setOrderId(String.valueOf(System.currentTimeMillis())) //개발사에서 관리하는 주문번호
                .setUser(user)
                .setExtra(extra)
                .setOrderName("플리츠레이어 카라숏원피스")
                .setNames(Arrays.asList("블랙 (COLOR)", "55 (SIZE)")) //결제창에 나타날 상품목록
                .setPrices(Arrays.asList(new BioPrice("상품가격", 89000.0),  //결제창에 나타날 가격목록
                        new BioPrice("쿠폰적용", -25000.0),
                        new BioPrice("배송비", 2500.0)));

        applyAuth(bioPayload);

        if(isPasswordMode == true) {
            requestPassword(bioPayload);
        } else if(isEditdMode == true) {
            requestEditMode(easyUserToken);
        }  else {
            requestBio(bioPayload);
        }

    }

    private void applyAuth(BioPayload bioPayload) {
        if(authMode == AuthMode.CLIENT_KEY) {
            bioPayload.setClientKey(clientKey);
        } else if(authMode == AuthMode.LEGACY_APPLICATION_ID) {
            bioPayload.setApplicationId(BootpayConstants.application_id);
        }
        // MISSING_KEY는 토큰은 client_key/server_key로 발급받고 payload 키만 비워 NEED_CLIENT_KEY를 검증합니다.
    }

    private void requestBio(BioPayload bioPayload) {

        BioThemeData bioThemeData = new BioThemeData();
//        bioThemeData.bgColor = getResources().getColor(R.color.bg_color, null);
//        bioThemeData.textColor = getResources().getColor(R.color.price_color, null);
//        bioThemeData.priceColor = getResources().getColor(R.color.price_color, null);
//        bioThemeData.cardText1Color = getResources().getColor(R.color.card1_text_color, null);
//        bioThemeData.card2Color = getResources().getColor(R.color.card2_color, null);
//        bioThemeData.cardIconColor = getResources().getColor(R.color.card_icon_color, null);
//        bioThemeData.buttonBgColor = getResources().getColor(R.color.button_bg_color, null);
//        bioThemeData.logoImageResource = R.drawable.example_logo;


        BootpayBio.init(this)
                .setBioPayload(bioPayload)
                .setThemeData(bioThemeData)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("-- bootpay cancel", data);
                    }

                    @Override
                    public void onError(String data) {

                        Log.d("-- bootpay error", data);

                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                        });

                    }

                    @Override
                    public void onClose() {
                        Log.d("-- bootpay close", "-- bootpay close");
                        BootpayBio.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("-- bootpay issued", data);

                    }


                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("-- bootpay confirm", data);
//                        return false; //재고 없으면 return false
//                        BootpayBio.transactionConfirm(data);
                        return true; // 재고 있으면 return true
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("-- bootpay done", data);
//                        BootpayBio.removePaymentWindow();
                    }
                })
                .requestBio();
    }

    private void requestPassword(BioPayload bioPayload) {

        BootpayBio.init(this)
                .setBioPayload(bioPayload)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("-- bootpay cancel", data);
                    }

                    @Override
                    public void onError(String data) {
                        Log.d("-- bootpay error", data);
                    }

                    @Override
                    public void onClose() {
                        Log.d("-- bootpay close", "-- bootpay close");
                        BootpayBio.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("-- bootpay issued", data);

                    }


                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("-- bootpay confirm", data);
//                        return false; //재고 없으면 return false
//                        BootpayBio.transactionConfirm(data);
                        return true; // 재고 있으면 return true
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("-- bootpay done", data);
//                        BootpayBio.removePaymentWindow();
                    }
                })
                .requestPassword();
    }

    private void requestEditMode(String easyUserToken) {
        BootpayBio.init(this)
                .setUserToken(easyUserToken)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("-- bootpay cancel", data);
                    }

                    @Override
                    public void onError(String data) {
                        Log.d("-- bootpay error", data);
                    }

                    @Override
                    public void onClose() {
                        Log.d("-- bootpay close", "-- bootpay close");
                        BootpayBio.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("-- bootpay issued", data);

                    }


                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("-- bootpay confirm", data);
//                        return false; //재고 없으면 return false
//                        BootpayBio.transactionConfirm(data);
                        return true; // 재고 있으면 return true
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("-- bootpay done", data);
//                        BootpayBio.removePaymentWindow();
                    }
                })
                .requestEditPayment();

    }
}
