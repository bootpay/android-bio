package kr.co.bootpay.bio.memory;

import java.util.List;

import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.bio.activity.BootpayBioActivity;
import kr.co.bootpay.bio.api.NextJobInterface;
import kr.co.bootpay.bio.constants.BioConstants;
import kr.co.bootpay.bio.models.BioDevice;
import kr.co.bootpay.bio.models.BioPayload;
import kr.co.bootpay.bio.models.BioThemeData;
import kr.co.bootpay.bio.models.data.WalletData;

public class CurrentBioRequest {
    private static CurrentBioRequest instance = null;
    public BootpayEventListener listener;
//    public Boo bioListener;
    public BootpayBioActivity activity;

    public BioPayload bioPayload;
    public BioThemeData bioThemeData;
    public int requestType = BioConstants.REQUEST_TYPE_NONE;
    public boolean isPasswordMode = false; //비밀번호 간편결제 호출인지
    public boolean isEditMode = false; //등록된 결제수단 편집 모드인지
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
        if(instance == null) {
            instance = new CurrentBioRequest();
            instance.bioThemeData = new BioThemeData();
        }
        return instance;
    }
}

