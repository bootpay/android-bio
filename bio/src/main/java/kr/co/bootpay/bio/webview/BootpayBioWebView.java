package kr.co.bootpay.bio.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import kr.co.bootpay.android.api.BootpayInterface;
import kr.co.bootpay.android.constants.BootpayBuildConfig;
import kr.co.bootpay.android.constants.BootpayConstant;
import kr.co.bootpay.android.pref.UserInfo;
import kr.co.bootpay.bio.BootpayBio;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.event.JSBioInterfaceBridge;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.NextJob;
import kr.co.bootpay.bio.models.ResBiometric;


public class BootpayBioWebView extends WebView implements BootpayInterface {
//    BootpayDialog mDialog;
//    BootpayDialogX mDialogX;

    BootpayBioWebViewClient mWebViewClient;
//    BioEventListener mEventListener;
//    BootpayBio

//    protected @Nullable
//    String injectedJS;
    BioPayload payload;

    protected @Nullable
    List<String> injectedJSBeforePayStart;

    public BootpayBioWebView(@NonNull Context context) {
        super(context);
        constructInit(context);
    }

    public BootpayBioWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        constructInit(context);
    }

    public BootpayBioWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructInit(context);
    }

    void constructInit(Context context) {
        payWebSettings(context);
        mWebViewClient = new BootpayBioWebViewClient(injectedJSBeforePayStart);
        setWebViewClient(mWebViewClient);
        setWebChromeClient(new BootpayBioWebViewChromeClient(context));
        constructBioInit(context);
    }

    void constructBioInit(Context context) {
        payload = CurrentBioRequest.getInstance().bioPayload;
//        mEventListener = CurrentBioRequest.getInstance().bioListener;
//        boolean quickPopup = false;
//        if(payload != null && payload.getExtra() != null && payload.getExtra().getPopup() == 1) quickPopup = true;
        String uuid = UserInfo.getInstance(context).getBootpayUuid();
        injectedJSBeforePayStart = BioConstants.getJSBeforePayStart(uuid);
    }

    public void setInjectedJSBeforePayStart(@Nullable List<String> injectedJSBeforePayStart) {
        this.injectedJSBeforePayStart = injectedJSBeforePayStart;
    }

    @SuppressLint("JavascriptInterface")
    void payWebSettings(Context context) {
        addJavascriptInterface(new BootpayJavascriptBridge(), BootpayBuildConfig.JSInterfaceBridgeName);
        getSettings().setAppCacheEnabled(true);
        getSettings().setAllowFileAccess(false);
        getSettings().setAllowContentAccess(false);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        getSettings().setDomStorageEnabled(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setUseWideViewPort(true);
        getSettings().setSupportMultipleWindows(true);

        if (BootpayBuildConfig.DEBUG == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.getApplicationInfo().flags &=  context.getApplicationInfo().FLAG_DEBUGGABLE;
            if (0 != context.getApplicationInfo().flags)  setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getSettings().setAllowFileAccessFromFileURLs(false);
            getSettings().setAllowUniversalAccessFromFileURLs(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }
    }

    @Override
    public void removePaymentWindow() {
        load("Bootpay.removePaymentWindow();");
//        if(mDialog != null) mDialog.removePaymentWindow();
//        else if(mDialogX != null) mDialogX.removePaymentWindow();
    }

    public void startBootpay() {
        connectBootpay();
    }

    public void requestPasswordToken() {
        Log.d("bootpay", "requestPasswordToken");
        requestScript();
    }

    public void requestDeleteCard(String token, BioPayload payload) {
        Log.d("bootpay", "requestDeleteCard");
        payload.setToken(token);
        this.payload = payload;
        CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_DELETE_CARD;
        requestScript();
    }

    public void requestAddCard() {
        Log.d("bootpay", "requestAddCard");
        CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_ADD_CARD;
        requestScript();
    }

    public void requestBioForPay(String otp, BioPayload payload) {
        Log.d("bootpay", "requestBioForPay");
        CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_BIO_FOR_PAY;
        payload.setToken(otp);
        this.payload = payload;
        requestScript();
    }


    public void stopProgressDialog() {
        CurrentBioRequest.getInstance().activity.showProgressBar(false);
    }

    public void requestPasswordForPay(String token, BioPayload payload) {
//        Log.d("bootpay", "requestPasswordForPay");
        CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_PASSWORD_FOR_PAY;
        payload.setToken(token);
        this.payload = payload;
        requestScript();
    }

    public void requestTotalForPay(BioPayload payload) {
        Log.d("bootpay", "requestTotalForPay");
        this.payload = payload;
        CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TOTAL_PAY;
        payload.setUserToken("");
        requestScript();

    }

    public void requestAddBioData(String token, BioPayload payload) {
        Log.d("bootpay", "requestAddBioData");
        payload.setToken(token);
        this.payload = payload;
        requestScript();
    }

    void requestScript() {
        Log.d("bootpay", "requestType: " + CurrentBioRequest.getInstance().requestType);

        if(CurrentBioRequest.getInstance().isCDNLoaded == false) {
            connectBootpay();
        } else {
            callInjectedJavaScript();
        }

//        this.is
    }

    private class BootpayJavascriptBridge implements JSBioInterfaceBridge {
        @JavascriptInterface
        @Override
        public void error(String data) {
            stopProgressDialog();
            //TODO - Password Token init
//            if (mEventListener != null) mEventListener.onError(data);
            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onError(data);

            if(CurrentBioRequest.getInstance().bioPayload.getExtra().isDisplayErrorResult() != true) {
                if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onClose(data);

            }
        }

        @JavascriptInterface
        @Override
        public void close(String data) {
            stopProgressDialog();
            Log.d("bootpay 1", "close 2: " + data);

            if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD ||
            CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY ||
            CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY ||
            CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD ||
            CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_CARD ||
//            CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIO_FOR_PAY ||
            CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY) {
//                NextJob job = getNextJob(data);
//                if (CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);

                NextJob job = getNextJob(data);
                if(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY == CurrentBioRequest.getInstance().requestType) {
                    job.nextType = BioConstants.NEXT_JOB_RETRY_PAY;
                } else if(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD == CurrentBioRequest.getInstance().requestType) {
                    job.nextType = BioConstants.NEXT_JOB_ADD_NEW_CARD;
                } else if(BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD == CurrentBioRequest.getInstance().requestType) {
                    job.nextType = BioConstants.NEXT_JOB_ADD_DELETE_CARD;
                } else if(BioConstants.REQUEST_ADD_CARD == CurrentBioRequest.getInstance().requestType) {
                    job.nextType = BioConstants.NEXT_JOB_GET_WALLET_LIST;
                } else if(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY == CurrentBioRequest.getInstance().requestType) {
                    job.nextType = BioConstants.REQUEST_PASSWORD_FOR_PAY;

                    try {
                        JSONObject json = null;
                        json = new JSONObject(data);
                        if(!"결제창이 닫혔습니다".equals( json.get("message"))) {
                            job.token = json.getString("message");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);

            } else {
//                NextJob job = new NextJob();
//                job.initToken = true;
//                if (CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);

                if(BioConstants.REQUEST_BIO_FOR_PAY != CurrentBioRequest.getInstance().requestType) {
                    if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onClose(data);
                }
            }
        }

        @JavascriptInterface
        @Override
        public void cancel(String data) {
            stopProgressDialog();

            Log.d("bootpay", "cancel: " + data);

            CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onCancel(data);
        }

        @Override
        public void issued(String data) {
            stopProgressDialog();
            Log.d("bootpay", "issued: " + data);

            CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onIssued(data);
        }
//
//        @JavascriptInterface
//        @Override
//        public void ready(String data) {
//            Log.d("bootpay", "ready: " + data);
////            if (mEventListener != null) mEventListener.onReady(data);
//            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onReady(data);
//        }

        @JavascriptInterface
        @Override
        public String confirm(String data) {
            stopProgressDialog();
            Log.d("bootpay", "confirm: " + data);

            boolean goTransaction = false;
//            if (mEventListener != null) goTransaction = mEventListener.onConfirm(data);
            if(CurrentBioRequest.getInstance().listener != null) goTransaction = CurrentBioRequest.getInstance().listener.onConfirm(data);
            if(goTransaction) BootpayBio.transactionConfirm(data);
            return String.valueOf(goTransaction);
        }

        @JavascriptInterface
        @Override
        public void done(String data) {
            stopProgressDialog();
            Log.d("bootpay", "done: " + data);
//            NextJob job = new NextJob();
//            job.initToken = true;
//            if (CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);
            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onDone(data);
        }

        @JavascriptInterface
        @Override
        public void redirectEvent(String data) {
            Log.d("bootpay", "redirectEvent: " + data);
            if("undefined".equals(data)) return;
            try {
                JSONObject json = new JSONObject(data);
                if(json.get("event") == null) return;
                String event = String.valueOf(json.get("event"));
                switch (event) {
                    case "error":
                        error(data);
                        if(!isDisplayError()) close(data);
                        break;
                    case "close":
                        close(data);
                        break;
                    case "cancel":
                        cancel(data);
                        close(data);
                        break;
                    case "issued":
                        issued(data);
                        if(!isDisplaySuccess()) close(data);
                        break;
                    case "confirm":
                        confirm(data);
                        break;
                    case "done":
                        done(data);
                        if(!isDisplaySuccess()) close(data);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        boolean isDisplaySuccess() {
            if(payload == null) return false;
            if(payload.getExtra() == null) return false;
            return payload.getExtra().isDisplaySuccessResult();
        }

        boolean isDisplayError() {
            if(payload == null) return false;
            if(payload.getExtra() == null) return false;
            return payload.getExtra().isDisplayErrorResult();
        }

//        @JavascriptInterface
//        @Override
//        public void easyCancel(String data) {
//            if (mEventListener != null) mEventListener.onEasyCancel(data);
//
//        }
//
//        @JavascriptInterface
//        @Override
//        public void easyError(String data) {
//            if (mEventListener != null) mEventListener.onEasyError(data);
//        }

        @JavascriptInterface
        @Override
        public void easyError(String data) {
            Log.d("bootpay", "easyError: " + data);


            try {
                JSONObject json = new JSONObject(data);
                if(json.get("error_code") == null) return;
                String error_code = String.valueOf(json.get("error_code"));
                if("USER_PW_TOKEN_NOT_FOUND".equals(error_code) || "USER_PW_TOKEN_EXPIRED".equals(error_code)) {

                    NextJob job = new NextJob();
                    job.initToken = true;
                    job.nextType = BioConstants.REQUEST_PASSWORD_FOR_PAY;
                    if(CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);
                    return;
                }
//                else if("PASSWORD_TOKEN_STOP".equals(error_code) || "PASSWORD_TOKEN_STOP".equals(error_code)) {
//
//                }
                else {
                    CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                    if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onError(data);


//                    NextJob job = new NextJob();
//                    job.initToken = true;
//                    if (CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);
//
//                    CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
//                    if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onError(data);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @JavascriptInterface
        @Override
        public void easySuccess(String data) {
            Log.d("bootpay", "easySuccess: " + data);
            if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN ||
                    CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD ||
                    CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD ||
                    CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY ||
                    CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY
            ) {
                NextJob job = new NextJob();
                job.type = CurrentBioRequest.getInstance().requestType;
                job.token = data.replaceAll("\"", "");
                if(CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);
            } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY) {
                ResBiometric biometric = new Gson().fromJson(data, ResBiometric.class);

                NextJob job = new NextJob();
                job.type = CurrentBioRequest.getInstance().requestType;
                job.nextType = BioConstants.NEXT_JOB_GET_WALLET_LIST;
                job.biometric_secret_key = biometric.biometric_secret_key;
                job.biometric_device_uuid = biometric.biometric_device_uuid;

                if(CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);
            } else {
                if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_FOR_PAY) {
                    NextJob job = new NextJob();
                    job.initToken = true;
                    if(CurrentBioRequest.getInstance().nextJobListener != null) CurrentBioRequest.getInstance().nextJobListener.onNextJob(job);
                }
                if(CurrentBioRequest.getInstance().requestType != BioConstants.REQUEST_ADD_CARD) {
                    CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                }
//                CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onDone(data);
//                if (mEventListener != null) mEventListener.onEasySuccess(data);
            }


        }
    }

    @NonNull
    private NextJob getNextJob(String data) {
        NextJob job = new NextJob();
        job.type = CurrentBioRequest.getInstance().requestType;

        if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY ||
//                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIO_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY ) {
            job.nextType = BioConstants.NEXT_JOB_RETRY_PAY;
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD) {
            job.nextType = BioConstants.NEXT_JOB_ADD_NEW_CARD;
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD) {
            job.nextType = BioConstants.NEXT_JOB_ADD_DELETE_CARD;
        } else if (CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_CARD) {
            job.nextType = BioConstants.NEXT_JOB_GET_WALLET_LIST;
        } else if (CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY) {
            job.nextType = BioConstants.REQUEST_PASSWORD_FOR_PAY;
            if(!"결제창이 닫혔습니다".equals(data)) {
                job.token = data;
            }
        }
        return job;
    }

    public void connectBootpay() {
        loadUrl( BioConstants.CDN_URL);
    }


    public void backPressed() {
        if(canGoBack()) goBack();
//        else if(mDialog != null) mDialog.dismiss();
//        else if(mDialogX != null) mDialogX.dismiss();
    }

    void evaluateJavascriptWithFallback(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script, null);
            return;
        }

        try {
            loadUrl("javascript:" + URLEncoder.encode(script, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should always be supported
            throw new RuntimeException(e);
        }
    }

    public void callJavaScript(@Nullable String script) {
        if (getSettings().getJavaScriptEnabled() &&
                script != null &&
                !TextUtils.isEmpty(script)) {
            Log.d("bootpay", script);

//            runon
            evaluateJavascriptWithFallback(script);
//            evaluateJavascriptWithFallback("(function() {\n" + script + ";\n})();");
        }
    }

//    public void setEventListener(BioEventListener listener) {
//        this.mEventListener = listener;
//    }

    public void callInjectedJavaScript() {
        if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_ADD_CARD ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_FOR_PASSWORD_FOR_PAY ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_TOKEN_DELETE_CARD) {
            callJavaScript(BioConstants.getJSPasswordToken(payload));
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_PASSWORD_FOR_PAY) {
            callJavaScript(BioConstants.getJSPasswordPay(getContext(), payload));
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_CARD) {
            callJavaScript(BioConstants.getJSAddCard(payload));
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIO_FOR_PAY) {
            callJavaScript(BioConstants.getJSBioOTPPay(payload));
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC ||
                CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY) {
            callJavaScript(BioConstants.getJSBiometricAuthenticate(payload));
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_TOTAL_PAY) {
            callJavaScript(BioConstants.getJSTotalPay(payload));
        } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_DELETE_CARD) {
            callJavaScript(BioConstants.getJSDestroyWallet(payload));
        }
    }

    public void callInjectedJavaScriptBeforePayStart() {
        if(injectedJSBeforePayStart == null) return;
        for(String js : injectedJSBeforePayStart) {
            callJavaScript(js);
        }
    }

    public void setIgnoreErrFailedForThisURL(@Nullable String url) {
        if(mWebViewClient != null) mWebViewClient.setIgnoreErrFailedForThisURL(url);
    }


//    public BootpayEventListener getEventListener() {
//        return mEventListener;
//    }

//    @Nullable
//    public String getInjectedJS() {
//        return injectedJS;
//    }

    @Nullable
    public List<String> getInjectedJSBeforePayStart() {
        return injectedJSBeforePayStart;
    }


    public void transactionConfirm() {
        String scriptList = BootpayConstant.loadParams(
//                "var data = JSON.parse('" + data + "');",
                "Bootpay.confirm()",
                ".then( function (res) {",
                BootpayConstant.confirm(),
                BootpayConstant.issued(),
                BootpayConstant.done(),
                "}, function (res) {",
                BootpayConstant.error(),
                BootpayConstant.cancel(),
                "})"
        );

        load(scriptList);
//        load("var data = JSON.parse('" + data + "'); BootPay.transactionConfirm(data);");
    }


    private void load(String script) {
        post(() -> loadUrl(String.format(Locale.KOREA, "javascript:(function(){%s})()", script)));
    }
}

