package com.live.hanbox.live_tang.wxapi;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.alipay.PayDemoActivity;
import com.live.hanbox.live_tang.app.LiveApp;
import com.live.hanbox.live_tang.tools.CommTools;
import com.live.hanbox.live_tang.view.CollectionFragment;
import com.live.hanbox.live_tang.view.DiscoverFragment;
import com.live.hanbox.live_tang.view.LiveFragment;
import com.live.hanbox.live_tang.view.MyFragment;
import com.live.hanbox.live_tang.view.live_ucloud.PublishDemo;
import com.live.hanbox.live_tang.view.live_ucloud.VideoActivity;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class WXEntryActivity extends AppCompatActivity implements LiveFragment.OnFragmentInteractionListener,CollectionFragment.OnFragmentInteractionListener,DiscoverFragment.OnFragmentInteractionListener,MyFragment.OnFragmentInteractionListener,IWXAPIEventHandler {
    private BottomNavigationBar bottom_bar;
    private LiveFragment m_LiveFragment = null;
    private CollectionFragment m_CollectionFragment  = null;
    private DiscoverFragment m_DiscoverFragment  = null;
    private MyFragment m_MyFragment  = null;
    private LiveApp m_app = null;
    private FloatingActionButton m_fab = null;
    private static Boolean isQuit = false;
    private Timer timer = new Timer();
    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wx78b065d04ec70e21";
    public static final String WX_SECRET = "a63859ff3b1cfea48707b993abe90849";
    public String WX_CODE = "";
    public String WX_TOKEN = "";

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI m_apiWX = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        m_apiWX = WXAPIFactory.createWXAPI(this, APP_ID, false);
        m_apiWX.registerApp(APP_ID);
        m_apiWX.handleIntent(getIntent(), this);

        m_app = (LiveApp) this.getApplication();
        m_app.setM_mainActivity(this);

        m_fab = (FloatingActionButton) findViewById(R.id.fab);
        m_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_DiscoverFragment != null) {
                    m_DiscoverFragment.reDectUrl(m_app.m_strHost + "topic/publish/");
                    m_fab.hide();
                }
            }
        });

        initView();
        initEvent();
    }

    private void initEvent() {
        bottom_bar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                if ( position == 0 ){
                    m_fab.show();
                }else {
                    m_fab.hide();
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (position) {
                    case 0:
                        if (m_DiscoverFragment == null) {
                            m_DiscoverFragment = DiscoverFragment.newInstance("1", "2");
                        }
                        fragmentTransaction.replace(R.id.tb, m_DiscoverFragment);

                        break;
                    case 1:
                        if (m_CollectionFragment == null) {
                            m_CollectionFragment = CollectionFragment.newInstance("1", "2");
                        }
                        fragmentTransaction.replace(R.id.tb, m_CollectionFragment);

                        break;
                    case 2:
                        if (m_LiveFragment == null) {
                            m_LiveFragment = LiveFragment.newInstance("1", "2");
                        }
                        fragmentTransaction.replace(R.id.tb, m_LiveFragment);
                        break;
                    case 3:
                        if (m_MyFragment == null) {
                            m_MyFragment = MyFragment.newInstance("1", "2");
                        }
                        fragmentTransaction.replace(R.id.tb, m_MyFragment);

                        break;
                }

                fragmentTransaction.commit();
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
    }

    /**
     * 设置默认的
     */
    private void setDefaultFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if ( m_DiscoverFragment == null )
            m_DiscoverFragment = DiscoverFragment.newInstance("1","2");
        fragmentTransaction.replace(R.id.tb, m_DiscoverFragment);
        fragmentTransaction.commit();
    }

    private void initView() {
        bottom_bar = (BottomNavigationBar) findViewById(R.id.bottom_bar);
        bottom_bar.addItem(new BottomNavigationItem(R.mipmap.menu_discover, getString(R.string.discover)))
                .addItem(new BottomNavigationItem(R.mipmap.menu_shoucang, getString(R.string.shoucang)))
                .addItem(new BottomNavigationItem(R.mipmap.menu_live, getString(R.string.live)))
                .addItem(new BottomNavigationItem(R.mipmap.menu_mine, getString(R.string.mine)))
                .initialise();

        bottom_bar.setMode(BottomNavigationBar.MODE_FIXED);
        bottom_bar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);

        setDefaultFragment();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private int mSupportedBitrateValues[] = new int[] { 2000, 1200, 800, 600 };
    private int mSupportedResolutionValues[] = new int[] { 1920, 1080, 1280, 720, 640, 480, 480, 360 };
    private int mSupportedFramerateValues[] = new int[] { 18, 15, 15, 15 };
    private boolean isOritationSwitcherChecked = false;
    private int mSelectedResolutionIndex = 1;

    @JavascriptInterface
    public void jsStartPushLive(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在里对Android应用的UI进行处理
                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, StreamingActivity.class);
                intent.setClass(WXEntryActivity.this, PublishDemo.class);
                intent.putExtra("push_url", str.split("####")[0]);
                intent.putExtra("char_room_id", str.split("####")[1]);
                intent.putExtra("char_id", str.split("####")[2]);
                intent.putExtra("user_id", str.split("####")[3]);
                startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void jsStartPullLive(final String str) {
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在里对Android应用的UI进行处理
                Intent intent = new Intent();
                //intent.setClass(MainActivity.this, VideoViewPlayingActivity.class);
                intent.setClass(WXEntryActivity.this, VideoActivity.class);
                intent.putExtra("pull_url", str.split("####")[0]);
                intent.putExtra("char_room_id", str.split("####")[1]);
                intent.putExtra("char_id", str.split("####")[2]);
                intent.putExtra("auth_id", str.split("####")[3]);
                intent.putExtra("user_id", str.split("####")[4]);
                startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void jsStartPay(final String str) {
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在里对Android应用的UI进行处理
                Intent intent = new Intent();
                intent.setClass(WXEntryActivity.this, PayDemoActivity.class);
                intent.putExtra("user_id", str.split("####")[0]);
                startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void jsStartRefund(final String str) {
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在里对Android应用的UI进行处理
                Intent intent = new Intent();
                intent.setClass(WXEntryActivity.this, PayDemoActivity.class);
                intent.putExtra("user_id", str.split("####")[0]);
                startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void jsLogin() {
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在里对Android应用的UI进行处理
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "wechat_sdk_demo_test";
                m_apiWX.sendReq(req);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( m_CollectionFragment != null && m_CollectionFragment.onKeyDown(keyCode, event) ){
            return true;
        }
        if ( m_LiveFragment != null && m_LiveFragment.onKeyDown(keyCode, event) ){
            return true;
        }
        if ( m_DiscoverFragment != null && m_DiscoverFragment.onKeyDown(keyCode, event) ){
            return true;
        }
        if ( m_MyFragment != null && m_MyFragment.onKeyDown(keyCode, event) ){
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isQuit == false) {
                isQuit = true;
                Toast.makeText(getBaseContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                TimerTask task = null;
                task = new TimerTask() {
                    @Override
                    public void run() {
                        isQuit = false;
                    }
                };
                timer.schedule(task, 2000);
            } else {
                finish();
                System.exit(0);
            }
        }
        return true;
    }

    @Override
    public void onReq(BaseReq baseReq) {
        String strdaa = baseReq.openId;
    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                WX_CODE = ((SendAuth.Resp) baseResp).code;
                getWXToken();
                break;

            default:
                break;
        }
    }

    private void getWXToken(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APP_ID + "&secret=" + WX_SECRET + "&code=" + WX_CODE + "&grant_type=authorization_code";
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

                    if(connection.getResponseCode() == 200){
                        InputStream is = connection.getInputStream();
                        String strRecv = CommTools.ConvertToString(is);
                        JSONObject jsStr = null;
                        String strMessage = "";

                        try {
                            jsStr = new JSONObject(strRecv);
                            String access_token = jsStr.getString("access_token");
                            String expires_in = jsStr.getString("expires_in");
                            String refresh_token = jsStr.getString("refresh_token");
                            String openid = jsStr.getString("openid");
                            String scope = jsStr.getString("scope");
                            String unionid = jsStr.getString("unionid");

                            getWXUserInfo(access_token, openid);
                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void getWXUserInfo(final String access_token, final String openid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid;
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

                    if(connection.getResponseCode() == 200){
                        InputStream is = connection.getInputStream();
                        String strRecv = CommTools.ConvertToString(is);
                        JSONObject jsStr = null;

                        try {
                            jsStr = new JSONObject(strRecv);
                            String nickname = jsStr.getString("nickname");
                            String headimgurl = jsStr.getString("headimgurl");
                            String unionid = jsStr.getString("unionid");

                            checkAuth(unionid, nickname, headimgurl);

                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void checkAuth(final String strUserid, final String strNackName, final String strImgUrl){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = m_app.m_strHost + "mine/chklogin/?user_id=" + strUserid + "&nickname=" + strNackName + "&headimgurl=" + strImgUrl + "&login_src=weixin";
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

                    if(connection.getResponseCode() == 200){
                        InputStream is = connection.getInputStream();
                        String strSessionid = CommTools.ConvertToString(is);
                        if ( strSessionid.equals("ok") ){
                            checkAuth(strUserid, strNackName, strImgUrl);
                            return;
                        }

                        strSessionid = strSessionid.substring(0, strSessionid.length() - 1);
                        strSessionid = "sessionid=" + strSessionid;

                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = strSessionid;
                        getHandler.sendMessage(msg);
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

    private Handler getHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0 ){
                String strSessionid = (String)msg.obj;
//                Toast.makeText(WXEntryActivity.this, strSessionid, Toast.LENGTH_SHORT).show();
                if ( m_CollectionFragment != null){
                    m_CollectionFragment.setCookie(strSessionid);
                }
                if ( m_LiveFragment != null ){
                    m_LiveFragment.setCookie(strSessionid);
                }
                if ( m_DiscoverFragment != null ){
                    m_DiscoverFragment.setCookie(strSessionid);
                }
                if ( m_MyFragment != null ){
                    m_MyFragment.setCookie(strSessionid);
                }
            }
        }
    };
}
