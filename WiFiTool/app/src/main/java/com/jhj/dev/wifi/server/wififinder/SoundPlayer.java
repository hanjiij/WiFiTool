package com.jhj.dev.wifi.server.wififinder;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.jhj.dev.wifi.server.R;


/**
 * @author 江华健
 */
public class SoundPlayer {
    private static SoundPlayer soundPlayer;

    /**
     * 应用上下文
     */
    private Context appContext;

    /**
     * 播放音效的对象
     */
    private SoundPool soundPool;

    /**
     * 音效播放状态改变监听器
     */
    private OnSoundStateChangedListener listener;

    /**
     * 加载音效后返回的音效ID
     */
    private int soundId_load;

    /**
     * 播放音效后返回的音效ID
     */
    private int soundId_play;

    /**
     * 播放音效的间隔
     */
    private int playInterval = 300;

    /**
     * 是否播放音效
     */
    private boolean isPlay;


    @SuppressWarnings("deprecation")
    public SoundPlayer(Context context)
    {
        appContext = context;

        //		AudioAttributes audioAttributes=new AudioAttributes.Builder()
        //		.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        //		.setUsage(AudioAttributes.USAGE_MEDIA)
        //		.setLegacyStreamType(AudioManager.STREAM_MUSIC)
        //		.build();

        //		soundPool=new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        soundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundId_load = soundPool.load(appContext, R.raw.beep, 1);
    }

    public static SoundPlayer getInstance(Context context)
    {
        if (soundPlayer == null) {
            soundPlayer = new SoundPlayer(context.getApplicationContext());
        }
        return soundPlayer;
    }

    /**
     * 设置音效播放状态
     *
     * @param listener 音效播放状态改变监听器
     */
    public void setOnSoundStateChangedListener(OnSoundStateChangedListener listener)
    {
        this.listener = listener;
    }

    /**
     * 播放音效
     */
    public void playSound()
    {
        isPlay = true;
        new Thread(new SoundPlayTask()).start();
    }

    /**
     * 暂停播放音效
     */
    public void pauseSound()
    {
        isPlay = false;
        if (soundId_play == 0 || soundPool == null) {
            return;
        }
        soundPool.pause(soundId_play);
    }

    /**
     * 停止播放音效
     */
    public void stopSound()
    {
        isPlay = false;
        if (soundId_play == 0 || soundPool == null) {
            return;
        }

        //停止播放音效
        soundPool.stop(soundId_play);
        //释放播放占用的内存资源
        soundPool.release();
        soundPool = null;
        soundPlayer = null;
    }

    /**
     * 获取音效播放间隔
     *
     * @return 音效播放间隔
     */
    public int getPlayInterval()
    {
        return playInterval;
    }

    /**
     * 设置音效播放间隔
     *
     * @param playInterval 音效播放间隔
     */
    public void setPlayInterval(int playInterval)
    {
        this.playInterval = playInterval;
    }


    //	public void changePlayState(boolean isPlayForever)
    //	{
    //		if (isPlayForever)
    //		{
    //			System.out.println("----------------isPlayForever----------------");
    //			soundId_play = soundPool.play(soundId_load, 1, 1, 1, -1, 1.0f);
    //		}else {
    //			soundPool.setLoop(soundId_play, 0);
    //		}
    //	}
    /**
     * 判断是否需要播放音效
     * @return 是否需要播放
     */
    //	public boolean isPlay()
    //	{
    //		return isPlay;
    //	}

    /**
     * 设置是否需要播放音效
     * @param isPlay 是否需要播放
     */
    //	public void setPlay(boolean isPlay)
    //	{
    //		this.isPlay = isPlay;
    //	}

    /**
     * 音效播放状态改变接口
     */
    public interface OnSoundStateChangedListener {
        /**
         * 当音效播放状态改变时回调方法
         *
         * @param isPlaying 是否正在播放音效
         */
        void onSoundStateChanged(boolean isPlaying);
    }

    /**
     * 音效播放任务
     */
    private class SoundPlayTask implements Runnable {
        @Override
        public void run()
        {
            if (soundId_load == 0) {
                return;
            }

            while (isPlay) {
                try {
                    Thread.sleep(playInterval - 100);
                    if (soundPool != null) {
                        listener.onSoundStateChanged(true);
                        soundId_play = soundPool.play(soundId_load, 1, 1, 1, 0, 1.0f);
                        Thread.sleep(100);
                        listener.onSoundStateChanged(false);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

    }

}
