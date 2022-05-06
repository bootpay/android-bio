package kr.co.bootpay.core.api;

import kr.co.bootpay.core.models.NextJob;

public interface NextJobInterface {
//    void transactionConfirm(String data);
//    void activityFinish();
    void onNextJob(NextJob data);
}
