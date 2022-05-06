package kr.co.bootpay.core;

import static androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.biometric.BiometricManager;

import kr.co.bootpay.core.activity.BootpayBioActivity;
import kr.co.bootpay.core.constants.BioConstants;
import kr.co.bootpay.core.memory.CurrentBioRequest;
import kr.co.bootpay.core.models.BioPayload;
import kr.co.bootpay.core.events.BootpayEventListener;

public class BootpayBioBuilder {
    private Context mContext;

    private BioPayload mPayload;
    private BootpayEventListener mEventListener;
//    private int requestType = BioConstants.REQUEST_TYPE_NONE;

    public BootpayBioBuilder(Context mContext) {
        this.mContext = mContext;
    }

    public BootpayBioBuilder setBioPayload(BioPayload payload) {
        this.mPayload = payload;
        return this;
    }

//    public void setRequestType(int type) {
//        this.requestType = type;
//    }

//    public int getRequestType() {
//        return this.requestType;
//    }

    public BootpayBioBuilder setEventListener(BootpayEventListener listener) {
        this.mEventListener = listener;
        CurrentBioRequest.getInstance().listener = listener;
        return this;
    }

    public void requestPayment() {
        long current = System.currentTimeMillis();
        if (current - CurrentBioRequest.getInstance().startWindowTime > 2000) {
            CurrentBioRequest.getInstance().startWindowTime = current;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    requestBioActivity();
                }
            });
        }
    }

    void requestBioActivity() {
//        CurrentBioRequest.getInstance().bioListener = null;
        if (mEventListener != null) CurrentBioRequest.getInstance().listener = mEventListener;

        CurrentBioRequest.getInstance().bioPayload = mPayload;
//        CurrentBioRequest.getInstance().activity = null;

        switch (BiometricManager.from(mContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BIOMETRIC_SUCCESS: {
                CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                Intent intent = new Intent(mContext, BootpayBioActivity.class);
                mContext.startActivity(intent);
            }
            break;
            default: {
                Log.d("bootpay", "status: " + BiometricManager.from(mContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK));
//                CurrentBioRequest.getInstance().type = BioConstants.REQUEST_TYPE_PASSWORD_PAY;
//                Intent intent = new Intent(mContext, BootpayBioWebviewActivity.class);
//                mContext.startActivity(intent);
            }
            break;
        }

    }

    public void transactionConfirm(String data) {
        if (CurrentBioRequest.getInstance().activity != null)
            CurrentBioRequest.getInstance().activity.transactionConfirm();
    }

    public void removePaymentWindow() {
        if(CurrentBioRequest.getInstance().activity != null) CurrentBioRequest.getInstance().activity.finish();
        CurrentBioRequest.getInstance().activity = null;
    }
}
