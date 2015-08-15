package com.jhj.dev.wifi.server.wifiaplist;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jhj.dev.wifi.server.DialogMgr;
import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.WifiMan;
import com.jhj.dev.wifi.server.util.BreakWifiUtil;


/**
 * @author 江华健
 */
public class WifiAPListFragment
        extends Fragment
        implements WifiMan.OnWifiInfoChangedListener {
    /**
     * Wifi接入点列表适配器
     */
    private WifiAPExpListAdapter adapter;

    /**
     * 可扩展接入点列表视图
     */
    private ExpandableListView expLV_wifiAPList;

    /**
     * wifi连接状态显示TextView
     */
    private TextView tv_wifiConState;

    /**
     * wifi破解工具类
     */
    private BreakWifiUtil breakWifiUtil;

    /**
     * Wifi管理员
     */
    private WifiMan wifiMan;

    /**
     * 对话框管理者
     */
    private DialogMgr dialogMgr;


    /**
     * 获取指定接入点的详情的接入点MAC地址
     */
    //	private String apBSSID;

    /**
     * 获取指定接入点的详情的接入点名
     */
    private String apName;

    /**
     * 获取指定接入点的详情信息
     */
    private String apDetails;

    /**
     * Wifi连接状态指示图标
     */
    private Drawable[] wifiConStateDrawables;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        wifiMan = WifiMan.getInstance(getActivity());
        wifiMan.addOnWifiInfoChangedListener(this);
        //		wifiScanner = WifiScanner.getInstance(getActivity());
        dialogMgr = DialogMgr.getInstance(getActivity(), getActivity());

        wifiConStateDrawables = new Drawable[]{getResources().getDrawable(R.drawable.ic_error_red),
                getResources().getDrawable(R.drawable.ic_warning),
                getResources().getDrawable(R.drawable.ic_ok)};
        System.out.println("WifiAPListFragment--------->onCreate()");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_ap_list, container, false);
        getUIObjHandles(rootView);

        System.out.println("WifiAPListFragment--------->onCreateView()");
        return rootView;
    }

    /**
     * 获取界面组件引用
     *
     * @param rootView Wifi接入点列表布局根视图
     */
    private void getUIObjHandles(View rootView) {
        tv_wifiConState = (TextView) rootView.findViewById(R.id.tv_wifiConState);
        tv_wifiConState.setText(
                wifiMan.isWifiOpened() ? wifiMan.isWifiConnected() ? wifiMan.getApConnectState()
                        : getString(
                        R.string.wifi_disconnected)
                        : getString(R.string.wlan_disabled));
        Drawable img = wifiMan.isWifiOpened() ? wifiMan.isWifiConnected() ? wifiConStateDrawables[2]
                : wifiConStateDrawables[1]
                : wifiConStateDrawables[0];
        img.setBounds(0, 0, img.getIntrinsicWidth(), img.getIntrinsicHeight());
        tv_wifiConState.setCompoundDrawables(img, null, null, null);
        tv_wifiConState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiMan.isWifiOpened() && wifiMan.isWifiConnected()) {
                    dialogMgr.showWifiAPDetailsDialog(
                            ((TextView) v).getText().toString().split(" ")[1],
                            wifiMan.getWifiAPConnectedDetails());
                }
            }
        });

        expLV_wifiAPList = (ExpandableListView) rootView.findViewById(R.id.expLV_wifiList);

        registerForContextMenu(expLV_wifiAPList);
        adapter = new WifiAPExpListAdapter();
        expLV_wifiAPList.setAdapter(adapter);

        View apListEmptyView = rootView.findViewById(R.id.apList_emptyView);
        expLV_wifiAPList.setEmptyView(apListEmptyView);

    }

    @Override
    public void onWifiInfoChanged() {
        //		System.out.println("+++++++++++");
        //刷新接入点列表
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onWifiConStateChanged(String newConState) {
        tv_wifiConState.setText(newConState);
        Drawable img = wifiMan.isWifiOpened() ? wifiMan.isWifiConnected() ? wifiConStateDrawables[2]
                : wifiConStateDrawables[1]
                : wifiConStateDrawables[0];
        img.setBounds(0, 0, img.getIntrinsicWidth(), img.getIntrinsicHeight());
        tv_wifiConState.setCompoundDrawables(img, null, null, null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.wifi_ap_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sortByName:
                wifiMan.changeSort("sortByWifiName");
                break;
            case R.id.action_sortByRSSI:
                wifiMan.changeSort("sortByWifiLevel");
                break;

            case R.id.automatic_break_wifi:

                if (breakWifiUtil == null) {

                    breakWifiUtil = new BreakWifiUtil(getActivity());
                }
                breakWifiUtil.InitBreakWifi(null, true);
                break;
            default:
                break;
        }
        item.setChecked(true);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.ap_list_item_context, menu);

        ExpandableListContextMenuInfo expListContextMenuInfo =
                (ExpandableListContextMenuInfo) menuInfo;

        View targetView = expListContextMenuInfo.targetView;

        long pp = expListContextMenuInfo.packedPosition;

        int type = ExpandableListView.getPackedPositionType(pp);

        int groupPosition = ExpandableListView.getPackedPositionGroup(pp);

        int childPosition = ExpandableListView.getPackedPositionChild(pp);

        boolean isGroup = type == ExpandableListView.PACKED_POSITION_TYPE_GROUP;

        //		apBSSID=wifiMan.getWifiAPBSSID(isGroup, isGroup?groupPosition:groupPosition,childPosition);

        apName = ((TextView) targetView.findViewById(R.id.tv_ap_SSID)).getText().toString();

        //		apDetails = wifiMan.isAPConnected(apBSSID) ? wifiMan
        //				.getWifiAPConnectedDetails() : wifiMan
        //				.getWifiAPSelectedDetails(isGroup, isGroup ? groupPosition
        //						: groupPosition, childPosition);
        apDetails =
                wifiMan.getWifiAPSelectedDetails(isGroup, isGroup ? groupPosition : groupPosition,
                        childPosition);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        //		ExpandableListContextMenuInfo expListContextMenuInfo=(ExpandableListContextMenuInfo) item.getMenuInfo();
        //
        //		long pp = expListContextMenuInfo.packedPosition;
        //
        //		int type = ExpandableListView.getPackedPositionType(pp);
        //
        //		int groupPosition = ExpandableListView.getPackedPositionGroup(pp);
        //
        //		int childPosition = ExpandableListView.getPackedPositionChild(pp);

        switch (item.getItemId()) {
            case R.id.menu_context_item_look_ap_details:

                dialogMgr.showWifiAPDetailsDialog(apName, apDetails);


                //			if (type==ExpandableListView.PACKED_POSITION_TYPE_GROUP)
                //			{
                //
                //			}else if (type==ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                //
                //			}
                break;
            case R.id.menu_context_item_connect_ap:
                if (wifiMan.isWifiConfigSaved("\"" + apName + "\"")) {
                    wifiMan.connectWifiDirect("\"" + apName + "\"");
                } else {
                    dialogMgr.showWifiAPConnectDialog(apName);
                }

                break;

            case R.id.menu_context_item_break_ap:

                if (!apDetails.equals("[ESS]")) {

                    if (breakWifiUtil == null) {

                        breakWifiUtil = new BreakWifiUtil(getActivity());
                    }

                    breakWifiUtil.InitBreakWifi(apName, false);
                } else {
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.is_open_ap),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {

        super.onResume();
        wifiMan.setRefreshWifiAPList(true);

    }

    //	@Override
    //	public void onHiddenChanged(boolean hidden)
    //	{
    //		Toast.makeText(getActivity(),
    //				"--------WifiAPListFragment--->onHiddenChanged---------",
    //				Toast.LENGTH_SHORT).show();
    //		wifiMan.setRefreshWifiAPList(!isHidden());
    //	}

    @Override
    public void onStop() {
        super.onStop();
        wifiMan.setRefreshWifiAPList(false);
    }

    /**
     * Wifi接入点数据显示列表适配器
     */
    private class WifiAPExpListAdapter
            extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {

            return wifiMan.getWifiAPGroupCount();
        }

        @Override
        public int getChildrenCount(int groupPosition) {

            return wifiMan.getWifiAPChildCount(groupPosition);
        }

        @Override
        public Object getGroup(int groupPosition) {

            return wifiMan.getWifiAPGroupName(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {

            return wifiMan.getWifiAPChildName(groupPosition, childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
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
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.item_wifi_ap_group, parent, false);
            }

            ImageView imgView_ap_level =
                    (ImageView) convertView.findViewById(R.id.imgView_ap_level);
            imgView_ap_level.setImageResource(wifiMan.getGroupWifiAPLevelIcon(groupPosition));

            ImageView imgView_groupIndicator =
                    (ImageView) convertView.findViewById(R.id.imgView_groupIndicator);
            imgView_groupIndicator.setBackgroundResource(
                    wifiMan.isHasChildren(groupPosition) ? isExpanded
                            ? R.drawable.expander_close_holo_dark
                            : R.drawable.expander_open_holo_dark
                            : 0);

            ImageView imgView_ap_5G = (ImageView) convertView.findViewById(R.id.imgView_ap_5G);
            imgView_ap_5G.setBackgroundResource(
                    wifiMan.isAP5GHz(true, groupPosition) ? R.drawable.ic_ap_5g_white : 0);

            TextView tv_apSSID = (TextView) convertView.findViewById(R.id.tv_ap_SSID);
            String apSSID = getGroup(groupPosition).toString();
            tv_apSSID.setText(apSSID.equals("") ? getString(R.string.txt_hide) : apSSID);

            TextView tv_apMAC = (TextView) convertView.findViewById(R.id.tv_ap_MAC);
            tv_apMAC.setText(wifiMan.getWifiAPMAC(true, groupPosition));

            TextView tv_apCapabilities =
                    (TextView) convertView.findViewById(R.id.tv_ap_capabilities);
            tv_apCapabilities.setText(wifiMan.getWifiAPCapabilities(true, groupPosition));

            TextView tv_apFrequency = (TextView) convertView.findViewById(R.id.tv_ap_frequency);
            int apFrequency = wifiMan.getWifiAPFrequency(true, groupPosition);
            tv_apFrequency.setText(apFrequency + getString(R.string.txt_MHz) +
                    wifiMan.judgeFrequency(apFrequency) +
                    getString(R.string.txt_GHz));

            WifiAPListItemRSSIView listItemRSSIView =
                    (WifiAPListItemRSSIView) convertView.findViewById(R.id.wifiAPListItemRSSIView);
            listItemRSSIView.setIsGroup(true, groupPosition);
            listItemRSSIView.invalidate();

            TextView tv_apOrganization =
                    (TextView) convertView.findViewById(R.id.tv_ap_organization);
            tv_apOrganization.setText(wifiMan.getWifiAPOrganization(true, groupPosition));

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.item_wifi_ap_child, parent, false);
            }

            ImageView imgView_ap_level =
                    (ImageView) convertView.findViewById(R.id.imgView_ap_level);
            imgView_ap_level.setImageResource(
                    wifiMan.getChildWifiAPLevelIcon(groupPosition, childPosition));

            ImageView imgView_ap_5G = (ImageView) convertView.findViewById(R.id.imgView_ap_5G);
            imgView_ap_5G.setBackgroundResource(
                    wifiMan.isAP5GHz(false, groupPosition, childPosition)
                            ? R.drawable.ic_ap_5g_white : 0);

            TextView tv_apSSID = (TextView) convertView.findViewById(R.id.tv_ap_SSID);
            String apSSID = getChild(groupPosition, childPosition).toString();
            tv_apSSID.setText(apSSID.equals("") ? getString(R.string.txt_hide) : apSSID);

            TextView tv_apMAC = (TextView) convertView.findViewById(R.id.tv_ap_MAC);
            tv_apMAC.setText(wifiMan.getWifiAPMAC(false, groupPosition, childPosition));

            TextView tv_apCapabilities =
                    (TextView) convertView.findViewById(R.id.tv_ap_capabilities);
            tv_apCapabilities
                    .setText(wifiMan.getWifiAPCapabilities(false, groupPosition, childPosition));

            TextView tv_apFrequency = (TextView) convertView.findViewById(R.id.tv_ap_frequency);
            int apFrequency = wifiMan.getWifiAPFrequency(false, groupPosition, childPosition);
            tv_apFrequency.setText(apFrequency + getString(R.string.txt_MHz) +
                    wifiMan.judgeFrequency(apFrequency) +
                    getString(R.string.txt_GHz));

            WifiAPListItemRSSIView listItemRSSIView =
                    (WifiAPListItemRSSIView) convertView.findViewById(R.id.wifiAPListItemRSSIView);
            listItemRSSIView.setIsGroup(false, groupPosition, childPosition);
            listItemRSSIView.invalidate();

            TextView tv_apOrganization =
                    (TextView) convertView.findViewById(R.id.tv_ap_organization);
            tv_apOrganization
                    .setText(wifiMan.getWifiAPOrganization(false, groupPosition, childPosition));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }


}
