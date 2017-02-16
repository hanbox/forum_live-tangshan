package com.live.hanbox.live_tang.view.live_ucloud;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.app.LiveApp;
import com.live.hanbox.live_tang.tools.CommTools;
import com.live.hanbox.live_tang.view.live_ucloud.permission.PermissionsActivity;
import com.live.hanbox.live_tang.view.live_ucloud.permission.PermissionsChecker;
import com.live.hanbox.live_tang.view.live_ucloud.tools.Settings;
import com.ucloud.common.util.DeviceUtils;
import com.ucloud.common.util.StringUtil;
import com.ucloud.live.UEasyStreaming;
import com.ucloud.live.UStreamingProfile;
import com.ucloud.live.widget.UAspectFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * 设置美颜滤镜效果 -> UEasyStreaming.applyFilterLevel()
 * 可参考以下5个效果等级：<br>
 * 1. 50, 0, 0<br>
 * 2. 60, 26, 14<br>
 * 3. 70, 53, 29<br>
 * 4. 80, 79, 69<br>
 * 5. 84, 100, 71<br>
 */
public class PublishDemo extends AppCompatActivity implements UEasyStreaming.UStreamingStateListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "PublishDemo";

    public static final int MSG_UPDATE_COUNTDOWN = 1;

    public static final int COUNTDOWN_DELAY = 1000;

    public static final  int COUNTDOWN_START_INDEX = 3;

    public static final  int COUNTDOWN_END_INDEX = 1;

    private static final int REQUEST_CODE = 0;

    protected Settings mSettings;

    protected String rtmpPushStreamDomain = "push.tangshanrenzhibo.cn";

    //Views
    protected ImageView mCameraToggleIv;
    protected ImageView mLampToggleIv;
    protected ImageButton mCloseRecorderImgBtn;
    protected Button mBackImgBtn;
    protected Button mCancelImgBtn;
    protected View mFocusIndex;
    protected TextView mBitrateTxtv;
    protected TextView mCountDownTxtv;
    protected TextView mRecordedTimeTxtv;
    protected TextView mOutputStreamInfoTxtv;
    protected TextView mBufferOverfloInfoTxtv;
    protected TextView mStatusInfoTxtv;
    protected ViewGroup mContainer;

    /* 磨皮、美白、肤色 */
    private SeekBar mSeekBar1;
    private SeekBar mSeekBar2;
    private SeekBar mSeekBar3;
    private TextView mV1;
    private TextView mV2;
    private TextView mV3;
    private int level1 = 50;
    private int level2 = 30;
    private int level3 = 20;

    protected UAspectFrameLayout mPreviewContainer;


    //for filter
    private ImageButton mToggleFilterImgBtn;

    protected boolean isShutDownCountdown = false;

    protected UEasyStreaming mEasyStreaming;

    protected UStreamingProfile mStreamingProfile;

    protected UiHandler uiHandler;

    protected int DEFAULT_CAMERA_ID = UStreamingProfile.CAMERA_FACING_BACK; //UStreamingProfile.CAMERA_FACING_BACK

    protected int whichCamera = DEFAULT_CAMERA_ID;

    private PermissionsChecker mPermissionsChecker; // 权限检测器

    private ScrollView mScrollView;

    private boolean isMute = false;

    private String mStreamingUrl = null;
    private String m_strChatid = "40";
    private String m_strRoomid = "e083ac8d-44a9-4570-8145-b8f4876b869b";
    private String m_strUserId = "";
    private String m_strlastChatid = "48";
    private ArrayAdapter<String> m_arrayAdapter = null;

    private ListView m_listChat = null;

    private ThreadGet m_thread = null;
    private LinearLayout beautyLevel = null;
    private boolean m_iBeautySettingState = false;

    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private LiveApp m_app = null;

    @Override
    public void onStateChanged(int type, Object event) {
       switch (type) {
            case UEasyStreaming.State.MSG_START_PREVIEW:
                //预览成功，startRecording方法需要在预览成功之后调用，若不想立即推流，可以给一个标志位，在合适的时候如点击按钮的时候，再调用startRecording激活。
                Log.i(TAG, "lifecycle->demo->event->MSG_START_PREVIEW");
                if (mEasyStreaming != null) {
                    mEasyStreaming.applyFilter(UEasyStreaming.FILTER_BEAUTIFY);
                    mEasyStreaming.applyFilterLevel(level1, level2, level3);
                }
                break;
            case UEasyStreaming.State.MSG_START_RECORDING:
                //收到了startRecording的消息
                updateStatusInfo("直播开始！", 0);
                Log.e(TAG, "lifecycle->demo->event->start");
                break;
            case UEasyStreaming.State.MSG_NETOWRK_BLOCKS:
                //推流网络状况不好 event int类型 值为内部统计blocks次数
                //mBufferOverfloInfoTxtv.setText("network block stats:" + Integer.valueOf(event.toString()));
                Log.e(TAG, "lifecycle->demo->event->network blocks");
                break;
            case UEasyStreaming.State.MSG_PREPARED_SUCCESSED:
                //推流prepare成功
                Log.e(TAG, "lifecycle->demo->event->" + event.toString());
                break;
            case UEasyStreaming.State.MSG_SIGNATRUE_FAILED:
                //推流key校验失败
                updateStatusInfo("streaming signature failed.", 0);
                Log.e(TAG, "lifecycle->demo->event->MSG_SIGNATRUE_FAILED:" + event.toString());
                break;
            case UEasyStreaming.State.MSG_NETWORK_SPPED:
                //当前手机全局网络速度
                if (mBitrateTxtv != null) {
                    mBitrateTxtv.setVisibility(View.VISIBLE);
                    long speed = Long.valueOf(event.toString());
                    if (speed > 1024) {
                        mBitrateTxtv.setText(speed / 1024 + "K/s");
                    }
                    else {
                        mBitrateTxtv.setText(speed + "B/s");
                    }
                }
                break;
            case UEasyStreaming.State.MSG_PUBLISH_STREAMING_TIME:
                //sdk内部记录的推流时间,若推流被中断stop之后，该值会重新从0开始计数
                if (mRecordedTimeTxtv != null) {
                    mRecordedTimeTxtv.setVisibility(View.VISIBLE);
                    long time = Long.valueOf(event.toString());
                    String retVal = StringUtil.getTimeFormatString(time);
                    mRecordedTimeTxtv.setText(retVal);
                }
                break;
           case UEasyStreaming.State.MSG_NETWORK_DISCONNECT:
               updateStatusInfo("网络异常！", 0);
               //当前网络状态处于断开状态
               Log.e(TAG, "lifecycle->demo->event->network disconnect.");
               break;
           case UEasyStreaming.State.MSG_PREPARED_FAILED:
               //推流prepare失败
               updateStatusInfo(event.toString(), 0);
               Log.e(TAG,  "lifecycle->demo->event->restart->after prepared error->"+ event.toString()+ ","+mEasyStreaming.isRecording());
               if (mEasyStreaming != null) {
                   mEasyStreaming.restart();
               }
               break;
           case UEasyStreaming.State.MSG_MUXER_FAILED:
               //推流写数据过程出现错误（如网络中断或其它未知的错误)
               updateStatusInfo("推流失败:" + event.toString(), 0);
               Log.e(TAG,  "llifecycle->demo->event->restart->after write frame error->"+ event.toString() + ","+ mEasyStreaming.isRecording());
               if (mEasyStreaming != null) {
                   mEasyStreaming.restart();
               }
               break;
           case UEasyStreaming.State.MSG_NETWORK_RECONNECT:
               //网络重新连接
               Log.e(TAG, "lifecycle->demo->event->restart->after network reconnect:" + ","+mEasyStreaming.isRecording());
               updateStatusInfo("网络重新连接中....", 0);
              if (mEasyStreaming != null) {
                   mEasyStreaming.restart();
               }
               break;
           case UEasyStreaming.State.MSG_STOP:
               updateStatusInfo("直播停止.", 0);
               Log.e(TAG, "lifecycle->demo->event->stop->");
               break;
        }
    }

    private class UiHandler extends Handler {
        public UiHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_COUNTDOWN:
                    handleUpdateCountdown(msg.arg1);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window= PublishDemo.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);

        m_app = (LiveApp) this.getApplication();
        mSettings = new Settings(this);
        setContentView(R.layout.live_layout_live_room_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mSettings.getVideoCaptureOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //参数处理
        mStreamingUrl = getIntent().getStringExtra("push_url");
        mSettings.setPublishStreamId(mStreamingUrl);
        m_strChatid = getIntent().getStringExtra("char_id");
        m_strRoomid = getIntent().getStringExtra("char_room_id");
        m_strUserId = getIntent().getStringExtra("user_id");

        m_listChat = (ListView) findViewById(R.id.list_chat);
        m_arrayAdapter = new ArrayAdapter<String>(this, R.layout.layout_list_chat, R.id.text_chat);
        m_listChat.setAdapter(m_arrayAdapter);

        init();
        new Thread(){
            public void run() {
                int i = COUNTDOWN_START_INDEX;
                do {
                    try {
                        Thread.sleep(COUNTDOWN_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_COUNTDOWN;
                    msg.arg1 = i;
                    uiHandler.sendMessage(msg);
                    i--;
                }while(i >= COUNTDOWN_END_INDEX);
            }
        }.start();

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

                    String strGetUrl = m_app.m_strHost + "/support/" + m_strRoomid + "/get_messages/?" + m_strChatid + "=" + m_strlastChatid;
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

    private void updateStatusInfo(final CharSequence text, long delay) {
        if (uiHandler != null && mStatusInfoTxtv != null) {
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mStatusInfoTxtv.append(text + "\n");
                    mStatusInfoTxtv.setTextColor(Color.GREEN);
                }
            }, delay);
        }
    }

    private void init() {
        uiHandler = new UiHandler(getMainLooper());
        mPermissionsChecker = new PermissionsChecker(this);
        initView();
        initEnv();
    }

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

                        String strGetUrl = m_app.m_strHost + "/support/" + m_strRoomid + "/post_message/?" + m_strChatid + "=" + m_strlastChatid + "&message=" + URLEncoder.encode(strMessage, "UTF-8") + "&user_id=" + m_strUserId;
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

    private void initView() {
        mCameraToggleIv = (ImageView) findViewById(R.id.img_bt_switch_camera);
        mLampToggleIv = (ImageView) findViewById(R.id.img_bt_lamp);
        mCloseRecorderImgBtn = (ImageButton) findViewById(R.id.img_bt_close_record);
        mFocusIndex = findViewById(R.id.focus_index);
        mBitrateTxtv = (TextView) findViewById(R.id.bitrate_txtv);
        mPreviewContainer = (UAspectFrameLayout)findViewById(R.id.container);
        mCountDownTxtv = (TextView) findViewById(R.id.countdown_txtv);
        mRecordedTimeTxtv = (TextView) findViewById(R.id.recorded_time_txtv);
        mOutputStreamInfoTxtv = (TextView) findViewById(R.id.output_url_txtv);

        mBufferOverfloInfoTxtv = (TextView) findViewById(R.id.network_overflow_count);
        mBackImgBtn = (Button) findViewById(R.id.btn_finish);
        mCancelImgBtn = (Button) findViewById(R.id.btn_cancel);
        mContainer = (ViewGroup) findViewById(R.id.live_finish_container);
        mStatusInfoTxtv = (TextView) findViewById(R.id.status_info);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);

        beautyLevel = (LinearLayout)findViewById(R.id.filter_level_bar);
        beautyLevel.setVisibility(View.INVISIBLE);

        mStatusInfoTxtv.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        mCameraToggleIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mEasyStreaming != null) {
                    mEasyStreaming.switchCamera();
                }
            }
        });
        mLampToggleIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               /* if (!isMute) {
                    isMute = true;
                } else {
                    isMute = false;
                }
                mEasyStreaming.mute(isMute);*/
                 if (mEasyStreaming != null) {
                     boolean retVal = mEasyStreaming.toggleFlashMode();
                     updateStatusInfo( "闪光灯:" + (retVal ? "打开" : "关闭"), 0);
                 }
            }

        });
        mCloseRecorderImgBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isShutDownCountdown = true;
                mCloseRecorderImgBtn.setEnabled(false);
                if (mEasyStreaming != null) {
                    mEasyStreaming.stopRecording();
                }
                mContainer.setVisibility(View.VISIBLE);
            }
        });

        mBackImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCancelImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEasyStreaming != null) {
                    mEasyStreaming.restart();
                }
                mContainer.setVisibility(View.INVISIBLE);
            }
        });

        mV1 = (TextView) findViewById(R.id.progress1);
        mV2 = (TextView) findViewById(R.id.progress2);
        mV3 = (TextView) findViewById(R.id.progress3);
        mV1.setText(String.valueOf(level1));
        mV2.setText(String.valueOf(level2));
        mV3.setText(String.valueOf(level3));
        mSeekBar1 = (SeekBar) findViewById(R.id.seek_bar_1);
        mSeekBar1.setProgress(level1);
        mSeekBar1.setOnSeekBarChangeListener(this);
        mSeekBar2 = (SeekBar) findViewById(R.id.seek_bar_2);
        mSeekBar2.setProgress(level2);
        mSeekBar2.setOnSeekBarChangeListener(this);
        mSeekBar3 = (SeekBar) findViewById(R.id.seek_bar_3);
        mSeekBar3.setProgress(level3);
        mSeekBar3.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "lifecycle->demo->activity->onPause");
        //if UEasyStermaing no inited in activity onCreate method , you need call this method after inited -> function as camera stopPreivew()
//        mEasyStreaming.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        } else {
            mEasyStreaming.onResume();
        }
        Log.e(TAG, "lifecycle->demo->activity->onResume");
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override protected
    void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        } else {
            mEasyStreaming.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        m_app.delroom(m_strRoomid);
        m_thread.end();
        mEasyStreaming.onDestroy();
        super.onDestroy();
        this.finish();
    }

    public String videoBitrateMode(int value) {
        switch (value) {
            case UStreamingProfile.VIDEO_BITRATE_LOW: return "VIDEO_BITRATE_LOW";
            case UStreamingProfile.VIDEO_BITRATE_NORMAL: return "VIDEO_BITRATE_NORMAL";
            case UStreamingProfile.VIDEO_BITRATE_MEDIUM: return "VIDEO_BITRATE_MEDIUM";
            case UStreamingProfile.VIDEO_BITRATE_HIGH: return "VIDEO_BITRATE_HIGH";
            default: return value +"";
        }
    }

    public String audioBitrateMode(int value) {
        switch (value) {
            case UStreamingProfile.AUDIO_BITRATE_NORMAL: return "AUDIO_BITRATE_NORMAL";
            default: return value +"";
        }
    }

    public void handleShowStreamingInfo(UEasyStreaming.UEncodingType encodingType) {
        if (mOutputStreamInfoTxtv != null) {
            mOutputStreamInfoTxtv.setVisibility(View.VISIBLE);
//            mOutputStreamInfoTxtv.setVisibility(View.INVISIBLE);
            String streamId = mStreamingProfile.getStream().getStreamId();
            String domain = mStreamingProfile.getStream().getPublishDomain();
            String info = "video width:" + mStreamingProfile.getVideoOutputWidth()+ "\n" +
                    "video height:" + mStreamingProfile.getVideoOutputHeight()+ "\n" +
                    "video bitrate:" + videoBitrateMode(mStreamingProfile.getVideoBitrate()) + "\n" +
                    "audio bitrate:" + audioBitrateMode(mStreamingProfile.getAudioBitrate()) + "\n" +
                    "video fps:" + mStreamingProfile.getVideoFrameRate() + "\n" +
                    "url:" + "rtmp://" + domain + ((streamId.startsWith("/") ? streamId  : ("/" + streamId)) + "\n" +
                    "brand:" + DeviceUtils.getDeviceBrand() + "_" + DeviceUtils.getDeviceModel() + "\n" +
                    "sdk version:" + com.ucloud.live.Build.VERSION + "\n" +
                    "android sdk version:" + Build.VERSION.SDK_INT + "\n" +
                    "codec type:" + (encodingType == UEasyStreaming.UEncodingType.MEDIA_CODEC ? "mediacodec" : "x264"));
//            mOutputStreamInfoTxtv.setText(info);
            Log.e(TAG, "@@" + info);
        }
        mToggleFilterImgBtn = (ImageButton) findViewById(R.id.img_bt_filter);
        if (encodingType == UEasyStreaming.UEncodingType.MEDIA_CODEC) {
            if (mSettings.getEncoderType() == UEasyStreaming.UEncodingType.MEDIA_CODEC) {
                mToggleFilterImgBtn.setVisibility(View.VISIBLE);
                mToggleFilterImgBtn.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if ( !m_iBeautySettingState ){
                                    beautyLevel.setVisibility(View.VISIBLE);
                                    m_iBeautySettingState = true;
                                }else{
                                    beautyLevel.setVisibility(View.INVISIBLE);
                                    m_iBeautySettingState = false;
                                }

                                if (mEasyStreaming != null) {
                                    mEasyStreaming.applyFilter(UEasyStreaming.FILTER_NONE);
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mEasyStreaming != null) {
                                    mEasyStreaming.applyFilter(UEasyStreaming.FILTER_BEAUTIFY);
                                    mEasyStreaming.applyFilterLevel(level1, level2, level3);
                                }
                                break;
                        }
                        return true;
                    }
                });
            }
        } else {
            mToggleFilterImgBtn.setVisibility(View.GONE);
//            LinearLayout beautyLevel = (LinearLayout)findViewById(R.id.filter_level_bar);
//            beautyLevel.setVisibility(View.GONE);
        }
    }

    public void handleUpdateCountdown(final int count) {
        if (mCountDownTxtv != null) {
            mCountDownTxtv.setVisibility(View.VISIBLE);
            mCountDownTxtv.setText(String.format("%d", count));
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(COUNTDOWN_DELAY);
            scaleAnimation.setFillAfter(false);
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mCountDownTxtv.setVisibility(View.GONE);

                    if (count == COUNTDOWN_END_INDEX && mEasyStreaming != null && !isShutDownCountdown) {
                        mEasyStreaming.startRecording();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (!isShutDownCountdown) {
                mCountDownTxtv.startAnimation(scaleAnimation);
            } else {
                mCountDownTxtv.setVisibility(View.GONE);
            }
        }
    }

    public void initEnv() {

        UStreamingProfile.Stream stream = new UStreamingProfile.Stream(rtmpPushStreamDomain, "rtmp//tangshanrenzhibo/ucloud/mytest/" + mStreamingUrl);

        mPreviewContainer.setShowMode(UAspectFrameLayout.Mode.FULL);

        mStreamingProfile = new UStreamingProfile.Builder()
                .setContext(this)
                .setPreviewContainerLayout(mPreviewContainer)
                .setEncodeType(mSettings.getEncoderType())
                .setCameraId(whichCamera)
                .setResolution(mSettings.getVideoResolution())
                .setVideoBitrate(mSettings.getVideoBitRate())
                .setVideoFrameRate(mSettings.getVideoFrameRate())
                .setAudioBitrate(UStreamingProfile.AUDIO_BITRATE_NORMAL)
                .setVideoCaptureOrientation(mSettings.getVideoCaptureOrientation())//UStreamingProfile.ORIENTATION_LANDSCAPE or UStreamingProfile.ORIENTATION_PORTRAIT
                .setStream(stream).build();
        handleShowStreamingInfo(mStreamingProfile.getEncodeType());
        mEasyStreaming = UEasyStreaming.Factory.newInstance(mStreamingProfile);

        mEasyStreaming.addListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seek_bar_1:
                mV1.setText(String.valueOf(progress));
                level1 = progress;
                break;
            case R.id.seek_bar_2:
                mV2.setText(String.valueOf(progress));
                level2 = progress;
                break;
            case R.id.seek_bar_3:
                mV3.setText(String.valueOf(progress));
                level3 = progress;
                break;
        }
        mEasyStreaming.applyFilterLevel(level1, level2, level3);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}