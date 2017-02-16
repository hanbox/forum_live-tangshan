package com.live.hanbox.live_tang.alipay;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.live.hanbox.live_tang.R;
import com.live.hanbox.live_tang.alipay.util.OrderInfoUtil2_0;
import com.live.hanbox.live_tang.app.LiveApp;
import com.live.hanbox.live_tang.view.MyFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 *  重要说明:
 *  
 *  这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
 *  真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
 *  防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险； 
 */
public class PayDemoActivity extends FragmentActivity {
	
	/** 支付宝支付业务：入参app_id */
	public static final String APPID = "2016102302300316";
	
	/** 支付宝账户登录授权业务：入参pid值 */
	public static final String PID = "";
	/** 支付宝账户登录授权业务：入参target_id值 */
	public static final String TARGET_ID = "";

	/** 商户私钥，pkcs8格式 */
	public static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJ/twrQi2C8Jv2DJ8iJ9MXc5F/HII5OHATPTt9XEIahuCumeYlraPFL+iQ6jrx4NVXyutvJkeEoGU0WQbIFz/et3yg1ZTCIQ+frqAvb9ENwfAwHf08JWyh1uMDjIHmui++4Q70CZ2wZO+o5g16bj7QheapKjQ5YBgCdtcVWp3ABBAgMBAAECgYB5poaTNWoSJa0Ad7yKRYap9LDlBmMhZfEhNivBHAaqMsJ0QDj6eMSoMNuaWiHrjyL2y4N3z7q8PCF9acVPlyaOkPj+l+cSH2sYui4PgjxGxoZPu8T/RHYh5uVGNh4S25ldqoFbDGPMZhg/mMpEDpABGWRjB4Ku93R4LeUtDPesAQJBANU6ensXjFfp79xF0rmJp/OlXHG87jT6hDsYVort/Xlv5KqOTK/VintW8RwoiWkk+QtViBEomVruhil7KU2/M6ECQQDAAkh/wjYUm8y5nld6BRsgI7vsGHIxwQXHwRi4+rXFSMH74fgkTYHlrmZprKFlaxZBUmKtemIL6B2O+J4yz4ihAkEAl/eZm2jpCS8dWCwbR0iofql3/UfdrbxtyYBLDbEYWhg5LKVGWnsiu0z3gk4RwPIs3LsUwsXgpkIhNCcnBNYkgQJBAJkc+P1QPNoAR6g5WV6HVdMyS7gA4odEejyJghJ4cp0I4Q/gaYVhfVVP1oEsVWPPmmaqsN/PuQHvriqFnXm9PyECQQCk3FydSSdu5/hZhOQ5hOXvkUz1UzHkSGXY7+mjzg5hcI8iUp2m7ch2DJpOYKW7iSbtyzN2xCg/RjnalAnYZ71Y";
	
	private static final int SDK_PAY_FLAG = 1;
	private static final int SDK_AUTH_FLAG = 2;
	private static final int PAY_RECHARGE_SUCCESS = 3;
	private static final int PAY_RECHARGE_FAIL = 4;
	private String m_strCount = "0";
	private LiveApp m_app = null;
	private String m_strUserId = "";

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				@SuppressWarnings("unchecked")
				PayResult payResult = new PayResult((Map<String, String>) msg.obj);
				/**
				 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
				 */
				String resultInfo = payResult.getResult();// 同步返回需要验证的信息
				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为9000则代表支付成功
				if (TextUtils.equals(resultStatus, "9000")) {
					// 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
					reCharge(m_strCount);

				} else {
					// 该笔订单真实的支付结果，需要依赖服务端的异步通知。
					Toast.makeText(PayDemoActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			case PAY_RECHARGE_SUCCESS: {
//					Toast.makeText(PayDemoActivity.this, "支付成功" + m_strCount + "元", Toast.LENGTH_SHORT).show();
					PayDemoActivity.this.finish();
					break;
				}

			case SDK_AUTH_FLAG: {
				@SuppressWarnings("unchecked")
				AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
				String resultStatus = authResult.getResultStatus();

				// 判断resultStatus 为“9000”且result_code
				// 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
				if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
					// 获取alipay_open_id，调支付时作为参数extern_token 的value
					// 传入，则支付账户为该授权账户
					Toast.makeText(PayDemoActivity.this,
							"授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT)
							.show();
				} else {
					// 其他状态值则为授权失败
					Toast.makeText(PayDemoActivity.this,
							"授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show();

				}
				break;
			}
			default:
				break;
			}
		};
	};

	private void reCharge(final String strPayCount){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {

					String strGetUrl = m_app.m_strHost + "vircurrency/recharge/?count_pay=" + strPayCount  + "&user_id=" + m_strUserId;
					URL url = new URL(strGetUrl);
					connection = (HttpURLConnection) url.openConnection();
					// 设置请求方法，默认是GET
					connection.setRequestMethod("GET");
					// 设置字符集
					connection.setRequestProperty("Charset", "UTF-8");
					// 设置文件类型
					connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

					if(connection.getResponseCode() == 200){
						Message msg = new Message();
						msg.what = PAY_RECHARGE_SUCCESS;
						mHandler.sendMessage(msg);
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
		setContentView(R.layout.pay_main);

		m_app = (LiveApp) this.getApplication();
		m_strUserId = getIntent().getStringExtra("user_id");
	}
	
	/**
	 * 支付宝支付业务
	 * 
	 * @param v
	 */
	public void payV2(View v) {
		if ( v.getId() == R.id.btn_pay_ok ) {
			if (TextUtils.isEmpty(APPID) || TextUtils.isEmpty(RSA_PRIVATE)) {
				new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialoginterface, int i) {
								//
								finish();
							}
						}).show();
				return;
			}

			m_strCount = ((EditText) findViewById(R.id.edit_count)).getText().toString();

			/**
			 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
			 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
			 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
			 *
			 * orderInfo的获取必须来自服务端；
			 */
			Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, m_strCount);
			String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
			String sign = OrderInfoUtil2_0.getSign(params, RSA_PRIVATE);
			final String orderInfo = orderParam + "&" + sign;

			Runnable payRunnable = new Runnable() {

				@Override
				public void run() {
					PayTask alipay = new PayTask(PayDemoActivity.this);
					Map<String, String> result = alipay.payV2(orderInfo, true);

					Message msg = new Message();
					msg.what = SDK_PAY_FLAG;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			};

			Thread payThread = new Thread(payRunnable);
			payThread.start();
		}else if ( v.getId() == R.id.btn_pay_cancel ){
			this.finish();
		}
	}

	/**
	 * 支付宝账户授权业务
	 * 
	 * @param v
	 */
	public void authV2(View v) {
		if ( v.getId() == R.id.btn_pay_ok ){
			if (TextUtils.isEmpty(PID) || TextUtils.isEmpty(APPID) || TextUtils.isEmpty(RSA_PRIVATE)
					|| TextUtils.isEmpty(TARGET_ID)) {
				new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER |APP_ID| RSA_PRIVATE| TARGET_ID")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialoginterface, int i) {
							}
						}).show();
				return;
			}

			/**
			 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
			 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
			 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
			 *
			 * authInfo的获取必须来自服务端；
			 */
			Map<String, String> authInfoMap = OrderInfoUtil2_0.buildAuthInfoMap(PID, APPID, TARGET_ID);
			String info = OrderInfoUtil2_0.buildOrderParam(authInfoMap);
			String sign = OrderInfoUtil2_0.getSign(authInfoMap, RSA_PRIVATE);
			final String authInfo = info + "&" + sign;
			Runnable authRunnable = new Runnable() {

				@Override
				public void run() {
					// 构造AuthTask 对象
					AuthTask authTask = new AuthTask(PayDemoActivity.this);
					// 调用授权接口，获取授权结果
					Map<String, String> result = authTask.authV2(authInfo, true);

					Message msg = new Message();
					msg.what = SDK_AUTH_FLAG;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			};

			// 必须异步调用
			Thread authThread = new Thread(authRunnable);
			authThread.start();
		}else if ( v.getId() == R.id.btn_pay_cancel ){
			this.finish();
		}
	}
	
	/**
	 * get the sdk version. 获取SDK版本号
	 * 
	 */
	public void getSDKVersion() {
		PayTask payTask = new PayTask(this);
		String version = payTask.getVersion();
//		Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 原生的H5（手机网页版支付切natvie支付） 【对应页面网页支付按钮】
	 * 
	 * @param v
	 */
	public void h5Pay(View v) {
		Intent intent = new Intent(this, H5PayDemoActivity.class);
		Bundle extras = new Bundle();
		/**
		 * url是测试的网站，在app内部打开页面是基于webview打开的，demo中的webview是H5PayDemoActivity，
		 * demo中拦截url进行支付的逻辑是在H5PayDemoActivity中shouldOverrideUrlLoading方法实现，
		 * 商户可以根据自己的需求来实现
		 */
		String url = "http://m.taobao.com";
		// url可以是一号店或者淘宝等第三方的购物wap站点，在该网站的支付过程中，支付宝sdk完成拦截支付
		extras.putString("url", url);
		intent.putExtras(extras);
		startActivity(intent);
	}

}
