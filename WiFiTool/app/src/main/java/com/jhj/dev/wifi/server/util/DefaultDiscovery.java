package com.jhj.dev.wifi.server.util;

import android.util.Log;

import com.jhj.dev.wifi.server.fragment.FinderFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 吉鹏
 */
public class DefaultDiscovery extends AbstractDiscovery {

    public final static String KEY_TIMEOUT_DISCOVER = "timeout_discover";
    public final static String DEFAULT_TIMEOUT_DISCOVER = "500";
    //    private final static int[] DPORTS = { 139, 445, 22, 80 };
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10; //FIXME: Test, plz set in options again ?
    private final String TAG = "DefaultDiscovery";
    private List<HostBean> hosts = null;// 资源可序列化集合
    //    private final int mRateMult = 5; // Number of alive hosts between Rate
    private int pt_move = 2; // 1=backward 2=forward
    private ExecutorService mPool;//地址池


    //    private static DefaultDiscovery defaultDiscovery;

    public DefaultDiscovery(FinderFragment discover) {
        super(discover);
        hosts = new ArrayList<HostBean>();// 实例化集合
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    //
    @Override
    protected Void doInBackground(Void... params) {
        if (mDiscover != null) {
            final FinderFragment discover = mDiscover.get();
            if (discover != null) {
                Log.v(TAG,
                      "start=" + NetInfo.getIpFromLongUnsigned(start) + " (" + start + "), end=" +
                      NetInfo.getIpFromLongUnsigned(end) + " (" + end + "), length=" + size);
                mPool = Executors.newFixedThreadPool(THREADS);//设置线程池的最大容量10
                if (ip <= end && ip >= start) {
                    Log.i(TAG, "Back and forth scanning");
                    // gateway
                    launch(start);//开始

                    // hosts
                    long pt_backward = ip;
                    long pt_forward = ip + 1;
                    long size_hosts = size - 1;

                    for (int i = 0; i < size_hosts; i++) {
                        // Set pointer if of limits
                        if (pt_backward <= start) {
                            pt_move = 2;
                        } else if (pt_forward > end) {
                            pt_move = 1;
                        }
                        // Move back and forth
                        if (pt_move == 1) {
                            launch(pt_backward);
                            pt_backward--;
                            pt_move = 2;
                        } else if (pt_move == 2) {
                            launch(pt_forward);
                            pt_forward++;
                            pt_move = 1;
                        }
                    }
                } else {
                    Log.i(TAG, "Sequencial scanning");
                    for (long i = start; i <= end; i++) {
                        launch(i);
                    }
                }
                mPool.shutdown();//结束任务
                try {
                    //当地址池查询超过时间限制所做的处理
                    if (!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)) {
                        mPool.shutdownNow();
                        Log.e(TAG, "关闭线程池");
                        if (!mPool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)) {
                            Log.e(TAG, "线程池没有结束");
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                    mPool.shutdownNow();//阻止所有积极的任务
                    Thread.currentThread().interrupt();//////中断当前线程
                }
            }
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
                // FIXME: Prevents some task to end (and close the Save DB)
            }
        }
        super.onCancelled();
    }

    private void launch(long i) {
        if (!mPool.isShutdown()) {//线程池是否被阻塞
            //ExecutorService中execute（）方法，在这个方法里开启一个线程，
            mPool.execute(new CheckRunnable(NetInfo.getIpFromLongUnsigned(i)));
        }
    }

    //返回是否超时
    private int getRate() {
        if (mDiscover != null) {
            final FinderFragment discover = mDiscover.get();
            if (discover != null) {
                return Integer.parseInt(discover.prefs.getString(KEY_TIMEOUT_DISCOVER,
                                                                 DEFAULT_TIMEOUT_DISCOVER));//返回超时
                //            	return 500;
            }
        }
        return 1;
    }

    //
    private void publish(final HostBean host) {
        hosts_done++;

        System.out.println("shuju====>" + hosts_done);
        if (host == null) {
            publishProgress((HostBean) null);
            return;
        }

        if (mDiscover != null) {
            final FinderFragment discover = mDiscover.get();
            if (discover != null) {
                // Mac Addr not already detected
                if (NetInfo.NOMAC.equals(host.hardwareAddress)) {
                    host.hardwareAddress = HardwareAddress.getHardwareAddress(host.ipAddress);
                }

                if (discover.net.gatewayIp.equals(host.ipAddress)) {
                    host.deviceType = HostBean.TYPE_GATEWAY;
                }
            }
        }
        int count = 0;
        System.out.println("=========>count:" + count++);
        publishProgress(host);
    }

    private class CheckRunnable implements Runnable {
        private String addr;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if (isCancelled()) {
                publish(null);
            }
            Log.e(TAG, "run=" + addr);
            // Create host object
            final HostBean host = new HostBean();
            host.responseTime = getRate();//结果返回时间
            host.ipAddress = addr;
            try {
                InetAddress h = InetAddress.getByName(addr);//获得局域网内的ip
                //                System.out.println("h====>h:"+h);
                // Arp Check #1
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
                    Log.e(TAG, "found using arp #1 " + addr);

                    publish(host);
                    return;
                }
                // Native InetAddress check
                if (h.isReachable(getRate())) {
                    Log.e(TAG, "found using InetAddress ping " + addr);
                    Log.i("CheckRunnable", host.toString());
                    publish(host);

                    return;
                }

                // Arp Check #2
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
                    Log.e(TAG, "found using arp #2 " + addr);
                    publish(host);
                    return;
                }
            } catch (IOException e) {
                publish(null);
                Log.e(TAG, e.getMessage());
            }
        }
    }


}
