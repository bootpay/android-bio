package kr.co.bootpay.bio.activity;

import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
import static androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import kr.co.bootpay.bio.R;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.helper.BioThemeHelper;
import kr.co.bootpay.bio.helper.DPHelper;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.BioPrice;
import kr.co.bootpay.bio.models.ResWalletList;
import kr.co.bootpay.bio.card.CardPagerAdapter;
import kr.co.bootpay.bio.card.CardViewPager;
import kr.co.bootpay.bio.models.data.WalletData;
import kr.co.bootpay.bio.presenter.BootpayBioPresenter;
import kr.co.bootpay.bio.webview.BootpayBioWebView;


public class BioActivityInterface extends FragmentActivity implements kr.co.bootpay.bio.api.BioActivityInterface {

    private Context context;
    private BootpayBioPresenter presenter;

    //apply biotheme bg color
    LinearLayout layout_card_actionsheet_top;
    LinearLayout layout_card_order_name;
    LinearLayout layout_card_order_sub_names;
    LinearLayout prices;
    LinearLayout quota_layout;
    LinearLayout card_actionsheet_bottom_layout;
    View view_card_pager_top;
    View view_card_pager_bottom;
    CardViewPager card_pager;
    TextView order_name_hint;
    TextView order_name;
    ///

    LinearLayout card_layout;


    LinearLayout card_actionsheet_layout;
    public BootpayBioWebView bioWebView;
    RelativeLayout mLayoutProgress;

    // data
//    ResEasyBiometric easyBiometric;
    ResWalletList walletList;
//    ResReceiptID receiptID;
    int bioFailCount = 0;

    WalletData walletData;
    boolean isBioFailPopUp = false;

    // view
//    HorizontalScrollView scrollView;
    private BioPayload bioPayload;
    TextView pg;
    ImageView logo_image;
    TextView text_bottom;
//    TextView msg;
    LinearLayout names;
//    LinearLayout prices;
//    BioWalletData data;
    CardPagerAdapter cardPagerAdapter;
    PowerSpinnerView quota_spinner;
//    View quota_line;
//    int currentIndex = 0;

    ProgressDialog progress;

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
//            if(CurrentBioRequest.getInstance().bioListener != null) CurrentBioRequest.getInstance().bioListener.onCancel("사용자가 창을 닫았습니다");
            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onCancel("사용자가 창을 닫았습니다");
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "결제를 종료하시려면 '뒤로' 버튼을 한번 더 눌러주세요.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_bio_pay);
        overridePendingTransition(R.anim.open, R.anim.close);

        this.context = this;
        bioPayload = CurrentBioRequest.getInstance().bioPayload;;
        initProgressCircle();

        CurrentBioRequest.getInstance().isCDNLoaded = false;
        CurrentBioRequest.getInstance().activity = this;
        CurrentBioRequest.getInstance().selectedQuota = "0";
        CurrentBioRequest.getInstance().selectedCardIndex = 0;

//        CurrentBioRequest.getInstance().activity = this;

        initView();
        getWalletList();
        initBiometricAuth();
        setLogoView();
        setNameViews();
        setPriceViews();
        setQuotaValue();
    }

    void initProgressCircle() {
        progress = new ProgressDialog(context, R.style.BootpayDialogStyle);

        progress.setIndeterminate(true);
        progress.setCancelable(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent),
                    PorterDuff.Mode.SRC_IN);
            progress.setIndeterminateDrawable(drawable);
        }
    }

    void setQuotaValue() {
        if(bioPayload == null) return;
        quota_layout.setVisibility(bioPayload.getPrice() < 50000 ? View.GONE : View.VISIBLE);
//        quota_line.setVisibility(bioPayload.getPrice() < 50000 ? View.GONE : View.VISIBLE);
        if(bioPayload.getPrice() < 50000) return;
        List<String> array = getQuotaList();
        if(array == null && array.size() == 0) return;
        quota_spinner.setItems(array);
        quota_spinner.selectItemByIndex(0);
        quota_spinner.setOnSpinnerItemSelectedListener(( oldIndex, oldItem, newIndex, newText) -> {
            CurrentBioRequest.getInstance().selectedQuota = getSelectedQuota(newIndex);
//            Log.d("bootpay", "index: " + newIndex + ", newText: " + newText + ", quota: " + CurrentBioRequest.getInstance().selectedQuota);


        });
    }

    String getSelectedQuota(int index) {
        List<String> quotaList = getQuotaList();
        if(quotaList == null || quotaList.size() == 0) return "0";
        String quota = quotaList.get(index);
        if("일시불".equals(quota)) return "0";
        return quota.replaceAll("개월", "");
    }

    List<String> getQuotaList() {
        List<String> result = new ArrayList<>();
        int maxQuota = 12;
        if(bioPayload.getExtra() == null || bioPayload.getExtra().getCardQuota() == null) {}
        else { //1,2,3
            maxQuota =  Integer.parseInt(bioPayload.getExtra().getCardQuota());
        }
        for(int i = 0; i <= maxQuota; i++) {
            if(i == 0) result.add("일시불");
            else if(i == 1) continue;
            else result.add(i + "개월");
        }

        return result;
    }

    public void showProgressBar(boolean isShow) {
//        if(mLayoutProgress == null) return;
//        if(isShow == true && mLayoutProgress.VISIBLE == View.VISIBLE) return;
//        if(isShow == false && mLayoutProgress.VISIBLE == View.GONE) return;
        runOnUiThread(() -> {
            mLayoutProgress.setVisibility(isShow == true ? View.VISIBLE : View.GONE);
        });
    }


    void initView() {
//        SharedPreferenceHelper.setValue(this, "password_token", "엄한값");

        card_layout = findViewById(R.id.card_layout);
        layout_card_actionsheet_top = findViewById(R.id.layout_card_actionsheet_top);
        layout_card_order_name = findViewById(R.id.layout_card_order_name);
        layout_card_order_sub_names = findViewById(R.id.layout_card_order_sub_names);
        view_card_pager_top = findViewById(R.id.view_card_pager_top);
        view_card_pager_bottom = findViewById(R.id.view_card_pager_bottom);

        bioWebView = findViewById(R.id.webview);
        mLayoutProgress = findViewById(R.id.layout_progress);


        this.presenter = new BootpayBioPresenter(context,this, bioWebView);
        this.presenter.setBioPayload(bioPayload);

//        scrollView = findViewById(R.id.scrollView);
        pg = findViewById(R.id.pg);
        logo_image = findViewById(R.id.logo_image);
        names = findViewById(R.id.names);
        prices = findViewById(R.id.prices);
        text_bottom = findViewById(R.id.text_bottom);
        order_name_hint = findViewById(R.id.order_name_hint);
        order_name = findViewById(R.id.order_name);
//        msg = findViewById(R.id.msg);
        card_pager = findViewById(R.id.card_pager);
        card_actionsheet_layout = findViewById(R.id.card_actionsheet_layout);
        card_actionsheet_bottom_layout = findViewById(R.id.card_actionsheet_bottom_layout);
        quota_layout = findViewById(R.id.quota_layout);
        quota_spinner = findViewById(R.id.quota_spinner);
//        quota_line = findViewById(R.id.quota_line);
        cardPagerAdapter = new CardPagerAdapter(getSupportFragmentManager(), this.context);
        cardPagerAdapter.setPresenter(this.presenter);
//        cardPagerAdapter.setParent(this);
        card_pager.setAdapter(cardPagerAdapter);
        card_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("bootpay", "position: " + position);
//                if(data == null || data.wallets == null || data.wallets.card == null || data.wallets.card.size() <= position) return;
                CurrentBioRequest.getInstance().selectedCardIndex = position;
                updateButtonTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        final BioActivityInterface scope = this;
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(CurrentBioRequest.getInstance().bioListener != null) CurrentBioRequest.getInstance().bioListener.onCancel("사용자가 창을 닫았습니다");
                if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onCancel("사용자가 창을 닫았습니다");
                finish();
            }
        });
        text_bottom.setOnClickListener(v -> {
            presenter.goClickCurrentCard(CurrentBioRequest.getInstance().selectedCardIndex);
        });

        card_pager.setAnimationEnabled(true);
        card_pager.setFadeEnabled(true);
        card_pager.setFadeFactor(0.6f);


        setBioThemeBgColor();


//                text.setTextColor(getThemeColor(CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color_option));
    }

    void setBioThemeBgColor() {
        layout_card_order_name.setVisibility(CurrentBioRequest.getInstance().isEditMode == true ? View.GONE : View.VISIBLE);
        prices.setVisibility(CurrentBioRequest.getInstance().isEditMode == true ? View.GONE : View.VISIBLE);

        if(CurrentBioRequest.getInstance().bioThemeData.bgColor != 0) {
            layout_card_actionsheet_top.setBackground(BioThemeHelper.getShapeRoundColor(this.context, CurrentBioRequest.getInstance().bioThemeData.bgColor, 15, false));
            layout_card_order_name.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.bgColor, R.color.white));
            layout_card_order_sub_names.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.bgColor, R.color.white));
            prices.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.bgColor, R.color.white));
            quota_layout.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.bgColor, R.color.white));
            card_actionsheet_bottom_layout.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.bgColor, R.color.white));
        }
        if(CurrentBioRequest.getInstance().bioThemeData.cardBgColor != 0) {
            view_card_pager_top.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.cardBgColor, R.color.card_pager_bg));
            view_card_pager_bottom.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.cardBgColor, R.color.card_pager_bg));
            card_pager.setBackgroundColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.cardBgColor, R.color.card_pager_bg));
        }

        if(CurrentBioRequest.getInstance().bioThemeData.textColor != 0) {
            order_name_hint.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color_info));
            order_name.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color));
        }

        if(CurrentBioRequest.getInstance().bioThemeData.buttonBgColor != 0) {
            text_bottom.setBackground(BioThemeHelper.getShapeRoundColor(this.context, CurrentBioRequest.getInstance().bioThemeData.buttonBgColor, 6, true));
            text_bottom.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.buttonTextColor, R.color.white));
        }
    }

    void updateButtonTitle() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if(CurrentBioRequest.getInstance().wallets == null) return;
            int size = CurrentBioRequest.getInstance().wallets.size();

//            if(C)
            if(CurrentBioRequest.getInstance().isEditMode == true) {
                if(CurrentBioRequest.getInstance().selectedCardIndex < size - 1) {
                    text_bottom.setText("이 카드를 편집하기");
                } else if(CurrentBioRequest.getInstance().selectedCardIndex == size - 2) {
                    text_bottom.setText("새로운 카드를 등록하기");
                }
            } else {
                if(CurrentBioRequest.getInstance().selectedCardIndex < size - 2) {
                    text_bottom.setText("이 카드로 결제하기");
                } else if(CurrentBioRequest.getInstance().selectedCardIndex == size - 2) {
                    text_bottom.setText("새로운 카드를 등록하기");
                } else {
                    text_bottom.setText("다른 결제수단으로 결제하기");
                }
            }

        });
    }

    private void setLogoView() {
        if(CurrentBioRequest.getInstance().bioThemeData.logoImageResource != 0) {
//            text_bottom.setBackground(BioThemeHelper.getShapeRoundColor(this.context, CurrentBioRequest.getInstance().bioThemeData.buttonBgColor, 6, true));
//            text_bottom.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.buttonTextColor, R.color.white));
            logo_image.setVisibility(View.VISIBLE);
            logo_image.setImageResource(CurrentBioRequest.getInstance().bioThemeData.logoImageResource);
            pg.setVisibility(View.GONE);
        } else {
            logo_image.setVisibility(View.GONE);
            pg.setVisibility(View.VISIBLE);
        }

    }

    private void setNameViews() {
        if(bioPayload == null) return;
        if(bioPayload.getNames() == null) return;
        order_name.setText(bioPayload.getOrderName());

        TextView text = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.setMargins(0, 0, 0, DPHelper.dp2px(context.getResources(), 5));
        text.setGravity(Gravity.RIGHT);
        text.setLayoutParams(params);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        text.setText(TextUtils.join(", ", bioPayload.getNames()));

//        if(CurrentBioRequest.getInstance().bioThemeData.textColor != 0)
        text.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color_option));


        names.addView(text);
    }


    private void setPriceViews() {
        if(bioPayload == null) return;
        if(bioPayload.getPrices() == null) return;
        for(BioPrice bioPrice : bioPayload.getPrices()) {
            LinearLayout layout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            params.setMargins(0, 0, 0, DPHelper.dp2px(context, 10
            ));
            layout.setLayoutParams(params);



            TextView left = new TextView(context);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            left.setLayoutParams(params1);
            left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            left.setText(bioPrice.getName());
//            left.setTextColor(getResources().getColor(R.color.font_color_info, null));
//            left.setTextColor(getResources().getColor(R.color.font_color_info, null));
            left.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color_info));
            layout.addView(left);

            TextView right = new TextView(context);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            right.setLayoutParams(params2);
            right.setGravity(Gravity.RIGHT);
//            right.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
            right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            right.setText(getComma(bioPrice.getPrice()));
//            right.setTextColor(getResources().getColor(R.color.font_color, null));
            right.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color));
            layout.addView(right);
            prices.addView(layout);
        }

        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        params.setMargins(0, 0, 0, DPHelper.dp2px(context, 5));
        layout.setLayoutParams(params);


        TextView left = new TextView(context);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        left.setLayoutParams(params1);
        left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        left.setText("총 결제금액");
//        left.setTextColor(getResources().getColor(R.color.font_color_info, null));
        left.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.textColor, R.color.font_color_info));
        layout.addView(left);

        TextView right = new TextView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        right.setLayoutParams(params2);
        right.setGravity(Gravity.RIGHT);
//        right.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        right.setTypeface(left.getTypeface(), Typeface.BOLD);
        right.setText(getComma(bioPayload.getPrice()));
//        right.setTextColor(getResources().getColor(R.color.blue, null));
        right.setTextColor(BioThemeHelper.getThemeColor(this.context, CurrentBioRequest.getInstance().bioThemeData.priceColor, R.color.blue));
        layout.addView(right);
        prices.addView(layout);
    }

    private String getComma(double value) {
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        return myFormatter.format(value) + "원";
    }

    public void clearCardPager() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cardPagerAdapter.removeData();
                cardPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setCardPager(List<WalletData> list) {
//        this.data = data;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cardPagerAdapter.setData(list);
                cardPagerAdapter.notifyDataSetChanged();
            }
        });
    }


    void getWalletList() {
        if(bioPayload == null) return;
        presenter.getWalletList(false);
    }


    void goPopUpError(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
                builder.setMessage(msg);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if(CurrentBioRequest.getInstance().bioListener != null) CurrentBioRequest.getInstance().bioListener.onError(msg);
                        if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onError(msg);
                        finish();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    public boolean nowAbleBioAuthDevice() {
        return bioFailCount <= 3;
    }


    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    void initBiometricAuth() {
        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if(bioFailCount > 3 || errorCode != 13 ) { //9
                    if(biometricPrompt != null) biometricPrompt.cancelAuthentication();
//                    goPopUpForPasswordPay();
                }
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(10); // 0.5초간 진동

                Log.d("bootpay", "requestType: " + CurrentBioRequest.getInstance().requestType);

                //1. 기기 인증이냐
                //2. 결제냐
                showProgressBar(true);
                if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_ADD_BIOMETRIC) {
                    presenter.requestAddBioData(BioConstants.REQUEST_ADD_BIOMETRIC);
                } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIOAUTH_FOR_BIO_FOR_PAY) {
                    presenter.requestAddBioData(BioConstants.REQUEST_ADD_BIOMETRIC_FOR_PAY);
                } else if(CurrentBioRequest.getInstance().requestType == BioConstants.REQUEST_BIO_FOR_PAY) {
                    presenter.requestBioForPay();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                bioFailCount++;
                if(bioFailCount > 2) {
                    //팝업 물어보고 OK시 verify password for pay
                    if(biometricPrompt != null) biometricPrompt.cancelAuthentication();
//                    goPopUpForPasswordPay();
                } else {
                    Toast.makeText(context, "지문인식에 인식에 실패하였습니다. (" + bioFailCount + "/3)", Toast.LENGTH_SHORT).show();
                }
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setSubtitle("결제를 진행하시려면")
                .setNegativeButtonText("취소")
                .setDeviceCredentialAllowed(false)
                .build();
    }

    @Override
    public void goBiometricAuth() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if(!presenter.isAblePasswordToken()) {
                presenter.requestPasswordToken(BioConstants.REQUEST_PASSWORD_TOKEN_FOR_BIO_FOR_PAY);
                return;
            }

//            if(!presenter.isAbleBioAuthDevice()) {
//                presenter.requestPasswordForPay();
//                return;
//            }

            switch (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                case BIOMETRIC_SUCCESS: {
                    biometricPrompt.authenticate(promptInfo);
                }
                break;
                case BIOMETRIC_ERROR_NO_HARDWARE: {
                    Toast.makeText(context, "생체인증 정보가 등록되지 않은 기기입니다. 비밀번호 인증 방식으로 진행합니다.", Toast.LENGTH_SHORT).show();

                    presenter.setRequestType(BioConstants.REQUEST_PASSWORD_FOR_PAY);
                    presenter.requestPasswordForPay();
                }
                break;
                default: {
//                        Toast.makeText(context, "생체인증 정보가 등록되었는지 알 수 없는 상태입니다. 생체인증을 지원하지 않는 기기일 수 있습니다. 비밀번호 인증 방식으로 진행합니다. " + BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK), Toast.LENGTH_SHORT).show();

                    presenter.setRequestType(BioConstants.REQUEST_PASSWORD_FOR_PAY);
                    presenter.requestPasswordForPay();
                }
                break;
            }
//                if(BiometricManager.from(context).canAuthenticate() == BIOMETRIC_SUCCESS) {
//                    biometricPrompt.authenticate(promptInfo);
//                }  else {
//                    Toast.makeText(context, "생체인증 정보가 등록되지 않은 기기입니다.", Toast.LENGTH_SHORT).show();
//                }
        });
    }


    void goPopup(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
                builder.setMessage(msg);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        CurrentBioRequest.getInstance().type = BioConstants.REQUEST_TYPE_ENABLE_DEVICE;
//                        goVeiryPassword();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }


    public void setWalletList(List<WalletData> list) {
//        if(walletList == null || walletList.size() == 0) return;
//        this.walletList.wallets = CurrentBioRequest.getInstance().wallets;

        if(this.walletList == null) this.walletList = new ResWalletList();
        if(this.walletList.wallets == null) this.walletList.wallets = new ArrayList<>();
        this.walletList.wallets = list;
//        if(this.walletList.wallets != null) this.walletList.wallets.clear();
//        this.walletList.wallets = walletList;
//
//        CurrentBioRequest.getInstance().wallets = walletList;

        setCardPager(list);
        updateButtonTitle();

    }

    @Override
    public void showCardView(boolean isRefresh) {
        runOnUiThread(() -> {
            card_layout.setVisibility(View.VISIBLE);
            card_actionsheet_layout.setVisibility(View.VISIBLE);
            card_actionsheet_bottom_layout.setVisibility(View.VISIBLE);
        });
    }

    public void hideWebView() {
//        runOnUiThread(() -> {
//            bioWebView.setVisibility(View.GONE);
//        });

        bioWebView.setVisibility(View.GONE);
    }

    public void hideCardView() {
//        runOnUiThread(() -> {
//            card_layout.setVisibility(View.GONE);
//            card_actionsheet_layout.setVisibility(View.GONE);
//            card_actionsheet_bottom_layout.setVisibility(View.GONE);
//        });

        card_layout.setVisibility(View.GONE);
        card_actionsheet_layout.setVisibility(View.GONE);
        card_actionsheet_bottom_layout.setVisibility(View.GONE);
    }

    @Override
    public void showWebView() {
//        runOnUiThread(() -> {
//            bioWebView.setVisibility(View.VISIBLE);
//        });

        bioWebView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isShowCardView() {
        return card_layout.getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean isShowWebView() {
        return bioWebView.getVisibility() == View.VISIBLE;
    }

    public void transactionConfirm() {
        if(bioPayload != null) bioWebView.transactionConfirm();
    }


//    int getThemeColor(int bioThemeColor, int defaultColor) {
//        if(bioThemeColor != 0) {
//            return bioThemeColor;
//        }
//        return getResources().getColor(defaultColor, null);
//    }
//
//    float dpToPx(Context context, float dp) {
//        DisplayMetrics dm = context.getResources().getDisplayMetrics();
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
//    }
//
//    Drawable getShapeRoundColor(Context context, int color, int dp, boolean isCornerAll) {
//        ShapeAppearanceModel shapeModel;
//        ShapeAppearanceModel.Builder builder = ShapeAppearanceModel.builder();
//        if(isCornerAll) {
//            shapeModel = builder.setAllCornerSizes(dpToPx(context, dp)).build();
//        } else {
//            shapeModel = builder
//                    .setTopLeftCorner(CornerFamily.ROUNDED, dpToPx(context, dp))
//                    .setTopRightCorner(CornerFamily.ROUNDED, dpToPx(context, dp))
//                    .build();
//        }
//
//        MaterialShapeDrawable shape = new MaterialShapeDrawable(shapeModel);
//        shape.setFillColor(ColorStateList.valueOf(color));
//        return shape;
//    }
}
