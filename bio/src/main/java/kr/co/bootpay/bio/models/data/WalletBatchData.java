package kr.co.bootpay.bio.models.data;

public class WalletBatchData {
    public String card_no;
    public String card_company;
    public String card_company_code;
    public String card_hash;
    public String card_type;

    public String getCard_no() {
        return card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }

    public String getCard_company() {
        return card_company;
    }

    public void setCard_company(String card_company) {
        this.card_company = card_company;
    }

    public String getCard_company_code() {
        return card_company_code;
    }

    public void setCard_company_code(String card_company_code) {
        this.card_company_code = card_company_code;
    }

    public String getCard_hash() {
        return card_hash;
    }

    public void setCard_hash(String card_hash) {
        this.card_hash = card_hash;
    }

    public String getCard_type() {
        return card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }
}
