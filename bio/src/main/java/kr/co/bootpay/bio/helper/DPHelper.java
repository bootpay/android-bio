package kr.co.bootpay.bio.helper;

import android.content.res.Resources;
import android.util.TypedValue;

public class DPHelper {
    public static int  dp2px(Resources resource, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resource.getDisplayMetrics());
    }
}
