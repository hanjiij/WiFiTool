package com.jhj.dev.wifi.server;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.jhj.dev.wifi.server.fragment.BreakWifiFragment;
import com.jhj.dev.wifi.server.fragment.FinderFragment;
import com.jhj.dev.wifi.server.fragment.HistoryLocationFragment;
import com.jhj.dev.wifi.server.fragment.MyBaiduMapFragment;
import com.jhj.dev.wifi.server.fragment.RadarFragment;
import com.jhj.dev.wifi.server.util.InitMacSQL;
import com.jhj.dev.wifi.server.util.SetWifiPoint;
import com.jhj.dev.wifi.server.wifiaplist.WifiAPListFragment;
import com.jhj.dev.wifi.server.wifiaplist.WifiScanner;
import com.jhj.dev.wifi.server.wififinder.WifiFinderFragment;
import com.jhj.dev.wifi.server.wifirssimap.WifiRSSIMapActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 江华健
 */
public class FragmentMgrActivity extends FragmentActivity {
    private static final int INTERVAL = 2500;
    private static final String ACTION_WIFI_STATE_CHANGED = WifiManager.WIFI_STATE_CHANGED_ACTION;
    private static final String ACTION_NETWORK_STATE_CHANGED =
            WifiManager.NETWORK_STATE_CHANGED_ACTION;
    private static final String ACITON_SCAN_RESULTS_AVAILABLE =
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
    private static final String ACTION_SUPPLICANT_CONNECTION_CHANGE =
            WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION;
    private static final String ACTION_SUPPLICANT_STATE_CHANGED =
            WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
    private static final String ACTION_NETWORK_IDS_CHANGED = WifiManager.NETWORK_IDS_CHANGED_ACTION;
    private static final String ACTION_RSSI_CHANGED = WifiManager.RSSI_CHANGED_ACTION;
    private static final String ACTION_CONNECTIVITY = ConnectivityManager.CONNECTIVITY_ACTION;
    private static final String TAG_WIFIAPLIST = "wifiAPListFragment";
    private static final String TAG_WIFIFINDER = "wifiFinderFragment";
    private static final String FRAGMENTPOSITION = "fragmentPosition";
    private static final String FRAGMENTTAG = "fragmentTag";
    private static final String TAG_BREAKWIFI = "breakWifiFragment";
    ProgressDialog islocationProgressDialog, searchingProgressDialog;
    boolean isLocationSuccess = false;
    private WifiActionReceiver wifiReceiver;
    private DialogMgr dialogMgr;
    private SetWifiPoint setWifiPoint;
    private long firstPressed = 0;
    private long latestPressed;
    /**
     * Wifi扫描器
     */
    private WifiScanner wifiScanner;
    //	private String fragmentTag;
    //	private int fragmentPosition;
    private int selectViewMenuItemId =R.id.action_selectView_apList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.activity_fragment);

        new InitMacSQL(this);

        dialogMgr = DialogMgr.getInstance(this, this);
        wifiScanner = WifiScanner.getInstance(this);
        setWifiPoint = new SetWifiPoint(getApplicationContext(), 175);
        setWifiPoint.setIsLocationListener(new SetWifiPoint.IsLocationListener() {
            @Override
            public void isLocationSuccess(boolean isSuccess) {
                // TODO Auto-generated method stub

                if (isSuccess) {

                    islocationProgressDialog.cancel();

                    isLocationSuccess = true;
                }
            }
        });

        //	    ActionBar actionBar = getActionBar();
        //	    if (actionBar!=null)
        //		{
        //	    	actionBar.setDisplayShowHomeEnabled(true);
        //			actionBar.setDisplayShowTitleEnabled(true);
        //		}
        launcherDefaultFragment();
        registWifiStateReceiver();
    }


    private void launcherDefaultFragment()
    {
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,
                                                           new WifiAPListFragment())
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                   .commit();
        //		fragmentPosition = R.id.action_selectView_apList;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        String screenOrientation = "??";
        Toast.makeText(this, screenOrientation, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        System.out.println("-------------activity---------onSaveInstanceState-------------------");
        //		outState.putString(FRAGMENTTAG, fragmentTag);
        //		outState.putInt(FRAGMENTPOSITION, fragmentPosition);
    }

    private void showView(Fragment fragment)
    {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragmentContainer, fragment)
                                   .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                   .commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {

        menu.findItem(R.id.action_selectView).getSubMenu()
            .findItem(selectViewMenuItemId).setChecked(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method method = menu.getClass()
                                        .getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (NoSuchMethodException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_selectView_apList:
                showView(new WifiAPListFragment());

                break;
            case R.id.action_selectView_breakWifi:
                showView(new BreakWifiFragment());

                break;
            case R.id.action_selectView_network_topology:
                showView(new FinderFragment());
                break;
            case R.id.action_selectView_detectionWifi:
                startSearchWifi();

                break;
            case R.id.action_selectView_locationMapWifi:
                showView(new MyBaiduMapFragment());

                break;
            case R.id.action_selectView_radarWifi:
                showView(new RadarFragment());

                break;
            case R.id.action_selectView_RSSIMap:
                Intent intent = new Intent(this, WifiRSSIMapActivity.class);
                startActivity(intent);
                break;
            case R.id.action_selectView_findWifi:
                showView(new WifiFinderFragment());
                break;
            case R.id.action_about:
                dialogMgr.showAboutDialog();
                break;
            case R.id.action_quit:
                finish();
            case R.id.action_help:
                dialogMgr.showHelpDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        if (itemId != R.id.action_about &&
            itemId != R.id.action_quit &&
            itemId != R.id.action_selectView_RSSIMap &&
            itemId != R.id.action_help)
        {
            selectViewMenuItemId = itemId;
        }

        return true;
    }

    /*
     * 注册Wifi事件广播接受
     */
    private void registWifiStateReceiver()
    {

        System.out.println("---------------registWifiStateReceiver-----------------------");

        IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(ACTION_WIFI_STATE_CHANGED);
        wifiIntentFilter.addAction(ACTION_NETWORK_STATE_CHANGED);
        wifiIntentFilter.addAction(ACITON_SCAN_RESULTS_AVAILABLE);
        wifiIntentFilter.addAction(ACTION_SUPPLICANT_CONNECTION_CHANGE);
        wifiIntentFilter.addAction(ACTION_SUPPLICANT_STATE_CHANGED);
        wifiIntentFilter.addAction(ACTION_NETWORK_IDS_CHANGED);
        wifiIntentFilter.addAction(ACTION_RSSI_CHANGED);
        wifiIntentFilter.addAction(ACTION_CONNECTIVITY);

        wifiReceiver = new WifiActionReceiver(this, this);
        registerReceiver(wifiReceiver, wifiIntentFilter);

    }

    @Override
    protected void onResume()
    {
        DialogMgr.changeActivity(this);
        wifiScanner.startScan();
        super.onResume();
    }


    @Override
    protected void onStop()
    {
        wifiScanner.stopScan();
        super.onStop();
    }

    //------------------TODO------------------------
    /*
     * 退出应用时撤销注册的Wifi广播接收
	 */
    @Override
    protected void onDestroy()
    {
        System.out.println("-----------------------------unregisterReceiver----------------");
        unregisterReceiver(wifiReceiver);
        super.onDestroy();
    }


    /**
     * 开启WiFi寻找
     */
    protected void startSearchWifi() {
        // TODO Auto-generated method stub

        setWifiPoint.StartSetWifiPoint(); // 开启

        islocationProgressDialog = InitprogressDialog("正在第一次定位请稍后");

        new Timer().schedule(new TimerTask() { // 定位超时取消定位

            @Override
            public void run() {
                // TODO Auto-generated method stub

                if (!isLocationSuccess) {
                    islocationProgressDialog.cancel();
                    cancel();
                    setWifiPoint.stopGPSLocation();
                }

                isLocationSuccess = false;
            }
        }, 10000, 10000);

        islocationProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub

                if (isLocationSuccess) {
                    InitButtonprogressDialog();
                } else {
                    Toast.makeText(FragmentMgrActivity.this, "定位失败，请到移步至开阔场合", Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });

    }

    /**
     * 创建正在侦测的dialog
     */
    private void InitButtonprogressDialog() {

        searchingProgressDialog = new ProgressDialog(this);
        searchingProgressDialog.setTitle("正在侦测WIFI的位置");
        searchingProgressDialog.setMessage("请随意走动<-.->");
        searchingProgressDialog.setIndeterminate(true);
        searchingProgressDialog.setCancelable(false);

        searchingProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "结束WIFI侦测",
                                          new DialogInterface.OnClickListener() {
                                              @Override
                                              public void onClick(DialogInterface dialog, int which)
                                              {
                                                  // TODO Auto-generated method stub

                                                  setWifiPoint.SetSalculate();

                                              }
                                          });

        searchingProgressDialog.show();
    }

    /**
     * 创建普通的progressdialog
     *
     * @param content dialog需要显示的内容
     * @return progressdialog的对象
     */
    private ProgressDialog InitprogressDialog(String content) {

        ProgressDialog progressDialog = new ProgressDialog(this);
        // 实例化
        progressDialog.setMessage(content);
        // 设置ProgressDialog 提示信息
        progressDialog.setIndeterminate(true);
        // 设置ProgressDialog 的进度条是否不明确
        progressDialog.setCancelable(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        progressDialog.show();
        // 让ProgressDialog显示

        return progressDialog;
    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof HistoryLocationFragment) {
            super.onBackPressed();
            return;
        }
        if (firstPressed == 0) {

            firstPressed = System.currentTimeMillis();
            Toast.makeText(this, "再按一次后退键退出", Toast.LENGTH_SHORT).show();

        } else {
            latestPressed = System.currentTimeMillis();

            if (latestPressed - firstPressed <= INTERVAL) {
                super.onBackPressed();
            } else {

                firstPressed = latestPressed;
                Toast.makeText(this, "再按一次后退键退出", Toast.LENGTH_SHORT).show();
            }
        }


    }
    //	@Override
    //	protected void onStop()
    //	{
    //		System.out
    //				.println("-----------------------------unregisterReceiver----------------");
    //		unregisterReceiver(wifiReceiver);
    //		super.onStop();
    //	}

}
