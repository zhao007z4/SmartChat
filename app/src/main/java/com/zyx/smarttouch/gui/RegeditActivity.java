package com.zyx.smarttouch.gui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.zyx.smarttouch.R;
import com.zyx.smarttouch.SnsStat;
import com.zyx.smarttouch.Task;
import com.zyx.smarttouch.common.Aes;
import com.zyx.smarttouch.common.Share;
import com.zyx.smarttouch.scan.ImageScanActivity;


public class RegeditActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etCode;
    private Button btnScan;
    private Button btnRegedit;
    private EditText etRegeditInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCode = (EditText)findViewById(R.id.etCode);
        btnScan = (Button)findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        btnRegedit =(Button)findViewById(R.id.btnRegedit);
        btnRegedit.setOnClickListener(this);

        Button btnGen = (Button)findViewById(R.id.btnGen);
        btnGen.setOnClickListener(this);

        etRegeditInfo =(EditText) findViewById(R.id.etRegeditInfo);
        etRegeditInfo.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.btnRegedit:
                regeditSys();
                break;
            case R.id.btnScan:
                Intent intent = new Intent(RegeditActivity.this, ImageScanActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.btnGen:
                TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String sText = tm.getDeviceId();
                sText ="imei:"+sText+"\n";
                sText+="brand:"+ Aes.getBrand()+"\n";
                sText += "product:"+Aes.getProduct();

                etRegeditInfo.setText(sText);
                etRegeditInfo.setVisibility(View.VISIBLE);

                break;
        }
    }
    private void regeditSys()
    {
        String sText = etCode.getText().toString().trim();
        if(sText.length()<1)
        {
            Toast.makeText(this,R.string.STR_REGEDIT_CODE,Toast.LENGTH_LONG).show();
            return;
        }

        if(sText.length()<16)
        {
            Toast.makeText(this,R.string.STR_CODE_INVAILD,Toast.LENGTH_LONG).show();
            return;
        }

        if(SnsApp.mInstance.request_start(sText,this))
        {
            Toast.makeText(this,R.string.STR_REGEDIT_SUCC,Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this,EntryActivity.class);
            startActivity(intent);
            this.finish();
        }
        else
        {
            Toast.makeText(this,R.string.STR_CODE_INVAILD,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK && data !=null) {
                    if(data.hasExtra("value"))
                    {
                        etCode.setText(data.getStringExtra("value"));
                    }
                }
                break;
        }
    }
}
