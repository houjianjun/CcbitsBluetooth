package com.ccbits.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    //
    private TextView txtDevice;
    public final String TAG = "Debug";
    static BluetoothService mBluetoothService;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;    //蓝牙设备
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        //获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //取得启动该Activity的Intent对象
        Intent intent = getIntent();
        //获取蓝牙服务对象
        message = intent.getStringExtra("address");
        //
        txtDevice = (TextView) findViewById(R.id.txtDevice);
        // txtDevice.setText("已连接设备："+mBluetoothService.mAdapter.getName());

        mBluetoothService = new BluetoothService(this, mHandler);

        txtDevice.setText("已连接设备：" + message);

        txtSendData = (EditText) findViewById(R.id.txtSendData);
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
            }
        });
        //接受数据清空
        btnClearReviceData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtReviceData.setText("");
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
                //取消发现，因为它开销大，而且需要连接
                mBluetoothAdapter.cancelDiscovery();
                // 获取设备MAC地址
                mDevice = mBluetoothAdapter
                        .getRemoteDevice(message);
                //试图连接到设备上
                mBluetoothService.connect(mDevice);
                Log.d(TAG, mBluetoothService.getState()+"");
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    //试图连接到设备上
                    mBluetoothService.connect(mDevice);
                }

                String msg = txtSend.getText().toString();
                if (msg.length() > 0) {
                    sendMessage(msg);
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
        startActivity(intent);
        mBluetoothService.stop();
        DebugActivity.this.finish();
    }

    /**
     * 功能：消息处理器
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg " + msg.arg1);
            switch (msg.what) {
                case MainActivity.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case MainActivity.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case MainActivity.MESSAGE_READ:
                    //将接受数据转为字节数组
                    byte[] readBuf = (byte[]) msg.obj;
                    //将字节转为字符串
                    String message = new String(readBuf, 0, msg.arg1);
                    //  readMessage(message);
                    break;
                case MainActivity.MESSAGE_DEVICE_NAME:
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    txtDeviceInfo.setText("设备：" + mConnectedDeviceName + "已连接");
//                    btnConnect.setText("断开连接");
//                    btnConnect.setEnabled(true);
                    Log.d(TAG, "MESSAGE_DEVICE_NAME " + msg);
                    break;
                case MainActivity.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST " + msg);
                    break;
            }
        }
    };

    /**
     * 功能：数据发送
     */
    private void sendMessage(String message) {
        // 在尝试任何东西之前，检查一下我们是否已经连接了
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        // 检查是否有发送的东西
        if (message.length() > 0) {
            // 获取消息字节通过 BluetoothChatService输出
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
        }
    }

}
