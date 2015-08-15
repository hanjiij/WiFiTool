package com.jhj.dev.wifi.server.wifirssimap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ToggleButton;

import com.jhj.dev.wifi.server.DialogMgr;
import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.WifiMan;
import com.jhj.dev.wifi.server.model.GUIDrawData;
import com.jhj.dev.wifi.server.model.WifiFilteredMapData;
import com.jhj.dev.wifi.server.model.WifiMapData;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 江华健
 */
public class WifiRSSIMapFragment extends Fragment
        implements WifiRSSIMapView.OnWifiInfoChangedListener
{
    /*
     * 刷新UI消息标识
     * */
    private static final int MSG_REFRESHUI = 1;

    /**
     * 设置按钮
     */
    private ImageButton imgBut_setting;

    /**
     * 返回按钮
     */
    private ImageButton imgBut_back;

    /**
     * 设置按钮弹窗菜单
     */
    private PopupMenu popupMenu;

    private WifiMan wifiMan;

    /**
     * wifi地图数据模型
     */
    private WifiMapData mapData;

    /**
     * 过滤下来的信号地图数据
     */
    private WifiFilteredMapData filteredMapData;

    /**
     * 存放勾选要保留的ap的容器
     */
    private LinkedHashSet<String> preFilteredAPBSSIDs;

    /**
     * 之前保留的apBSSID
     */
    private Set<String> beforeFilteredAPBSSIDs;

    /**
     * ap高亮显示按钮的布局容器
     */
    private LinearLayout butLayout;

    /**
     * wifi信号监听者
     */
    private WifiRSSIMonitor rssiMonitor;

    /**
     * GUI绘制数据模型
     */
    private GUIDrawData drawData;

    /**
     * 信号地图视图
     */
    private WifiRSSIMapView mapView;

    /**
     * 存放高亮显示按钮的Map
     */
    private Map<String, ToggleButton> butMap;

    /**
     * 对话框管理员
     */
    private DialogMgr dialogMgr;
    /*
     * 高亮显示按钮监听器
     * */
    private CompoundButton.OnCheckedChangeListener togButCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    //System.out.println("------------------onCheckedChanged----------------");
                    //			buttonView.playSoundEffect(SoundEffectConstants.CLICK);
                    if (isChecked) {
                        for (ToggleButton but_highLight : butMap.values()) {
                            if (but_highLight != buttonView) {
                                but_highLight.setChecked(false);
                            }

                        }
                        int apIndex = ((ToggleButton) buttonView).getId();
                        //System.out.println("apindex------------------>"+apIndex);
                        mapView.setIsHightLightShow(true, filteredMapData.getFilteredAP(apIndex));
                    } else {
                        mapView.setIsHightLightShow(false, null);
                    }

                }
            };
    private View.OnClickListener togBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {

        }
    };
    /*
     * ui更新handler
     * */
    private Handler uiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == MSG_REFRESHUI) {
                @SuppressWarnings("unchecked")
                List<LinkedList<Integer>> apList = (List<LinkedList<Integer>>) msg.obj;
                changeAPBut(apList);
            }

        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        wifiMan = WifiMan.getInstance(getActivity());

        dialogMgr = DialogMgr.getInstance(getActivity(), getActivity());

        drawData = GUIDrawData.getInstance(getActivity());

        mapData = WifiMapData.getInstance(getActivity());
        filteredMapData = WifiFilteredMapData.getInstance(getActivity().getApplicationContext());

        rssiMonitor = WifiRSSIMonitor.getInstance(getActivity());
        preFilteredAPBSSIDs = new LinkedHashSet<String>();
        butMap = new LinkedHashMap<String, ToggleButton>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View wifiRSSIMapView = inflater.inflate(R.layout.fragment_wifi_rssi_map, container, false);

        mapView = (WifiRSSIMapView) wifiRSSIMapView.findViewById(R.id.wifiRSSIMapView);
        mapView.setOnWifiInfoChangedListener(this);

        butLayout = (LinearLayout) wifiRSSIMapView.findViewById(R.id.ly_but);

        imgBut_setting = (ImageButton) wifiRSSIMapView.findViewById(R.id.imgBut_setting);
        imgBut_setting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                popupMenu.show();
            }
        });

        popupMenu = new PopupMenu(getActivity(), imgBut_setting);
        popupMenu.inflate(R.menu.rssi_map_menu);
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId()) {
                    case R.id.action_filter:

                        displayFilterSelectDialog();

                        break;
                    case R.id.action_setScanInterver:

                        diaplayScanInterverSelectDialog();

                        break;
                    default:
                        break;
                }

                return true;
            }
        });


        imgBut_back = (ImageButton) wifiRSSIMapView.findViewById(R.id.imgBut_back);
        imgBut_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                //getActivity().getSupportFragmentManager().popBackStack();
                //				getActivity().setRequestedOrientation(
                //						ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                getActivity().finish();
            }
        });

        return wifiRSSIMapView;
    }

    /**
     * 显示设置扫描间隔的对话框
     */
    protected void diaplayScanInterverSelectDialog()
    {
        dialogMgr.showScanInterverSelectDialog();
    }

    /**
     * 显示选择过滤的对话框
     */
    protected void displayFilterSelectDialog()
    {
        final String[] apInfos = wifiMan.getApInfos();

        LinkedList<String> apBSSIDs = new LinkedList<String>();

        boolean[] checkedItems = new boolean[apInfos.length];

        beforeFilteredAPBSSIDs = mapData.getBeforeFilteredAPBSSIDs();

        preFilteredAPBSSIDs.addAll(beforeFilteredAPBSSIDs);

        for (String apInfo : apInfos) {
            apBSSIDs.add(apInfo.split("\n")[1]);
        }

        for (String apBSSID : beforeFilteredAPBSSIDs) {
            int apIndex = apBSSIDs.indexOf(apBSSID);

            if (apIndex != -1) {
                checkedItems[apIndex] = true;
            }
        }

        dialogMgr.showWifiFilteredDialog(apInfos, checkedItems,
                                         (LinkedHashSet<String>) beforeFilteredAPBSSIDs,
                                         preFilteredAPBSSIDs);
    }

    /**
     * 改变高亮显示点击按钮的状态
     *
     * @param apList 接入点列表
     */
    protected void changeAPBut(List<LinkedList<Integer>> apList)
    {
        for (LinkedList<Integer> ap : apList) {
            String apBSSID = filteredMapData.getFilteredAPBSSID(ap);

            if (!butMap.containsKey(apBSSID)) {

                String apName = filteredMapData.getFilteredAPName(ap);
                int brokenLineColor = drawData.getBrokenLineColor(ap);

                ToggleButton but_highLight = new ToggleButton(getActivity());
                but_highLight.setOnCheckedChangeListener(togButCheckedChangeListener);
                but_highLight.setOnClickListener(togBtnClickListener);
                but_highLight.setSoundEffectsEnabled(true);
                but_highLight.setTextAppearance(getActivity(), R.style.TextAppearance_but);
                but_highLight.setId(apList.indexOf(ap));
                but_highLight.setTextColor(brokenLineColor);
                but_highLight.setTextOn(apName.equals("") ? "隐藏" : apName);
                but_highLight.setTextOff(apName.equals("") ? "隐藏" : apName);
                but_highLight.setChecked(false);
                but_highLight.setPadding(0, 0, 0, 0);
                but_highLight.setBackgroundResource(R.drawable.selector_togbut);

                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 88,
                                                            butLayout.getResources()
                                                                     .getDisplayMetrics());
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35,
                                                             butLayout.getResources()
                                                                      .getDisplayMetrics());
                int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                                                                butLayout.getResources()
                                                                         .getDisplayMetrics());
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(width, height);
                if (apList.indexOf(ap) != 0) {
                    layoutParams.topMargin = topMargin;
                }
                butMap.put(apBSSID, but_highLight);
                butLayout.addView(but_highLight, layoutParams);

            }

        }

        if (mapData.isFilter) {
            List<String> apBSSIDs = filteredMapData.getFilteredAPBSSIDs();
            for (String key : butMap.keySet()) {
                ToggleButton but_highLight = butMap.get(key);

                int apVisiblity = !apBSSIDs.contains(key) ? View.GONE : View.VISIBLE;
                but_highLight.setVisibility(apVisiblity);

                if (apVisiblity == View.VISIBLE) {
                    but_highLight.setId(apBSSIDs.indexOf(key));
                }
            }
        }
    }

    /*
     * 当wifi信息改变时更改高亮显示的按钮状态
     * */
    @Override
    public void onWifiInfoChanged(List<LinkedList<Integer>> apList)
    {
        uiHandler.obtainMessage(MSG_REFRESHUI, apList).sendToTarget();
    }
}
