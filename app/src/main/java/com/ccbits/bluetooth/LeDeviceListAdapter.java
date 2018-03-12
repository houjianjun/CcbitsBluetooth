package com.ccbits.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 创建时间： 2018/3/11.
 * 作    者： 侯建军
 * 功能描述： ListView适配器
 */
public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    //LayoutInflater是用来找res/layout/下的xml布局文件
    LayoutInflater inflater;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        inflater = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public void clearDevice() {
        if (mLeDevices.size() > 0) {
            mLeDevices.clear();
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.item, null);
            viewHolder = new ViewHolder();
            viewHolder.tv1 = (TextView) view.findViewById(R.id.tv1);
            viewHolder.tv2 = (TextView) view.findViewById(R.id.tv2);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName()+" ";
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.tv1.setText(deviceName);
        else
            viewHolder.tv1.setText("未知");
        viewHolder.tv2.setText(device.getAddress());
        return view;
    }

    //辅助类
    class ViewHolder {
        TextView tv1;
        TextView tv2;
    }
}
