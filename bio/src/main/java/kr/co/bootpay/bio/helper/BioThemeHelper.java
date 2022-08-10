package kr.co.bootpay.bio.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class BioThemeHelper {
    public static int getThemeColor(Context context, int bioThemeColor, int defaultColor) {
        if(bioThemeColor != 0) {
            return bioThemeColor;
        }
        return context.getResources().getColor(defaultColor, null);
    }

    public static Drawable getShapeRoundColor(Context context, int color, float dp, boolean isCornerAll) {
        ShapeAppearanceModel shapeModel;
        ShapeAppearanceModel.Builder builder = ShapeAppearanceModel.builder();
        if(isCornerAll) {
            shapeModel = builder.setAllCornerSizes(DPHelper.dp2px(context, dp)).build();
        } else {
            shapeModel = builder
                    .setTopLeftCorner(CornerFamily.ROUNDED, DPHelper.dp2px(context, dp))
                    .setTopRightCorner(CornerFamily.ROUNDED, DPHelper.dp2px(context, dp))
                    .build();
        }

        MaterialShapeDrawable shape = new MaterialShapeDrawable(shapeModel);
        shape.setFillColor(ColorStateList.valueOf(color));
        return shape;
    }
//    public static int  dp2px(Resources resource, int dp) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resource.getDisplayMetrics());
//    }
}
