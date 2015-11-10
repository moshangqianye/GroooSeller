package com.wenym.groooseller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import http.HttpUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.grooo.seller.grooo_seller.R;
import cn.jpush.android.api.JPushInterface;


public class MyLoginActivity extends Activity implements Callback {

	public static final int LOGIN_ERROR = 1003;
	public static final int LOGIN_SUCCESS = 1002;
	public static final int AUTOLOGIN_SUCCESS = 1005;
	public static final int SEVER_ERROR = 1004;

	public static final int HAS_UPDATE = 1006;
	public static final int NO_UPDATE = 1007;

	private HandlerThread thread;
	private Handler handler;

	private ProgressDialog mDialog = null;

	private String username, password;

	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		thread = new HandlerThread("login");
		thread.start();
		handler = new Handler(thread.getLooper(), this);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (HttpUtils.isConnect(this) == false) {
			new AlertDialog.Builder(this)
					.setTitle("网络错误")
					.setMessage("网络连接失败，请确认网络连接")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									finish();
								}
							}).show();
		} else {
			mDialog = ProgressDialog.show(this, "", "正在检查更新");
			HttpUtils.checkUpdate(handler, MyLoginActivity.this);
		}

	}

	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		JPushInterface.onPause(this);
		super.onPause();
	}

	public void onSubmit(View view) {

		username = ((EditText) findViewById(R.id.editText1)).getText()
				.toString();
		password = ((EditText) findViewById(R.id.editText2)).getText()
				.toString();
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			Toast.makeText(MyLoginActivity.this, "用户名或密码不能为空",
					Toast.LENGTH_SHORT).show();
		} else {
			mDialog = ProgressDialog.show(this, "", "正在登录");
			mDialog.setCanceledOnTouchOutside(true);
			SellerApplication.username = username;
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			}
			digest.update(password.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2)
					hexString.append(0);
				hexString.append(shaHex);
			}
			SellerApplication.password = hexString.toString();
			HttpUtils.onSubmit(handler, getApplicationContext());
		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case LOGIN_SUCCESS:
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString("password", SellerApplication.password);
			editor.putString("username", SellerApplication.username);
			editor.putBoolean("isAutoLogin", true);
			editor.commit();
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			Intent toMain = new Intent(MyLoginActivity.this, MainActivity.class);
			startActivity(toMain);
			finish();
			break;
		case AUTOLOGIN_SUCCESS:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			Intent toMain1 = new Intent(MyLoginActivity.this,
					MainActivity.class);
			startActivity(toMain1);
			finish();
			break;
		case LOGIN_ERROR:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			Toast.makeText(MyLoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT)
					.show();
			break;
		case SEVER_ERROR:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			Toast.makeText(MyLoginActivity.this, "服务器出去送外卖了，请联系15603309067",
					Toast.LENGTH_LONG).show();
			break;
		case HAS_UPDATE:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			UpdateAppManager updateManager = new UpdateAppManager(
					MyLoginActivity.this);
			updateManager.checkUpdateInfo();
			break;
		case NO_UPDATE:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			if (mSharedPreferences.getBoolean("isAutoLogin", false)) {
				mDialog = ProgressDialog.show(this, "", "正在登录");
				HttpUtils.autoLogin(handler, getApplicationContext());
			}
			break;
		}
		return false;
	}
}
