package kr.co.bootpay.bio.event;

import android.webkit.JavascriptInterface;

import kr.co.bootpay.android.events.JSInterfaceBridge;

public interface JSBioInterfaceBridge extends JSInterfaceBridge {
//    @JavascriptInterface
//    void easyCancel(String data);
//
    @JavascriptInterface
    void easyError(String data);

    @JavascriptInterface
    void easySuccess(String data);
}
