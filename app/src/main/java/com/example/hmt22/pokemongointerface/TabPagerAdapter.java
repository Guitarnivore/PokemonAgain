package com.example.hmt22.pokemongointerface;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class TabPagerAdapter extends FragmentPagerAdapter {

    int tabCount;

    public TabPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.tabCount = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("pager", "getItem: " + position);
        switch (position) {
            case 0:
                YourRaidsTabFragment tab1 = new YourRaidsTabFragment();
                return tab1;
            case 1:
                AllRaidsTabFragment tab2 = new AllRaidsTabFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}


