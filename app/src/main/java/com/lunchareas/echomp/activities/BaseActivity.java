/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lunchareas.echomp.activities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.utils.Constants;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    protected int id;
    protected long currSong;
    protected Toolbar toolbar;
    protected DrawerLayout menuDrawer;
    protected NavigationView navView;
    protected ActionBarDrawerToggle toggle;

    public BaseActivity(int id) {
        this.id = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(Constants.FONT_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(id);

        // Setup the toolbar
        initToolbar();
    }

    protected void initToolbar() {

        // Init toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set menu
        menuDrawer = (DrawerLayout) findViewById(R.id.menu_drawer);
        toggle = new ActionBarDrawerToggle(this, menuDrawer, toolbar, R.string.confirm, R.string.cancel);
        menuDrawer.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        navView = (NavigationView) findViewById(R.id.menu_drawer_list);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectNavItem(item);
                return true;
            }
        });
    }

    protected abstract void selectNavItem(MenuItem item);
}
