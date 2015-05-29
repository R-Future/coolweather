package service;

import receiver.AutoUpdateReceiver;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service{
	public IBinder onBind(Intent intent){
		return null;
	}
	public int onStartCommand(Intent intent,int flags,int startid){
		new Thread(new Runnable(){
			public void run(){
				updateWeather();
			}
		}).start();
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
		int hour=8*60*60*1000;
		long triggerattime=SystemClock.elapsedRealtime()+hour;
		Intent i=new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerattime, pi);
		return super.onStartCommand(intent, flags, startid);
	}
	//update weather
	private void updateWeather(){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weathercode=prefs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo"+weathercode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			public void onFinish(String response){
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			public void onError(Exception e){
				e.printStackTrace();
			}
		});
	}
}
