package com.zyx.smarttouch.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zyx.smarttouch.R;
import com.zyx.smarttouch.Task;

/**
 * Created by corous360 on 23/8/16.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(SnsApp.mInstance.request_start_sys(this))
        {
            Intent intent = new Intent(this,EntryActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this,RegeditActivity.class);
            startActivity(intent);
        }
        this.finish();
    }
}
