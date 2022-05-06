package kr.co.bootpay.core.api;

import kr.co.bootpay.core.models.data.WalletData;

public interface BioActivityInterface {
    void startBioPay(WalletData walletData);
    void goNewCardActivity();
    void goOtherActivity();
}
