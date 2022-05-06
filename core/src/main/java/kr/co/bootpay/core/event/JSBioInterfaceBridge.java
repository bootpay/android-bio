package kr.co.bootpay.core.event;

import android.webkit.JavascriptInterface;

import kr.co.bootpay.core.events.JSInterfaceBridge;

public interface JSBioInterfaceBridge extends JSInterfaceBridge {
//    @JavascriptInterface
//    void easyCancel(String data);
//
    @JavascriptInterface
    void easyError(String data);

    @JavascriptInterface
    void easySuccess(String data);
}
