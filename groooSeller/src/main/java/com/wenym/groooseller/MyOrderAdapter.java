package com.wenym.groooseller;

import http.HttpUtils;

import java.util.List;

import cn.grooo.seller.grooo_seller.R;

import com.andexert.expandablelayout.library.ExpandableLayoutItem;
import com.andexert.expandablelayout.library.ExpandableLayoutListView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class MyOrderAdapter extends BaseAdapter {

	private List<Order> order;
	private LayoutInflater layoutInflater;
	private Handler handler;
	private Context mContext;
	private ExpandableLayoutListView listView;

	AlertDialog dialog;

	public MyOrderAdapter(Context context, List<Order> order, Handler handler,
			ExpandableLayoutListView orderList) {
		super();
		mContext = context;
		this.order = order;
		this.layoutInflater = LayoutInflater.from(context);
		this.handler = handler;
		this.listView = orderList;
	}

	public MyOrderAdapter(Context mContext2, List<Order> orderToday,
			ExpandableLayoutListView orderToday2) {
		super();
		mContext = mContext2;
		this.order = orderToday;
		this.layoutInflater = LayoutInflater.from(mContext2);
		this.listView = orderToday2;
	}

	@Override
	public int getCount() {
		return order.size();
	}

	@Override
	public Object getItem(int position) {
		return order.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		viewHolder holder = null;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.order_item, null);
			holder = new viewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (viewHolder) convertView.getTag();
		}
		final Order tempOrder = order.get(position);
		holder.remark.setText(tempOrder.remark);
		holder.method.setText(tempOrder.method);
		holder.linearLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showOrhide(v, position, R.id.status_color);
			}
		});
		switch (Integer.parseInt(tempOrder.status)) {
		case 0:
			holder.orderNumber.setText("δ�ӵ�-�����ţ�" + tempOrder.id);
			holder.linearLayout.setBackgroundColor(Color
					.parseColor("#FF93BAEA"));
			break;
		case 1:
			if ("��ȡ".equals(tempOrder.method)) {
				holder.orderNumber.setText("�ѽӵ�-�����ţ�" + tempOrder.id);
			} else {
				holder.orderNumber.setText("�ȴ�����-�����ţ�" + tempOrder.id);
			}
			holder.linearLayout.setBackgroundColor(Color
					.parseColor("#FF599737"));
			break;
		case 2:
			holder.orderNumber.setText("�����˵�-�����ţ�" + tempOrder.id);
			holder.linearLayout.setBackgroundColor(Color
					.parseColor("#FFFF0000"));
			break;
		case 3:
			holder.orderNumber.setText("����Ч-�����ţ�" + tempOrder.id);
			holder.linearLayout.setBackgroundColor(Color
					.parseColor("#FF787878"));
			break;
		case 4:
			holder.orderNumber.setText("������-�����ţ�" + tempOrder.id);
			holder.linearLayout.setBackgroundColor(Color
					.parseColor("#FF599737"));
			break;
		default:
			break;
		}
		holder.orderPrice.setText(tempOrder.price + "Ԫ");
		holder.orderTime.setText("�µ�ʱ�䣺" + tempOrder.time);
		holder.address.setText("��ַ��" + tempOrder.address);
		holder.phoneNumber.setText("�绰��" + tempOrder.phoneNumber);
		holder.foodList.removeAllViews();
		for (int i = 0; i < tempOrder.orderFood.size() + 1; i++) {
			LinearLayout ll = null;
			if (tempOrder.orderFood.size() != i) {
				ll = (LinearLayout) layoutInflater.inflate(
						R.layout.order_linear, null);
				TextView textView = (TextView) ll.findViewById(R.id.name);
				TextView textView2 = (TextView) ll.findViewById(R.id.price);
				textView.setText(tempOrder.orderFood.get(i));
				textView2.setText("X" + tempOrder.foodCount.get(i));
				holder.foodList.addView(ll);
			} else if (handler != null) {
				Button button = new Button(mContext);
				button.setBackgroundResource(R.drawable.button);
				if (tempOrder.status.equals("0")) {
					button.setText("���ܶ���");
					button.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(mContext)
									.setTitle("�ӵ�")
									.setMessage("ȷ��Ҫ���ܶ�����")
									.setPositiveButton(
											"ȷ��",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													HttpUtils.postOK(handler,
															tempOrder.id,
															position);
												}
											}).setNegativeButton("ȡ��", null)
									.show();
						}
					});
				} else if (tempOrder.status.equals("1")) {
					button.setText("ȡ������");
					button.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(mContext)
									.setTitle("�ӵ�")
									.setMessage("ȷ��Ҫ���ܶ�����")
									.setPositiveButton(
											"ȷ��",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													HttpUtils.postNotOK(
															handler,
															tempOrder.id,
															position);
												}
											}).setNegativeButton("ȡ��", null)
									.show();
						}
					});
				} else if (tempOrder.status.equals("2")) {
					button.setText("ȷ���˵�");
					button.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(mContext)
									.setTitle("�ӵ�")
									.setMessage("ȷ��Ҫ���ܶ�����")
									.setPositiveButton(
											"ȷ��",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													HttpUtils.postBack(handler,
															tempOrder.id,
															position, mContext);
												}
											}).setNegativeButton("ȡ��", null)
									.show();
						}
					});
				}
				if (!tempOrder.status.equals("3")) {
					holder.foodList.addView(button);
				}
			}
		}
		Linkify.addLinks(holder.phoneNumber, Linkify.PHONE_NUMBERS);
		return convertView;
	}

	public void showOrhide(View view, int position, long id) {
		if (listView != null) {
			MainActivity.stopMusic();
			listView.performItemClick(view, position, id);
		}
	}

	private class viewHolder {
		private ExpandableLayoutItem item;
		private TextView orderNumber, orderTime, orderPrice, phoneNumber,
				address, method, remark;
		private LinearLayout linearLayout, foodList;

		public viewHolder(View convertView) {

			item = (ExpandableLayoutItem) convertView
					.findViewById(R.id.order_item_order);
			remark = (TextView) item
					.findViewById(R.id.order_remark);
			method = (TextView) item
					.findViewById(R.id.order_method);
			orderNumber = (TextView) item
					.findViewById(R.id.order_status);
			orderPrice = (TextView) item
					.findViewById(R.id.price_all);
			orderTime = (TextView) item
					.findViewById(R.id.order_time);
			foodList = (LinearLayout) item
					.findViewById(R.id.food_list);
			address = (TextView) item
					.findViewById(R.id.order_address);
			phoneNumber = (TextView)item
					.findViewById(R.id.order_phone);
			linearLayout = (LinearLayout) item
					.findViewById(R.id.status_color);
		}
	}

}
