package com.example.scorpio92.asl_monitor;

import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;


public class Options extends ActionBarActivity {

    public static boolean OnlyDATA=true;
    private RadioButton rb1;
    private RadioButton rb2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rb1 = (RadioButton)findViewById(R.id.radioButton);
        rb2 = (RadioButton)findViewById(R.id.radioButton2);
        rb1.setChecked(true);
    }

    public void rb1_Click(View view) {
        rb2.setChecked(false);
        OnlyDATA=true;
    }

    public void rb2_Click(View view) {
        rb1.setChecked(false);
        OnlyDATA=false;
    }
}
