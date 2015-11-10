package com.wenym.groooseller;

import http.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import cn.grooo.seller.grooo_seller.R;
import cn.jpush.android.api.JPushInterface;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.testin.agent.TestinAgent;
import com.wenym.groooseller.utils.Toasts;

/*
 * 如果您的工程中实现了Application的继承类，那么，您需要将父类改为com.baidu.frontia.FrontiaApplication。
 * 如果您没有实现Application的继承类，那么，请在AndroidManifest.xml的Application标签中增加属性： 
 * <application android:name="com.baidu.frontia.FrontiaApplication"
 * 。。。
 */
public class SellerApplication extends Application {

	public static String username, password;
	public static String shopId;
	public static String shopName;
	public static String logoUrl;
	public static String status;

	public static String gonggao;
	public static String jieshao;

	public static Drawable shopIcon;

	public static List<Order> orderOK = new ArrayList<Order>(),
			orderNotOK = new ArrayList<Order>();

	public static List<Order> orderToday = new ArrayList<Order>(),
			orderYesterday = new ArrayList<Order>(),
			orderTheDayBefore = new ArrayList<Order>();

	public static List<Menu> menu = new ArrayList<Menu>();

	public static final String url = "http://grooo.sinaapp.com/seller/login/";
	public static final String url2 = "http://grooo.sinaapp.com/seller/finshorder/";
	public static final String urlNormal = "http://grooo.sinaapp.com/seller/";
	public static final String update = "http://grooo.sinaapp.com/seller/update/version/";
	public static final String updateDownload = "http://grooo.sinaapp.com/seller/update/download/";
	public static final String updateurl = "http://7vzt7o.com1.z0.glb.clouddn.com/Grooo/seller/GroooSeller.apk";

	public static DisplayImageOptions options;

	private static Context ct;

	@Override
	public void onCreate() {
		super.onCreate();
		HttpUtils.init();
		ct = getApplicationContext();

		// 以下是您原先的代码实现，保持不变
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.push)
				.showImageOnFail(R.drawable.push).cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		ImageLoader.getInstance()
				.init(ImageLoaderConfiguration
						.createDefault(getApplicationContext()));

		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
		TestinAgent.init(this);

	}

	public static Context getSellerContext() {
		return ct;
	}
}
