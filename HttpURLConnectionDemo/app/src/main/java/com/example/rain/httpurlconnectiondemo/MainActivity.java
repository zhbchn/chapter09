package com.example.rain.httpurlconnectiondemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    protected static final int CHANGE_UI = 1;
    protected static final int ERROR = 2;
    private EditText et_path;
    private ImageView ivPic;
    private TextView tvMsg;

    // 主线程创建消息处理器
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == CHANGE_UI) {
              tvMsg.setText((String)msg.obj);
            } else if (msg.what == ERROR) {
                Toast.makeText(MainActivity.this, "获取数据错误",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMsg=(TextView)findViewById(R.id.tv_msg);

          }


    /**
     * @功能 读取流
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }
    public void GetMsg(View view) {
        //子线程请求网络,Android4.0以后访问网络不能放在主线程中
       final String path="http://www.aust.edu.cn";
            new Thread() {
                private HttpURLConnection conn;
                private Bitmap bitmap;
                public void run() {
                    // 连接服务器 get 请求 获取图片
                    try {
                        //创建URL对象
                        URL url = new URL(path);
                        // 根据url 发送 http的请求
                        conn = (HttpURLConnection) url.openConnection();
                        // 设置请求的方式
                        conn.setRequestMethod("GET");
                        //设置超时时间
                        conn.setConnectTimeout(5000);
                        // 得到服务器返回的响应码
                        int code = conn.getResponseCode();
                        //请求网络成功后返回码是200
                        if (code == 200) {
                            //获取输入流
                            InputStream is = conn.getInputStream();
                            //将流转换成Bitmap对象
                            byte[] rebyte = readStream(is);
                            String remess = new String(rebyte);

                            //bitmap = BitmapFactory.decodeStream(is);
                            //将更改主界面的消息发送给主线程
                            Message msg = new Message();
                            msg.what = CHANGE_UI;
                            msg.obj = remess;
                            handler.sendMessage(msg);
                        } else {
                            //返回码不等于200 请求服务器失败
                            Message msg = new Message();
                            msg.what = ERROR;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message msg = new Message();
                        msg.what = ERROR;
                        handler.sendMessage(msg);
                    }
                    //关闭连接
                    conn.disconnect();
                }
            }.start();

    }
}
