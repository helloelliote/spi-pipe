package kr.djspi.pipe01.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabAdapter extends FragmentStatePagerAdapter {

    private final int mNumOfTabs;

    public TabAdapter(FragmentManager fragmentManager, int NumOfTabs) {
        super(fragmentManager);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new InfoTab();
            case 1:
                return new SectionTab();
            case 2:
                return new PlaneTab();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
