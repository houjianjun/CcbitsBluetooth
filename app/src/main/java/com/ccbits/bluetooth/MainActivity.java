package com.ccbits.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ccbits.bluetooth.view.RockerView;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 日期：2018/3/8-18:58
 * 作者：侯建军
 * 功能：主界面类
 */
public class MainActivity extends AppCompatActivity {
    //摇杆视图
    private TextView rocker;
    //蓝牙调试按钮
    private Button btnDebug;
    //蓝牙搜索按钮
    private Button btnSearchBlue;
    //设置列表
    private ListView lvDevices;
    //蓝牙适配器
    private BluetoothAdapter bluetoothAdapter;
    private List<String> bluetoothDevicesList = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    //蓝牙设备
    private BluetoothDevice device;
    //蓝牙客户端
    private BluetoothSocket clientSocket;
    //UUID
    private final UUID CCBITS_UUID = UUID
            .fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    //输出流，发送数据
    private OutputStream os;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载一个布局
        setContentView(R.layout.activity_main);

        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //打开蓝牙
        bluetoothAdapter.enable();

        /**
         * 功能：获取与本机蓝牙所有绑定的远程蓝牙信息，以BluetoothDevice类实例返回。
         * 注意：如果蓝牙为开启，该函数会返回一个空集合 。
         */
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        //如果找到设备，将其添加到列表中

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevicesList.add(device.getName() + ":"
                        + device.getAddress() + "\n");
            }
        }

        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,bluetoothDevicesList);
        //获取listview
        lvDevices = (ListView) findViewById(R.id.bluetoothDevices);
        //添加数据到ListView
        lvDevices.setAdapter(arrayAdapter);
        //连接设备
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = arrayAdapter.getItem(position);
                //获取地址
                String address = s.substring(s.indexOf(":") + 1).trim();
                try {
                    //如果发现蓝牙
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    try {
                        //如果设备为空，获取远程设备
                        if (device == null) {
                            device = bluetoothAdapter.getRemoteDevice(address);
                        }
                        if (clientSocket == null) {
                            clientSocket = device
                                    .createRfcommSocketToServiceRecord(CCBITS_UUID);
                            clientSocket.connect();
                            os = clientSocket.getOutputStream();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "错误"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    if (os != null) {
                        //数据发送
                        os.write("发送信息到其他蓝牙设备".getBytes("utf-8"));
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "错误"+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
//        //打开和关闭蓝牙
//        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
//        //打开蓝牙
//        adapter.enable();
//        //关闭蓝牙
//        adapter.disable();

        //蓝牙调试
        btnDebug = (Button) findViewById(R.id.btnDebugBlue);
        //蓝牙搜索
        btnSearchBlue = (Button) findViewById(R.id.btnSearchBlue);
        btnSearchBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setProgressBarIndeterminateVisibility(true);
                setTitle("正在扫描...");
                //是否正在处于扫描过程中
                if (bluetoothAdapter.isDiscovering()) {
                    //取消扫描过程。
                    bluetoothAdapter.cancelDiscovery();
                }
                //扫描蓝牙设备
                bluetoothAdapter.startDiscovery();
            }
        });

        //添加事件
        btnDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DebugActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });
        //意图过滤器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //注册数据接受
        this.registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);
        //接受线程
//        acceptThread = new AcceptThread();
//        acceptThread.start();

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
    /**
     * 日期：2018/3/10-10:50
     * 作者：侯建军
     * 功能：广播接受
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //扫描到了任一蓝牙设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //找到的设备
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //蓝牙已经绑定
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothDevicesList.add(device.getName() + ":"
                            + device.getAddress() + "\n");
                    //数据刷新
                    arrayAdapter.notifyDataSetChanged();
                }
            // 蓝牙扫描过程结束
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setTitle("蓝牙设备搜索完成");
            }
        }
    };
    //方向获取
    private String getDirection(RockerView.Direction direction) {
        String message = null;
        switch (direction) {
            case DIRECTION_LEFT:
                message = "左";
                break;
            case DIRECTION_RIGHT:
                message = "右";
                break;
            case DIRECTION_UP:
                message = "上";
                break;
            case DIRECTION_DOWN:
                message = "下";
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
                break;
        }
        return message;
    }
}
