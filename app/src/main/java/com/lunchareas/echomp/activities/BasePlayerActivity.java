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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public abstract class BasePlayerActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected List<Song> songList;

    protected int id;
    protected long currSong;

    public BasePlayerActivity(int id) {
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

        // Setup back button
        initReturn();

        // Init rest of view
        initMainView();

        // Get data
        getDispData();

        // Show data
        showDispData();

        // Setup listener
        initListener();
    }

    private void initReturn() {

        // Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected abstract void getDispData();

    protected abstract void showDispData();

    protected abstract void updateDispData();

    protected abstract void initMainView();

    protected abstract void initListener();
}
