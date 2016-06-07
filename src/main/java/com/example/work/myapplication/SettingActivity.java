package com.example.work.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingActivity extends Activity {

    private static final String TAG = SettingActivity.class.getSimpleName();
    private TextView mConfirmText;
    private EditText mContentText;
    private EditText mPhoneNumEditText;
    private TextView mTestText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        initView();

    }

    private void initView() {
        mContentText = (EditText) findViewById(R.id.et_content);
        mPhoneNumEditText = (EditText) findViewById(R.id.et_phone_num);
        mConfirmText = (TextView) findViewById(R.id.tv_confirm);
        mTestText = (TextView) findViewById(R.id.tv_test);

        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
        mTestText.setText(defaultSmsPackage);


        String phoneNum = SPUtils.getString(this, SPUtils.PHONE_NUMBER, null);
        String content = SPUtils.getString(this, SPUtils.CONTENT, null);
        Log.d(TAG, "initView content = " + content);
        Log.d(TAG, "initView phoneNum = " + phoneNum);

        if (!TextUtils.isEmpty(phoneNum)) {
            mPhoneNumEditText.setText(phoneNum);
        }

        if (!TextUtils.isEmpty(content)) {
            mContentText.setText(content);
        }

        mConfirmText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSetting();
            }
        });

    }

    private void saveSetting() {
        String phoneNumSetting = mPhoneNumEditText.getText().toString();
        String contentSetting = mContentText.getText().toString();
        SPUtils.putString(this, SPUtils.PHONE_NUMBER, phoneNumSetting);
        SPUtils.putString(this, SPUtils.CONTENT, contentSetting);
        ToastUtils.show(this, "设置成功");
        exit();
    }

    private void exit() {
        finish();
    }

}
