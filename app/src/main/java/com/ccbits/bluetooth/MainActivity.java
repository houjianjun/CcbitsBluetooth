package com.ccbits.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ccbits.bluetooth.view.RockerView;

import java.util.Set;

/**
 * 日期：2018/3/8-18:58
 * 作者：侯建军
 * 功能：主界面类
 */
public class MainActivity extends AppCompatActivity {
    //摇杆视图
    private TextView rocker;
    private TextView txtDeviceType;
    private TextView txtDeviceInfo;
    //蓝牙调试按钮
    private Button btnDebug;
    //蓝牙搜索按钮
    private Button btnSearchBlue;
    //蓝牙连接
    private Button btnConnect;
    //小车停止
    private Button btnStop;
    //数字开关控制
    private Switch switch1;
    private Switch switch2;

    //设置列表
    private ListView lvDevices;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    private static TextView A0;
    private static TextView A1;
    private static TextView A2;
    private static TextView A3;
    private static TextView A4;
    private static TextView A5;
    //调试信息
    public final String TAG = "Main";
    //自己定义局部常量
    private final static int REQUEST_ENABLE_BT = 1;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    // 连接设备名称
    private static String mConnectedDeviceName = null;
    //从blue牙签服务处理程序接收到的密钥名
    public static final String DEVICE_NAME = "device_name";
    //消息类型
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    private BluetoothDevice mDevice;    //蓝牙设备
    public static final String TOAST = "toast";
    public static String address;
    //声明蓝牙服务对象
    static BluetoothService mBluetoothService;

    private static Context context;

    //模拟标志
    private static boolean flagAnalog = false;
    private  Switch switchAnalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载一个布局
        setContentView(R.layout.activity_main);
        setTitle("Ccbits蓝牙遥控1.0V");

        Intent intent = getIntent();

        A0 = (TextView) findViewById(R.id.txtA0);
        A1 = (TextView) findViewById(R.id.txtA1);
        A2 = (TextView) findViewById(R.id.txtA2);
        A3 = (TextView) findViewById(R.id.txtA3);
        A4 = (TextView) findViewById(R.id.txtA4);
        A5 = (TextView) findViewById(R.id.txtA5);

        //开关控件
        switch1 = (Switch) findViewById(R.id.switch1);
        switch2 = (Switch) findViewById(R.id.switch2);
        switchAnalog = (Switch) findViewById(R.id.switchAnalog);

        txtDeviceType = (TextView) findViewById(R.id.txtDeviceType);
        txtDeviceInfo = (TextView) findViewById(R.id.txtDeviceInfo);

        //获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //判断设备是否支持ble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //获取蓝牙适配器
            txtDeviceType.setText("传统蓝牙");
        } else {
            txtDeviceType.setText("低功耗蓝牙");
        }
        context = getApplication();
        //
        initBluetoothAdapter();

        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        //获取listview
        lvDevices = (ListView) findViewById(R.id.bluetoothDevices);
        //添加适配器
        lvDevices.setAdapter(mLeDeviceListAdapter);
        //扫描设备
        ScanDevice();
        //蓝牙调试
        btnDebug = (Button) findViewById(R.id.btnDebugBlue);
        //蓝牙搜索
        btnSearchBlue = (Button) findViewById(R.id.btnSearchBlue);
        //蓝牙连接
        btnConnect = (Button) findViewById(R.id.btnConnect);
        //小车停止
        btnStop = (Button) findViewById(R.id.btnStop);

        // 注册广播接收器。接收蓝牙发现讯息
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        //数字口开关
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //sendMessage("APT+SPP8888\n\r");
                if (isChecked) {
                    sendMessage("C:");
                } else {
                    sendMessage("D:");
                }
            }
        });
        //数字口开关
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    sendMessage("G:");
                } else {
                    sendMessage("F:");
                }
            }
        });
        //模拟开关
        switchAnalog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    flagAnalog = true;
                } else {
                    flagAnalog = false;
                }
            }
        });

        //获取蓝牙服务对象
        String debug = intent.getStringExtra("debug");
        if (debug == null) {
            btnConnect.setEnabled(false);
            btnDebug.setEnabled(false);
            mBluetoothService = new BluetoothService(this, mHandler);
        } else {
            btnDebug.setEnabled(true);
            btnConnect.setEnabled(true);
            btnConnect.setText("断开连接");
            txtDeviceInfo.setText("设备：" + mConnectedDeviceName + "已连接");
        }
        //连接设备
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //查找选择设备
                mDevice = mLeDeviceListAdapter.getDevice(position);
                Log.d(TAG, mDevice.getAddress());
                //取消发现，因为它开销大，而且需要连接
                mBluetoothAdapter.cancelDiscovery();
                address = mDevice.getAddress();
                // 获取设备MAC地址
                BluetoothDevice device = mBluetoothAdapter
                        .getRemoteDevice(address);

                //试图连接到设备上
                mBluetoothService.connect(device);
            }
        });
        //搜索蓝牙
        btnSearchBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBluetoothAdapter();
                mBluetoothAdapter.startDiscovery();
                if (mBluetoothAdapter.isDiscovering()) {
                    txtDeviceInfo.setText("设备：扫描中……");
                    mBluetoothAdapter.cancelDiscovery();
                }
                btnSearchBlue.setEnabled(false);
                //设备扫描
                //ScanDevice();
                btnSearchBlue.setEnabled(true);
            }
        });

        //蓝牙调试事件
        btnDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DebugActivity.class);
                intent.putExtra("address", address);
                //传递参数
                startActivity(intent);
                MainActivity.this.finish();
            }
        });

        //蓝牙连接
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnConnect.getText().equals("断开连接")) {
                    mBluetoothService.stop();
                    btnConnect.setEnabled(false);
                    btnDebug.setEnabled(false);
                    txtDeviceInfo.setText("设备：断开");
                    //清除列表数据
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    mBluetoothAdapter.disable();
                }
            }
        });
        //小车停止
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("N:");
            }
        });
        //摇杆视图
        rocker = (TextView) findViewById(R.id.rocker);
        RockerView rockerView = (RockerView) findViewById(R.id.rockerView);
        //如果摇杆不为空
        if (rocker != null) {
            //设置回调模式为状态变化进行回调
            rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            //摇动监听，4个方向的
            rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {
                    //开始
                    rocker.setText(null);
                }

                @Override
                public void direction(RockerView.Direction direction) {
                    String dir = getDirection(direction);
                    //摇动方向
                    rocker.setText("方向 : " + dir);
                    switch (dir) {
                        case "上":
                            break;
                        case "下":
                            break;
                        case "左":
                            break;
                        case "右":
                            break;
                        default:
                            //停止
                            break;
                    }
                }

                @Override
                public void onFinish() {
                    //结束
                    rocker.setText(null);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        mBluetoothAdapter.disable();
        System.exit(0);//正常退出App
    }

    private void initBluetoothAdapter() {
        // 如果用户的设备没有开启蓝牙，则弹出开启蓝牙设备的对话框，让用户开启蓝牙
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //打开蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //后台打开蓝牙，不做提示
//            bluetoothAdapter.enable();
        }
    }

    /**
     * 功能：消息处理器
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg " + msg.arg1);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case MESSAGE_READ:
                    //将接受数据转为字节数组
                    byte[] readBuf = (byte[]) msg.obj;
                    //将字节转为字符串
                    String message = new String(readBuf, 0, msg.arg1);
                    Log.d("data1", message);
                    readMessage(message);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    txtDeviceInfo.setText("设备：" + mConnectedDeviceName + "已连接");
                    btnConnect.setText("断开连接");
                    btnConnect.setEnabled(true);
                    btnDebug.setEnabled(true);

                    Log.d(TAG, "MESSAGE_DEVICE_NAME " + msg);
                    break;
                case MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST " + msg);
                    break;
            }
        }
    };

    /**
     * 功能：传统扫描
     */
    private void ScanDevice() {
        // 获取一组当前成对的设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //搜索设备，将其添加到列表中
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // Looper.prepare();
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
//                bluetoothDevicesList.add(device.getName() + ":"
//                        + device.getAddress() + "\n");
                // Looper.loop();
            }
        }
    }

    /**
     * 功能：摇杆数据发送
     */
    private String getDirection(RockerView.Direction direction) {
        String message = "";
        switch (direction) {
            case DIRECTION_LEFT:
                message = "左";
                sendMessage("L:");
                break;
            case DIRECTION_RIGHT:
                sendMessage("H:");
                message = "右";
                break;
            case DIRECTION_UP:
                message = "上";
                //发送消息
                sendMessage("W:");
                break;
            case DIRECTION_DOWN:
                message = "下";
                sendMessage("Q:");
                break;
//            case DIRECTION_UP_LEFT:
//                message = "左上";
//                break;
//            case DIRECTION_UP_RIGHT:
//                message = "右上";
//                break;
//            case DIRECTION_DOWN_LEFT:
//                message = "左下";
//                break;
//            case DIRECTION_DOWN_RIGHT:
//                message = "右下";
//                break;
            default:
                message = "停";
                sendMessage("N:");
                break;
        }
        return message;
    }

    /**
     * 功能：数据发送
     */
    public static void sendMessage(String message) {

        // 在尝试任何东西之前，检查一下我们是否已经连接了
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        // 检查是否有发送的东西
        if (message.length() > 0) {
            // 获取消息字节通过 BluetoothChatService输出
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
        }
    }

    /**
     * 功能：读取消息
     */
    private static void readMessage(String message) {
        if (flagAnalog) {
            Log.d("readAAA", message);
            String[] strRevData = message.split(":");
            if (null != strRevData && strRevData.length > 1) {
                switch (strRevData[0]) {
                    case "A0":
                        A0.setText("A0:" + strRevData[1]);
                        break;
                    case "A1":
                        A1.setText("A1:" + strRevData[1]);
                        break;
                    case "A2":
                        A2.setText("A2:" + strRevData[1]);
                        break;
                    case "A3":
                        A3.setText("A3:" + strRevData[1]);
                        break;
                    case "A4":
                        A4.setText("A4:" + strRevData[1]);
                        break;
                    case "A5":
                        A5.setText("A5:" + strRevData[1]);
                        break;
                    default:
                        break;
                }
            }
        } else {
            if (null != DebugActivity.txtReviceData) {
                if (DebugActivity.reviceDataStr.length() > 200) {
                    DebugActivity.reviceDataStr.setLength(0);
                    DebugActivity.txtReviceData.setText("");
                }
                DebugActivity.reviceDataStr.append(message.replace("APT\\+SPP8888", "").replace("791310", "").replace("SPP8888", "").trim() + "\r\n");
                DebugActivity.txtReviceData.setText(DebugActivity.reviceDataStr.toString());
                DebugActivity.txtReviceData.setMovementMethod(ScrollingMovementMethod.getInstance());
                DebugActivity.txtReviceData.setSelection(DebugActivity.txtReviceData.getText().length(), DebugActivity.txtReviceData.getText().length());
            }
        }
    }

    /**
     * 功能：广播接收发现蓝牙设备
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
            txtDeviceInfo.setText("设备：扫描完成。");
        }
    };
}
