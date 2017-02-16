package com.live.hanbox.live_tang.view.live_ucloud;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.app.LiveApp;
import com.live.hanbox.live_tang.tools.CommTools;
import com.live.hanbox.live_tang.view.live_ucloud.tools.Settings;
import com.live.hanbox.live_tang.view.sendGiftActivity;
import com.ucloud.common.logger.L;
import com.ucloud.player.widget.v2.UVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class VideoActivity extends AppCompatActivity implements UVideoView.Callback {

    private static final String TAG = "VideoActivity";

    private UVideoView mVideoView;

    String rtmpPlayStreamUrl = "http://rtmp.tangshanrenzhibo.cn/rtmp//tangshanrenzhibo/ucloud/mytest/%s.flv";

    Settings mSettings;

    private ListView m_listChat = null;
    private ArrayAdapter<String> m_arrayAdapter = null;

    private ThreadGet m_thread = null;
    private String m_strlastChatid = "48";
    private String m_strChatid = "40";
    private String m_strRoomid = "e083ac8d-44a9-4570-8145-b8f4876b869b";
    private String m_strUserId = "";
    private String m_strAuthId = "";
    private sendGiftActivity m_sendGiftActivity = null;
    private LiveApp m_app = null;
    private String m_strUrl = "0";
    private String m_strNickname = "";
    private String m_strHeadUrl = "";
    private Toast toast = null;
    private Drawable m_dHeadImg = null;
    private int m_dGift = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window= VideoActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);

        m_app = (LiveApp) this.getApplication();
        setContentView(R.layout.activity_play);
        mVideoView = (UVideoView) findViewById(R.id.videoview);

        mSettings = new Settings(this);

        mVideoView.setPlayType(UVideoView.PlayType.LIVE);
        mVideoView.setPlayMode(UVideoView.PlayMode.NORMAL);
        mVideoView.setRatio(UVideoView.VIDEO_RATIO_FILL_PARENT);
        mVideoView.setDecoder(UVideoView.DECODER_VOD_SW);

        mVideoView.registerCallback(this);

        m_strUrl = getIntent().getStringExtra("pull_url");
        mVideoView.setVideoPath(String.format(rtmpPlayStreamUrl, m_strUrl));

        m_listChat = (ListView) findViewById(R.id.list_chat);
        m_arrayAdapter = new ArrayAdapter<String>(this, R.layout.layout_list_chat, R.id.text_chat);
        m_listChat.setAdapter(m_arrayAdapter);
        m_listChat.setFocusable(true);
        m_listChat.requestFocus();

        m_strChatid = getIntent().getStringExtra("char_id");
        m_strRoomid = getIntent().getStringExtra("char_room_id");
        m_strAuthId = getIntent().getStringExtra("auth_id");
        m_strUserId = getIntent().getStringExtra("user_id");

        getUserInfo();

        m_thread = new ThreadGet();
        m_thread.begin();
        m_thread.start();
    }

    public class ThreadGet extends Thread{
        private boolean bState  = false;

        public void begin(){
            bState = true;
        }

        public void end(){
            bState = false;
        }

        public void run() {
            while ( bState ){
                HttpURLConnection connection = null;
                try {
                    if ( m_strRoomid.isEmpty() || m_strChatid.isEmpty() || m_strlastChatid.isEmpty() ){
                        continue;
                    }

                    String strGetUrl = m_app.m_strHost + "support/" + m_strRoomid + "/get_messages/?" + m_strChatid + "=" + m_strlastChatid;
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
                    // 设置请求参数，可通过Servlet的getHeader()获取
                    connection.setRequestProperty("Cookie", "AppName=" + URLEncoder.encode("你好", "UTF-8"));

                    if(connection.getResponseCode() == 200){
                        InputStream is = connection.getInputStream();
                        String strRecv = CommTools.ConvertToString(is);

                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = strRecv;
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

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    };

    private Handler getHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 0 ){
                JSONObject jsStr = null;
                String strMessage = "";

                try {
                    jsStr = new JSONObject((String)msg.obj);
                    JSONArray list_RecvMsg = jsStr.getJSONObject("chats").getJSONObject(m_strChatid).getJSONArray("messages");
                    JSONObject obrecvMsg = list_RecvMsg.getJSONObject(0);
                    String strid = obrecvMsg.getString("pk");
                    String strName = obrecvMsg.getString("name");
                    String strData = obrecvMsg.getString("message");

                    strMessage = strName;
                    strMessage += ":";
                    strMessage += strData;
                    if ( !m_strlastChatid.equals(strid) ){
                        m_arrayAdapter.add(strMessage);
                        m_strlastChatid = strid;

                        m_listChat.setSelection(m_arrayAdapter.getCount()-1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(msg.what == 1 ){
                String strPayCount = (String)msg.obj;

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.sendgift, (ViewGroup) findViewById(R.id.llToast));
                TextView nickname = (TextView) layout.findViewById(R.id.text_gift_nickname);
                nickname.setText(m_strNickname);
                ImageView image = (ImageView) layout.findViewById(R.id.img_gift_head);
                image.setImageDrawable(m_dHeadImg);
                ImageView image_gift = (ImageView) layout.findViewById(R.id.img_gift_gift);
                image_gift.setImageResource(m_dGift);

                toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.RIGHT | Gravity.TOP, 12, 40);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();

            }else if(msg.what == 2 ){
                Toast.makeText(VideoActivity.this, "余额不足，请充值后重试！", Toast.LENGTH_SHORT).show();
            }else if(msg.what == 3 ){

            }
        }
    };

    public void OnClick(View v){
        if ( v.getId() == R.id.btn_send ){
            final String strMessage = ((EditText)findViewById(R.id.edit_send)).getText().toString();
            if ( strMessage.isEmpty() ){
                return;
            }

            sendChatMessage(strMessage);
        }else if ( v.getId() == R.id.image_gift ){
            m_sendGiftActivity = new sendGiftActivity(VideoActivity.this, null);
            m_sendGiftActivity.showAtLocation(VideoActivity.this.findViewById(R.id.root1), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        }else if ( v.getId() == R.id.image_gift_1 ){
            PayforAuth("1");
            m_dGift = R.drawable.gift_1;
        }else if ( v.getId() == R.id.image_gift_2 ){
            PayforAuth("1");
            m_dGift = R.drawable.gift_2;
        }else if ( v.getId() == R.id.image_gift_3 ){
            PayforAuth("1");
            m_dGift = R.drawable.gift_3;
        }else if ( v.getId() == R.id.image_gift_4 ){
            PayforAuth("1");
            m_dGift = R.drawable.gift_4;
        }else if ( v.getId() == R.id.image_gift_5 ){
            PayforAuth("5");
            m_dGift = R.drawable.gift_5;
        }else if ( v.getId() == R.id.image_gift_6 ){
            PayforAuth("8");
            m_dGift = R.drawable.gift_6;
        }else if ( v.getId() == R.id.image_gift_7 ){
            PayforAuth("18");
            m_dGift = R.drawable.gift_7;
        }else if ( v.getId() == R.id.image_gift_8 ){
            PayforAuth("28");
            m_dGift = R.drawable.gift_8;
        }else if ( v.getId() == R.id.image_gift_9 ){
            PayforAuth("38");
            m_dGift = R.drawable.gift_9;
        }else if ( v.getId() == R.id.image_gift_10 ){
            PayforAuth("48");
            m_dGift = R.drawable.gift_10;
        }else if ( v.getId() == R.id.image_gift_11 ){
            PayforAuth("68");
            m_dGift = R.drawable.gift_11;
        }else if ( v.getId() == R.id.image_gift_12 ){
            PayforAuth("88");
            m_dGift = R.drawable.gift_12;
        }else if ( v.getId() == R.id.image_gift_13 ){
            PayforAuth("188");
            m_dGift = R.drawable.gift_13;
        }else if ( v.getId() == R.id.image_gift_14 ){
            PayforAuth("288");
            m_dGift = R.drawable.gift_14;
        }else if ( v.getId() == R.id.image_gift_15 ){
            PayforAuth("666");
            m_dGift = R.drawable.gift_15;
        }else if ( v.getId() == R.id.image_gift_16 ){
            PayforAuth("888");
            m_dGift = R.drawable.gift_16;
        }else if ( v.getId() == R.id.image_gift_17 ){
            PayforAuth("999");
            m_dGift = R.drawable.gift_17;
        }
    }

    private void getUserInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = m_app.m_strHost + "mine/getuserinfo/?user_id=" + m_strUserId;
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
                        String strRet = CommTools.ConvertToString(is);
                        String[] strRecv = strRet.split(",");
                        if ( strRecv != null && strRecv.length >= 2 ){
                            m_strNickname = strRecv[0];
                            m_strHeadUrl = strRecv[1];
                            m_strHeadUrl = m_strHeadUrl.substring(0, m_strHeadUrl.length() - 1);

                            try {
                                // 可以在这里通过文件名来判断，是否本地有此图片
                                m_dHeadImg = Drawable.createFromStream(
                                        new URL(m_strHeadUrl).openStream(), "image.jpg");
                            } catch (IOException e) {
                                Log.d("test", e.getMessage());
                            }
                            if (m_dHeadImg == null) {
                                Log.d("test", "null drawable");
                            } else {
                                Log.d("test", "not null drawable");
                            }
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

    private void PayforAuth(final String strPayCount){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = m_app.m_strHost + "vircurrency/pay/?count_pay=" + strPayCount + "&auth_id=" + m_strAuthId + "&user_id=" + m_strUserId;
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
                        String strRet = CommTools.ConvertToString(is);
                        if ( strRet.contains("ok") ){
                            Message msg = Message.obtain();
                            msg.what = 1;
                            msg.obj = strPayCount;
                            getHandler.sendMessage(msg);
                        }else{
                            Message msg = Message.obtain();
                            msg.what = 2;
                            getHandler.sendMessage(msg);
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

    private void sendChatMessage(final String strMsg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    if ( m_strRoomid.isEmpty() || m_strChatid.isEmpty() || m_strlastChatid.isEmpty() ){
                        return;
                    }

                    String strGetUrl = m_app.m_strHost + "support/" + m_strRoomid + "/post_message/?" + m_strChatid + "=" + m_strlastChatid + "&message=" + URLEncoder.encode(strMsg, "UTF-8") + "&user_id=" + m_strUserId;
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
                    // 设置请求参数，可通过Servlet的getHeader()获取
                    connection.setRequestProperty("Cookie", "AppName=" + URLEncoder.encode("你好", "UTF-8"));
                    // 设置自定义参数
                    connection.setRequestProperty("MyProperty", "this is me!");

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

        ((EditText)findViewById(R.id.edit_send)).setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.setVolume(0,0);
            mVideoView.stopPlayback();
            mVideoView.release(true);
        }
    }

    @Override
    public void onEvent(int what, String message) {
        Log.d(TAG, "what:" + what + ", message:" + message);
        switch (what) {
            case UVideoView.Callback.EVENT_PLAY_START:
                break;
            case UVideoView.Callback.EVENT_PLAY_PAUSE:
                break;
            case UVideoView.Callback.EVENT_PLAY_STOP:
                break;
            case UVideoView.Callback.EVENT_PLAY_COMPLETION:
                Toast.makeText(this, "主播未开播！", Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "EVENT_PLAY_COMPLETION", Toast.LENGTH_SHORT).show();
                break;
            case UVideoView.Callback.EVENT_PLAY_DESTORY:
                break;
            case UVideoView.Callback.EVENT_PLAY_ERROR:
                Toast.makeText(this, "主播未开播！", Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "EVENT_PLAY_ERROR:" + message, Toast.LENGTH_SHORT).show();
                break;
            case UVideoView.Callback.EVENT_PLAY_RESUME:
                break;
            case UVideoView.Callback.EVENT_PLAY_INFO_BUFFERING_START:
                L.e(TAG, "network block start....");
//              Toast.makeText(VideoActivity.this, "unstable network", Toast.LENGTH_SHORT).show();
                break;
            case UVideoView.Callback.EVENT_PLAY_INFO_BUFFERING_END:
                L.e(TAG, "network block end....");
                break;
        }
    }

    public void close(View view) {
        finish();
    }
}
