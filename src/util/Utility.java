package util;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
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
}
