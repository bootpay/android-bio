package kr.co.bootpay.bio.api;

import kr.co.bootpay.bio.models.data.WalletData;

public interface BioActivityInterface {
    void startBioPay(WalletData walletData);
    void goNewCardActivity();
    void goOtherActivity();
}
