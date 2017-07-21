package com.github.codetanzania.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.fragment.IDFragment;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class IDActivity extends AppCompatActivity implements IDFragment.OnCacheReporterInfo {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_id);
        setupAppBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registration_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_done:
                return ((IDFragment)getSupportFragmentManager()
                   .findFragmentById(R.id.id_frag)).isValidUserInput();
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void cacheReporterInfo(Reporter reporter) {
        Util.storeCurrentReporter(this, reporter);
        startActivity(new Intent(this, SplashScreenActivity.class));
        finish();
    }

    private void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.registration_toolbar_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }
}
