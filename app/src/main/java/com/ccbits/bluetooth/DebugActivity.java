package com.ccbits.bluetooth;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 日期：2018/3/8-22:15
 * 作者：侯建军
 * 功能：蓝牙调试
 */
public class DebugActivity extends AppCompatActivity {
    private EditText txtSendData;
    private EditText txtReviceData;
    //清空发送数据
    private Button btnClearSendData;
    private Button btnClearReviceData;
    //数据发送
    private EditText txtSend;
    private Button btnSend;
    private Button btnClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        txtSendData = (EditText) findViewById(R.id.txtSendData);
        btnClearSendData = (Button) findViewById(R.id.btnClearSendData);
        btnClearReviceData = (Button) findViewById(R.id.btnClearReceiveData);

        txtSend=(EditText)findViewById(R.id.txtSend);
        btnSend=(Button)findViewById(R.id.btnSend);
        btnClear=(Button)findViewById(R.id.btnClear);

        btnClearSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSendData.setText("");
            }
        });
        btnClearReviceData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtReviceData.setText("");
            }
        });
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DebugActivity.this, MainActivity.class);
        startActivity(intent);
        DebugActivity.this.finish();
    }
}
