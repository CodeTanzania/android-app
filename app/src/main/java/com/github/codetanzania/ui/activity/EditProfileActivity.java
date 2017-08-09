package com.github.codetanzania.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.fragment.EditProfileFragment;

import tz.co.codetanzania.R;

public class EditProfileActivity extends AppCompatActivity implements EditProfileFragment.OnReporterSaved {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set content view
        setContentView(R.layout.activity_edit_profile);
        showCurrentReporter();

        setupToolbar();

        View fab = findViewById(R.id.fab_EditProfile);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                save();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_done:
                // save and exit
                save();
                return true;
            case android.R.id.home:
                // exit without saving
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.basic_toolbar_layout);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        actionBar.setTitle(R.string.edit_profile_title);
    }

    private void showCurrentReporter() {
        ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_frag)).showCurrentReporter();
    }

    private void save() {
        ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_frag)).verifyAndComplete();
    }

    @Override
    public void onReporterSaved(Reporter reporter) {
        Toast.makeText(EditProfileActivity.this,
                getString(R.string.text_item_saved), Toast.LENGTH_SHORT).show();

        setResult(Activity.RESULT_OK, getIntent());
        finish();
    }
}
