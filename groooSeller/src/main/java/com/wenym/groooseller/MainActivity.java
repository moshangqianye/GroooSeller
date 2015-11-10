package com.wenym.groooseller;

import http.HttpUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout.LayoutParams;
import cn.grooo.seller.grooo_seller.R;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.testin.agent.TestinAgent;

/*
 * 云推送Demo主Activity。
 * 代码中，注释以Push标注开头的，表示接下来的代码块是Push接口调用示例
 */
public class MainActivity extends FragmentActivity implements Callback {

	// 载入图片
	public static final int LOAD_SUCCESS = 1002;
	// 开张成功
	public static final int START_SUCCESS = 1001;
	// 开张失败
	public static final int START_FAILED = 2001;
	// 关闭成功
	public static final int END_SUCCESS = 999;
	// 关闭失败
	public static final int END_FAILED = 998;
	// 刷新成功
	public static final int RELOAD_SUCCESS = 6666;
	// 刷新失败
	public static final int RELOAD_FAILED = 7777;
	// 完成订单成功
	public static final int FINISH_SUCCESS = 666;
	// 完成订单失败
	public static final int FINISH_FAILED = 6661;
	// 取消完成订单失败
	public static final int UNFINISH_SUCCESS = 777;
	// 取消完成订单失败
	public static final int UNFINISH_FAILED = 7771;
	// 确认退单成功
	public static final int BACK_SUCCESS = 776;
	// 确认退单成功
	public static final int BACK_FAILED = 778;
	// 显示dialog
	public static final int ALERT_DIALOG = 7771;
	// 刷新成功
	public static final int CHANGE_SUCCESS = 1234;
	// 刷新失败
	public static final int CHANGE_FAILED = 4321;

	private static final String TAG = MainActivity.class.getSimpleName();

	private HandlerThread thread;
	public static Handler handler;

	public SlidingMenu menu;
	public ProgressDialog mDialog = null;

	public static String currTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		thread = new HandlerThread("finish");
		thread.start();
		handler = new Handler(thread.getLooper(), this);

		initSlidingMenu();

		TestinAgent.setUserInfo(SellerApplication.username);

	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		switch (listFragment.currItem) {
		case 0:
			MenuItem mun0 = menu.add(0, 0, 0, "刷新");
			mun0.setIcon(android.R.drawable.ic_menu_rotate);
			mun0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			break;
		case 2:
			MenuItem mun2 = menu.add(0, 0, 0, "刷新");
			mun2.setIcon(android.R.drawable.ic_menu_rotate);
			mun2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			break;
		case 4:
			MenuItem mun4 = menu.add(0, 0, 0, "刷新");
			mun4.setIcon(android.R.drawable.ic_menu_rotate);
			mun4.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			MenuItem mun44 = menu.add(0, 1, 0, "统计");
			mun44.setIcon(android.R.drawable.ic_menu_today);
			mun44.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			break;
		default:
			break;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		stopMusic();
		switch (item.getItemId()) {
		case 0:
			switch (listFragment.currItem) {
			case 0:
				mDialog = ProgressDialog.show(this, "", "正在刷新订单");
				mDialog.setCanceledOnTouchOutside(true);
				HttpUtils.reloadOrder(handler, getApplicationContext());
				break;
			case 2:
				if (MyMenuFragment.isChild) {
					listFragment.menuFragment.classItemFragment.flashMenu();
					listFragment.menuFragment.adapter.notifyDataSetChanged();
				} else {
					listFragment.menuFragment.flashMenu();
				}
				break;
			case 4:
				listFragment.orderAllFragment.setAdapter();
				break;
			default:
				break;
			}
			return true;
		case 1:
			alertDialogCount(listFragment.orderAllFragment.currentItem);
			return true;
		case android.R.id.home:
			toggleMenu();
			return true;
		}
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
		updateDisplay();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public static void stopMusic() {
		if (JPushReceiver.player != null && JPushReceiver.player.isPlaying()) {
			JPushReceiver.player.pause();
			;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}


	// 更新界面显示内容
	private void updateDisplay() {
		mDialog = ProgressDialog.show(this, "", "正在刷新订单");
		mDialog.setCanceledOnTouchOutside(true);
		HttpUtils.reloadOrder(handler, getApplicationContext());
		listFragment.switchContent(listFragment.mContent,
				listFragment.orderTodayFragment);
		listFragment.diaplayShopIcon(listFragment.shopIcon);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case RELOAD_FAILED:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mDialog.isShowing()) {
						mDialog.dismiss();
						Toast.makeText(MainActivity.this, "刷新失败",
								Toast.LENGTH_LONG).show();
					}
				}
			});
			break;
		case END_SUCCESS:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "关闭成功，好好休息",
							Toast.LENGTH_SHORT).show();
					listFragment.buttonStartEnd.setText("已停止营业");
				}
			});
			break;
		case START_SUCCESS:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "已经开张，等待订单",
							Toast.LENGTH_SHORT).show();
					listFragment.buttonStartEnd.setText("正在营业");
				}
			});
			break;
		case START_FAILED:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "开始营业失败，请检查网络或重新登录",
							Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case RELOAD_SUCCESS:
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					listFragment.orderTodayFragment.orderNew
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderNotOK, handler,
									listFragment.orderTodayFragment.orderNew));
					listFragment.orderTodayFragment.orderDone
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderOK, handler,
									listFragment.orderTodayFragment.orderDone));
					if (mDialog.isShowing()) {
						mDialog.dismiss();
					}
				}
			});
			break;
		case FINISH_SUCCESS:
			Order order = (Order) msg.obj;
			SellerApplication.orderNotOK.remove(order);
			order.status = "1";
			SellerApplication.orderOK.add(order);
			SellerApplication.orderToday = new ArrayList<Order>();
			SellerApplication.orderToday.addAll(SellerApplication.orderNotOK);
			SellerApplication.orderToday.addAll(SellerApplication.orderOK);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					listFragment.orderTodayFragment.orderNew
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderNotOK, handler,
									listFragment.orderTodayFragment.orderNew));
					listFragment.orderTodayFragment.orderDone
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderOK, handler,
									listFragment.orderTodayFragment.orderDone));
				}
			});
			break;
		case UNFINISH_SUCCESS:
			Order order1 = (Order) msg.obj;
			SellerApplication.orderOK.remove(order1);
			order1.status = "0";
			SellerApplication.orderNotOK.add(order1);
			SellerApplication.orderToday = new ArrayList<Order>();
			SellerApplication.orderToday.addAll(SellerApplication.orderNotOK);
			SellerApplication.orderToday.addAll(SellerApplication.orderOK);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					listFragment.orderTodayFragment.orderNew
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderNotOK, handler,
									listFragment.orderTodayFragment.orderNew));
					listFragment.orderTodayFragment.orderDone
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderOK, handler,
									listFragment.orderTodayFragment.orderDone));
				}
			});
			break;
		case BACK_SUCCESS:
			Order order2 = (Order) msg.obj;
			SellerApplication.orderNotOK.remove(order2);
			order2.status = "3";
			SellerApplication.orderOK.add(order2);
			SellerApplication.orderToday = new ArrayList<Order>();
			SellerApplication.orderToday.addAll(SellerApplication.orderNotOK);
			SellerApplication.orderToday.addAll(SellerApplication.orderOK);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					listFragment.orderTodayFragment.orderNew
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderNotOK, handler,
									listFragment.orderTodayFragment.orderNew));
					listFragment.orderTodayFragment.orderDone
							.setAdapter(new MyOrderAdapter(MainActivity.this,
									SellerApplication.orderOK, handler,
									listFragment.orderTodayFragment.orderDone));
				}
			});
			break;
		case BACK_FAILED:
		case FINISH_FAILED:
		case UNFINISH_FAILED:
			Toast.makeText(this, "操作失败，服务器订单状态已改变！请刷新查看", Toast.LENGTH_SHORT)
					.show();
			break;
		case CHANGE_SUCCESS:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			currTask = "";
			Toast.makeText(this, "更改密码成功", Toast.LENGTH_SHORT).show();
			break;
		case CHANGE_FAILED:
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			if ("正在提交更改".equals(currTask)) {
				Toast.makeText(this, "提交更改失败，请确认密码或检查网络", Toast.LENGTH_SHORT)
						.show();
				currTask = "";
			}
		default:
			break;
		}
		return false;
	}

	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true;
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				public void run() {
					isExit = false;
				}
			}, 2000);

		} else {
			PackageManager pm = getPackageManager();
			ResolveInfo homeInfo = pm.resolveActivity(new Intent(
					Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
			ActivityInfo ai = homeInfo.activityInfo;
			Intent startIntent = new Intent(Intent.ACTION_MAIN);
			startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			startIntent
					.setComponent(new ComponentName(ai.packageName, ai.name));
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				startActivity(startIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// finish();
			// System.exit(0);
		}
	}

	private MyListFragment listFragment;

	/**
	 * 初始化滑动菜单
	 */
	@SuppressLint("NewApi")
	private void initSlidingMenu() {
		// 设置主界面视图
		setContentView(R.layout.activity_ordertoday);
		getActionBar().setTitle(R.string.order_today);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// 设置滑动菜单的属性
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		// 设置滑动菜单的视图界面
		menu.setMenu(R.layout.menu_frame);
		listFragment = new MyListFragment(MainActivity.this);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, listFragment).commit();

	}

	@Override
	public void onBackPressed() {
		if (mDialog.isShowing()) {
			mDialog.dismiss();
		}
		// 点击返回键关闭滑动菜单
		if (menu.isMenuShowing()) {
			menu.showContent();
		} else {
			exitBy2Click();
		}
	}

	public void toggleMenu() {
		menu.toggle(true);
	}

	@SuppressLint("InflateParams")
	private void alertDialogCount(int curr) {
		String string[] = { "未接单数：", "等待配送单数：", "等待退单数：", "已无效单数：", "已配送单数：",
				"自取单数：" };
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		linearLayout.setGravity(Gravity.CENTER);
		int count[] = new int[6];
		float income = 0;
		float income2 = 0;
		switch (curr) {
		case 0:
			for (int i = 0; i < SellerApplication.orderToday.size(); i++) {
				System.out.println(SellerApplication.orderToday.get(i).status);
				switch (Integer
						.parseInt(SellerApplication.orderToday.get(i).status)) {
				case 0:
					count[0]++;
					break;
				case 1:
					if ("自取".equals(SellerApplication.orderToday.get(i).method)) {
						count[5]++;
						income2 += Float.parseFloat(SellerApplication.orderToday
								.get(i).price);
					} else {
						count[1]++;
					}
					break;
				case 2:
					count[2]++;
					break;
				case 3:
					count[3]++;
					break;
				case 4:
					count[4]++;
					income += Float.parseFloat(SellerApplication.orderToday
							.get(i).price);
					break;
				default:
					break;
				}
			}
			break;

		default:
			break;
		}

		for (int i = 0; i < 6; i++) {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.order_linear, null);
			TextView textView = (TextView) ll.findViewById(R.id.name);
			TextView textView2 = (TextView) ll.findViewById(R.id.price);
			textView.setText(string[i]);
			textView2.setText("X" + count[i]);
			linearLayout.addView(ll);
		}
		LinearLayout ll2 = (LinearLayout) getLayoutInflater().inflate(
				R.layout.order_linear, null);
		TextView textView222 = (TextView) ll2.findViewById(R.id.name);
		TextView textView22 = (TextView) ll2.findViewById(R.id.price);
		textView222.setText("营业额（自取）：");
		textView22.setText(income2 + "元");
		linearLayout.addView(ll2);
		LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
				R.layout.order_linear, null);
		TextView textView = (TextView) ll.findViewById(R.id.name);
		TextView textView2 = (TextView) ll.findViewById(R.id.price);
		textView.setText("营业额（已配送）：");
		textView2.setText(income + "元");
		linearLayout.addView(ll);
		new AlertDialog.Builder(this).setView(linearLayout)
				.setPositiveButton("确定", null).show();
	}
}
