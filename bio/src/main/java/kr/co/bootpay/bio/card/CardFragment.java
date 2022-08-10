package kr.co.bootpay.bio.card;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import kr.co.bootpay.bio.R;
import kr.co.bootpay.bio.constants.CardCode;
import kr.co.bootpay.bio.helper.BioThemeHelper;
import kr.co.bootpay.bio.memory.CurrentBioRequest;
import kr.co.bootpay.bio.models.BioThemeData;
import kr.co.bootpay.bio.models.data.WalletData;
import kr.co.bootpay.bio.presenter.BootpayBioPresenter;

public class CardFragment extends Fragment {
//    BioWallet bioWallet;
//    BioDeviceUse user;
    Context context;
    WalletData walletData;

    LinearLayout card;
    TextView bank_name;
    ImageView card_chip;
    TextView bank_num;

    RelativeLayout icon_card_circle;
    ImageView icon_card;
    TextView text_bottom;
    BootpayBioPresenter presenter;
//    BioActivityInterface parent;
//    BootpayBioActivity parent;

    public static CardFragment newInstance(BootpayBioPresenter presenter, WalletData walletData, Context context) {
        CardFragment fragment = new CardFragment();
//        fragment.bioWallet = bioWallet;
        fragment.context = context;
        fragment.walletData = walletData;
//        fragment.user = user;
        fragment.presenter = presenter;
        return fragment;
    }

    private void updateVisible() {
        if(walletData == null) return;
        if(bank_name == null) return;
        bank_name.setVisibility(walletData.wallet_type == -1 ? View.VISIBLE : View.GONE);
        bank_num.setVisibility(walletData.wallet_type == -1 ? View.VISIBLE : View.GONE);
        card_chip.setVisibility(walletData.wallet_type == -1 ? View.VISIBLE : View.GONE);
        icon_card_circle.setVisibility(walletData.wallet_type != -1 ? View.VISIBLE : View.GONE);
        icon_card.setVisibility(walletData.wallet_type != -1 ? View.VISIBLE : View.GONE);
        text_bottom.setVisibility(walletData.wallet_type != -1 ? View.VISIBLE : View.GONE);
    }

    private void updateBackground() {
        if(walletData == null) return;
        if(card == null) return;
        if(walletData.wallet_type == -1) {
            card.setBackground(getResources().getDrawable(R.drawable.rounded_gray, null));
        } else if(walletData.wallet_type == 1) {
//            card.setBackground(getResources().getDrawable(R.drawable.card_new, null));
            text_bottom.setText("새로운 카드 등록");

            if(CurrentBioRequest.getInstance().bioThemeData.card1Color != 0) {
                card.setBackground(BioThemeHelper.getShapeRoundColor(context, CurrentBioRequest.getInstance().bioThemeData.card1Color, 6, true));
//                icon_card_circle.setBackground(BioThemeHelper.getShapeRoundColor(context, CurrentBioRequest.getInstance().bioThemeData.cardText2Color, 17.5f, true));
                icon_card.setColorFilter(CurrentBioRequest.getInstance().bioThemeData.card1Color);
            } else {
                card.setBackground(BioThemeHelper.getShapeRoundColor(context, context.getResources().getColor(R.color.white, null), 6, true));
//                icon_card_circle.setBackground(BioThemeHelper.getShapeRoundColor(context, CurrentBioRequest.getInstance().bioThemeData.cardText2Color, 17.5f, true));
//                card.setBackground(getResources().getDrawable(R.drawable.card_new, null));
                icon_card.setColorFilter(getResources().getColor(R.color.white, null));
            }

            if(CurrentBioRequest.getInstance().bioThemeData.cardText1Color != 0) {
                text_bottom.setTextColor(BioThemeHelper.getThemeColor(context, CurrentBioRequest.getInstance().bioThemeData.cardText1Color, R.color.blue));
                icon_card_circle.setBackground(BioThemeHelper.getShapeRoundColor(context, CurrentBioRequest.getInstance().bioThemeData.cardText1Color, 17.5f, true));
            } else {
                text_bottom.setTextColor(context.getResources().getColor(R.color.blue, null));
                icon_card_circle.setBackground(BioThemeHelper.getShapeRoundColor(context, context.getResources().getColor(R.color.blue, null), 17.5f, true));
            }

            icon_card.setImageResource(R.drawable.ico_plus_outline);
        } else if(walletData.wallet_type == 2) {
//            card.setBackground(getResources().getDrawable(R.drawable.card_other, null));
            text_bottom.setText("다른 결제 수단");

            if(CurrentBioRequest.getInstance().bioThemeData.card2Color != 0) {
                card.setBackground(BioThemeHelper.getShapeRoundColor(context, CurrentBioRequest.getInstance().bioThemeData.card2Color, 6, true));
            } else {
                card.setBackground(getResources().getDrawable(R.drawable.card_other, null));
            }

            if(CurrentBioRequest.getInstance().bioThemeData.cardText2Color != 0) {
                text_bottom.setTextColor(BioThemeHelper.getThemeColor(context, CurrentBioRequest.getInstance().bioThemeData.cardText2Color, R.color.white));
                icon_card_circle.setBackground(BioThemeHelper.getShapeRoundColor(context, CurrentBioRequest.getInstance().bioThemeData.cardText2Color, 17.5f, true));
            } else {
                text_bottom.setTextColor(context.getResources().getColor(R.color.white, null));
            }
            if(CurrentBioRequest.getInstance().bioThemeData.cardIconColor != 0) {
                icon_card.setColorFilter(CurrentBioRequest.getInstance().bioThemeData.cardIconColor);
            } else {
                icon_card.setColorFilter(context.getResources().getColor(R.color.blue, null));
            }
//            text_bottom.setTextColor(context.getResources().getColor(R.color.white, null));
            icon_card.setImageResource(R.drawable.ico_card_outline);
        }
    }

    private void updateCardValue() {
//        walletData.batch_data.
//        walletData.batch_data.card_company;
        if(walletData == null || walletData.batch_data == null) return;
        if(card == null) return;
        bank_name.setText(walletData.batch_data.card_company);
        bank_num.setText(walletData.batch_data.card_no);
    }

    private void updateCardStyle() {
        if(walletData == null || walletData.batch_data == null) return;
        if(card == null) return;
        bank_name.setTextColor(getResources().getColor(CardCode.getColorText(walletData.batch_data.card_company_code), null));
        bank_num.setTextColor(getResources().getColor(CardCode.getColorText(walletData.batch_data.card_company_code), null));
        card.setBackground(getResources().getDrawable(CardCode.getColorBackground(walletData.batch_data.card_company_code), null));
    }

    public void updateView() {
        updateVisible();
        updateBackground();
        if(walletData != null || walletData.wallet_type == -1) {
            updateCardValue();
            updateCardStyle();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup cardView = (ViewGroup) inflater.inflate(R.layout.layout_card, container, false);
        card = cardView.findViewById(R.id.card);
        card_chip =  cardView.findViewById(R.id.card_chip);
        bank_name = cardView.findViewById(R.id.bank_name);
        bank_num = cardView.findViewById(R.id.bank_num);
        icon_card = cardView.findViewById(R.id.icon_card);
        icon_card_circle = cardView.findViewById(R.id.icon_card_circle);
        text_bottom = cardView.findViewById(R.id.text_bottom);
        updateView();

//        bank_num.setTextColor(R.);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goClickCard(view);
            }
        });
        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                goDeleteCard(v);
                return true;
            }
        });

//        cardView.setLongClickable(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//                return false;
//            }
//        });
        return cardView;
    }

    public void goClickCard(View v) {
        presenter.goClickCard(walletData);
    }

    public void goDeleteCard(View v) {
        presenter.goDeleteCard(walletData);
    }

//    void goCardClick() {
//        if(presenter != null) presenter.startBioPay(walletData);
//    }
//
//    void goNewCard() {
//        if(presenter != null) presenter.goNewCardActivity();
//    }
//
//    void goOther() {
//        if(presenter != null) presenter.goOtherActivity();
//    }
}
