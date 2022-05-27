package kr.co.bootpay.core.memory;

import java.util.List;

import kr.co.bootpay.core.activity.BootpayBioActivity;
import kr.co.bootpay.core.api.NextJobInterface;
import kr.co.bootpay.core.constants.BioConstants;
import kr.co.bootpay.core.models.BioDevice;
import kr.co.bootpay.core.models.BioPayload;
import kr.co.bootpay.core.models.data.WalletData;
import kr.co.bootpay.android.events.BootpayEventListener;

public class CurrentBioRequest {
    private static CurrentBioRequest instance = null;
    public BootpayEventListener listener;
//    public Boo bioListener;
    public BootpayBioActivity activity;

    public BioPayload bioPayload;
    public int requestType = BioConstants.REQUEST_TYPE_NONE;
    public boolean isPasswordMode = false; //비밀번호 간편결제 호출인지
    public String token;
    public String otp;
    public boolean isCDNLoaded = false;
//    public BootpayBioWebView webView;
//    public FragmentActivity activity;

    public long startWindowTime = System.currentTimeMillis() - 3000;

    public BioDevice bioDevice; // 이 기기에서 생체인증 정보가 활성화 되었는지
    public List<WalletData> wallets; //내 지갑 리스트
    public NextJobInterface nextJobListener;
    public int selectedCardIndex = 0;
    public String selectedQuota = "0";

    public static CurrentBioRequest getInstance() {
        if(instance == null) instance = new CurrentBioRequest();
        return instance;
    }
}

