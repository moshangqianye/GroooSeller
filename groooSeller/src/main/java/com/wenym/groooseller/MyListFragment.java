package com.wenym.groooseller;

import http.HttpUtils;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.grooo.seller.grooo_seller.R;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * �����������б�Fragment��������ʾ�б���ͼ
 * 
 * @author Wouldyou
 *
 */
public class MyListFragment extends ListFragment {

	public Button buttonStartEnd;
	public TextView shopName;
	public ImageView shopIcon;

	Context mContext;

	public int currItem = 0;

	public MyOrderTodayFragment orderTodayFragment;
	public MyMenuFragment menuFragment;
	public MyShopFragment shopFragment;
	public MyOrderAllFragment orderAllFragment;
	
	FragmentManager manager;

	public MyListFragment(Context context) {
		super();
		mContext = context;
	}

	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.list, null);
		shopIcon = (ImageView) mView.findViewById(R.id.shop_icon);
		shopName = (TextView) mView.findViewById(R.id.shop_name);
		buttonStartEnd = (Button) mView.findViewById(R.id.shop_status);

		diaplayShopIcon(shopIcon);

		shopName.setText(SellerApplication.shopName);

		buttonStartEnd.setText("true".equals(SellerApplication.status) ? "����Ӫҵ"
				: "��ֹͣӪҵ");
		buttonStartEnd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if ("true".equals(SellerApplication.status)) {
					HttpUtils.endSell(mContext, MainActivity.handler);
				} else {
					HttpUtils.startSell(mContext, MainActivity.handler);
				}
			}
		});
		orderTodayFragment = new MyOrderTodayFragment(mContext);
		mContent = orderTodayFragment;
		menuFragment = new MyMenuFragment(mContext);
		shopFragment = new MyShopFragment(mContext);
		orderAllFragment = new MyOrderAllFragment(mContext);
		manager = getActivity().getSupportFragmentManager();
		manager.beginTransaction()
				.replace(R.id.activity_ordertoday, orderTodayFragment).commit();
		return mView;
	}

	public void diaplayShopIcon(ImageView imageView) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		ImageLoader.getInstance()
				.displayImage(
						sharedPreferences.getString("logourl",
								SellerApplication.logoUrl), imageView);
	}

	private String slidingList[] = { "���ն���", "��������", "��Ʒ����", "�޸�����", "��������",
			"�˳���¼", "��ϵ����" };

	private int menuIcon[] = { R.drawable.order_today, R.drawable.shop_manage,
			R.drawable.menu_manage, R.drawable.account_center,
			R.drawable.order_all, R.drawable.logout, R.drawable.about_us };

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity());
		for (int i = 0; i < 7; i++) {
			adapter.add(new SampleItem(slidingList[i], menuIcon[i]));
		}
		setListAdapter(adapter);
	}

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> implements
			OnTouchListener, OnClickListener {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		@SuppressLint("InflateParams")
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row, null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.row_icon);
				holder.textView = (TextView) convertView
						.findViewById(R.id.row_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imageView.setImageResource(getItem(position).iconRes);
			holder.textView.setText(getItem(position).tag);
			holder.textView.setTag(position);

			convertView.setOnClickListener(this);
			convertView.setOnTouchListener(this);
			return convertView;
		}

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				v.setBackgroundColor(getResources().getColor(R.color.blue));
			} else if (MotionEvent.ACTION_MOVE == event.getAction()) {
				if (!v.isFocused()) {
					v.setBackgroundColor(getResources().getColor(
							android.R.color.transparent));
				}
			}
			return false;
		}

		@SuppressLint({ "NewApi", "InflateParams" })
		@Override
		public void onClick(View v) {
			((MainActivity) mContext).toggleMenu();
			v.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			ViewHolder vh = (ViewHolder) v.getTag();
			switch ((Integer) vh.textView.getTag()) {
			case 0:
				currItem = 0;
				getActivity().getActionBar().setTitle(R.string.order_today);
				switchContent(mContent, orderTodayFragment);
				break;
			case 1:
				currItem = 1;
				getActivity().getActionBar().setTitle(R.string.shop_manage);
				switchContent(mContent, shopFragment);
				break;
			case 2:
				currItem = 2;
				getActivity().getActionBar().setTitle(R.string.menu_manage);
				switchContent(mContent, menuFragment);
				break;
			case 3:
				currItem = 3;
				LinearLayout ll = (LinearLayout) LayoutInflater.from(mContext)
						.inflate(R.layout.change_password, null);
				final EditText editText = (EditText) ll
						.findViewById(R.id.curr_password);
				final EditText editText2 = (EditText) ll
						.findViewById(R.id.new_password);
				final EditText editText3 = (EditText) ll
						.findViewById(R.id.confirm_password);
				new AlertDialog.Builder(mContext)
						.setTitle("�޸�����")
						.setView(ll)
						.setIcon(android.R.drawable.ic_dialog_info)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String currPassword, newPassword, confirmPassword;
										currPassword = ecodePassword(editText
												.getText().toString());
										newPassword = editText2.getText()
												.toString();
										confirmPassword = editText3.getText()
												.toString();
										if (newPassword.equals(confirmPassword)) {
											MainActivity.currTask = "�����ύ����";
											((MainActivity) getActivity()).mDialog = ProgressDialog
													.show(mContext, "",
															"�����ύ����");
											HttpUtils.changePassword(
													MainActivity.handler,
													mContext, currPassword,
													ecodePassword(newPassword));
										} else {
											Toast.makeText(mContext,
													"��������������벻��ͬ",
													Toast.LENGTH_SHORT).show();
										}
									}
								}).setNegativeButton("ȡ��", null).create()
						.show();
				break;
			case 4:
				currItem = 4;
				getActivity().getActionBar().setTitle(R.string.order_all);
				switchContent(mContent, orderAllFragment);
				orderAllFragment.setAdapter();
				break;
			case 5:
				new AlertDialog.Builder(mContext)
						.setTitle("��ʾ")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage("�Ƿ��˳���¼��")
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										SharedPreferences mSharedPreferences = PreferenceManager
												.getDefaultSharedPreferences(mContext);
										SharedPreferences.Editor editor = mSharedPreferences
												.edit();
										editor.putBoolean("isAutoLogin", false);
										editor.commit();
										((FragmentActivity) mContext).finish();
									}
								}).setNegativeButton("ȡ��", null).create()
						.show();
				break;
			case 6:
				new AlertDialog.Builder(mContext)
						.setTitle("��ϵ����")
						.setMessage(
								"��������κ����⣬����ϵ��\n��ͬѧ��13653351433\n��ͬѧ��18712785090\n۳ͬѧ��15603309067\n\n�鿴���ͽ��棺www.grooo.cn")
						.setPositiveButton("ȷ��", null).show();
				break;

			default:
				break;
			}
			getActivity().supportInvalidateOptionsMenu();
		}
	}

	public Fragment mContent;

	public void switchContent(Fragment from, Fragment to) {
		if (mContent != to) {
			mContent = to;
			FragmentTransaction transaction = manager.beginTransaction()
					.setCustomAnimations(android.R.anim.fade_in,
							android.R.anim.fade_out);
			if (!to.isAdded()) { // ���ж��Ƿ�add��
				transaction.hide(from).add(R.id.activity_ordertoday, to)
						.commit(); // ���ص�ǰ��fragment��add��һ����Activity��
			} else {
				transaction.hide(from).show(to).commit(); // ���ص�ǰ��fragment����ʾ��һ��
			}
		}
	}

	private String ecodePassword(String password) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
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
		return hexString.toString();
	}

	private class ViewHolder {
		ImageView imageView;
		TextView textView;
	}

}
