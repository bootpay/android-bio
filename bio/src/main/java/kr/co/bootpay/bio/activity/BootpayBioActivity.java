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

import kr.co.bootpay.bio.R;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.helper.DPHelper;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.BioPrice;
import kr.co.bootpay.bio.models.ResWalletList;
import kr.co.bootpay.bio.models.data.CardPagerAdapter;
import kr.co.bootpay.bio.models.data.CardViewPager;
import kr.co.bootpay.bio.models.data.WalletData;
import kr.co.bootpay.bio.presenter.BootpayBioPresenter;
import kr.co.bootpay.bio.webview.BootpayBioWebView;


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
//            if(CurrentBioRequest.getInstance().bioListener != null) CurrentBioRequest.getInstance().bioListener.onCancel("???????????? ?????? ???????????????");
            if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onCancel("???????????? ?????? ???????????????");
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "????????? ?????????????????? '??????' ????????? ?????? ??? ???????????????.", Toast.LENGTH_SHORT).show();
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
        if("?????????".equals(quota)) return "0";
        return quota.replaceAll("??????", "");
    }

    List<String> getQuotaList() {
        List<String> result = new ArrayList<>();
        int maxQuota = 12;
        if(bioPayload.getExtra() == null || bioPayload.getExtra().getCardQuota() == null) {}
        else { //1,2,3
            maxQuota =  Integer.parseInt(bioPayload.getExtra().getCardQuota());
        }
        for(int i = 0; i <= maxQuota; i++) {
            if(i == 0) result.add("?????????");
            else if(i == 1) continue;
            else result.add(i + "??????");
        }

        return result;
    }


    void initView() {
//        SharedPreferenceHelper.setValue(this, "password_token", "?????????");

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
                    text_bottom.setText("??? ????????? ???????????????");
                } else if(position == size - 2) {
                    text_bottom.setText("????????? ????????? ???????????????");
                } else {
                    text_bottom.setText("?????? ?????????????????? ???????????????");
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
//                if(CurrentBioRequest.getInstance().bioListener != null) CurrentBioRequest.getInstance().bioListener.onCancel("???????????? ?????? ???????????????");
                if(CurrentBioRequest.getInstance().listener != null) CurrentBioRequest.getInstance().listener.onCancel("???????????? ?????? ???????????????");
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
        left.setText("??? ????????????");
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
        return myFormatter.format(value) + "???";
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
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                vibrator.vibrate(10); // 0.5?????? ??????

                Log.d("bootpay", "requestType: " + CurrentBioRequest.getInstance().requestType);

                //1. ?????? ????????????
                //2. ?????????
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
                    //?????? ???????????? OK??? verify password for pay
                    if(biometricPrompt != null) biometricPrompt.cancelAuthentication();
//                    goPopUpForPasswordPay();
                } else {
                    Toast.makeText(context, "??????????????? ????????? ?????????????????????. (" + bioFailCount + "/3)", Toast.LENGTH_SHORT).show();
                }
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("?????? ??????")
                .setSubtitle("????????? ??????????????????")
                .setNegativeButtonText("??????")
                .setDeviceCredentialAllowed(false)
                .build();
    }

    public void goBiometricAuth() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                switch (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                    case BIOMETRIC_SUCCESS: {
                        biometricPrompt.authenticate(promptInfo);
                    }
                    break;
                    case BIOMETRIC_ERROR_NO_HARDWARE: {
                        Toast.makeText(context, "???????????? ????????? ???????????? ?????? ???????????????. ???????????? ?????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();

                        presenter.setRequestType(BioConstants.REQUEST_PASSWORD_FOR_PAY);
                        presenter.requestPasswordForPay();
                    }
                    break;
                    default: {
                        Toast.makeText(context, "???????????? ????????? ???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                    }
                    break;


                }
//                if(BiometricManager.from(context).canAuthenticate() == BIOMETRIC_SUCCESS) {
//                    biometricPrompt.authenticate(promptInfo);
//                }  else {
//                    Toast.makeText(context, "???????????? ????????? ???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
//                }
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
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        CurrentBioRequest.getInstance().type = BioConstants.REQUEST_TYPE_ENABLE_DEVICE;
//                        goVeiryPassword();
                    }
                }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
