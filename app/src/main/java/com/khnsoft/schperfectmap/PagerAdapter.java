package com.khnsoft.schperfectmap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                UserFragment tab1 = new UserFragment();
                return tab1;
            case 1:
                AdminFragment tab2 = new AdminFragment();
                return tab2;
            case 2:
                SettingFragment tab3 = new SettingFragment();
                return tab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
