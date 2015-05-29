package activity;


import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.coolweather.app.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	//change city
	private Button switchcity;
	//update weather
	private Button refreshweather;
	private LinearLayout weatherinfolayout;
	//show city
	private TextView citynametext;
	//show publish time
	private TextView publishtext;
	//show the description of weather
	private TextView weatherdesptext;
	//show temp1
	private TextView temp1text;
	//show temp2
	private TextView temp2text;
	//show current time
	private TextView currentdatetext;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//initialize the components
		weatherinfolayout=(LinearLayout) findViewById(R.id.weather_info_layout);
		citynametext=(TextView) findViewById(R.id.city_name);
		publishtext=(TextView) findViewById(R.id.publish_text);
		weatherdesptext=(TextView) findViewById(R.id.weather_desp);
		temp1text=(TextView) findViewById(R.id.temp1);
		temp2text=(TextView) findViewById(R.id.temp2);
		currentdatetext=(TextView) findViewById(R.id.current_date);
		String countycode=getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countycode)){
			//show weather info. if the county exits
			publishtext.setText("同步中...");
			weatherinfolayout.setVisibility(View.INVISIBLE);
			citynametext.setVisibility(View.INVISIBLE);
			queryWeatherCode(countycode);
		}
		else{
			showWeather();
		}
		
		switchcity=(Button) findViewById(R.id.switch_city);
		refreshweather=(Button) findViewById(R.id.refresh_weather);
		switchcity.setOnClickListener(this);
		refreshweather.setOnClickListener(this);
	}
	public void onClick(View v){
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishtext.setText("同步中...");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weathercode=prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weathercode)){
				queryWeatherInfo(weathercode);
			}
			break;
		default:break;
		}
	}
	
	//query the weather code according to county code
	private void queryWeatherCode(String countycode){
		String address="http://www.weather.com.cn/data/list3/city"+countycode+".xml";
		queryFromServer(address,"countycode");
	}
	//query weather according to weather code
	private void queryWeatherInfo(String weathercode){
		String address="http://www.weather.com.cn/data/cityinfo/"+weathercode+".xml";
		queryFromServer(address,"weathercode");
	}
	//query the weather info from the server according to address and type
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			public void onFinish(final String response){
				if("countycode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//translate the weather code 
						String[] array=response.split("\\|");
						if(array!=null&&array.length==2){
							String weathercode=array[1];
							queryWeatherInfo(weathercode);
						}
					}
				}
				else if("weathercode".equals(type)){
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable(){
						public void run(){
							showWeather();
						}
					});
				}
			}
			public void onError(Exception e){
				runOnUiThread(new Runnable(){
					public void run(){
						publishtext.setText("同步失败");
					}
				});
			}
		});
	}
	//read weather info. from SharedPreferences file and show
	private void showWeather(){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		citynametext.setText(prefs.getString("city_name",""));
		temp1text.setText(prefs.getString("temp1",""));
		temp2text.setText(prefs.getString("temp2",""));
		weatherdesptext.setText(prefs.getString("weather_desp",""));
		publishtext.setText("今天"+prefs.getString("publish_time","")+"发布");
		currentdatetext.setText(prefs.getString("current_date",""));
		weatherinfolayout.setVisibility(View.VISIBLE);
		citynametext.setVisibility(View.VISIBLE);
	}
}
