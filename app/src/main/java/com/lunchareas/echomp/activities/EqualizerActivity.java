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


import android.content.Context;
import android.graphics.PorterDuff;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.widgets.VerticalSeekBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class EqualizerActivity extends AppCompatActivity {

    private static final String TAG = EqualizerActivity.class.getName();
    private static final int MAX_STRENGTH = 1000;

    private LinearLayout layoutBars;
    private LinearLayout layoutHeaders;
    private Spinner spinner;
    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private Toolbar toolbar;
    private Switch toggle;
    private List<SeekBar> seekBarList;

    private short numBands;
    private short lowerBound;
    private short upperBound;
    private int audioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        // Setup equalizer
        initEqualizer();

        // Setup bass boost
        initBassBoost();

        // Setup virtualizer
        initVirtualizer();

        // Setup spinner
        initSpinner();

        // Setup content
        initMainView();

        // Turn off bars
        toggleBars();

        // Wait for click
        initListener();
    }

    private void initEqualizer() {

        // Create the equalizer
        seekBarList = new ArrayList<>();
        audioId = MediaControlUtils.audioId;
        equalizer = new Equalizer(0, audioId);
        equalizer.setEnabled(true);

        // Get bands and view
        layoutBars = (LinearLayout) findViewById(R.id.main);
        layoutHeaders = (LinearLayout) findViewById(R.id.headers);
        numBands = equalizer.getNumberOfBands();
        lowerBound = equalizer.getBandLevelRange()[0];
        upperBound = equalizer.getBandLevelRange()[1];

        // Append to bands to layout
        for (short i = 0; i < numBands; i++) {
            final short idx = i;

            // Create seekbar holder
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            // Add seekbar
            VerticalSeekBar seekBar = new VerticalSeekBar(this);
            seekBar.setId(idx);
            seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
            seekBar.setMax(upperBound - lowerBound);
            seekBar.setProgress(equalizer.getBandLevel(idx) - lowerBound);
            seekBar.setBackgroundDrawable(null);
            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            linearLayout.addView(seekBar);

            // Handle seekbar change
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (toggle.isChecked()) {
                        equalizer.setBandLevel(idx, (short)(progress + lowerBound));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // Add headers
            TextView textView = new TextView(this);
            textView.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            String text;
            if (equalizer.getCenterFreq(idx)/1000 > 1000) {
                if (equalizer.getCenterFreq(idx) % 1000000 != 0)
                    text = ((double)equalizer.getCenterFreq(idx)/1000000f) + " kHz";
                else
                    text = (equalizer.getCenterFreq(idx)/1000000) + " kHz";
            } else {
                if (equalizer.getCenterFreq(idx) % 1000 != 0)
                    text = ((double)equalizer.getCenterFreq(idx)/1000f) + " Hz";
                else
                    text = (equalizer.getCenterFreq(idx)/1000) + " Hz";
            }
            textView.setText(text);

            // Add views
            layoutBars.addView(linearLayout);
            layoutHeaders.addView(textView);
            seekBarList.add(seekBar);
        }
    }

    private void initBassBoost() {

        // Set initial bass boost
        bassBoost = new BassBoost(0, audioId);
        bassBoost.setEnabled(true);
        if (bassBoost.getStrengthSupported()) {

            // Get the seekbar
            SeekBar seekBar = (SeekBar) findViewById(R.id.bass_boost);
            seekBar.setMax(MAX_STRENGTH);
            seekBar.setBackgroundDrawable(null);
            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (toggle.isChecked()) {
                        bassBoost.setStrength((short) progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBarList.add(seekBar);
        }
    }

    private void initVirtualizer() {

        // Create virtualizer
        virtualizer = new Virtualizer(0, audioId);
        virtualizer.setEnabled(true);
        if (virtualizer.getStrengthSupported()) {

            // Get the seekbar
            SeekBar seekBar = (SeekBar) findViewById(R.id.virtualizer);
            seekBar.setMax(MAX_STRENGTH);
            seekBar.setBackgroundDrawable(null);
            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (toggle.isChecked()) {
                        virtualizer.setStrength((short) progress);
                        Log.d(TAG, "Strength changing!");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBarList.add(seekBar);
        }
    }

    private void initSpinner() {

        // Set presets
        List<String> presetNames = new ArrayList<>();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, presetNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            presetNames.add(equalizer.getPresetName(i));
        }
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);

        // Handle selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (toggle.isChecked())
                    equalizer.usePreset((short) position);
                for (short i = 0; i < numBands; i++) {
                    if (toggle.isChecked()) {
                        VerticalSeekBar seekBar = (VerticalSeekBar) findViewById(i);
                        seekBar.setProgress(equalizer.getBandLevel(i) - lowerBound);
                        seekBar.setProgressAndThumb(equalizer.getBandLevel(i) - lowerBound);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initMainView() {

        // Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EqualizerActivity.this.setTitle("Equalizer");
    }

    private void toggleBars() {

        // Turn off seek bars
        for (SeekBar seekBar: seekBarList) {
            if (seekBar.isEnabled()) {
                seekBar.setEnabled(false);
            } else {
                seekBar.setEnabled(true);
            }
        }
    }

    private void initListener() {

        // Get switch
        toggle = (Switch) findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBars();
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_equalizer, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.toggle: {
                Log.d(TAG, "Toggle");
                toggleBars();
                break;
            }
        }

        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }
}