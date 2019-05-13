package kr.djspi.pipe01.tab;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabAdapter extends FragmentStatePagerAdapter {

    private final int mNumOfTabs;

    public TabAdapter(FragmentManager fragmentManager, int NumOfTabs) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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
            case 3:
                return new PreviewTab();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
