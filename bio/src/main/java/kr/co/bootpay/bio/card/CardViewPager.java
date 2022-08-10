package kr.co.bootpay.bio.card;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import kr.co.bootpay.bio.helper.DPHelper;

public class CardViewPager extends ViewPager implements ViewPager.PageTransformer {
    private float MAX_SCALE = 0.0f;
    private int mPageMargin;
    private boolean animationEnabled=true;
    private boolean fadeEnabled=false;
    private  float fadeFactor=0.5f;


    public CardViewPager(Context context) {
        this(context, null);
    }

    public CardViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // clipping should be off on the pager for its children so that they can scale out of bounds.
        setClipChildren(false);
        setClipToPadding(false);
        // to avoid fade effect at the end of the page
        setOverScrollMode(2);
        setPageTransformer(false, this);
        setOffscreenPageLimit(3);
        mPageMargin = DPHelper.dp2px(context, 40);
//        int verticalPadding = dp2px(context.getResources(), 10);
        setPadding(mPageMargin, 0, mPageMargin, 0);
    }

//    public int dp2px(Resources resource, int dp) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resource.getDisplayMetrics());
//    }
    public void setAnimationEnabled(boolean enable) {
        this.animationEnabled = enable;
    }

    public void setFadeEnabled(boolean fadeEnabled) {
        this.fadeEnabled = fadeEnabled;
    }

    public void setFadeFactor(float fadeFactor) {
        this.fadeFactor = fadeFactor;
    }


    @Override
    public void setPageMargin(int marginPixels) {
        mPageMargin = marginPixels;
//        setPadding(mPageMargin, 0, mPageMargin, 0);
        setPadding(mPageMargin, mPageMargin, mPageMargin, mPageMargin);
    }

    @Override
    public void transformPage(View page, float position) {
        if (mPageMargin <= 0|| !animationEnabled)
            return;
        page.setPadding(mPageMargin / 3, mPageMargin / 3, mPageMargin / 3, mPageMargin / 3);

        if (MAX_SCALE == 0.0f && position > 0.0f && position < 1.0f) {
            MAX_SCALE = position;
        }
        position = position - MAX_SCALE;
        float absolutePosition = Math.abs(position);
        if (position <= -1.0f || position >= 1.0f) {
            if(fadeEnabled)
                page.setAlpha(fadeFactor);

        } else if (position == 0.0f) {
            page.setScaleX((1));
            page.setScaleY((1));
            page.setAlpha(1);
        } else {
            page.setScaleX(1 - MAX_SCALE / 3 * absolutePosition);
            page.setScaleY(1 - MAX_SCALE / 3 * absolutePosition);
            if(fadeEnabled)
                page.setAlpha( Math.max(fadeFactor, 1 - absolutePosition));
        }
    }
}
