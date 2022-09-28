package kr.co.bootpay.bio.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import kr.co.bootpay.android.pref.UserInfo;
import kr.co.bootpay.bio.R;
import kr.co.bootpay.bio.activity.BioActivityInterface;
import kr.co.bootpay.bio.api.BioApiService;
import kr.co.bootpay.bio.api.WebviewEventInterface;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.helper.SharedPreferenceHelper;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioMetric;
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

public class BootpayBioPresenter implements WebviewEventInterface {

    public ResWalletList walletList = new ResWalletList();
    public BioPayload bioPayload;
    public BioActivityInterface bioActivity;
    public BootpayBioWebView bioWebView;
//    public int selectedCardIndex = -1;
    BioApiService service;
    Context context;

    public BootpayBioPresenter(Context context, BioActivityInterface activity, BootpayBioWebView webView) {
        CurrentBioRequest.getInstance().webviewEventListner = this;
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
//        if(walletList != null && walletList.size() > 0) {
//            bioActivity.setWalletList(walletList);
//        }
//        bioActivity.setWalletList();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            bioActivity.showCardView(false);
            bioActivity.hideWebView();
        });
    }

    public void showWebView() {
        if(bioActivity == null) return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            bioActivity.hideCardView();
            bioActivity.showWebView();
        });
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
//        return bioActivity.nowAbleBioAuthDevice();
        return true;
    }

    public boolean didAbleBioAuthDevice() {
//        if(walletList == null) return false;
//        return walletList.biometric.biometric_confirmed;
        String biometric_secret_key = SharedPreferenceHelper.getValue(context, "biometric_secret_key");
        return walletList.biometric.biometric_confirmed && biometric_secret_key.length() > 0;

//        return didAbleBioAuthDevice() && nowAbleBioAuthDevice();
    }

    public void setPasswordToken(String token) {
         SharedPreferenceHelper.setValue(context, "password_token", token);
    }

    public String getPasswordToken() {
        return SharedPreferenceHelper.getValue(context, "password_token").replaceAll("\"", "");
    }

    public void initDeviceBioInfo() {
        SharedPreferenceHelper.setValue(context, "biometric_secret_key", "");
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
//                        CurrentBioRequest.getInstance().wallets = walletList.wallets;
                        bioActivity.setWalletList(walletList.wallets);

                        if(requestBioPay == true) {
                            requestBioForPay();
                            return;
                        }
//                        scope.bioActivity.setWalletList(walletList.wallets);

                        if(walletList.biometric.biometric_confirmed == false) {
                            if(walletList.wallets.size() == 0) {
                                SharedPreferenceHelper.setValue(context, "biometric_secret_key", "");
                            }
                            setPasswordToken("");
//                            initDeviceBioInfo();
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
        builder.setTitle("카드 삭제");
        builder.setMessage("등록된 카드를 삭제합니다.\n정말 삭제하시겠습니까?");
        builder.setNegativeButton("닫기", (dialog, which) -> {

        });
        builder.setPositiveButton("삭제", (dialogInterface, i) -> {
//            selectedCardIndex = index;
            setPasswordToken("");
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
//            SharedPreferenceHelper.set(context, "server_unixtime", data.serverUnixtime);
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
            } else if(data.nextType == BioConstants.REQUEST_DELETE_CARD) {
                requestDeleteCard();
            } else if(data.nextType == BioConstants.NEXT_JOB_GET_WALLET_LIST) {
                if(data.type == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY) {
                    getWalletList(true);
                } else {
                    getWalletList(false);
                }
            }
        });
    }

    @Override
    public void onWebViewCancel(String data) {
        Log.d("bootpay", "onWebViewCancel : " + data);
        if(CurrentBioRequest.getInstance().listener != null) {
            CurrentBioRequest.getInstance().listener.onCancel(data);
            CurrentBioRequest.getInstance().listener.onClose();
        }
    }

    @Override
    public void onWebViewClose(String data) {
        Log.d("bootpay", "onWebViewClose : " + data + ", " + CurrentBioRequest.getInstance().requestType);
//        if(CurrentBioRequest.getInstance().listener != null) {
//            CurrentBioRequest.getInstance().listener.onCancel(data);
//            CurrentBioRequest.getInstance().listener.onClose();
//        }

        //토큰 받은 후 결제
        if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIOAUTH_FOR_BIO_FOR_PAY) {
//            bioActivity.showCardView(false);
            return;
        }

        if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIO_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_CARD ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_DELETE_CARD ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_FOR_PAY) {
            return;
        }

//        if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_FOR_PAY) {
//            NextJob job = new NextJob();
//            job.initToken = true;
//            onNextJob(job);
//        }
        if(CurrentBioRequest.getInstance().listener != null) {
            CurrentBioRequest.getInstance().listener.onClose();
        }
    }

    @Override
    public void onWebViewError(String data) {
        Log.d("bootpay", "onWebViewError : " + data);

        if(CurrentBioRequest.getInstance().listener != null) {
            CurrentBioRequest.getInstance().listener.onError(data);

            if(bioPayload != null) {
                if(bioPayload.getExtra() != null) {
                    if(bioPayload.getExtra().isDisplayErrorResult() != true) {
                        CurrentBioRequest.getInstance().listener.onClose();
                    }
                }
            }
        }
    }

    @Override
    public void onWebViewIssued(String data) {
        Log.d("bootpay", "onWebViewIssued : " + data);

        if(CurrentBioRequest.getInstance().listener != null) {
            CurrentBioRequest.getInstance().listener.onIssued(data);

            if(bioPayload != null) {
                if(bioPayload.getExtra() != null) {
                    if(bioPayload.getExtra().isDisplaySuccessResult() != true) {
                        CurrentBioRequest.getInstance().listener.onClose();
                    }
                }
            }
        }
    }

    @Override
    public void onWebViewConfirm(String data) {
        Log.d("bootpay", "onWebViewConfirm : " + data);
        goConfirmEvent(data);
    }

    @Override
    public void onWebViewDone(String data) {
        Log.d("bootpay", "onWebViewDone : " + data);
        if(CurrentBioRequest.getInstance().listener != null) {
            CurrentBioRequest.getInstance().listener.onDone(data);

            if(bioPayload != null) {
                if(bioPayload.getExtra() != null) {
                    if(bioPayload.getExtra().isDisplaySuccessResult() != true) {
                        CurrentBioRequest.getInstance().listener.onClose();
                    }
                }
            }
        }
    }

    @Override
    public void onWebViewRedirect(String data) {
        Log.d("bootpay", "onWebViewRedirect : " + data);
        if("undefined".equals(data)) return;
        try {
            JSONObject json = new JSONObject(data);
            String event = String.valueOf(json.get("event"));
            switch (event) {
                case "error":
                    onWebViewError(data);
                    break;
                case "close":
                    onWebViewClose(data);
                    break;
                case "cancel":
                    onWebViewCancel(data);
                    break;
                case "issued":
                    onWebViewIssued(data);
                    break;
                case "confirm":
                    onWebViewConfirm(data);
                    break;
                case "done":
                    onWebViewDone(data);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebViewEasySuccess(String data) {
        Log.d("bootpay", "onWebViewEasySuccess : " + data + ", " + CurrentBioRequest.getInstance().requestType);

        if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY) {

            NextJob job = new NextJob();
            job.type = CurrentBioRequest.getInstance().requestType;
            job.token = data.replaceAll("\"", "");
//            job.initToken = true;
            if(CurrentBioRequest.getInstance().requestType != BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD) {
                job.nextType = BioConstants.NEXT_JOB_RETRY_PAY;
            } else {
                job.nextType = BioConstants.REQUEST_DELETE_CARD;
            }
            onNextJob(job);
            return;
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY) {
            BioMetric bioMetric = new Gson().fromJson(data, BioMetric.class);

            NextJob job = new NextJob();
            job.type = CurrentBioRequest.getInstance().requestType;
            job.nextType = BioConstants.NEXT_JOB_GET_WALLET_LIST;
            job.biometric_device_uuid = bioMetric.biometric_device_uuid;
            job.biometric_secret_key = bioMetric.biometric_secret_key;
            job.serverUnixtime = bioMetric.server_unixtime;
            onNextJob(job);
        } else {
            if(BioConstants.REQUEST_PASSWORD_FOR_PAY == CurrentBioRequest.getInstance().requestType ||
                    BioConstants.REQUEST_ADD_CARD == CurrentBioRequest.getInstance().requestType) {
                NextJob job = new NextJob();
                job.initToken = true;
                onNextJob(job);
            }

            if(BioConstants.REQUEST_DELETE_CARD == CurrentBioRequest.getInstance().requestType ||
                    BioConstants.REQUEST_ADD_CARD == CurrentBioRequest.getInstance().requestType) {

                getWalletList(false);
//                showCardView(null);
                return;
            }

            if(bioPayload.getExtra() != null && bioPayload.getExtra().isSeparatelyConfirmedBio() == true) {
                if(CurrentBioRequest.getInstance().listener != null) {
                    CurrentBioRequest.getInstance().listener.onConfirm(data);
                }
            } else {
                if(bioPayload.getExtra() != null && !"redirect".equals(bioPayload.getExtra().getOpenType())) {
                    //redirect 가 아니고, 분리승인일 수 있음  (통합결제)
                    try {
                        JSONObject json = new JSONObject(data);
                        String event = String.valueOf(json.get("event"));
                        switch (event) {
                            case "confirm":
//                                onWebViewConfirm(data);
                                goConfirmEvent(data);
                                break;
                            case "done":
//                                onWebViewDone(data);
                                if(CurrentBioRequest.getInstance().listener != null) {
                                    CurrentBioRequest.getInstance().listener.onConfirm(data);
                                }
                                if(bioPayload.getExtra() != null && bioPayload.getExtra().isDisplaySuccessResult() == true) {
                                    if(CurrentBioRequest.getInstance().listener != null) {
                                        CurrentBioRequest.getInstance().listener.onClose();
                                    }
                                }
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(CurrentBioRequest.getInstance().listener != null) {
                    CurrentBioRequest.getInstance().listener.onDone(data);
                    CurrentBioRequest.getInstance().listener.onClose();
                }
            }
        }

    }

    @Override
    public void onWebViewEasyError(String data) {
        Log.d("bootpay", "onWebViewEasyError : " + data);

        try {
            JSONObject json = new JSONObject(data);
            String event = String.valueOf(json.get("error_code"));
            NextJob job = new NextJob();
            switch (event) {
                case "USER_BIOMETRIC_OTP_INVALID":
                    job.initToken = true;
                    onNextJob(job);
                    initDeviceBioInfo();
                    CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                    if(CurrentBioRequest.getInstance().listener != null) {
                        CurrentBioRequest.getInstance().listener.onError(data);
                    }
                    break;
                case "USER_PW_TOKEN_NOT_FOUND":
                case "USER_PW_TOKEN_EXPIRED":
                    job.initToken = true;
                    job.nextType = BioConstants.REQUEST_PASSWORD_FOR_PAY;
                    onNextJob(job);
                    break;
                default:
                    CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                    if(CurrentBioRequest.getInstance().listener != null) {
                        CurrentBioRequest.getInstance().listener.onError(data);
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if(data.wallet_id == null || data.wallet_id.isEmpty()) return;
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

//        setRequestType(BioConstants.REQUEST_BIO_FOR_PAY);
//        if(!isAblePasswordToken()) {
//            requestPasswordToken(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY);
//            return;
//        }

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

    void goConfirmEvent(String data) {
        boolean goTransaction = false;
        if(CurrentBioRequest.getInstance().listener != null) goTransaction = CurrentBioRequest.getInstance().listener.onConfirm(data);
        if(goTransaction) bioActivity.transactionConfirm();

//        if (mExtEventListener != null) mExtEventListener.onProgressShow(true);
//        boolean goTransaction = false;
//        if (mEventListener != null) goTransaction = mEventListener.onConfirm(data);
//        if(goTransaction) transactionConfirm(data);
//        return String.valueOf(goTransaction);
    }

    boolean isDisplaySuccess() {
        if(bioPayload == null) return false;
        if(bioPayload.getExtra() == null) return false;
        return bioPayload.getExtra().isDisplaySuccessResult();
    }

    boolean isDisplayError() {
        if(bioPayload == null) return false;
        if(bioPayload.getExtra() == null) return false;
        return bioPayload.getExtra().isDisplayErrorResult();
    }
}
