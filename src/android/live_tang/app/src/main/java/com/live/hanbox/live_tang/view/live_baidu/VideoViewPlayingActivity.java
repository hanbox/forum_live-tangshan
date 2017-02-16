package com.live.hanbox.live_tang.view.live_baidu;

import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.core.BVideoView.OnCompletionListener;
import com.baidu.cyberplayer.core.BVideoView.OnErrorListener;
import com.baidu.cyberplayer.core.BVideoView.OnInfoListener;
import com.baidu.cyberplayer.core.BVideoView.OnPreparedListener;
import com.baidu.cyberplayer.core.BVideoView.OnPlayingBufferCacheListener;
import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.tools.CommTools;
import com.live.hanbox.live_tang.view.sendGiftActivity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.PowerManager.WakeLock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class VideoViewPlayingActivity extends Activity implements OnPreparedListener, OnCompletionListener,
        OnErrorListener, OnInfoListener, OnPlayingBufferCacheListener
{

    private final String TAG = "VideoViewPlayingActivity";

    /**
     * 您的AK
     * 请到http://console.bce.baidu.com/iam/#/iam/accesslist获取
     */
    private String AK = "6932006e0b11421082cdc4a03293c1f1";   //请录入您的AK !!!

    private String mVideoSource = null;

    private ListView m_listChat = null;
    private ArrayAdapter<String> m_arrayAdapter = null;

    /**
     * 记录播放位置
     */
    private int mLastPos = 0;

    /**
     * 播放状态
     */
    private  enum PLAYER_STATUS {
        PLAYER_IDLE, PLAYER_PREPARING, PLAYER_PREPARED,
    }

    private PLAYER_STATUS mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;

    private BVideoView mVV = null;

    private EventHandler mEventHandler;
    private HandlerThread mHandlerThread;

    private final Object SYNC_Playing = new Object();

    private WakeLock mWakeLock = null;
    private static final String POWER_LOCK = "VideoViewPlayingActivity";

    private boolean mIsHwDecode = false;

    private final int EVENT_PLAY = 0;
    private final int UI_EVENT_UPDATE_CURRPOSITION = 1;

    private ThreadGet m_thread = null;
    private String m_strlastChatid = "48";
    private String m_strChatid = "40";
    private String m_strRoomid = "e083ac8d-44a9-4570-8145-b8f4876b869b";
    private String m_strUserId = "";
    private String m_strAuthId = "";
    private sendGiftActivity m_sendGiftActivity = null;

    class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PLAY:
                    /**
                     * 如果已经播放了，等待上一次播放结束
                     */
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        synchronized (SYNC_Playing) {
                            try {
                                SYNC_Playing.wait();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }

                    /**
                     * 设置播放url
                     */
                    mVV.setVideoPath(mVideoSource);

                    /**
                     * 续播，如果需要如此
                     */
                    if (mLastPos > 0) {

                        mVV.seekTo(mLastPos);
                        mLastPos = 0;
                    }

                    /**
                     * 显示或者隐藏缓冲提示
                     */
                    mVV.showCacheInfo(true);

                    /**
                     * 开始播放
                     */
                    mVV.start();

                    mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
                    break;
                default:
                    break;
            }
        }
    }

    public void OnClick(View v){
        if ( v.getId() == R.id.btn_send ){
            final String strMessage = ((EditText)findViewById(R.id.edit_send)).getText().toString();
            if ( strMessage.isEmpty() ){
                return;
            }

            sendMessage(strMessage);
        }else if ( v.getId() == R.id.image_gift ){
            m_sendGiftActivity = new sendGiftActivity(VideoViewPlayingActivity.this, null);
            m_sendGiftActivity.showAtLocation(VideoViewPlayingActivity.this.findViewById(R.id.root1), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        }else if ( v.getId() == R.id.image_gift_1 ){
            PayforAuth("1");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    BVideoView.getMediaInfo(VideoViewPlayingActivity.this, mVideoSource);
                    String[] lsit1 = BVideoView.getSupportedResolution();
                    System.out.println(lsit1);
                    int[] lsit2 = BVideoView.getSupportedBitrateKb();
                    System.out.println(lsit1);
                }
            }).start();
        }else if ( v.getId() == R.id.image_gift_2 ){
            PayforAuth("5");
        }else if ( v.getId() == R.id.image_gift_3 ){
            PayforAuth("10");
        }else if ( v.getId() == R.id.image_gift_4 ){
            PayforAuth("50");
        }else if ( v.getId() == R.id.image_gift_5 ){
            PayforAuth("100");
        }else if ( v.getId() == R.id.image_gift_6 ){
            PayforAuth("200");
        }
    }

    private void sendMessage(final String strMsg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    if ( m_strRoomid.isEmpty() || m_strChatid.isEmpty() || m_strlastChatid.isEmpty() ){
                        return;
                    }

                    String strGetUrl = "http://192.168.0.103:8080/support/" + m_strRoomid + "/post_message/?" + m_strChatid + "=" + m_strlastChatid + "&message=" + URLEncoder.encode(strMsg, "UTF-8") + "&user_id=" + m_strUserId;
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

    private void PayforAuth(final String strPayCount){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    String strGetUrl = "http://192.168.0.103:8080/vircurrency/pay/?count_pay=" + strPayCount + "&auth_id=" + m_strAuthId + "&user_id=" + m_strUserId;
                    URL url = new URL(strGetUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方法，默认是GET
                    connection.setRequestMethod("GET");
                    // 设置字符集
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型
                    connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

                    if(connection.getResponseCode() == 200){
                        sendMessage("<------" + m_strUserId + "用户赠送给主播" + strPayCount + "个唐币----->");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_view_playing);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, POWER_LOCK);

        mIsHwDecode = getIntent().getBooleanExtra("isHW", false);

        String strUrl = "rtmp://play.bcelive.com/live/lss-gjttqaxir9fq713i";
        strUrl = getIntent().getStringExtra("pull_url");
        Uri uriPath = Uri.parse(strUrl);
        Toast.makeText(this, getIntent().getStringExtra("pull_url"), Toast.LENGTH_LONG).show();
        if (null != uriPath) {
            String scheme = uriPath.getScheme();
            if (null != scheme) {
                mVideoSource = uriPath.toString();
            } else {
                mVideoSource = uriPath.getPath();
            }
        }

        m_strChatid = getIntent().getStringExtra("char_id");
        m_strRoomid = getIntent().getStringExtra("char_room_id");
        m_strAuthId = getIntent().getStringExtra("auth_id");
        m_strUserId = getIntent().getStringExtra("user_id");

        m_thread = new ThreadGet();
        m_thread.begin();
        initUI();
        m_thread.start();

        /**
         * 开启后台事件处理线程
         */
        mHandlerThread = new HandlerThread("event handler thread",
                Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());
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

                    String strGetUrl = "http://192.168.0.103:8080/support/" + m_strRoomid + "/get_messages/?" + m_strChatid + "=" + m_strlastChatid;
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


            }
        }
    };


    /**
     * 初始化界面
     */
    private void initUI() {
        m_listChat = (ListView) findViewById(R.id.list_chat);
        m_arrayAdapter = new ArrayAdapter<String>(this, R.layout.layout_list_chat, R.id.text_chat);
        m_listChat.setAdapter(m_arrayAdapter);
        m_listChat.setFocusable(true);
        m_listChat.requestFocus();
        /**
         * 设置ak
         */
        BVideoView.setAK(AK);

        /**
         *获取BVideoView对象
         */
        mVV = (BVideoView) findViewById(R.id.video_view);

        /**
         * 注册listener
         */
        mVV.setOnPreparedListener(this);
        mVV.setOnCompletionListener(this);
        mVV.setOnErrorListener(this);
        mVV.setOnInfoListener(this);

        /**
         * 设置解码模式
         */
        mVV.setDecodeMode(mIsHwDecode?BVideoView.DECODE_HW:BVideoView.DECODE_SW);
    }

    private void updateTextViewWithTimeFormat(TextView view, int second){
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String strTemp = null;
        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }
        view.setText(strTemp);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        /**
         * 在停止播放前 你可以先记录当前播放的位置,以便以后可以续播
         */
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_PREPARED) {
            mLastPos = mVV.getCurrentPosition();
            mVV.stopPlayback();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (null != mWakeLock && (!mWakeLock.isHeld())) {
            mWakeLock.acquire();
        }
        /**
         * 发起一次播放任务,当然您不一定要在这发起
         */
        mEventHandler.sendEmptyMessage(EVENT_PLAY);
    }

    private long mTouchTime;
    private boolean barShow = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            mTouchTime = System.currentTimeMillis();
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            long time = System.currentTimeMillis() - mTouchTime;
            if (time < 400) {
//                updateControlBar(!barShow);
            }
        }

        return true;
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        /**
         * 退出后台事件处理线程
         */
        mHandlerThread.quit();
        m_thread.end();
    }

    @Override
    public boolean onInfo(int what, int extra) {
        // TODO Auto-generated method stub
        switch(what){
            /**
             * 开始缓冲
             */
            case BVideoView.MEDIA_INFO_BUFFERING_START:
                break;
            /**
             * 结束缓冲
             */
            case BVideoView.MEDIA_INFO_BUFFERING_END:
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 当前缓冲的百分比， 可以配合onInfo中的开始缓冲和结束缓冲来显示百分比到界面
     */
    @Override
    public void onPlayingBufferCache(int percent) {
        // TODO Auto-generated method stub

    }

    /**
     * 播放出错
     */
    @Override
    public boolean onError(int what, int extra) {
        // TODO Auto-generated method stub
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        return true;
    }

    /**
     * 播放完成
     */
    @Override
    public void onCompletion() {
        // TODO Auto-generated method stub
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
    }

    /**
     * 准备播放就绪
     */
    @Override
    public void onPrepared() {
        // TODO Auto-generated method stub
        mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
    }
}
