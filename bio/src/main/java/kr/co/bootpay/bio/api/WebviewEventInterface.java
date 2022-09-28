package kr.co.bootpay.bio.api;

import kr.co.bootpay.bio.models.NextJob;

public interface WebviewEventInterface {
//    void transactionConfirm(String data);
//    void activityFinish();
    void onNextJob(NextJob data);
    void onWebViewCancel(String data);
    void onWebViewClose(String data);
    void onWebViewError(String data);
    void onWebViewIssued(String data);
    void onWebViewConfirm(String data);
    void onWebViewDone(String data);
    void onWebViewRedirect(String data);
    void onWebViewEasySuccess(String data);
    void onWebViewEasyError(String data);

}
