package kr.co.bootpay.bio.card;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import kr.co.bootpay.bio.models.data.WalletData;
import kr.co.bootpay.bio.presenter.BootpayBioPresenter;

public class CardPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;
//    private BioWalletData data;
    private List<WalletData> data;
    private BootpayBioPresenter presenter;
//    private BioActivityInterface parent;

    public CardPagerAdapter(FragmentManager fm, Context context) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
    }

    public void removeData() {
        if(data != null) data.clear();
//        data = new BioWalletData()

    }

    public void setData(List<WalletData> data) {
        if(this.data != null) this.data.clear();
        this.data = data;
        if(this.data == null) return;
//        if(this.data.wallets == null) return;
        addWallet(1);
        addWallet(2);
    }

    void addWallet(int walletType) {
        WalletData walletData = new WalletData();
        walletData.wallet_type = walletType;
        data.add(walletData);

//        BioWallet bioWallet = new BioWallet();
//        bioWallet.wallet_type = walletType;
//        data.wallets.card.add(bioWallet);
    }

    public void setPresenter(BootpayBioPresenter presenter) {
        this.presenter = presenter;
    }
//    public void setParent(BioActivityInterface parent) {
//        this.parent = parent;
//    }

    @Override
    public Fragment getItem(int position) {
//        CardFragment obj = CardFragment.newInstance(parent, data.user, data.wallets.card.get(position), context);
        CardFragment obj = CardFragment.newInstance(presenter, data.get(position), context);
        obj.updateView();
        return obj;
    }

    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        if(data == null) return 0;
//        if(data.wallets == null) return 0;
//        if(data.wallets.card == null) return 0;
        return data.size();
    }
}
