package com.wenym.groooseller;

import java.util.ArrayList;
import java.util.List;

import cn.grooo.seller.grooo_seller.R;

import com.andexert.expandablelayout.library.ExpandableLayoutListView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyOrderTodayFragment extends Fragment {

	public static final int MENU_SUCCESS = 5555;
	public static final int MENU_FAILED = 555;

	private ViewPager viewPager;
	private ImageView imageView;
	private List<View> lists = new ArrayList<View>();
	private MyAdapter myAdapter;
	private Bitmap cursor;
	private int offSet;
	private int currentItem;
	private Matrix matrix = new Matrix();
	private int bmWidth;
	private Animation animation;
	private TextView textView1;
	private TextView textView2;

	public ExpandableLayoutListView orderNew;
	public ExpandableLayoutListView orderDone;

	Context mContext;

	LayoutInflater inflater;

	public MyOrderTodayFragment(Context context) {
		super();
		mContext = context;
		inflater = LayoutInflater.from(context);
	}

	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.activity_ordertoday, null);
		imageView = (ImageView) mView.findViewById(R.id.cursor);
		textView1 = (TextView) mView.findViewById(R.id.textView1);
		textView2 = (TextView) mView.findViewById(R.id.textView2);

		View view1 = inflater.inflate(R.layout.layout1, null);
		View view2 = inflater.inflate(R.layout.layout2, null);

		orderNew = (ExpandableLayoutListView) view1.findViewById(R.id.no_done);
		orderDone = (ExpandableLayoutListView) view2
				.findViewById(R.id.order_done);

		if (null != SellerApplication.orderNotOK
				|| null != SellerApplication.orderOK) {
			orderNew.setAdapter(new MyOrderAdapter(mContext,
					SellerApplication.orderNotOK, MainActivity.handler, orderNew));
			orderDone.setAdapter(new MyOrderAdapter(mContext,
					SellerApplication.orderOK, MainActivity.handler, orderDone));
		}

		lists.add(view1);
		lists.add(view2);

		initeCursor();

		myAdapter = new MyAdapter(lists);

		viewPager = (ViewPager) mView.findViewById(R.id.viewPager);
		viewPager.setAdapter(myAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				switch (arg0) {
				case 0:
					if (currentItem == 1) {
						animation = new TranslateAnimation(
								offSet * 2 + bmWidth, 0, 0, 0);
					}
					break;
				case 1:
					if (currentItem == 0) {
						animation = new TranslateAnimation(0, offSet * 2
								+ bmWidth, 0, 0);
					}
					break;
				}
				currentItem = arg0;

				animation.setDuration(500);
				animation.setFillAfter(true);
				imageView.startAnimation(animation);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		textView1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				viewPager.setCurrentItem(0);
			}
		});

		textView2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				viewPager.setCurrentItem(1);
			}
		});

		return mView;
	}

	private void initeCursor() {
		cursor = BitmapFactory
				.decodeResource(getResources(), R.drawable.cursor);
		bmWidth = cursor.getWidth();

		DisplayMetrics dm;
		dm = getResources().getDisplayMetrics();

		offSet = (dm.widthPixels - 2 * bmWidth) / 4;
		matrix.setTranslate(offSet, 0);
		imageView.setImageMatrix(matrix);
		currentItem = 0;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

}
