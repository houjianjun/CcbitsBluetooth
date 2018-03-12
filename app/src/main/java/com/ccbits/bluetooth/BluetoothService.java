package com.ccbits.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 创建时间： 2018/3/12.
 * 作    者： 侯建军
 * 功能描述：蓝牙服务类
 */
public class BluetoothService {
    // 调试信息
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;

    // 创建服务器套接字时的SDP记录的名称
    private static final String NAME = "MainActivity";

    //UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    //蓝牙适配器
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    //表示当前连接状态的常量
    public static final int STATE_NONE = 0; //无状态
    public static final int STATE_LISTEN = 1; //监听
    public static final int STATE_CONNECTING = 2; //初始化一个连接
    public static final int STATE_CONNECTED = 3; //连接到一个远程设备

    // INSECURE "8ce255c0-200a-11e0-ac64-0800200c9a66"
    // SECURE "fa87c0d0-afac-11de-8a39-0800200c9a66"
    // SPP "0001101-0000-1000-8000-00805F9B34FB"


    /**
     * 日期：2018/3/12-1:15
     * 作者：侯建军
     * 功能：蓝牙设备构造器
     */
    public BluetoothService(Context context, Handler handler) {
        //获取蓝牙适配器
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }
    /**
     * 设置当前状态为聊天连接
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        //将新状态交给处理程序以便UI活动可以更新
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * 返回当前连接状态
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * 开始聊天服务。特别是开始接受一个开始
     * 会话监听 (server) 模式. 调用 Activity onResume()
     */
    public synchronized void start() {
        if (D) Log.d(TAG, "开始");

        // 取消任何试图建立连接的线程
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // 取消当前运行连接的任何线程
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // 启动线程监听 BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * 启动ConnectThread，启动连接到远程设备的连接。
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "连接到: " + device);

        // 取消任何试图建立连接的线程
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        //取消当前运行连接的任何线程
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        //启动线程与给定的设备连接
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * 启动ConnectedThread，开始管理蓝牙连接
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "已连接");

        // 取消完成连接的线程
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        //取消当前运行连接的任何线程
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // 取消接收线程，因为我们只想连接到一个设备
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // 启动线程来管理连接并执行传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // 将连接设备的名称发送回UIActivity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * 停止所有线程
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "停止");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    /**
     * 以不同步的方式写入连接线程
     * @param out The bytes to write
     */
    public void write(byte[] out) {
        //创建临时对象
        ConnectedThread r;
        //同步一个连接线程的副本
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // 执行写同步
        r.write(out);
    }

    /**
     * 表明连接尝试失败并通知了 UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        // 将一条失败消息发送回 Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "不能连接设备");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * 显示连接丢失并通知UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);
        // 将一条失败消息发送回Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "连接已断开");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * 这个线程在监听传入连接时运行。它的行为就像一个服务器端客户端。
     * 它运行直到连接被接受(或者直到取消).
     */
    private class AcceptThread extends Thread {
        //一个本地 server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            //创建一个 server socket监听
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() 失败", e);
            }
            mmServerSocket = tmp;
        }
        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            //如果我们没有连接，请监听服务器套接字
            while (mState != STATE_CONNECTED) {
                try {
                    // 这是一个阻塞调用，只返回一个成功连接或异常
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() 失败", e);
                    break;
                }

                // 如果有一个可接受的连接
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //情况正常。开始连接线程。
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //要么没有准备好，要么已经连接好了。终止新的socket。
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "不能关闭不想要的 socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "取消 " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() 服务器失败", e);
            }
        }
    }

    /**
     * 此线程在尝试与设备进行传出连接时运行。它直接通过运行;连接或成功或失败
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // 获取一个 BluetoothSocket连接给 BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() 失败", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 总是取消发现因为它会减慢连接速度
            mAdapter.cancelDiscovery();

            // 与蓝牙 BluetoothSocket连接
            try {
                // 这是一个阻塞调用，只返回一个成功的连接或异常
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "连接失败期间无法close()socket ", e2);

                }
                // 启动服务重新启动监听模式
                BluetoothService.this.start();
                return;
            }

            // 重置ConnectThread，因为我们已经完成了
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // 开始 connected 线程
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() socket 失败", e);
            }
        }
    }

    /**
     * 该线程在与远程设备的连接期间运行。
     * 它处理所有传入和传出的传输。
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "创建 ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            //获取BluetoothSocket输入和输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "临时 sockets不能创建", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            //在连接时继续监听InputStream
            while (true) {
                try {
                    //读取 InputStream
                    bytes = mmInStream.read(buffer);

                    // 将获得的字节发送到 UI Activity
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "连接已断开", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * 日期：2018/3/12-1:57
         * 作者：侯建军
         * 功能：按字节发送
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                //将发送的消息返回给 UI Activity
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "写数据异常", e);
                connectionLost();
            }
        }

        /**
         * 日期：2018/3/12-1:58
         * 作者：侯建军
         * 功能：关闭通信
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭连接失败！", e);
            }
        }
    }
}
