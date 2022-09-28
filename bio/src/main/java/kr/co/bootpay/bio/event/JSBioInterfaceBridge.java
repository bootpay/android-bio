package kr.co.bootpay.bio.event;

import android.webkit.JavascriptInterface;

import kr.co.bootpay.android.events.JSInterfaceBridge;

public interface JSBioInterfaceBridge {
//    @JavascriptInterface
//    void easyCancel(String data);
//
    @JavascriptInterface
    void easyError(String data);

    @JavascriptInterface
    void easySuccess(String data);


    @JavascriptInterface
    void error(String data);

    @JavascriptInterface
    void close(String data);

    @JavascriptInterface
    void cancel(String data);

    @JavascriptInterface
    void issued(String data);

    @JavascriptInterface
    void confirm(String data);

    @JavascriptInterface
    void done(String data);

    @JavascriptInterface
    void redirectEvent(String data);
}
