package com.live.hanbox.live_tang.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.app.LiveApp;
import com.live.hanbox.live_tang.tools.NetworkUtils;
import com.live.hanbox.live_tang.tools.UpdateApp;
import com.live.hanbox.live_tang.wxapi.WXEntryActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    private LiveApp m_app = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_app = (LiveApp) this.getApplication();
        setContentView(R.layout.activity_splash);
        if (!checkNetwork()) {
            return;
        }

        checkUpdate();

        new Handler().postDelayed(new TimerTask() {
            @Override
            public void run() {
                SplashActivity.this.startActivityForResult(new Intent(SplashActivity.this, WXEntryActivity.class), 0);
            }
        }, 700);
        // checkUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }

    private boolean checkNetwork() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, "请检查网络状态！", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void checkUpdate() {
        Toast.makeText(this, "检查新版本...", Toast.LENGTH_SHORT).show();
        checkVersion();
    }

    private void checkVersion(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = m_app.m_strHost + "update/check/android/?vs=" + "1.1.2";
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

                    if(connection.getResponseCode() == 200){
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection
                                .getInputStream()));

                        String lines="";
                        String strconVer="";
                        while((lines = in.readLine()) != null){
                            strconVer+=lines;
                        }
                        String strCurVs = getVersion(SplashActivity.this);

                        int iRet = VersionComparison(strconVer, strCurVs);
                        if ( iRet > 0 ){
                            Intent startIntent = new Intent(SplashActivity.this, UpdateApp.class);
                            startService(startIntent);
                        }
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

    /**
     *
     * @param version
     * @param index
     *            the starting point
     * @return the number between two dots, and the index of the dot
     */
    public static int[] getValue(String version, int index) {
        int[] value_index = new int[2];
        StringBuilder sb = new StringBuilder();
        while (index < version.length() && version.charAt(index) != '.') {
            sb.append(version.charAt(index));
            index++;
        }
        value_index[0] = Integer.parseInt(sb.toString());
        value_index[1] = index;

        return value_index;
    }

    public static int VersionComparison(String versionServer, String versionLocal) {
        String version1 = versionServer;
        String version2 = versionLocal;
        if (version1 == null || version1.length() == 0 || version2 == null || version2.length() == 0)
            throw new IllegalArgumentException("Invalid parameter!");

        int index1 = 0;
        int index2 = 0;
        while (index1 < version1.length() && index2 < version2.length()) {
            int[] number1 = getValue(version1, index1);
            int[] number2 = getValue(version2, index2);

            if (number1[0] < number2[0]){
                return -1;
            }
            else if (number1[0] > number2[0]){
                return 1;
            }
            else {
                index1 = number1[1] + 1;
                index2 = number2[1] + 1;
            }
        }
        if (index1 == version1.length() && index2 == version2.length())
            return 0;
        if (index1 < version1.length())
            return 1;
        else
            return -1;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号，默认是1.0.0
     */
    public static String getVersion(Context mContext) {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }
}
