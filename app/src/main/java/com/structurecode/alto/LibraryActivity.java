package com.structurecode.alto;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class LibraryActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_library;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.navigation_library;
    }
}
