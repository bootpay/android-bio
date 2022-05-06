package kr.co.bootpay.core.models.data;

public class WalletData {
    public String wallet_id;
    public int type;
    public int sandbox;
    public WalletBatchData batch_data;
    public String card_code;
    public int wallet_type = -1; // 1: new, 2: other

    public String getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(String wallet_id) {
        this.wallet_id = wallet_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSandbox() {
        return sandbox;
    }

    public void setSandbox(int sandbox) {
        this.sandbox = sandbox;
    }

    public WalletBatchData getBatch_data() {
        return batch_data;
    }

    public void setBatch_data(WalletBatchData batch_data) {
        this.batch_data = batch_data;
    }

    public String getCard_code() {
        return card_code;
    }

    public void setCard_code(String card_code) {
        this.card_code = card_code;
    }

    public int getWallet_type() {
        return wallet_type;
    }

    public void setWallet_type(int wallet_type) {
        this.wallet_type = wallet_type;
    }
}
