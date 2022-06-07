package kr.co.bootpay.bio.api;

import kr.co.bootpay.bio.models.NextJob;

public interface NextJobInterface {
//    void transactionConfirm(String data);
//    void activityFinish();
    void onNextJob(NextJob data);
}
