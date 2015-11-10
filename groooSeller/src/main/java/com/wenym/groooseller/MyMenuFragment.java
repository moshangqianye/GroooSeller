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
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 功能描述：列表Fragment，用来显示列表视图
 * 
 * @author Wouldyou
 *
 */
public class MyMenuFragment extends Fragment implements Callback {

	public static final int MENU_SUCCESS = 5555;
	public static final int MENU_FAILED = 555;

	public static final int PUSHMENU_SUCCESS = 6666;
	public static final int PUSHMENU_FAILED = 666;
	Context mContext;

	private static ProgressDialog progressDialog;

	private ExpandableListView expandableListView;
	public BaseExpandableListAdapter adapter;
	private HandlerThread thread;
	public static Handler handler;
	private View mView;

	private Button addClass;

	private static String currTask;

	public static boolean isChild = false;

	public MyClassItemFragment classItemFragment;

	public MyMenuFragment(Context context) {
		super();
		mContext = context;
		thread = new HandlerThread("menumanage");
		thread.start();
		handler = new Handler(thread.getLooper(), this);
		adapter = new BaseExpandableListAdapter() {

			// 重写ExpandableListAdapter中的各个方法
			@Override
			public int getGroupCount() {
				return SellerApplication.menu.size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return SellerApplication.menu.get(groupPosition);
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
				return SellerApplication.menu.get(groupPosition).className;
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
				if (((Menu) getGroup(groupPosition)).isTop) {
					textView.setText(((Menu) getGroup(groupPosition)).className
							+ "（已置顶）");
				} else {
					textView.setText(((Menu) getGroup(groupPosition)).className);
				}
				textView1.setText("共"
						+ ((Menu) getGroup(groupPosition)).food.size() + "道菜");
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
										.setTitle("是否删除该类别？")
										.setPositiveButton("确定",
												new OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														currTask = "正在删除菜单";
														progressDialog = ProgressDialog
																.show(getActivity(),
																		"",
																		currTask);
														HttpUtils
																.delClass(
																		handler,
																		getActivity(),
																		SellerApplication.menu
																				.get(groupPosition).className);
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
								new AlertDialog.Builder(mContext)
										.setTitle("类别重命名")
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
																	"菜单名不能为空",
																	Toast.LENGTH_SHORT)
																	.show();
															return;
														}
														currTask = "正在重命名菜单";
														progressDialog = ProgressDialog
																.show(getActivity(),
																		"",
																		currTask);
														HttpUtils
																.renClass(
																		handler,
																		getActivity(),
																		SellerApplication.menu
																				.get(groupPosition).className,
																		editText.getText()
																				.toString());
													}
												})
										.setNegativeButton("取消", null).show();
							}

						});
				((Button) ll.findViewById(R.id.detail_class))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								isChild = true;
								classItemFragment = new MyClassItemFragment(
										mContext, groupPosition);
								getFragmentManager()
										.beginTransaction()
										.add(R.id.activity_ordertoday,
												classItemFragment)
										.setTransition(
												FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
										.addToBackStack(null).commit();
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

	@Override
	public void onResume() {
		currTask = "正在刷新菜单";
		progressDialog = ProgressDialog.show(mContext, "", currTask);
		HttpUtils.getMenu(handler, mContext);
		super.onResume();
	}

	public void flashMenu() {
		currTask = "正在刷新菜单";
		progressDialog = ProgressDialog.show(mContext, "", currTask);
		HttpUtils.getMenu(handler, getActivity());
	}

	public View onCreateView(final LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		if (null == mView) {
			mView = inflater.inflate(R.layout.activity_menumanage, null);
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
							R.layout.add_class, null);
					final EditText editText = (EditText) ladd
							.findViewById(R.id.add_class);
					final Switch switch1 = (Switch) ladd
							.findViewById(R.id.is_class_ontop);
					new AlertDialog.Builder(mContext).setView(ladd)
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String string = editText.getText()
											.toString();
									boolean b = true;
									for (int i = 0; i < SellerApplication.menu
											.size(); i++) {
										if (SellerApplication.menu.get(i).className
												.equals(string))
											b = false;
									}
									if (b) {
										Menu menu = new Menu(string, switch1
												.isChecked());
										SellerApplication.menu.add(menu);
										expandableListView.setAdapter(adapter);
										isChild = true;
										classItemFragment = new MyClassItemFragment(
												mContext, SellerApplication.menu
														.indexOf(menu));
										getFragmentManager()
												.beginTransaction()
												.add(R.id.activity_ordertoday,
														classItemFragment)
												.setTransition(
														FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
												.addToBackStack(null).commit();
									} else {
										Toast.makeText(mContext, "菜品类别不能重复",
												Toast.LENGTH_SHORT).show();
									}
								}
							}).setNegativeButton("取消", null).show();
				}
			});
		}
		return mView;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MENU_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					expandableListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
				}
			});
			break;
		case MENU_FAILED:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				if ("正在刷新菜单".equals(currTask)) {
					Toast.makeText(mContext, "加载菜单失败，请检查网络或重新登录",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case PUSHMENU_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Toast.makeText(mContext, "上传菜单成功，请刷新确认", Toast.LENGTH_SHORT).show();
			break;
		case PUSHMENU_FAILED:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				if ("正在上传菜单".equals(currTask)) {
					Toast.makeText(mContext, "上传菜单失败，请检查网络或重新登录",
							Toast.LENGTH_SHORT).show();
				} else if ("正在删除菜单".equals(currTask)) {
					Toast.makeText(getActivity(), "删除菜单失败，请刷新后重试",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case MyClassItemFragment.DEL_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			String string = (String) msg.obj;
			for (int i = 0; i < SellerApplication.menu.size(); i++) {
				if (string.equals(SellerApplication.menu.get(i).className)) {
					SellerApplication.menu.remove(i);
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
			break;
		case MyClassItemFragment.MOD_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				if ("正在重命名菜单".equals(currTask)) {
					Toast.makeText(getActivity(), "菜单重命名成功", Toast.LENGTH_SHORT)
							.show();
				}
			}
			String[] strings = msg.obj.toString().split("@");
			for (int i = 0; i < SellerApplication.menu.size(); i++) {
				if (SellerApplication.menu.get(i).className.equals(strings[0])) {
					SellerApplication.menu.get(i).className = strings[1];
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
			break;
		default:
			break;
		}
		return false;
	}

}
