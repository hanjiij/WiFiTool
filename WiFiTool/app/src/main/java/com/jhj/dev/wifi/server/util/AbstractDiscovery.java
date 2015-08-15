package com.jhj.dev.wifi.server.util;


import android.os.AsyncTask;

import com.jhj.dev.wifi.server.fragment.FinderFragment;

import java.lang.ref.WeakReference;


/**
 * @author 吉鹏
 */
public abstract class AbstractDiscovery extends AsyncTask<Void, HostBean, Void> {

    //private final String TAG = "AbstractDiscovery";

    public final static String KEY_VIBRATE_FINISH = "vibrate_finish";
    public final static boolean DEFAULT_VIBRATE_FINISH = false;
    final protected WeakReference<FinderFragment> mDiscover;//fragment弱类型引用
    protected int hosts_done = 0;
    protected long ip;
    protected long start = 0;
    protected long end = 0;
    protected long size = 0;

    public AbstractDiscovery(FinderFragment discover) {
        mDiscover = new WeakReference<FinderFragment>(discover);//activity弱应用
    }

    public void setNetwork(long ip, long start, long end) {
        this.ip = ip;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void onPreExecute() {//执行一个异步任务，需要我们在代码中调用此方法，触发异步任务的执行
        size = (int) (end - start + 1);

    }

    //在调用publishProgress(Progress... values)时，此方法被执行，直接将进度信息更新到UI组件上。
    @Override
    protected void onProgressUpdate(HostBean... host) {
        //异步数据加载
        if (mDiscover != null) {
            final FinderFragment discover = mDiscover.get();
            if (discover != null) {
                if (!isCancelled()) {
                    if (host[0] != null) {
                        discover.addHost(host[0]);//添加发掘到的数据
                    }
                }
            }
        }
    }

    //后台处理结果返回将调用此方法
    @Override
    protected void onPostExecute(Void unused) {
        if (mDiscover != null) {
            final FinderFragment discover = mDiscover.get();
            discover.stopDiscovering();//停止扫描
        }
    }

}
