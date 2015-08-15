package com.jhj.dev.wifi.server.wififinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.jhj.dev.wifi.server.DialogMgr;
import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.WifiMan;

/**
 * @author 江华健
 */
public class WifiFinderFragment extends Fragment
        implements WifiMan.OnSpecifiedAPRSSIChangedListener, SoundPlayer.OnSoundStateChangedListener
{
    public static final int REQUEST_APNAME = 0;

    private static final int MSG_CHANGELED = 0;

    private static final int MSG_CHANGESTEPCOUNT = 1;

    private OnSpecAPRSSIChangeListener specAPRSSIChangeListener;

    /**
     * 选择接入点按钮
     */
    private Button but_selectAP;

    /**
     * 指示灯图片
     */
    private ImageView imgView_led;

    /**
     * 提示音开关切换按钮
     */
    private ToggleButton togBut_tone;


    //	private TextView tv_apRSSI;

    //	private TextView tv_stepCount;

    private WifiFinderView finderView;

    /**
     * Wifi管理者
     */
    private WifiMan wifiMan;

    /**
     * 对话框管理者
     */
    private DialogMgr dialogMgr;

    /**
     * 音效播放间隔
     */
    private int[] soundPlayIntervals;

    /**
     * 音效播放器
     */
    private SoundPlayer soundPlayer;


    //    private SensorMan sensorMan;

    //    private int apSSID;

    //    private int newSpecAPRSSI;
    private Handler refreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == MSG_CHANGELED) {
                imgView_led.setImageResource((boolean) msg.obj ? R.drawable.ic_meter_led_green_on
                                                               : R.drawable.ic_meter_led_green_off);
            } else if (msg.what == MSG_CHANGESTEPCOUNT) {
                //				tv_stepCount.setText(msg.obj.toString());
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        wifiMan = WifiMan.getInstance(getActivity());
        wifiMan.setOnSpecifiedAPRSSIChangedListener(this);

        dialogMgr = DialogMgr.getInstance(getActivity(), getActivity());


        soundPlayIntervals =
                getActivity().getResources().getIntArray(R.array.soundEffectPlayInterval);
        System.out.println("WifiFinderFragment----->onCreate()");

        //		sensorMan=SensorMan.getInstance(getActivity());
    }

    /**
     * @param specAPRSSIChangeListener 指定接入点信号改变监听器
     */
    public void setOnSpecAPRSSIChangedListener(OnSpecAPRSSIChangeListener specAPRSSIChangeListener)
    {
        this.specAPRSSIChangeListener = specAPRSSIChangeListener;

    }

    /**
     * 初始化显示数据
     */
    private void initData()
    {
        String[] apInfos = wifiMan.getApInfos();
        if (apInfos.length > 0) {
            String[] apInfo = apInfos[0].split("\n");
            wifiMan.setSpecBSSID(apInfo[1]);
            but_selectAP.setText(apInfo[0]);
            //			apSSID=wifiMan.getAPRSSIBySpecBSSID(apInfo[1]);
            //			tv_apRSSI.setText(apSSID+"dBm");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View wifiFinderView = inflater.inflate(R.layout.fragment_wifi_finder, container, false);
        getUIObjHandle(wifiFinderView);

        initData();

        return wifiFinderView;
    }


    /**
     * 获取界面组件引用
     *
     * @param wifiFinderView 界面根视图
     */
    private void getUIObjHandle(View wifiFinderView)
    {
        finderView = (WifiFinderView) wifiFinderView.findViewById(R.id.wifiFinderView);
        setOnSpecAPRSSIChangedListener(finderView);

        but_selectAP = (Button) wifiFinderView.findViewById(R.id.but_selectAP);
        but_selectAP.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                showSelectAPDialog();
            }
        });

        imgView_led = (ImageView) wifiFinderView.findViewById(R.id.imgView_led);

        togBut_tone = (ToggleButton) wifiFinderView.findViewById(R.id.togBut_tone);
        togBut_tone.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked) {
                    soundPlayer.playSound();
                } else {
                    soundPlayer.pauseSound();
                }
            }
        });

        //		tv_apRSSI=(TextView)wifiFinderView.findViewById(R.id.tv_apRSSI);

        //		tv_stepCount=(TextView)wifiFinderView.findViewById(R.id.tv_stepCount);

    }

    /**
     * 显示选择接入点对话框
     */
    protected void showSelectAPDialog()
    {
        String[] apInfos = wifiMan.getApInfos();

        if (apInfos.length > 0) {
            dialogMgr.showSelectAPDialog(this, REQUEST_APNAME, apInfos, wifiMan.getSpecBSSID());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != REQUEST_APNAME) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            String[] apInfo = data.getStringExtra(SelectAPFragment.EXTRA_APNAME).split("\n");
            but_selectAP.setText(apInfo[0]);
            wifiMan.setSpecBSSID(apInfo[1]);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onSpecifiedAPRSSIChanged(int specAPRSSI)
    {
        //		int newSpecAPRSSI=specAPRSSI;
        //		System.out.println("---onSpecifiedAPRSSIChanged---");
        //		tv_apRSSI.setText(specAPRSSI+"dBm");
        if (soundPlayer != null) {
            soundPlayer.setPlayInterval(changeSoundPlayInterval(specAPRSSI));
            specAPRSSIChangeListener.onSpecAPRSSIChanged(specAPRSSI);
        }

    }


    /**
     * 改变音效播放间隔
     *
     * @param specAPRSSI 指定接入点信号值
     * @return 音效播放间隔
     */
    private int changeSoundPlayInterval(int specAPRSSI)
    {
        int playInterval = 0;

        if (specAPRSSI >= -55) {
            playInterval = soundPlayIntervals[0];
        } else if (specAPRSSI >= -60) {
            playInterval = soundPlayIntervals[1];
        } else if (specAPRSSI >= -70) {
            playInterval = soundPlayIntervals[2];
        } else if (specAPRSSI >= -85) {
            playInterval = soundPlayIntervals[3];
        } else {
            playInterval = soundPlayIntervals[4];
        }

        return playInterval;
    }

    //	@Override
    //	public void onHiddenChanged(boolean hidden)
    //	{
    //		Toast.makeText(getActivity(),
    //				"--------WifiFinderFragment--->onHiddenChanged---------",
    //				Toast.LENGTH_SHORT).show();
    //		wifiMan.setRefreshSpecifiedAPRSSI(!isHidden());
    //		soundPlayer.setPlay(!isHidden());
    //		super.onHiddenChanged(hidden);
    //	}

    @Override
    public void onResume()
    {
        System.out.println("WifiFinderFragment----->onResume()");

        soundPlayer = SoundPlayer.getInstance(getActivity());
        soundPlayer.setOnSoundStateChangedListener(this);


        wifiMan.setRefreshSpecifiedAPRSSI(true);

        //		soundPlayer.setPlay(true);


        //		sensorMan.registerListener();
        super.onResume();
    }

    @Override
    public void onStop()
    {
        System.out.println("WifiFinderFragment----->onStop()");
        wifiMan.setRefreshSpecifiedAPRSSI(false);
        soundPlayer.stopSound();
        togBut_tone.setChecked(false);

        //		sensorMan.unregisterListener();
        super.onStop();
    }

    @Override
    public void onSoundStateChanged(boolean isPlaying)
    {
        refreshHandler.obtainMessage(MSG_CHANGELED, isPlaying).sendToTarget();
    }

    /**
     * 指定接入点信号改变监听接口
     */
    public static interface OnSpecAPRSSIChangeListener {
        //指定接入点信号改变回调
        void onSpecAPRSSIChanged(int specAPRSSI);
    }

    //	@Override
    //	public void onStepCountChanged(int stepCount)
    //	{
    //		refreshHandler.obtainMessage(MSG_CHANGESTEPCOUNT, stepCount).sendToTarget();
    //
    //	}


    //	public void startRefreshSpecAPRSSI()
    //	{
    //		wifiMan.setRefreshSpecifiedAPRSSI(true);
    //	}
    //	public void stopRefershSpecAPRSSI()
    //	{
    //		wifiMan.setRefreshSpecifiedAPRSSI(false);
    //	}

}
