package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Utility {
	//解析和处理服务器返回的省级数据
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolweatherDB, String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
				for(String p:allProvinces){
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolweatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	//市级
	public static boolean handleCityResponse(CoolWeatherDB coolweatherDB, String response, int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0){
				for(String c:allCities){
					String[] array=c.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolweatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	//县级
	public static boolean handleCountyResponse(CoolWeatherDB coolweatherDB, String response, int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
			if(allCounties!=null&allCounties.length>0){
				for(String c:allCounties){
					String[] array=c.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					coolweatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	//translate JSON data and restore them in local file
	public static void handleWeatherResponse(Context context, String response){
		try{
			JSONObject jsonobject=new JSONObject(response);
			JSONObject weatherinfo=jsonobject.getJSONObject("weatherinfo");
			String cityname=weatherinfo.getString("city");
			String weathercode=weatherinfo.getString("cityid");
			String temp1=weatherinfo.getString("temp1");
			String temp2=weatherinfo.getString("temp2");
			String weatherdesp=weatherinfo.getString("weather");
			String publishtime=weatherinfo.getString("ptime");
			saveWeatherInfo(context,cityname,weathercode,temp1,temp2,weatherdesp,publishtime);
		}
		catch(JSONException e){
			e.printStackTrace();
		}
	}
	//restore the weather info. into the SharedPreferences file
	public static void saveWeatherInfo(Context context,String cityname,String weathercode,String temp1,String temp2,String weatherdesp,String publishtime){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityname);
		editor.putString("weather_code", weathercode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherdesp);
		editor.putString("publish_time", publishtime);
		editor.putString("current_time", sdf.format(new Date()));
		editor.commit();
	}
}
