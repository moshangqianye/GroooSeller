package com.wenym.groooseller;

import http.HttpUtils;
import cn.grooo.seller.grooo_seller.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 功能描述：列表Fragment，用来显示列表视图
 * 
 * @author Wouldyou
 *
 */
public class MyShopFragment extends Fragment implements Callback {

	public static final int SETDESCRIPTION_SUCCESS = 5555;
	public static final int SETDESCRIPTION_FAILED = 555;

	public static final int SETANNOUNCEMENT_SUCCESS = 6666;
	public static final int SETANNOUNCEMENT_FAILED = 666;

	public static final int GET_SUCCESS = 998;

	Context mContext;

	private ProgressDialog progressDialog;

	private HandlerThread thread;
	private static Handler handler;
	private View mView;

	private TextView gonggao, jieshao;

	private Button gonggao1, jieshao1;

	public MyShopFragment(Context context) {
		super();
		mContext = context;
		thread = new HandlerThread("shopmanage");
		thread.start();
		handler = new Handler(thread.getLooper(), this);
	}

	public View onCreateView(final LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		progressDialog = ProgressDialog.show(mContext, "", "正在加载");
		HttpUtils.getAnnouncement(mContext, handler);
		HttpUtils.getDescription(mContext, handler);
		if (null == mView) {
			mView = inflater.inflate(R.layout.activity_shopmanage, null);
		}
		if (null == gonggao) {
			gonggao = (TextView) mView.findViewById(R.id.gonggao);
		}
		if (null == jieshao) {
			jieshao = (TextView) mView.findViewById(R.id.jieshao);
		}
		if (null == gonggao1) {
			gonggao1 = (Button) mView.findViewById(R.id.set_gonggao);
			gonggao1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					LinearLayout ladd = (LinearLayout) inflater.inflate(
							R.layout.change_food_attribute, null);
					final EditText editText = (EditText) ladd
							.findViewById(R.id.changeprice);
					editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
					editText.setHint("最多100字");
					new AlertDialog.Builder(mContext).setTitle("请输入公告")
							.setView(ladd)
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									progressDialog = ProgressDialog.show(
											mContext, "", "正在上传公告");
									SellerApplication.gonggao = editText
											.getText().toString();
									HttpUtils.setAnnouncement(mContext,
											handler, editText.getText()
													.toString());
								}
							}).setNegativeButton("取消", null).show();
				}
			});
		}
		if (null == jieshao1) {
			jieshao1 = (Button) mView.findViewById(R.id.set_jieshao);
			jieshao1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					LinearLayout ladd = (LinearLayout) inflater.inflate(
							R.layout.change_food_attribute, null);
					final EditText editText = (EditText) ladd
							.findViewById(R.id.changeprice);
					editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
					editText.setHint("最多100字");
					new AlertDialog.Builder(mContext).setTitle("请输入介绍")
							.setView(ladd)
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									progressDialog = ProgressDialog.show(
											mContext, "", "正在上传介绍");
									SellerApplication.jieshao = editText
											.getText().toString();
									HttpUtils.setDescription(mContext, handler,
											editText.getText().toString());
								}
							}).setNegativeButton("取消", null).show();
				}
			});
		}

		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case SETANNOUNCEMENT_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					gonggao.setText(SellerApplication.gonggao);
				}
			});
			Toast.makeText(mContext, "设置公告成功", Toast.LENGTH_SHORT).show();
			break;
		case SETANNOUNCEMENT_FAILED:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				Toast.makeText(mContext, "设置公告失败，请检查网络", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case SETDESCRIPTION_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					jieshao.setText(SellerApplication.jieshao);
				}
			});
			Toast.makeText(mContext, "设置介绍成功", Toast.LENGTH_SHORT).show();
			break;
		case SETDESCRIPTION_FAILED:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				Toast.makeText(mContext, "设置介绍失败，请检查网络", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case GET_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (null != SellerApplication.gonggao) {
						gonggao.setText(SellerApplication.gonggao);
					}
					if (null != SellerApplication.jieshao) {
						jieshao.setText(SellerApplication.jieshao);
					}
				}
			});
			break;
		default:
			break;
		}
		return false;
	}

}
