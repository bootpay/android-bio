package kr.co.bootpay.bio.helper;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DPHelper {
    public static int  dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
