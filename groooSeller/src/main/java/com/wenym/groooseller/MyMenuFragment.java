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
 * �����������б�Fragment��������ʾ�б���ͼ
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

			// ��дExpandableListAdapter�еĸ�������
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
							+ "�����ö���");
				} else {
					textView.setText(((Menu) getGroup(groupPosition)).className);
				}
				textView1.setText("��"
						+ ((Menu) getGroup(groupPosition)).food.size() + "����");
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
										.setTitle("�Ƿ�ɾ�������")
										.setPositiveButton("ȷ��",
												new OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														currTask = "����ɾ���˵�";
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
										.setNegativeButton("ȡ��", null).show();
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
										.setTitle("���������")
										.setView(ladd)
										.setPositiveButton("ȷ��",
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
																	"�˵�������Ϊ��",
																	Toast.LENGTH_SHORT)
																	.show();
															return;
														}
														currTask = "�����������˵�";
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
										.setNegativeButton("ȡ��", null).show();
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
		currTask = "����ˢ�²˵�";
		progressDialog = ProgressDialog.show(mContext, "", currTask);
		HttpUtils.getMenu(handler, mContext);
		super.onResume();
	}

	public void flashMenu() {
		currTask = "����ˢ�²˵�";
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
							.setPositiveButton("ȷ��", new OnClickListener() {

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
										Toast.makeText(mContext, "��Ʒ������ظ�",
												Toast.LENGTH_SHORT).show();
									}
								}
							}).setNegativeButton("ȡ��", null).show();
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
				if ("����ˢ�²˵�".equals(currTask)) {
					Toast.makeText(mContext, "���ز˵�ʧ�ܣ�������������µ�¼",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case PUSHMENU_SUCCESS:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Toast.makeText(mContext, "�ϴ��˵��ɹ�����ˢ��ȷ��", Toast.LENGTH_SHORT).show();
			break;
		case PUSHMENU_FAILED:
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
				if ("�����ϴ��˵�".equals(currTask)) {
					Toast.makeText(mContext, "�ϴ��˵�ʧ�ܣ�������������µ�¼",
							Toast.LENGTH_SHORT).show();
				} else if ("����ɾ���˵�".equals(currTask)) {
					Toast.makeText(getActivity(), "ɾ���˵�ʧ�ܣ���ˢ�º�����",
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
				if ("�����������˵�".equals(currTask)) {
					Toast.makeText(getActivity(), "�˵��������ɹ�", Toast.LENGTH_SHORT)
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
