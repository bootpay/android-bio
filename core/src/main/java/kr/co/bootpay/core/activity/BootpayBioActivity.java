package kr.co.bootpay.core.activity;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.concurrent.Executor;

import kr.co.bootpay.core.R;
import kr.co.bootpay.core.constants.BioConstants;
import kr.co.bootpay.core.helper.DPHelper;
import kr.co.bootpay.core.memory.CurrentBioRequest;
import kr.co.bootpay.core.models.BioPayload;
import kr.co.bootpay.core.models.BioPrice;
import kr.co.bootpay.core.models.ResWalletList;
import kr.co.bootpay.core.models.data.CardPagerAdapter;
import kr.co.bootpay.core.models.data.CardViewPager;
import kr.co.bootpay.core.models.data.WalletData;
import kr.co.bootpay.core.presenter.BootpayBioPresenter;
import kr.co.bootpay.core.webview.BootpayBioWebView;


public class BootpayBioActivity extends FragmentActivity  {

    private Context context;
    private BootpayBioPresenter presenter;

    LinearLayout card_layout;
    LinearLayout card_actionsheet_layout;
    LinearLayout card_actionsheet_bottom_layout;
    public BootpayBioWebView bioWebView;

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
    TextView text_bottom;
    TextView order_name;
//    TextView msg;
    LinearLayout names;
    LinearLayout prices;
//    BioWalletData data;
    CardViewPager card_pager;
    CardPagerAdapter cardPagerAdapter;
    PowerSpinnerView quota_spinner;
    LinearLayout quota_layout;
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


    void initView() {
//        SharedPreferenceHelper.setValue(this, "password_token", "엄한값");

        card_layout = findViewById(R.id.card_layout);
        bioWebView = findViewById(R.id.webview);


        this.presenter = new BootpayBioPresenter(context,this, bioWebView);
        this.presenter.setBioPayload(bioPayload);

//        scrollView = findViewById(R.id.scrollView);
        pg = findViewById(R.id.pg);
        names = findViewById(R.id.names);
        prices = findViewById(R.id.prices);
        text_bottom = findViewById(R.id.text_bottom);
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
                if(CurrentBioRequest.getInstance().wallets == null) return;
                int size = CurrentBioRequest.getInstance().wallets.size();

                if(position < size - 2) {
                    text_bottom.setText("이 카드로 결제합니다");
                } else if(position == size - 2) {
                    text_bottom.setText("새로운 카드를 등록합니다");
                } else {
                    text_bottom.setText("다른 결제수단으로 결제합니다");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        final BootpayBioActivity scope = this;
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
        text.setTextColor(getResources().getColor(R.color.font_color_option, null));
        names.addView(text);
    }

    private void setPriceViews() {
        if(bioPayload == null) return;
        for(BioPrice bioPrice : bioPayload.getPrices()) {
            LinearLayout layout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            params.setMargins(0, 0, 0, DPHelper.dp2px(context.getResources(), 10
            ));
            layout.setLayoutParams(params);



            TextView left = new TextView(context);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            left.setLayoutParams(params1);
            left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            left.setText(bioPrice.getName());
            left.setTextColor(getResources().getColor(R.color.font_color_info, null));
            layout.addView(left);

            TextView right = new TextView(context);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            right.setLayoutParams(params2);
            right.setGravity(Gravity.RIGHT);
//            right.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
            right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            right.setText(getComma(bioPrice.getPrice()));
            right.setTextColor(getResources().getColor(R.color.font_color, null));
            layout.addView(right);
            prices.addView(layout);
        }

        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        params.setMargins(0, 0, 0, DPHelper.dp2px(context.getResources(), 5));
        layout.setLayoutParams(params);


        TextView left = new TextView(context);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        left.setLayoutParams(params1);
        left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        left.setText("총 결제금액");
        left.setTextColor(getResources().getColor(R.color.font_color_info, null));
        layout.addView(left);

        TextView right = new TextView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        right.setLayoutParams(params2);
        right.setGravity(Gravity.RIGHT);
//        right.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        right.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        right.setTypeface(left.getTypeface(), Typeface.BOLD);
        right.setText(getComma(bioPayload.getPrice()));
        right.setTextColor(getResources().getColor(R.color.blue, null));
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

    public void setCardPager() {
//        this.data = data;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cardPagerAdapter.setData(walletList.wallets);
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

    public void goBiometricAuth() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(BiometricManager.from(context).canAuthenticate() == BIOMETRIC_SUCCESS) {
                    biometricPrompt.authenticate(promptInfo);
                }  else {
                    Toast.makeText(context, "생체인증 정보가 등록되지 않은 기기입니다.", Toast.LENGTH_SHORT).show();
                }
            }
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


    public void setWalletList(List<WalletData> walletList) {
        if(walletList == null) return;

        if(this.walletList == null) this.walletList = new ResWalletList();
        if(this.walletList.wallets != null) this.walletList.wallets.clear();
        this.walletList.wallets = walletList;

        CurrentBioRequest.getInstance().wallets = walletList;

        setCardPager();

    }

    public void showCardView() {
        card_layout.setVisibility(View.VISIBLE);
        card_actionsheet_layout.setVisibility(View.VISIBLE);
        card_actionsheet_bottom_layout.setVisibility(View.VISIBLE);
    }

    public void hideWebView() {
        bioWebView.setVisibility(View.GONE);
    }

    public void hideCardView() {
        card_layout.setVisibility(View.GONE);
        card_actionsheet_layout.setVisibility(View.GONE);
        card_actionsheet_bottom_layout.setVisibility(View.GONE);
    }

    public void showWebView() {
        bioWebView.setVisibility(View.VISIBLE);
    }

    public boolean isShowCardView() {
        return card_layout.getVisibility() == View.VISIBLE;
    }

    public boolean isShowWebView() {
        return bioWebView.getVisibility() == View.VISIBLE;
    }

    public void transactionConfirm() {
        if(bioPayload != null) bioWebView.transactionConfirm();
    }
}
