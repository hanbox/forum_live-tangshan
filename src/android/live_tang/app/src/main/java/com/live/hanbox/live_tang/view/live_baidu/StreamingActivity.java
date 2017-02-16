package com.live.hanbox.live_tang.view.live_baidu;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.recorder.api.LiveConfig;
import com.baidu.recorder.api.LiveSession;
import com.baidu.recorder.api.LiveSessionHW;
import com.baidu.recorder.api.LiveSessionSW;
import com.baidu.recorder.api.SessionStateListener;
import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.tools.CommTools;
import com.live.hanbox.live_tang.wxapi.WXEntryActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class StreamingActivity extends Activity
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final String TAG = "StreamingActivity";
    private LiveSession mLiveSession = null;
    private Button mRecorderButton = null;
    private ImageView mFocusIcon = null;
    private Button mFlashStateButton = null;
    private Button mBeautyEffectStateButton = null;
    private TextView statusView = null;
    private LinearLayout mLoadingAnimation = null;
    private boolean isSessionReady = false;
    private boolean isSessionStarted = false;
    private boolean isConnecting = false;
    private boolean needRestartAfterStopped = false;

    private static final int UI_EVENT_RECORDER_CONNECTING = 0;
    private static final int UI_EVENT_RECORDER_STARTED = 1;
    private static final int UI_EVENT_RECORDER_STOPPED = 2;
    private static final int UI_EVENT_SESSION_PREPARED = 3;
    private static final int UI_EVENT_HIDE_FOCUS_ICON = 4;
    private static final int UI_EVENT_RESTART_STREAMING = 5;
    private static final int UI_EVENT_RECONNECT_SERVER = 6;
    private static final int UI_EVENT_STOP_STREAMING = 7;
    private static final int UI_EVENT_SHOW_TOAST_MESSAGE = 8;
    private static final int UI_EVENT_RESIZE_CAMERA_PREVIEW = 9;
    private static final int TEST_EVENT_SHOW_UPLOAD_BANDWIDTH = 10;
    private Handler mUIEventHandler = null;
    private SurfaceView mCameraView = null;
    private SessionStateListener mStateListener = null;
    private GestureDetectorCompat mDetector = null;
    // private SurfaceHolder mHolder = null;
    private int mCurrentCamera = -1;
    private boolean isFlashOn = false;
    private boolean hasBueatyEffect = false;

    private int mVideoWidth = 1280;
    private int mVideoHeight = 720;
    private int mFrameRate = 15;
    private int mBitrate = 1024000;
    private String mStreamingUrl = null;
    private boolean isOritationLanscape = false;
    private String m_strChatid = "40";
    private String m_strRoomid = "e083ac8d-44a9-4570-8145-b8f4876b869b";
    private String m_strUserId = "";

    private ThreadGet m_thread = null;
    private String m_strlastChatid = "48";
    private ListView m_listChat = null;
    private ArrayAdapter<String> m_arrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.requestFeature(Window.FEATURE_NO_TITLE);
        isOritationLanscape = getIntent().getBooleanExtra("oritation_landscape", false);
        if (isOritationLanscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_streaming_landscape);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_streaming_portrait);
        }

        m_listChat = (ListView) findViewById(R.id.list_chat);
        m_arrayAdapter = new ArrayAdapter<String>(this, R.layout.layout_list_chat, R.id.text_chat);
        m_listChat.setAdapter(m_arrayAdapter);

        mVideoWidth = getIntent().getIntExtra("res_w", 1280);
        mVideoHeight = getIntent().getIntExtra("res_h", 720);
        mFrameRate = getIntent().getIntExtra("frame_rate", 15);
        mBitrate = getIntent().getIntExtra("bitrate", 1024) * 1000;
        mStreamingUrl = getIntent().getStringExtra("push_url");
        m_strChatid = getIntent().getStringExtra("char_id");
        m_strRoomid = getIntent().getStringExtra("char_room_id");
        m_strUserId = getIntent().getStringExtra("user_id");
        Toast.makeText(this, mStreamingUrl, Toast.LENGTH_SHORT).show();

        initUIElements();

        mCurrentCamera = LiveConfig.CAMERA_FACING_FRONT;
        isFlashOn = false;
        initUIEventHandler();
        initStateListener();
        initRTMPSession(mCameraView.getHolder());

        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        m_thread = new ThreadGet();
        m_thread.begin();
        m_thread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged orientation=" + newConfig.orientation);
        super.onConfigurationChanged(newConfig);
    }

    private void initUIElements() {
        mLoadingAnimation = (LinearLayout) findViewById(R.id.loading_anim);
        mRecorderButton = (Button) findViewById(R.id.btn_streaming_action);
        mRecorderButton.setEnabled(false);
        mCameraView = (SurfaceView) findViewById(R.id.sv_camera_preview);
        mFocusIcon = (ImageView) findViewById(R.id.iv_ico_focus);
        mFlashStateButton = (Button) findViewById(R.id.iv_flash_state);
        mBeautyEffectStateButton = (Button) findViewById(R.id.iv_effect_state);
        statusView = (TextView) findViewById(R.id.tv_streaming_action);
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

    public void OnClick(View v){
        if ( v.getId() == R.id.btn_send ){
            final String strMessage = ((EditText)findViewById(R.id.edit_send)).getText().toString();
            if ( strMessage.isEmpty() ){
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        if ( m_strRoomid.isEmpty() || m_strChatid.isEmpty() || m_strlastChatid.isEmpty() ){
                            return;
                        }

                        String strGetUrl = "http://192.168.0.103:8080/support/" + m_strRoomid + "/post_message/?" + m_strChatid + "=" + m_strlastChatid + "&message=" + URLEncoder.encode(strMessage, "UTF-8") + "&user_id=" + m_strUserId;
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
        }if ( v.getId() == R.id.image_music ){
            Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");
            startActivity(intent);
        }
    }

    private void initUIEventHandler() {
        mUIEventHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UI_EVENT_RECORDER_CONNECTING:
                        isConnecting = true;
                        mRecorderButton.setBackgroundResource(R.drawable.btn_block_streaming);
                        mRecorderButton.setPadding(0, 0, 0, 0);
                        statusView.setText("连接中");
                        mRecorderButton.setEnabled(false);
                        break;
                    case UI_EVENT_RECORDER_STARTED:
                        Log.i(TAG, "Starting Streaming succeeded!");
                        serverFailTryingCount = 0;
                        isSessionStarted = true;
                        needRestartAfterStopped = false;
                        isConnecting = false;
                        mRecorderButton.setBackgroundResource(R.drawable.btn_stop_streaming);
                        mRecorderButton.setPadding(0, 0, 0, 0);
                        statusView.setText("停止直播");
                        mRecorderButton.setEnabled(true);
                        break;
                    case UI_EVENT_RECORDER_STOPPED:
                        Log.i(TAG, "Stopping Streaming succeeded!");
                        serverFailTryingCount = 0;
                        isSessionStarted = false;
                        needRestartAfterStopped = false;
                        isConnecting = false;
                        mRecorderButton.setBackgroundResource(R.drawable.btn_start_streaming);
                        mRecorderButton.setPadding(0, 0, 0, 0);
                        statusView.setText("开始直播");
                        mRecorderButton.setEnabled(true);

                        endLive();

                        break;
                    case UI_EVENT_SESSION_PREPARED:
                        isSessionReady = true;
                        mLoadingAnimation.setVisibility(View.GONE);
                        mRecorderButton.setEnabled(true);
                        break;
                    case UI_EVENT_HIDE_FOCUS_ICON:
                        mFocusIcon.setVisibility(View.GONE);
                        break;
                    case UI_EVENT_RECONNECT_SERVER:
                        Log.i(TAG, "Reconnecting to server...");
                        if (isSessionReady && mLiveSession != null) {
                            mLiveSession.startRtmpSession(mStreamingUrl);
                        }
                        if (mUIEventHandler != null) {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
                        }

                        break;
                    case UI_EVENT_STOP_STREAMING:
                        if (!isConnecting) {
                            Log.i(TAG, "Stopping current session...");
                            if (isSessionReady) {
                                mLiveSession.stopRtmpSession();
                            }
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                        break;
                    case UI_EVENT_RESTART_STREAMING:
                        if (!isConnecting) {
                            Log.i(TAG, "Restarting session...");
                            isConnecting = true;
                            needRestartAfterStopped = true;
                            if (isSessionReady && mLiveSession != null) {
                                mLiveSession.stopRtmpSession();
                            }
                            if (mUIEventHandler != null) {
                                mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
                            }

                        }
                        break;
                    case UI_EVENT_SHOW_TOAST_MESSAGE:
                        String text = (String) msg.obj;
                        Toast.makeText(StreamingActivity.this, text, Toast.LENGTH_SHORT).show();
                        break;
                    case UI_EVENT_RESIZE_CAMERA_PREVIEW:
                        String hint = "注意：当前摄像头不支持您所选择的分辨率\n实际分辨率为" + mVideoWidth + "x" + mVideoHeight;
                        Toast.makeText(StreamingActivity.this, hint, Toast.LENGTH_LONG).show();
                        fitPreviewToParentByResolution(mCameraView.getHolder(), mVideoWidth, mVideoHeight);
                        break;
                    case TEST_EVENT_SHOW_UPLOAD_BANDWIDTH:
                        if (mLiveSession != null) {
                            Log.d(TAG, "Current upload bandwidth is " + mLiveSession.getCurrentUploadBandwidthKbps()
                                    + " KBps.");
                        }
                        if (mUIEventHandler != null) {
                            mUIEventHandler.sendEmptyMessageDelayed(TEST_EVENT_SHOW_UPLOAD_BANDWIDTH, 2000);
                        }
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void endLive(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    if ( m_strRoomid.isEmpty() || m_strChatid.isEmpty() || m_strlastChatid.isEmpty() ){
                        return;
                    }

                    String strGetUrl = "http://192.168.0.103:8080/live/wantlive/delroom/?session_push=" + mStreamingUrl;
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
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                    m_thread.end();
                }
            }
        }).start();

        Intent intent = new Intent();
        intent.setClass(StreamingActivity.this, WXEntryActivity.class);
        startActivity(intent);
        m_thread.end();
    }

    private void initStateListener() {
        mStateListener = new SessionStateListener() {
            @Override
            public void onSessionPrepared(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_SESSION_PREPARED);
                    }
                    int realWidth = mLiveSession.getAdaptedVideoWidth();
                    int realHeight = mLiveSession.getAdaptedVideoHeight();
                    if (realHeight != mVideoHeight || realWidth != mVideoWidth) {
                        mVideoHeight = realHeight;
                        mVideoWidth = realWidth;
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RESIZE_CAMERA_PREVIEW);
                    }
                }
            }

            @Override
            public void onSessionStarted(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STARTED);
                    }
                } else {
                    Log.e(TAG, "Starting Streaming failed!");
                }
            }

            @Override
            public void onSessionStopped(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        if (needRestartAfterStopped && isSessionReady) {
                            mLiveSession.startRtmpSession(mStreamingUrl);
                        } else {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                    }
                } else {
                    Log.e(TAG, "Stopping Streaming failed!");
                }
            }

            @Override
            public void onSessionError(int code) {
                switch (code) {
                    case SessionStateListener.ERROR_CODE_OF_OPEN_MIC_FAILED:
                        Log.e(TAG, "Error occurred while opening MIC!");
                        onOpenDeviceFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_OPEN_CAMERA_FAILED:
                        Log.e(TAG, "Error occurred while opening Camera!");
                        onOpenDeviceFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_PREPARE_SESSION_FAILED:
                        Log.e(TAG, "Error occurred while preparing recorder!");
                        onPrepareFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_CONNECT_TO_SERVER_FAILED:
                        Log.e(TAG, "Error occurred while connecting to server!");
                        if (mUIEventHandler != null) {
                            serverFailTryingCount++;
                            if (serverFailTryingCount > 5) {
                                Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
                                msg.obj = "自动重连服务器失败，请检查网络设置";
                                mUIEventHandler.sendMessage(msg);
                                mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                            } else {
                                Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
                                msg.obj = "连接推流服务器失败，自动重试5次，当前为第" + serverFailTryingCount + "次";
                                mUIEventHandler.sendMessage(msg);
                                mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_RECONNECT_SERVER, 2000);
                            }

                        }
                        break;
                    case SessionStateListener.ERROR_CODE_OF_DISCONNECT_FROM_SERVER_FAILED:
                        Log.e(TAG, "Error occurred while disconnecting from server!");
                        isConnecting = false;
                        // Although we can not stop session successfully, we still
                        // need to take it as stopped
                        if (mUIEventHandler != null) {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                        break;
                    default:
                        onStreamingError(code);
                        break;
                }
            }
        };
    }

    int serverFailTryingCount = 0;

    private void onOpenDeviceFailed() {
        if (mUIEventHandler != null) {
            Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
            msg.obj = "摄像头或MIC打开失败！请确认您已开启相关硬件使用权限！";
            mUIEventHandler.sendMessage(msg);
        }
        // StreamingActivity.this.finish();
    }

    private void onPrepareFailed() {
        isSessionReady = false;
    }

    int mWeakConnectionHintCount = 0;

    private void onStreamingError(int errno) {
        Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
        switch (errno) {
            case SessionStateListener.ERROR_CODE_OF_PACKET_REFUSED_BY_SERVER:
            case SessionStateListener.ERROR_CODE_OF_SERVER_INTERNAL_ERROR:
                msg.obj = "因服务器异常，当前直播已经中断！正在尝试重新推流...";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                }
                break;
            case SessionStateListener.ERROR_CODE_OF_WEAK_CONNECTION:
                Log.i(TAG, "Weak connection...");
                msg.obj = "当前网络不稳定，请检查网络信号！";
                mWeakConnectionHintCount++;
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    if (mWeakConnectionHintCount >= 5) {
                        mWeakConnectionHintCount = 0;
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                    }
                }
                break;
            case SessionStateListener.ERROR_CODE_OF_CONNECTION_TIMEOUT:
                Log.i(TAG, "Timeout when streaming...");
                msg.obj = "连接超时，请检查当前网络是否畅通！我们正在努力重连...";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                }
                break;
            default:
                Log.i(TAG, "Unknown error when streaming...");
                msg.obj = "未知错误，当前直播已经中断！正在重试！";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_RESTART_STREAMING, 1000);
                }
                break;
        }
    }

    private void initRTMPSession(SurfaceHolder sh) {
        int orientation = isOritationLanscape ? LiveConfig.ORIENTATION_LANDSCAPE : LiveConfig.ORIENTATION_PORTRAIT;
        LiveConfig liveConfig = new LiveConfig.Builder().setCameraId(LiveConfig.CAMERA_FACING_FRONT) // 选择摄像头为前置摄像头
                .setCameraOrientation(orientation) // 设置摄像头为竖向
                .setVideoWidth(mVideoWidth) // 设置推流视频宽度, 需传入长的一边
                .setVideoHeight(mVideoHeight) // 设置推流视频高度，需传入短的一边
                .setVideoFPS(mFrameRate) // 设置视频帧率
                .setInitVideoBitrate(mBitrate) // 设置视频码率，单位为bit per seconds
                .setAudioBitrate(64 * 1000) // 设置音频码率，单位为bit per seconds
                .setAudioSampleRate(LiveConfig.AUDIO_SAMPLE_RATE_44100) // 设置音频采样率
                .setGopLengthInSeconds(2) // 设置I帧间隔，单位为秒
                .build();
        Log.d(TAG, "Calling initRTMPSession..." + liveConfig.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mLiveSession = new LiveSessionHW(this, liveConfig);
        } else {
            mLiveSession = new LiveSessionSW(this, liveConfig);
        }
        mLiveSession.setStateListener(mStateListener);
        mLiveSession.bindPreviewDisplay(sh);
        mLiveSession.prepareSessionAsync();
    }

    public void onClickQuit(View v) {
        if (isSessionStarted) {
            Toast.makeText(this, "直播过程中不能返回，请先停止直播！", Toast.LENGTH_SHORT).show();
        } else {
            this.finish();

            endLive();
        }
    }

    public void onClickSwitchFlash(View v) {
        if (mCurrentCamera == LiveConfig.CAMERA_FACING_BACK) {
            mLiveSession.toggleFlash(!isFlashOn);
            isFlashOn = !isFlashOn;
            if (isFlashOn) {
                mFlashStateButton.setBackgroundResource(R.drawable.btn_flash_on);
            } else {
                mFlashStateButton.setBackgroundResource(R.drawable.btn_flash_off);
            }
        }
    }

    public void onClickSwitchCamera(View v) {
        if (mLiveSession.canSwitchCamera()) {
            if (mCurrentCamera == LiveConfig.CAMERA_FACING_BACK) {
                mCurrentCamera = LiveConfig.CAMERA_FACING_FRONT;
                mLiveSession.switchCamera(mCurrentCamera);
                if (isFlashOn) {
                    mFlashStateButton.setBackgroundResource(R.drawable.btn_flash_off);
                }
            } else {
                mCurrentCamera = LiveConfig.CAMERA_FACING_BACK;
                mLiveSession.switchCamera(mCurrentCamera);
                if (isFlashOn) {
                    mFlashStateButton.setBackgroundResource(R.drawable.btn_flash_on);
                }
            }
        } else {
            Toast.makeText(this, "抱歉！该分辨率下不支持切换摄像头！", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSwitchBeautyEffect(View v) {
        hasBueatyEffect = !hasBueatyEffect;
        mLiveSession.enableDefaultBeautyEffect(hasBueatyEffect);
        mBeautyEffectStateButton
                .setBackgroundResource(hasBueatyEffect ? R.drawable.btn_effect_on : R.drawable.btn_effect_off);
    }

    public void onClickStreamingButton(View v) {
        if (!isSessionReady) {
            return;
        }
        if (!isSessionStarted && !TextUtils.isEmpty(mStreamingUrl)) {
            if (mLiveSession.startRtmpSession(mStreamingUrl)) {
                Log.i(TAG, "Starting Streaming in right state!");
            } else {
                Log.e(TAG, "Starting Streaming in wrong state!");
            }
            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
        } else {
            if (mLiveSession.stopRtmpSession()) {
                Log.i(TAG, "Stopping Streaming in right state!");
            } else {
                Log.e(TAG, "Stopping Streaming in wrong state!");
            }
            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
        }
    }

    @Override
    public void onBackPressed() {
        if (isSessionStarted) {
            Toast.makeText(this, "直播过程中不能返回，请先停止直播！", Toast.LENGTH_SHORT).show();
        } else {
            finish();

            endLive();
        }
    }

    @Override
    public void onStart() {
        Log.i(TAG, "===========> onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "===========> onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "===========> onDestroy()");
        mUIEventHandler.removeCallbacksAndMessages(null);
        if (isSessionStarted) {
            mLiveSession.stopRtmpSession();
            isSessionStarted = false;
        }
        if (isSessionReady) {
            mLiveSession.destroyRtmpSession();
            mLiveSession = null;
            mStateListener = null;
            mUIEventHandler = null;
            isSessionReady = false;
        }
        super.onDestroy();

        endLive();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg0) {
        if (mLiveSession != null && !mLiveSession.zoomInCamera()) {
            Log.e(TAG, "Zooming camera failed!");
            mLiveSession.cancelZoomCamera();
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg0) {
        if (mLiveSession != null) {
            mLiveSession.focusToPosition((int) arg0.getX(), (int) arg0.getY());
            mFocusIcon.setX(arg0.getX() - mFocusIcon.getWidth() / 2);
            mFocusIcon.setY(arg0.getY() - mFocusIcon.getHeight() / 2);
            mFocusIcon.setVisibility(View.VISIBLE);
            mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_HIDE_FOCUS_ICON, 1000);
        }
        return true;
    }

    private void fitPreviewToParentByResolution(SurfaceHolder holder, int width, int height) {
        // Adjust the size of SurfaceView dynamically
        int screenHeight = getWindow().getDecorView().getRootView().getHeight();
        int screenWidth = getWindow().getDecorView().getRootView().getWidth();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) { // If
            // portrait,
            // we
            // should
            // swap
            // width
            // and
            // height
            width = width ^ height;
            height = width ^ height;
            width = width ^ height;
        }
        // Fit height
        int adjustedVideoHeight = screenHeight;
        int adjustedVideoWidth = screenWidth;
        if (width * screenHeight > height * screenWidth) { // means width/height
            // >
            // screenWidth/screenHeight
            // Fit width
            adjustedVideoHeight = height * screenWidth / width;
            adjustedVideoWidth = screenWidth;
        } else {
            // Fit height
            adjustedVideoHeight = screenHeight;
            adjustedVideoWidth = width * screenHeight / height;
        }
        holder.setFixedSize(adjustedVideoWidth, adjustedVideoHeight);
    }

}
