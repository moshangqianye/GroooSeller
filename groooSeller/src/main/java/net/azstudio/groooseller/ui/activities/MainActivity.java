package net.azstudio.groooseller.ui.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.octicons_typeface_library.Octicons;
import com.testin.agent.TestinAgent;

import net.azstudio.groooseller.R;
import net.azstudio.groooseller.http.NetworkWrapper;
import net.azstudio.groooseller.model.app.PushInfo;
import net.azstudio.groooseller.model.app.ShopInfo;
import net.azstudio.groooseller.model.business.FoodOrder;
import net.azstudio.groooseller.ui.base.BaseActivity;
import net.azstudio.groooseller.ui.widgets.EmptySupportRecyclerView;
import net.azstudio.groooseller.ui.widgets.adapter.OrderAdapter;
import net.azstudio.groooseller.utils.AppManager;
import net.azstudio.groooseller.utils.AppPreferences;
import net.azstudio.groooseller.utils.Logs;
import net.azstudio.groooseller.utils.RxEvent.OrderEvent;
import net.azstudio.groooseller.utils.RxJava.RxBus;
import net.azstudio.groooseller.utils.RxJava.RxNetWorking;
import net.azstudio.groooseller.utils.Toasts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.jpush.android.api.JPushInterface;
import rx.Observable;


/*
 * 云推送Demo主Activity。
 * 代码中，注释以Push标注开头的，表示接下来的代码块是Push接口调用示例
 */
public class MainActivity extends BaseActivity implements OnCheckedChangeListener {
    private static final int PROFILE_LOGOUT = 100000;

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int[] backgrounds = {R.drawable.mat1,
            R.drawable.mat2, R.drawable.mat3, R.drawable.mat4};
    public static boolean isForeground = false;
    private AccountHeader headerResult = null;

    private IProfile profile;
    private Drawer result = null;
    private SwitchDrawerItem shopStatus;

    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout refreshLayout;

    private List<FoodOrder> undoOrder = new ArrayList<>(), finishedOrder = new ArrayList<>();

    private Observable<ShopInfo> shopInfoObservable;

    private Observable<List<FoodOrder>[]> observableRefreshData;

    private OrderAdapter undo, finished;

    @Override
    protected boolean isDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppPreferences.get().getAuthUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            NetworkWrapper.get()
                    .setPushInfo(new PushInfo(JPushInterface.getRegistrationID(this)))
                    .subscribe(s -> {
                                Logs.d(s);
                            }, throwable ->
                                    Snackbar.make(pager, throwable.getMessage(), Snackbar.LENGTH_SHORT).show()
                    );
            TestinAgent.setUserInfo(AppManager.getShopInfo().getName());
            setUpDrawer(savedInstanceState);
            registerMessageReceiver();
            setUpPager();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            observableRefreshData = NetworkWrapper.get()
                    .getOrder(new Date(), calendar.getTime())
                    .compose(RxNetWorking.bindRefreshing(refreshLayout));
            shopInfoObservable = NetworkWrapper.get().getShopInfo(AppManager.getShopInfo().getId());
            refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryLight, R.color.colorPrimary);
            RxSwipeRefreshLayout.refreshes(refreshLayout)
                    .doOnUnsubscribe(() -> {
                        refreshLayout.setRefreshing(false);
                    })
                    .compose(bindToLifecycle())
                    .subscribe(aVoid -> {
                        refreshData();
                    });
            addSubscription(RxBus.getDefault().toObserverable(OrderEvent.class).subscribe(orderEvent -> {
                finishedOrder.add(0, orderEvent.getOrder());
                finished.notifyDataSetChanged();
            }));
        }

    }

    @Override
    protected void onResume() {
        isForeground = true;
        refreshData();
        super.onResume();
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    private void refreshData() {
        observableRefreshData.subscribe(menus -> notifyDataSetChanged(menus)
                , throwable -> Snackbar.make(pager, throwable.getMessage(), Snackbar.LENGTH_SHORT).show());
        shopInfoObservable.subscribe(shopInfo -> {
            AppPreferences.get().setShopInfo(shopInfo);
            AppManager.setShopInfo(shopInfo);
            headerResult.getActiveProfile().withEmail(shopInfo.getDescription());
            headerResult.getActiveProfile().withName(shopInfo.getName());
            headerResult.getActiveProfile().withIcon(shopInfo.getLogo());
            headerResult.updateProfile(profile);
            shopStatus.withName("营业状态:" + (shopInfo.getStatus() == 1 ? "开" : "关"));
            result.updateItem(shopStatus);
        }, throwable -> Snackbar.make(pager, throwable.getMessage(), Snackbar.LENGTH_SHORT).show());
    }

    private void notifyDataSetChanged(List<FoodOrder>[] orders) {
        undoOrder.clear();
        finishedOrder.clear();
        undoOrder.addAll(orders[0]);
        finishedOrder.addAll(orders[1]);
        undo.notifyDataSetChanged();
        finished.notifyDataSetChanged();
    }

    private void setUpPager() {
        undo = new OrderAdapter(undoOrder);
        finished = new OrderAdapter(finishedOrder);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                refreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View v = getLayoutInflater().inflate(R.layout.item_viewpager, container, false);
                EmptySupportRecyclerView recyclerView = (EmptySupportRecyclerView) v.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setEmptyView(v.findViewById(R.id.empty_view));
                if (position == 0)
                    recyclerView.setAdapter(undo);
                else {
                    recyclerView.setAdapter(finished);
                }
                container.addView(v);
                return v;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0)
                    return "未处理";
                else return "已处理";
            }
        });
        tabs.setupWithViewPager(pager);
    }

    private void setUpDrawer(Bundle savedInstanceState) {

        shopStatus = new SwitchDrawerItem().withName("营业状态:" + (AppManager.getShopInfo().getStatus() == 1 ? "开" : "关"))
                .withIcon(Octicons.Icon.oct_tools).withChecked(AppManager.getShopInfo().getStatus() == 1 ? true : false).withIdentifier(1).withOnCheckedChangeListener(this);

        profile = new ProfileDrawerItem().withName(AppManager.getShopInfo().getName())
                .withEmail(AppManager.getShopInfo().getDescription()).withIcon(AppManager.getShopInfo().getLogo()).withIdentifier(100);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(backgrounds[new Random().nextInt(backgrounds.length)])
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("退出登录").withDescription("清楚用户信息").withIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_lock_outline).actionBar().colorRes(R.color.material_drawer_primary_text)).withIdentifier(PROFILE_LOGOUT)
                )
                .withOnAccountHeaderListener((view, profile1, current) -> {
                    //sample usage of the onProfileChanged listener
                    //if the clicked item has the identifier 1 add a new profile ;)
                    if (profile1 instanceof IDrawerItem && profile1.getIdentifier() == PROFILE_LOGOUT) {
                        AppPreferences.get().clearAll();
                        finish();
                    }

                    //false if you have not consumed the event and it should close the drawer
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(getToolbar())
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        shopStatus,
                        new PrimaryDrawerItem().withName(R.string.activitylabel_ordertoday).withTextColorRes(R.color.material_drawer_primary_text).withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(2).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.activitylabel_shopmanage).withTextColorRes(R.color.material_drawer_primary_text).withIcon(GoogleMaterial.Icon.gmd_shop).withIdentifier(3).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.activitylabel_menumanage).withTextColorRes(R.color.material_drawer_primary_text).withIcon(GoogleMaterial.Icon.gmd_menu).withIdentifier(4).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.activitylabel_allorder).withTextColorRes(R.color.material_drawer_primary_text).withIcon(GoogleMaterial.Icon.gmd_done_all).withIdentifier(5).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.activitylabel_exit).withTextColorRes(R.color.material_drawer_primary_text).withIcon(GoogleMaterial.Icon.gmd_assignment_return).withIdentifier(6).withSelectable(false)
                ).addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.activitylabel_aboutus).withTextColorRes(R.color.material_drawer_primary_text).withIcon(GoogleMaterial.Icon.gmd_extension).withIdentifier(7).withSelectable(false)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    //check if the drawerItem is set.
                    //there are different reasons for the drawerItem to be null
                    //--> click on the header
                    //--> click on the footer
                    //those items don't contain a drawerItem

                    if (drawerItem != null) {
                        Intent intent = null;
                        switch ((int) drawerItem.getIdentifier()) {
                            case 3:
                                intent = new Intent(MainActivity.this, ManageActivity.class);
                                break;
                            case 4:
                                intent = new Intent(MainActivity.this, MenuActivity.class);
                                break;
                            case 5:
                                break;
                        }
                        if (intent != null) {
                            MainActivity.this.startActivity(intent);
                        }
                    }

                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 10

            //set the active profile
            headerResult.setActiveProfile(profile);
        }
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


    @Override
    public void onBackPressed() {
        // 点击返回键关闭滑动菜单
        if (result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            exitBy2Click();
        }
    }

    @Override
    public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {

        switch ((int) drawerItem.getIdentifier()) {
            case 1:
                addSubscription(NetworkWrapper.get()
                        .setShopStatus(isChecked ? 1 : 0)
                        .compose(bindToLifecycle())
                        .subscribe(message -> {
                            MainActivity.this.shopStatus.withName("营业状态:" + (isChecked ? "开" : "关"));
                            AppManager.getShopInfo().setStatus(isChecked ? 1 : 0);
                            result.updateItem(MainActivity.this.shopStatus);
                        }, throwable -> Snackbar.make(pager, throwable.getMessage(), Snackbar.LENGTH_SHORT).show()));
                break;
        }
    }

    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                setCostomMsg(showMsg.toString());
            }
        }

    }

    private void setCostomMsg(String msg) {
        Snackbar.make(pager, msg, Snackbar.LENGTH_SHORT).show();
    }
}
