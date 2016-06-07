package com.example.work.myapplication;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private TextView mTestText;

    private SmsObserver mSmsObserver;

    public Handler smsHandler = new Handler() {
        //这里可以进行回调的操作
        //TODO

    };


    // private Uri SMS_INBOX = Uri.parse("content://sms/inbox/");
    private Uri SMS_INBOX = Uri.parse("content://sms/");
    private TextView mSettingText;
    private ToggleButton mUpdateSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        mSmsObserver = new SmsObserver(this, smsHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        mSettingText = (TextView) findViewById(R.id.tv_setting);
        mTestText = (TextView) findViewById(R.id.tv_test);

        mUpdateSwitch = (ToggleButton) findViewById(R.id.st_update_in_wifi);
        mUpdateSwitch.setChecked(SPUtils.getBoolean(this,
                SPUtils.IS_SWITCH_ON, false));
        mUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putBoolean(MainActivity.this, SPUtils.IS_SWITCH_ON, isChecked);
                if (isChecked) {
                    switch_on();
                } else {
                    switch_off();
                }

                //TODO test sendSMS Method
            }
        });

        mSettingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSettingActivity();
            }
        });

    }

    private void goSettingActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * 关闭开关
     */
    private void switch_off() {
        ToastUtils.show(this, "关闭了...");
        SPUtils.put(this, SPUtils.IS_SWITCH_ON, false);
        getContentResolver().unregisterContentObserver(mSmsObserver);
    }

    /**
     * 打开开关
     */
    private void switch_on() {
        ToastUtils.show(this, "打开了...");
        SPUtils.put(this, SPUtils.IS_SWITCH_ON, true);
        getContentResolver().registerContentObserver(SMS_INBOX, true,
                mSmsObserver);
    }


    class SmsObserver extends ContentObserver {

        public SmsObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //每当有新短信到来时，使用我们获取短消息的方法
            getSmsFromPhone();
        }
    }

    public void getSmsFromPhone() {
        Log.d(TAG, "getSmsFromPhone");
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"body", "service_center"};// "_id",
        // "address",
        // "person",, "date",
        // "type
        String where = "date >  "
                + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
        Log.d(TAG, "getSmsFromPhone cur = " + cur.toString());
        if (null == cur)
            return;
        boolean moveToNext = cur.moveToNext();
        if (moveToNext) {
            Log.d(TAG, "getSmsFromPhone moveToNext = " + moveToNext);
            // String number = cur.getString(cur.getColumnIndex("address"));//
            // 手机号
            // String name = cur.getString(cur.getColumnIndex("person"));//
            // 联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));
            String scAddress = cur.getString(cur
                    .getColumnIndex("service_center"));

            String phoneNum = SPUtils.getString(this, SPUtils.PHONE_NUMBER, null);
            String content = SPUtils.getString(this, SPUtils.CONTENT, null);
            Log.d(TAG, "getSmsFromPhone phoneNum = " + phoneNum + ", content = " + content + ", scAddress = " + scAddress);
            if (body != null && body.contains(content) && phoneNum != null && content != null && scAddress != null) {
                sendSMS(phoneNum, body, scAddress);
            }
        } else {
            Log.d(TAG, "getSmsFromPhone not moveToNext = " + moveToNext);
        }


//        ContentResolver cr = getContentResolver();
//        /**
//         *     _id：短信序号，如100 　　
//         * 　　thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的 　　
//         * 　　address：发件人地址，即手机号，如+8613811810000 　　
//         * 　　person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null 　　
//         * 　　date：日期，long型，如1256539465022，可以对日期显示格式进行设置 　　
//         * 　　protocol：协议0SMS_RPOTO短信，1MMS_PROTO彩信
//         * 　　read：是否阅读0未读，1已读 　　
//         * 　　status：短信状态-1接收，0complete,64pending,128failed 　　
//         * 　　type：短信类型1是接收到的，2是已发出 　　 　　
//         * 　　body：短信具体内容 　　
//         * 　　service_center：短信服务中心号码编号，如+8613800755500
//         */
//        String[] projection = new String[]{"body", "service_center"};//"_id", "address", "person",, "date", "type
////        String where = " address = '1066321332' AND date >  "
////                + (System.currentTimeMillis() - 10 * 60 * 1000);
//        String where = " date >  "
//                + (System.currentTimeMillis() - 10 * 60 * 1000);
//        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
//        if (null == cur)
//            return;
//        if (cur.moveToNext()) {
////            String number = cur.getString(cur.getColumnIndex("address"));//手机号
////            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
//            String body = cur.getString(cur.getColumnIndex("body"));
//            String scAddress = cur.getString(cur.getColumnIndex("service_center"));
//            //这里我是要获取自己短信服务号码中的验证码~~
////            Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
////            Matcher matcher = pattern.matcher(body);
////            if (matcher.find()) {
////                String res = matcher.group().substring(1, 11);
////                mTestText.setText(res);
////                sendSMS();
////            }
//            String phoneNum = (String) SPUtils.get(this, SPUtils.PHONE_NUMBER, null);
//            String content = (String) SPUtils.get(this, SPUtils.CONTENT, null);
//            if (body != null && body.contains(content) && phoneNum != null && content != null) {
//                sendSMS(phoneNum, body, scAddress);
//            }
//        }
    }

    /**
     * @param phone
     * @param body
     * @param scAddress
     */
    private void sendSMS(String phone, String body, String scAddress) {


        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();


        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        ArrayList<String> smsBodyParts = smsManager.divideMessage(body);
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();

        for (int i = 0; i < smsBodyParts.size(); i++) {
            sentPendingIntents.add(sentPendingIntent);
            deliveredPendingIntents.add(deliveredPendingIntent);
        }

// For when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_SENT));

// For when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_DELIVERED));

// Send a text based SMS
        smsManager.sendMultipartTextMessage(phone, scAddress, smsBodyParts, sentPendingIntents, deliveredPendingIntents);

        Log.d(TAG, "sendSMS phone = " + phone + ", scAddress = " + scAddress + ", body = " + body);

        /**
         * -- destinationAddress：目标电话号码,你要发给谁
         * -- scAddress：短信中心号码，测试可以不填
         * -- text: 短信内容
         * -- sentIntent：发送 -->中国移动 --> 中国移动发送失败 --> 返回发送成功或失败信号 --> 后续处理
         *即，这个意图包装了短信发送状态的信息,是否发送成功
         * -- deliveryIntent： 发送 -->中国移动 --> 中国移动发送成功 --> 返回对方是否收到这个信息 --> 后续处理
         *即：这个意图包装了短信是否被对方收到的状态信息,对方是否成功接收（供应商已经发送成功，但是对方没有收到）。
         *
         **/

        ToastUtils.show(this, "短信发送成功。。", false);

    }
}
