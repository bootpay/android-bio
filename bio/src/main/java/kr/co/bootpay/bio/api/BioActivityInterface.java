package kr.co.bootpay.bio.api;

import kr.co.bootpay.bio.models.data.WalletData;

public interface BioActivityInterface {
//    void startBioPay(WalletData walletData);
//    void goNewCardActivity();
//    void goOtherActivity();

    void showCardView(boolean isRefresh);
    void showWebView();
    boolean isShowCardView();
    boolean isShowWebView();
    boolean nowAbleBioAuthDevice();
    void goBiometricAuth();

//    void startPayWithSelectedCard();
//    void addNewCard();
//    void requestDeleteCard();
//    void requestPasswordForPay();
//    void getWalletList(boolean requestBioPay);
}
