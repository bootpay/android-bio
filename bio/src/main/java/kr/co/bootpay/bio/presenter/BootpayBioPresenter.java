package kr.co.bootpay.bio.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import kr.co.bootpay.android.pref.UserInfo;
import kr.co.bootpay.bio.R;
import kr.co.bootpay.bio.activity.BootpayBioActivity;
import kr.co.bootpay.bio.api.BioApiService;
import kr.co.bootpay.bio.api.NextJobInterface;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.helper.SharedPreferenceHelper;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.NextJob;
import kr.co.bootpay.bio.models.ResError;
import kr.co.bootpay.bio.models.ResWalletList;
import kr.co.bootpay.bio.models.data.WalletData;
import kr.co.bootpay.bio.webview.BootpayBioWebView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BootpayBioPresenter implements NextJobInterface {

    public ResWalletList walletList = new ResWalletList();
    public BioPayload bioPayload;
    public BootpayBioActivity bioActivity;
    public BootpayBioWebView bioWebView;
//    public int selectedCardIndex = -1;
    BioApiService service;
    Context context;

    public BootpayBioPresenter(Context context, BootpayBioActivity activity, BootpayBioWebView webView) {
        CurrentBioRequest.getInstance().nextJobListener = this;
        this.service = new BioApiService(context);
        this.bioActivity = activity;
        this.bioWebView = webView;
        this.context = context;
    }

    public void setBioPayload(BioPayload bioPayload) {
        this.bioPayload = bioPayload;
    }

    //ui state
    public void showCardView(List<WalletData> walletList) {

        if(bioActivity == null) return;
        if(walletList != null) {
            bioActivity.setWalletList(walletList);
        }

        bioActivity.showCardView();
        bioActivity.hideWebView();
    }

    public void showWebView() {
        if(bioActivity == null) return;
        bioActivity.hideCardView();
        bioActivity.showWebView();
    }

    public boolean isShowCardView() {
        if(bioActivity == null) return false;
        return bioActivity.isShowCardView();
    }

    public boolean isShowWebView() {
        if(bioActivity == null) return false;
        return bioActivity.isShowWebView();
    }

    public boolean isAblePasswordToken() {
        String password_token = SharedPreferenceHelper.getValue(context, "password_token");
        if(password_token.length() > 0) return true;
        return false;
    }

    public boolean isAbleBioAuthDevice() {
        return didAbleBioAuthDevice() && nowAbleBioAuthDevice();
    }

    public boolean nowAbleBioAuthDevice() {
        return bioActivity.nowAbleBioAuthDevice();
    }

    public boolean didAbleBioAuthDevice() {
        if(walletList == null) return false;
//        return walletList.biometric.biometric_confirmed;
        return walletList.biometric.biometric_confirmed && SharedPreferenceHelper.getValue(context, "biometric_secret_key").length() > 0;
    }

    public void setPasswordToken(String token) {
         SharedPreferenceHelper.setValue(context, "password_token", token);
    }

    public String getPasswordToken() {
        return SharedPreferenceHelper.getValue(context, "password_token").replaceAll("\"", "");
    }

    public void initDeviceBioInfo() {
        SharedPreferenceHelper.setValue(context, "password_token", "");
    }


    //api
    public void getWalletList(boolean requestBioPay) {
        if(bioPayload == null) return;
        final BootpayBioPresenter scope = this;

        String deviceUUID = UserInfo.getInstance(context).getBootpayUuid();
        String userToken = bioPayload.getUserToken();

        Call<ResponseBody> dataCall = service.getApi().getWalletList(deviceUUID, userToken);
        dataCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if(response.isSuccessful()) {
                        walletList = new Gson().fromJson(response.body().string(), ResWalletList.class);
                        CurrentBioRequest.getInstance().wallets = walletList.wallets;

                        if(requestBioPay == true) {
                            requestBioForPay();
                            return;
                        }
//                        scope.bioActivity.setWalletList(walletList.wallets);

                        if(walletList.biometric.biometric_confirmed == false) {
                            if(walletList.wallets.size() == 0) {
                                SharedPreferenceHelper.setValue(context, "biometric_secret_key", "");
                            }
                            initDeviceBioInfo();
                        }

//                        if(walletList.wallets.size() == 0) {
//                            addNewCard();
//                        } else {
//                            showCardView(walletList.wallets);
//                        }
                        showCardView(walletList.wallets);

                    } else {
                        ResError error = new Gson().fromJson(response.body().string(), ResError.class);
                        Toast.makeText(context, error.error_code + "\n" + error.message, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                String temp = response.body().toString();
                Log.d("bootpay", "222222 " + t.getMessage());
            }
        });
    }

    public void addNewCard() {
        setRequestType(BioConstants.REQUEST_ADD_CARD);
        if(!isShowWebView()) showWebView();
//        if(!isAblePasswordToken()) {
//            requestPasswordToken(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD);
//            return;
//        }
        requestAddCard();
    }

    public void deleteCard(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setTitle("결제수단 삭제");
        builder.setMessage("선택하신 결제수단을 삭제하시겠습니까?");
        builder.setNegativeButton("취소", (dialog, which) -> {

        });
        builder.setPositiveButton("확인", (dialogInterface, i) -> {
//            selectedCardIndex = index;
            CurrentBioRequest.getInstance().selectedCardIndex = index;
            bioPayload.setWalletId(walletList.wallets.get(index).wallet_id);
            if(!isShowWebView()) showWebView();
            if(!isAblePasswordToken()) {
                requestPasswordToken(BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD);
                return;
            }
            requestDeleteCard();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
//        Log.d("delete card", "index: " + index);
    }

    public void setRequestType(int type) {
        CurrentBioRequest.getInstance().requestType = type;
//        BootpayBio.setRequestType(type);
    }

    public void requestPasswordToken(int type) {
        if(bioWebView == null) return;
        setRequestType(type);
        if(!isShowWebView()) showWebView();
        bioWebView.requestPasswordToken();
    }

    public void requestDeleteCard() {
        if(bioWebView == null) return;
        setRequestType(BioConstants.REQUEST_DELETE_CARD);
        bioWebView.requestDeleteCard(getPasswordToken(), bioPayload);
    }

    public void requestAddCard() {
        if(bioWebView == null) return;
        bioWebView.requestAddCard();
    }

    public void requestBioForPay() {
        if(bioWebView == null) return;
        if(bioPayload == null) return;
        String secretKey = SharedPreferenceHelper.getValue(context, "biometric_secret_key");
        int serverUnixTime = walletList.biometric.server_unixtime;
        String otp = getOTPValue(secretKey, serverUnixTime);
//        bioActivity.showProgressBar(true);
        if(otp != null && otp.length() > 0) bioWebView.requestBioForPay(otp, bioPayload);
    }

    public void requestPasswordForPay() {
        if(bioWebView == null) return;
        if(bioPayload == null) return;

        if(!isAblePasswordToken()) {
            requestPasswordToken(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY);
            return;
        }

        if(!isShowWebView()) showWebView();
        bioWebView.requestPasswordForPay(getPasswordToken(), bioPayload);
    }

    public void requestTotalForPay() {
        if(bioWebView == null) return;
        if(bioPayload == null) return;
        if(!isShowWebView()) showWebView();
        bioWebView.requestTotalForPay(bioPayload);
    }

    public void requestAddBioData(int type) {
        if(bioWebView == null) return;
        if(bioPayload == null) return;
        setRequestType(type);
        if(!isShowWebView()) showWebView();
        bioWebView.requestAddBioData(getPasswordToken(), bioPayload);
    }


    //bio
    public void goBiometricAuth() {
        bioActivity.goBiometricAuth();
    }

    public String getOTPValue(String secretKey, int serverTime) {
        CodeGenerator generator = new DefaultCodeGenerator(HashingAlgorithm.SHA512, 8);
        long time = Long.valueOf(serverTime) / 30;

        try {
            return generator.generate(secretKey, time);
        } catch (CodeGenerationException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onNextJob(NextJob data) {
        if(data.initToken) {
            setPasswordToken("");
            bioPayload.setToken("");
        } else if(data.token != null && data.token.length() > 0) {
            setPasswordToken(data.token.replaceAll("\"", ""));
            bioPayload.setToken(data.token);
        }

        if(data.biometric_device_uuid != null && data.biometric_device_uuid.length() > 0 &&
                data.biometric_secret_key != null && data.biometric_secret_key.length() > 0) {
            SharedPreferenceHelper.setValue(context, "biometric_device_uuid", data.biometric_device_uuid);
            SharedPreferenceHelper.setValue(context, "biometric_secret_key", data.biometric_secret_key);
        }

        bioActivity.runOnUiThread(() -> {
            if(data.nextType == BioConstants.NEXT_JOB_RETRY_PAY) {
                startPayWithSelectedCard();
            } else if(data.nextType == BioConstants.NEXT_JOB_ADD_NEW_CARD) {
                addNewCard();
            } else if(data.nextType == BioConstants.NEXT_JOB_ADD_DELETE_CARD) {
                requestDeleteCard();
            } else if(data.nextType == BioConstants.REQUEST_PASSWORD_FOR_PAY) {
               requestPasswordForPay();
            }  else if(data.nextType == BioConstants.NEXT_JOB_GET_WALLET_LIST) {
                if(data.type == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY) {
                    getWalletList(true);
                } else {
                    getWalletList(false);
                }
            }
        });
    }

    public void goClickCard(WalletData data) {
        if(data.wallet_type == 1) {
            addNewCard();
        } else if(data.wallet_type == 2) {
            requestTotalForPay();
        } else {
            CurrentBioRequest.getInstance().selectedCardIndex = walletList.wallets.indexOf(data);
            startPayWithSelectedCard();
        }
    }

    public void goDeleteCard(WalletData data) {
        deleteCard(walletList.wallets.indexOf(data));
    }

    public void goClickCurrentCard(int index) {
        if(index < walletList.wallets.size() - 2) {
            startPayWithSelectedCard();
        } else if(index == walletList.wallets.size() - 2) {
            addNewCard();
        } else {
            requestTotalForPay();
        }
    }

    private void startPayWithSelectedCard() {
        Log.d("bootpay", "selectedCardIndex: " + CurrentBioRequest.getInstance().selectedCardIndex);
        this.bioPayload.setWalletId(walletList.wallets.get(CurrentBioRequest.getInstance().selectedCardIndex).wallet_id);

        if(CurrentBioRequest.getInstance().isPasswordMode) {
            requestPasswordForPay();
            return;
        }

        setRequestType(BioConstants.REQUEST_BIO_FOR_PAY);
        if(!isAblePasswordToken()) {
            requestPasswordToken(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY);
            return;
        }
        if(isAbleBioAuthDevice()) {
            goBioForPay();
            return;
        } else if(nowAbleBioAuthDevice()) {
            //기기활성화 먼저해야함
            goBioForEnableDevice();
            return;
        }

        setRequestType(BioConstants.REQUEST_PASSWORD_FOR_PAY);
        requestPasswordForPay();
    }

    private void goBioForEnableDevice() {
        setRequestType(BioConstants.REQUEST_BIOAUTH_FOR_BIO_FOR_PAY);
        goBiometricAuth();
    }

    private void goBioForPay() {
        setRequestType(BioConstants.REQUEST_BIO_FOR_PAY);
        goBiometricAuth();
    }
}
