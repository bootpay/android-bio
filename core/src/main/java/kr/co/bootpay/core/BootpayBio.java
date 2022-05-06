package kr.co.bootpay.core;

import android.content.Context;


public class BootpayBio {
    protected  static BootpayBioBuilder builder;

    public static BootpayBioBuilder init(Context context) {
        return builder = new BootpayBioBuilder(context);
    }

    public static void removePaymentWindow() {
        if (builder != null) builder.removePaymentWindow();
    }

    public static void transactionConfirm(String data) {
        if (builder != null) builder.transactionConfirm(data);
    }
}
