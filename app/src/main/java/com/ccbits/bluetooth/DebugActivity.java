package com.ccbits.bluetooth;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 日期：2018/3/8-22:15
 * 作者：侯建军
 * 功能：蓝牙调试
 */
public class DebugActivity extends AppCompatActivity {
    private EditText txtSendData;
    public static EditText txtReviceData;
    //清空发送数据
    private Button btnClearSendData;
    private Button btnClearReviceData;
    public static StringBuffer sendDataStr = new StringBuffer();
    public static StringBuffer reviceDataStr = new StringBuffer();
    //数据发送
    private EditText txtSend;
    private Button btnSend;
    private Button btnClear;
    //
    private TextView txtDevice;
    public final String TAG = "Debug";
    static BluetoothService mBluetoothService;

    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        //获取蓝牙服务参数
        mBluetoothService = MainActivity.mBluetoothService;
        Intent intent = getIntent();
        //获取蓝牙服务对象
        message = intent.getStringExtra("address");
        txtDevice = (TextView) findViewById(R.id.txtDevice);
        txtDevice.setText("已连接设备：" + message);

        txtSendData = (EditText) findViewById(R.id.txtSendData);
        txtReviceData = (EditText) findViewById(R.id.txtReviceData);
        txtSendData.setBackgroundColor(Color.parseColor("#9370DB"));
        txtReviceData.setBackgroundColor(Color.parseColor("#9AFF9A"));

        btnClearSendData = (Button) findViewById(R.id.btnClearSendData);
        btnClearReviceData = (Button) findViewById(R.id.btnClearReceiveData);

        txtSend = (EditText) findViewById(R.id.txtSend);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnClear = (Button) findViewById(R.id.btnClear);
        //发送数据清空
        btnClearSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSendData.setText("");
                sendDataStr.setLength(0);
            }
        });
        //接受数据清空
        btnClearReviceData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtReviceData.setText("");
                reviceDataStr.setLength(0);
            }
        });
        //数据清空
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSend.setText("");
            }
        });
        //数据发送事件处理
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = txtSend.getText().toString();
                if (msg.length() > 0) {
                    //数据发送
                    MainActivity.sendMessage(msg);
                    txtSend.setText("");
                    sendDataStr.append(msg + "\r\n");
                    txtSendData.setText(sendDataStr.toString());
                    txtSendData.setMovementMethod(ScrollingMovementMethod.getInstance());
                    txtSendData.setSelection(txtSendData.getText().length(), txtSendData.getText().length());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(true);
//            finish();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    /**
     * 日期：2018/3/12-15:59
     * 作者：侯建军
     * 功能：返回按钮事件
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DebugActivity.this, MainActivity.class);
        intent.putExtra("debug", "debug");
        startActivity(intent);
        DebugActivity.this.finish();
    }
}
