package com.wenym.groooseller;

import http.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class MyClassItemFragment extends Fragment implements Callback {

	public static final int DEL_SUCCESS = 1;
	public static final int MOD_SUCCESS = 2;
	public static final int ADD_SUCCESS = 3;

	Context mContext;

	private ProgressDialog progressDialog;

	private ExpandableListView expandableListView;
	private BaseExpandableListAdapter adapter;
	private HandlerThread thread;
	private static Handler handler;
	private View mView;
	private int indexofMenu;
	private String className;

	private Button addClass, back;

	private Menu menu;

	public MyClassItemFragment(Context context, int menuId) {
		super();
		mContext = context;
		thread = new HandlerThread("foodmanage");
		thread.start();
		handler = new Handler(thread.getLooper(), this);
		indexofMenu = menuId;
		className = SellerApplication.menu.get(menuId).className;
		menu = SellerApplication.menu.get(menuId);
		adapter = new BaseExpandableListAdapter() {

			// 重写ExpandableListAdapter中的各个方法
			@Override
			public int getGroupCount() {
				return menu.food.size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return menu.food.get(groupPosition);
			}

			@Override
			public long getGroupId(int groupPosition) {
				return groupPosition;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return 1;
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				return menu.food.get(groupPosition);
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return childPosition;
			}

			@Override
			public boolean hasStableIds() {
				return true;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				LinearLayout ll = (LinearLayout) LayoutInflater.from(mContext)
						.inflate(R.layout.menumanage_linear, null);
				TextView textView = (TextView) ll.findViewById(R.id.textClass);
				TextView textView1 = (TextView) ll.findViewById(R.id.textNum);
				textView.setText((String) getGroup(groupPosition));
				textView1.setText(menu.price.get(groupPosition) + "元\n餐盒费"
						+ menu.packageprice.get(groupPosition) + "元");
				return ll;
			}

			@Override
			public View getChildView(final int groupPosition,
					int childPosition, boolean isLastChild, View convertView,
					ViewGroup parent) {
				LinearLayout ll = (LinearLayout) LayoutInflater.from(mContext)
						.inflate(R.layout.class_fix, null);
				((Button) ll.findViewById(R.id.delete_class))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								new AlertDialog.Builder(mContext)
										.setTitle("是否删除菜品？")
										.setPositiveButton("确定",
												new OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {

														try {
															JSONObject jsonObject = new JSONObject();
															jsonObject
																	.put("id",
																			menu.foodid
																					.get(groupPosition));
															progressDialog = ProgressDialog
																	.show(mContext,
																			"",
																			"正在提交更改");
															HttpUtils
																	.delFood(
																			handler,
																			getActivity(),
																			jsonObject);
														} catch (JSONException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}
													}
												})
										.setNegativeButton("取消", null).show();
							}

						});
				((Button) ll.findViewById(R.id.rename_class))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								LinearLayout ladd = (LinearLayout) LayoutInflater
										.from(mContext).inflate(
												R.layout.rename_food, null);
								final EditText editText = (EditText) ladd
										.findViewById(R.id.rename_food);
								editText.setHint("请输入菜品名");
								new AlertDialog.Builder(mContext)
										.setTitle("菜品重命名")
										.setView(ladd)
										.setPositiveButton("确定",
												new OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														if ("".equals(editText
																.getText()
																.toString())) {
															Toast.makeText(
																	getActivity(),
																	"菜品名不能为空",
																	Toast.LENGTH_SHORT)
																	.show();
															return;
														}
														try {
															JSONObject jsonObject = new JSONObject();
															jsonObject
																	.put("name",
																			editText.getText()
																					.toString());
															jsonObject
																	.put("foodclass",
																			menu.className);
															jsonObject
																	.put("packageprice",
																			menu.packageprice
																					.get(groupPosition));
															jsonObject
																	.put("price",
																			menu.price
																					.get(groupPosition));
															jsonObject
																	.put("ontop",
																			menu.price
																					.get(groupPosition));
															jsonObject
																	.put("id",
																			menu.foodid
																					.get(groupPosition));
															progressDialog = ProgressDialog
																	.show(mContext,
																			"",
																			"正在提交更改");
															HttpUtils
																	.whatFood(
																			handler,
																			getActivity(),
																			jsonObject);
														} catch (JSONException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}
													}
												})
										.setNegativeButton("取消", null).show();
							}

						});
				Button button = (Button) ll.findViewById(R.id.detail_class);
				button.setText("价格");
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						LinearLayout ladd = (LinearLayout) LayoutInflater.from(
								mContext).inflate(
								R.layout.change_food_attribute, null);
						final EditText editText = (EditText) ladd
								.findViewById(R.id.changeprice);
						final EditText editText1 = (EditText) ladd
								.findViewById(R.id.changepackageprice);
						new AlertDialog.Builder(mContext).setView(ladd)
								.setPositiveButton("确定", new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											JSONObject jsonObject = new JSONObject();
											jsonObject.put("name", menu.food
													.get(groupPosition));
											jsonObject.put("foodclass",
													menu.className);
											jsonObject.put("packageprice", ""
													.equals(editText1.getText()
															.toString()) ? "0"
													: editText1.getText()
															.toString());
											jsonObject.put("price", ""
													.equals(editText.getText()
															.toString()) ? "0"
													: editText.getText()
															.toString());
											jsonObject.put("ontop", menu.price
													.get(groupPosition));
											jsonObject.put("id", menu.foodid
													.get(groupPosition));
											progressDialog = ProgressDialog
													.show(mContext, "",
															"正在提交更改");
											HttpUtils.whatFood(handler,
													getActivity(), jsonObject);
										} catch (JSONException e) {
											// TODO
											// Auto-generated
											// catch block
											e.printStackTrace();
										}
									}
								}).setNegativeButton("取消", null).show();
					}

				});
				// LinearLayout ll = (LinearLayout)
				// LayoutInflater.from(mContext)
				// .inflate(R.layout.order_linear, null);
				// TextView textView = (TextView) ll.findViewById(R.id.name);
				// TextView textView2 = (TextView) ll.findViewById(R.id.price);
				// textView.setText(getChild(groupPosition, childPosition)
				// .toString());
				// textView2.setText(DemoApplication.menu.get(groupPosition).price
				// .get(childPosition));
				return ll;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
				return true;
			}

		};
	}

	public void flashMenu() {
		progressDialog = ProgressDialog.show(mContext, "", "正在刷新子菜单");
		HttpUtils.getMenu(handler, mContext);
	}

	public View onCreateView(final LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		if (null == mView) {
			mView = inflater.inflate(R.layout.activity_foodmanage, null);
		}
		if (null == expandableListView) {
			expandableListView = (ExpandableListView) mView
					.findViewById(R.id.menu_manage);
			expandableListView
					.setOnGroupExpandListener(new OnGroupExpandListener() {

						@Override
						public void onGroupExpand(int groupPosition) {
							for (int i = 0; i < adapter.getGroupCount(); i++) {
								if (groupPosition != i) {
									expandableListView.collapseGroup(i);
								}
							}
						}
					});
		}
		if (null == addClass) {
			addClass = (Button) mView.findViewById(R.id.add_menu);
			addClass.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					LinearLayout ladd = (LinearLayout) inflater.inflate(
							R.layout.add_food, null);
					final EditText editText = (EditText) ladd
							.findViewById(R.id.add_food);
					final EditText editText2 = (EditText) ladd
							.findViewById(R.id.change_price);
					final EditText editText3 = (EditText) ladd
							.findViewById(R.id.package_price);
					new AlertDialog.Builder(mContext).setView(ladd)
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if ("".equals(editText.getText().toString())) {
										Toast.makeText(mContext, "菜品名不能为空！",
												Toast.LENGTH_SHORT).show();
										return;
									}
									JSONObject jsonObject = new JSONObject();
									try {
										jsonObject.put("name", editText
												.getText().toString());
										jsonObject.put("price", ""
												.equals(editText2.getText()
														.toString()) ? "0"
												: editText2.getText()
														.toString());
										jsonObject.put("packageprice", ""
												.equals(editText3.getText()
														.toString()) ? "0"
												: editText3.getText()
														.toString());
										jsonObject.put("ontop", menu.isTop);
										jsonObject.put("foodclass",
												menu.className);
										progressDialog = ProgressDialog.show(
												mContext, "", "正在提交更改");
										HttpUtils.addFood(handler, mContext,
												jsonObject);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}).setNegativeButton("取消", null).show();
				}
			});
		}

		if (null == back) {
			back = (Button) mView.findViewById(R.id.back_to_main);
			back.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					getFragmentManager().popBackStack();
					MyMenuFragment.isChild = false;
					MyMenuFragment.handler.sendMessage(handler
							.obtainMessage(MyMenuFragment.MENU_SUCCESS));
				}
			});
		}
		expandableListView.setAdapter(adapter);
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public boolean handleMessage(Message arg0) {

		switch (arg0.what) {
		case ADD_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Toast.makeText(mContext, "新增菜品成功", Toast.LENGTH_SHORT).show();
			flashMenu();
			break;
		case MOD_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Toast.makeText(mContext, "修改菜品成功", Toast.LENGTH_SHORT).show();
			JSONObject jsonObject2 = (JSONObject) arg0.obj;
			int id;
			try {
				id = menu.foodid.indexOf(jsonObject2.getInt("id"));
				menu.food.set(id, jsonObject2.getString("name"));
				menu.packageprice
						.set(id, jsonObject2.getString("packageprice"));
				menu.price.set(id, jsonObject2.getString("price"));
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case DEL_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Toast.makeText(mContext, "删除菜品成功，请刷新确认", Toast.LENGTH_SHORT).show();
			JSONObject jsonObject = (JSONObject) arg0.obj;
			try {
				int id1 = menu.foodid.indexOf(jsonObject.getInt("id"));
				menu.foodid.remove(id1);
				menu.food.remove(id1);
				menu.price.remove(id1);
				menu.isOnTop.remove(id1);
				if (menu.food.size() == 0) {
					for (int i = 0; i < SellerApplication.menu.size(); i++) {
						if (SellerApplication.menu.get(i).foodid.size() == 0) {
							SellerApplication.menu.remove(i);
						}
					}
					getFragmentManager().popBackStack();
					MyMenuFragment.isChild = false;
					MyMenuFragment.handler.sendMessage(handler
							.obtainMessage(MyMenuFragment.MENU_SUCCESS));
					break;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case MyMenuFragment.PUSHMENU_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Toast.makeText(mContext, "上传菜单成功成功，请刷新确认", Toast.LENGTH_SHORT)
					.show();
			JSONObject jsonObject1 = (JSONObject) arg0.obj;
			try {
				menu.food.add(jsonObject1.getString("name"));
				menu.isOnTop.add(jsonObject1.getBoolean("ontop"));
				menu.price.add(jsonObject1.getString("price"));
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case MyMenuFragment.PUSHMENU_FAILED:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				Toast.makeText(mContext, "上传菜单失败，请检查网络", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case MyMenuFragment.MENU_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			for (int i = 0; i < SellerApplication.menu.size(); i++) {
				if (className.equals(SellerApplication.menu.get(i).className)) {
					indexofMenu = i;
					break;
				}
			}
			menu = SellerApplication.menu.get(indexofMenu);
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		default:
			break;
		}
		return false;
	}

}
