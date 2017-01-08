package com.mai.allmute;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Button mute, stopMute;
    private MyBroadcastReceiver myBroadcastReceiver;
    private AudioManager audioManager;
    private CheckBox stream_alarm;
    private static boolean isChecked_stream_alarm, isDebug;
    private SharedPreferences.Editor editor;
    private static SharedPreferences sharedPreferences;
    private int currentAlarmVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isDebug = false;
        stopMute = (Button) findViewById(R.id.stop_mute);
        stream_alarm = (CheckBox) findViewById(R.id.stream_alarm);
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(myBroadcastReceiver, intentFilter);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (currentAlarmVolume != 0) {
            editor.putInt("currentAlarmVolume", currentAlarmVolume);
            editor.apply();
        }
        Toast.makeText(this, "系统声音、来电声音、媒体声音已设置为静音模式", Toast.LENGTH_SHORT).show();
        isChecked_stream_alarm = sharedPreferences.getBoolean("isChecked_stream_alarm", false);
        stream_alarm.setChecked(isChecked_stream_alarm);
        stream_alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("isChecked_stream_alarm", isChecked);
                editor.apply();
                isChecked_stream_alarm = isChecked;
                AllMute(MainActivity.this);
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "系统声音、来电声音、媒体声音、闹钟声音已设置为静音模式", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "已取消闹钟静音模式", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AllMute(this);
        stopMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(myBroadcastReceiver);
                audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                int AlarmVolume = sharedPreferences.getInt("currentAlarmVolume", 5);
                try {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, AlarmVolume, 0);
                    Toast.makeText(MainActivity.this, "全部声音已恢复为静音前状态", Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    e.printStackTrace();
                    String error = e.toString();
                    if ("java.lang.SecurityException: Not allowed to change Do Not Disturb state".equals(error)) {
                        Toast.makeText(MainActivity.this, "请退出勿扰模式后再恢复声音！", Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    if (isDebug) {
                        LogView(audioManager);
                    }
                    finish();
                }
            }
        });

    }

    private static void LogView(AudioManager audioManager) {
        Log.d("TAG", "系统声音: " + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        Log.d("TAG", "闹钟铃声: " + audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        Log.d("TAG", "媒体声音: " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        Log.d("TAG", "来电声音: " + audioManager.getStreamVolume(AudioManager.STREAM_RING));
        Log.d("TAG", "------------------------------------------");
    }

    public static void AllMute(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        if (am.getStreamVolume(AudioManager.STREAM_MUSIC) != 0
                || am.getStreamVolume(AudioManager.STREAM_RING) != 0
                || am.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            am.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);

        }
        if (isChecked_stream_alarm) {
            am.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        } else {
            int AlarmVolume = sharedPreferences.getInt("currentAlarmVolume", 5);
            am.setStreamVolume(AudioManager.STREAM_ALARM, AlarmVolume, 0);
        }
        if (isDebug) {
            LogView(am);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
