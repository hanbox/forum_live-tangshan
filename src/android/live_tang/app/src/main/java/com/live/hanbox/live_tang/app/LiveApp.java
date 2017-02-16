package com.live.hanbox.live_tang.app;

import android.app.Activity;
import android.app.Application;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by HanBox on 2016/10/18.
 */

public class LiveApp extends Application {
    private Activity m_mainActivity = null;
    public String m_strHost = "http://106.75.77.243:8005/";
//    public String m_strHost = "http://192.168.0.103:8080/";

    public Activity getM_mainActivity() {
        return m_mainActivity;
    }

    public void setM_mainActivity(Activity m_mainActivity) {
        this.m_mainActivity = m_mainActivity;
    }

    public void delroom(final String roomid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = m_strHost + "live/wantlive/delroom/?room_id=" + roomid;
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

                    if(connection.getResponseCode() == 200){

                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
