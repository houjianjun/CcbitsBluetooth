package com.ccbits.bluetooth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ccbits.bluetooth.view.RockerView;

/**
 * 日期：2018/3/8-18:58
 * 作者：侯建军
 * 功能：主界面类
 */
public class MainActivity extends AppCompatActivity {
    //摇杆视图
    private TextView rocker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载一个布局
        setContentView(R.layout.activity_main);
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
                    String dir=getDirection(direction);
                    //摇动方向
                    rocker.setText("方向 : " +dir);
                    switch (dir){
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
