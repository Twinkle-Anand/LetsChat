package com.example.twinkleanand.whatsapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Twinkle Anand on 11/22/2017.
 */

class SectionPagerAdapter extends FragmentPagerAdapter {
    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:{
                RequestFragment fragment = new RequestFragment();
                return fragment;
            }
            case 1:
            {
                ChatFragment fragment = new ChatFragment();
                return fragment;
            }
            case 2:
            {
                FriendsFragment fragment = new FriendsFragment();
                return fragment;
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2 :
                return "FRIENDS";

             default:
                 return null;
        }
    }
}
