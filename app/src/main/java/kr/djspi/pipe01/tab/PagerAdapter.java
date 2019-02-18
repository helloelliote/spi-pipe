//package kr.djspi.pipe01.tab;
//
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentStatePagerAdapter;
//
//public class PagerAdapter extends FragmentStatePagerAdapter {
//
//    private final int mNumOfTabs;
//
//    public PagerAdapter(FragmentManager fragmentManager, int NumOfTabs) {
//        super(fragmentManager);
//        this.mNumOfTabs = NumOfTabs;
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//
//        switch (position) {
//            case 0:
//                return new NfcInfo();
//                break;
//            case 1:
//                return new NfcSection();
//                break;
//            case 2:
//                return new NfcPlane();
//                break;
//            case 3:
//                return new NfcPreview();
//                break;
//            default:
//                return null;
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return mNumOfTabs;
//    }
//}
