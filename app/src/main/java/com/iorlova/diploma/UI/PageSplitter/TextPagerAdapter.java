package com.iorlova.diploma.UI.PageSplitter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class TextPagerAdapter extends FragmentStatePagerAdapter {
    private final List<CharSequence> pageTexts;

    public TextPagerAdapter(FragmentManager fm, List<CharSequence> pageTexts) {
        super(fm);
        this.pageTexts = pageTexts;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(pageTexts.get(position));
    }

    @Override
    public int getCount() {
        return pageTexts.size();
    }
}
