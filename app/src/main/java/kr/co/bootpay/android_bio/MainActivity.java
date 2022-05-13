package kr.co.bootpay.android_bio;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import kr.co.bootpay.android_bio.deprecated.BootpayRest;
import kr.co.bootpay.android_bio.deprecated.BootpayRestImplement;
import kr.co.bootpay.android_bio.deprecated.EasyPayUserTokenData;
import kr.co.bootpay.android_bio.deprecated.TokenData;
import kr.co.bootpay.core.BootpayBio;
import kr.co.bootpay.core.models.BioPayload;
import kr.co.bootpay.core.models.BioPrice;
import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootUser;

public class MainActivity extends AppCompatActivity implements BootpayRestImplement {
    String applicationId = "5b8f6a4d396fa665fdc2b5e8"; //production
//    String applicationId = "5b9f51264457636ab9a07cdc"; //developement

    @Deprecated
    String restApplicationId = "5b8f6a4d396fa665fdc2b5ea"; //production
//    String restApplicationId = "5b9f51264457636ab9a07cde"; //developement

    @Deprecated
    String privateKey = "rm6EYECr6aroQVG2ntW0A6LpWnkTgP4uQ3H18sDDUYw="; //production
//    String privateKey = "sfilSOSVakw+PZA+PRux4Iuwm7a//9CXXudCq9TMDHk="; //developement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goBioPay(View v) {


        BootpayRest.getRestToken(this, this, restApplicationId, privateKey);
    }


    @Override
    public void callbackRestToken(TokenData token) {
        Log.d("bootpay", token.access_token);

//        String userId = UserInfo.getInstance(this).getBootpayUuid(); // 실제값을 적용하실 때에는, 관리하시는 user_id를 입력해주세요.
        String userId = "123411aaaaaaaaaaaabd4ss11";



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
        BootExtra extra = new BootExtra().setCardQuota("6");

        BioPayload bioPayload = new BioPayload();
//        bioPayload.set
//        bioPayload.set

        bioPayload.setPg("nicepay")
                .setApplicationId(applicationId)
                .setOrderName("bootpay test")
                .setUserToken(easyUserToken)
                .setPrice(50000.0) //최종 결제 금액
                .setOrderId(String.valueOf(System.currentTimeMillis())) //개발사에서 관리하는 주문번호
                .setUser(user)
                .setExtra(extra)
                .setOrderName("플리츠레이어 카라숏원피스")
                .setNames(Arrays.asList("블랙 (COLOR)", "55 (SIZE)")) //결제창에 나타날 상품목록
                .setPrices(Arrays.asList(new BioPrice("상품가격", 89000.0),  //결제창에 나타날 가격목록
                        new BioPrice("쿠폰적용", -25000.0),
                        new BioPrice("배송비", 2500.0)));

        BootpayBio.init(this)
                .setBioPayload(bioPayload)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("bootpay cancel", data);
                    }

                    @Override
                    public void onError(String data) {
                        Log.d("bootpay error", data);
//                        BootpayBio.removePaymentWindow();
                    }

                    @Override
                    public void onClose(String data) {
                        Log.d("bootpay close", data);
                        BootpayBio.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("bootpay issued", data);

                    }


                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("bootpay confirm", data);
//                        return false; //재고 없으면 return false
//                        BootpayBio.transactionConfirm(data);
                        return true; // 재고 있으면 return true
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("bootpay done", data);
//                        BootpayBio.removePaymentWindow();
                    }
                })
                .requestPayment();
    }
}