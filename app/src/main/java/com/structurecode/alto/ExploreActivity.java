package com.structurecode.alto;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.structurecode.alto.Fragments.RecommendedFragment;
import com.structurecode.alto.Fragments.TrendingFragment;

import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends BaseActivity  {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = findViewById(R.id.pager_music);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs_music);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TrendingFragment(), getString(R.string.trending));
        adapter.addFragment(new RecommendedFragment(), getString(R.string.recommendation));
        viewPager.setAdapter(adapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_explore;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.navigation_explore;
    }

    @Override
    Context getContext() {
        return ExploreActivity.this;
    }
}
