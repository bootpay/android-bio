package kr.co.bootpay.bio;

import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
import static androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN;
import static androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.biometric.BiometricManager;

import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.bio.activity.BootpayBioActivity;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.BioThemeData;

public class BootpayBioBuilder {
    private Context mContext;

    private BioPayload mPayload;
    private String userToken;


    private BioThemeData mBioThemeData;
    private BootpayEventListener mEventListener;
//    private int requestType = BioConstants.REQUEST_TYPE_NONE;

    public BootpayBioBuilder(Context mContext) {
        this.mContext = mContext;
    }

    public BootpayBioBuilder setBioPayload(BioPayload payload) {
        this.mPayload = payload;
        return this;
    }

    public BootpayBioBuilder setUserToken(String userToken) {
        this.userToken = userToken;
        return this;
    }

    public BootpayBioBuilder setThemeData(BioThemeData themeData) {
        this.mBioThemeData = themeData;
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

    public void requestBio() {
        long current = System.currentTimeMillis();
        if (current - CurrentBioRequest.getInstance().startWindowTime > 2000) {
            CurrentBioRequest.getInstance().startWindowTime = current;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                CurrentBioRequest.getInstance().isPasswordMode = false;
                CurrentBioRequest.getInstance().isEditMode = false;
                requestBioActivity();
            });
        }
    }

    public void requestPassword() {
        long current = System.currentTimeMillis();
        if (current - CurrentBioRequest.getInstance().startWindowTime > 2000) {
            CurrentBioRequest.getInstance().startWindowTime = current;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                CurrentBioRequest.getInstance().isPasswordMode = true;
                CurrentBioRequest.getInstance().isEditMode = false;
                requestBioActivity();
            });
        }
    }

    public void requestEditPayment() {
        mPayload = new BioPayload();
        mPayload.setUserToken(userToken);

        long current = System.currentTimeMillis();
        if (current - CurrentBioRequest.getInstance().startWindowTime > 2000) {
            CurrentBioRequest.getInstance().startWindowTime = current;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                CurrentBioRequest.getInstance().isPasswordMode = false;
                CurrentBioRequest.getInstance().isEditMode = true;
                requestBioActivity();
            });
        }
    }

    void requestBioActivity() {
//        CurrentBioRequest.getInstance().bioListener = null;
        if (mEventListener != null) CurrentBioRequest.getInstance().listener = mEventListener;

        CurrentBioRequest.getInstance().bioPayload = mPayload;
        if(mBioThemeData != null) {
            CurrentBioRequest.getInstance().bioThemeData = mBioThemeData;
        }


//        CurrentBioRequest.getInstance().activity = null;

        switch (BiometricManager.from(mContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BIOMETRIC_SUCCESS:
            case BIOMETRIC_STATUS_UNKNOWN: {
                CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_TYPE_NONE;
                Intent intent = new Intent(mContext, BootpayBioActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mContext.startActivity(intent);
            }
            break;
            case BIOMETRIC_ERROR_NO_HARDWARE: {
                CurrentBioRequest.getInstance().requestType = BioConstants.REQUEST_PASSWORD_FOR_PAY;
                Intent intent = new Intent(mContext, BootpayBioActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
