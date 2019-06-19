package com.lunchareas.echomp.activities;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.lunchareas.echomp.R;
import com.lunchareas.echomp.adapters.SearchAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = SearchActivity.class.getName();

    private final Activity activity = this;

    private Toolbar toolbar;
    private SearchView searchView;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Setup main view
        initMainView();
    }

    private void initMainView() {

        // Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SearchActivity.this.setTitle("");

        // Setup recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new SearchAdapter(activity, new ArrayList<>()));
    }

    private void initSearch(Menu menu) {

        // Setup search in menu
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setIconified(false);
        searchView.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackgroundColor(
                ContextCompat.getColor(this, android.R.color.transparent));

        // Setup query text listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1) {
                    ((SearchAdapter) recyclerView.getAdapter()).searchMedia(newText.toLowerCase());
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Setup menu
        getMenuInflater().inflate(R.menu.overflow_menu_search, menu);
        super.onCreateOptionsMenu(menu);
        initSearch(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
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
