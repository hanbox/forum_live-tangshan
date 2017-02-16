package com.live.hanbox.live_tang.view.live_ucloud.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.live.hanbox.live_tang.R;
import com.ucloud.live.UEasyStreaming;

import static com.ucloud.live.UStreamingProfile.Resolution;


public class Settings {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private int videoWidth;
    private int videoHeight;

    public Settings(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     *
     * @return 3 s_encoder 1 h_encoder
     */
    public UEasyStreaming.UEncodingType getEncoderType() {
        String key = "pref.decoder_type";
        int type = Integer.parseInt(mSharedPreferences.getString(key, 2 + ""));
        if(2 == type) {
            //return  UEasyStreaming.UEncodingType.MEDIA_X264;
            return  UEasyStreaming.UEncodingType.MEDIA_CODEC;
        }
        if(3 == type) {
            return  UEasyStreaming.UEncodingType.MEDIA_CODEC;
        }
       return UEasyStreaming.UEncodingType.MEDIA_CODEC;
    }

    public void setDecoderType(String type) {
        String key = "pref.decoder_type";
        mSharedPreferences.edit().putString(key, type).commit();
    }

    public int getVideoFrameRate() {
        String key = "pref.video_frame_rate";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_frame_rate)));
    }

    public void setVideoFrameRate(int value) {
        String key = "pref.video_frame_rate";
        mSharedPreferences.edit().putInt(key, value).commit();
    }

    public void setVideoBitRate(int value) {
        String key = "pref.video_encoding_bit_rate";
        mSharedPreferences.edit().putInt(key, value).commit();
    }

    public int getVideoBitRate() {
        String key = "pref.video_encoding_bit_rate";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_encoding_bit_rate)));
    }

//    public boolean isOpenLogRecoder() {
//        String key = "pref.open_log";
//        return  !mSharedPreferences.getBoolean(key, true);
//    }
//
//    public String getLogCacheDir() {
//        String key = "pref.log_dir";
//        return mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_log_cache_dir));
//    }

    @Deprecated
    public int getVideoWidth() {
        String key = "pref.video_size_width";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_size_width)));
    }

    @Deprecated
    public int getVideoHeight() {
        String key = "pref.video_size_height";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_size_height)));
    }

    public void setVideoWidth(int width) {
        String key = "pref.video_size_width";
        mSharedPreferences.edit().putString(key, width +"").commit();
    }

    public int getVideoCaptureWidth() {
        return getVideoWidth();
    }

    public int getVideoCaptureHeight() {
        return getVideoHeight();
    }

    public void setVideoCaptureWidth(int width) {
        setVideoWidth(width);
    }

    public void setVideoCaptureHeight(int height) {
        setVideoHeight(height);
    }

    public int getVideoOutputHeight() {
        String key = "pref.video_size_output_height";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_size_height)));
    }

    public int getVideoOutputWidth() {
        String key = "pref.video_size_output_width";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_size_width)));
    }

    public void setVideoOutputWidth(int width) {
        String key = "pref.video_size_output_width";
        mSharedPreferences.edit().putString(key, width +"").commit();
    }

    public void setVideoOutputHeight(int width) {
        String key = "pref.video_size_output_height";
        mSharedPreferences.edit().putString(key, width +"").commit();
    }

    @Deprecated
    public void setVideoHeight(int height) {
        String key = "pref.video_size_height";
        mSharedPreferences.edit().putString(key, height +"").commit();
    }

    public String getPusblishStreamId() {
        String key = "pref.video_publish_and_play_id";
        return mSharedPreferences.getString(key, "0");
    }

    @Override
    public String toString() {
        return "Settings{" +
                "video decoder type: " + getEncoderType() +
                ", video width: " + getVideoWidth() +
                ", video height: " + getVideoHeight() +
                ", video frame rate: " + getVideoFrameRate() +
                ", video bitrate: " + getVideoBitRate() +
//                ", log is open: " + isOpenLogRecoder() +
//                ", log cache dir: " + getLogCacheDir() +
                '}';
    }

    public void setPublishStreamId(String publishStreamId) {
        String key = "pref.video_publish_and_play_id";
        if (!TextUtils.isEmpty(publishStreamId)) {
            mSharedPreferences.edit().putString(key, publishStreamId).commit();
        }
    }

    public void setVideoCaptureOrientation(String orientation) {
        String key = "pref.video_capture_orientation";
        mSharedPreferences.edit().putString(key, orientation).commit();
    }

    public int getVideoCaptureOrientation() {
        String key = "pref.video_capture_orientation";
        return Integer.parseInt(mSharedPreferences.getString(key, mContext.getResources().getString(R.string.pref_default_video_capture_orientation)));
    }

    /**
     * 获取推流分辨率(capture, output)
     */
    public Resolution getVideoResolution() {
        String key = "pref.video_resolution";
        String resolutionId = mSharedPreferences.getString(key, String.valueOf(Resolution.RATIO_AUTO.ordinal()));
        Resolution resolution = null;
        switch (resolutionId) {
            case "0":
                resolution = Resolution.RATIO_AUTO;
                break;
            case "1":
                resolution = Resolution.RATIO_4x3;
                break;
            case "2":
                resolution = Resolution.RATIO_16x9;
                break;
            case "3":
                resolution = Resolution.RATIO_16x9_LOW;
                break;
            case "4":
                resolution = Resolution.RATIO_16x9_HIGH;
                break;
            default:
                break;
        }
        return resolution;
    }
}
