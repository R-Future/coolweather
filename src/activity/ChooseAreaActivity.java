package activity;

import java.util.*;

import com.coolweather.app.R;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolweatherDB;
	private List<String> datalist=new ArrayList<String>();
	//province list
	private List<Province> provincelist;
	//city list
	private List<City> citylist;
	//county list
	private List<County> countylist;
	//selected province
	private Province selectedprovince;
	//selected city
	private City selectedcity;
	//selected county
	private County selectedcounty;
	//current selected level
	private int currentlevel;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)){
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView) findViewById(R.id.list_view);
		titleText=(TextView) findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datalist);
		listView.setAdapter(adapter);
		coolweatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View view,int index, long arg3){
				if(currentlevel==LEVEL_PROVINCE){
					selectedprovince=provincelist.get(index);
					queryCities();
				}
				else if(currentlevel==LEVEL_CITY){
					selectedcity=citylist.get(index);
					queryCounties();
				}
				else if(currentlevel==LEVEL_COUNTY){
					String countycode=countylist.get(index).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countycode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}
	//query all provinces, from database first,then from server.
	private void queryProvinces(){
		provincelist=coolweatherDB.loadProvinces();
		if(provincelist.size()>0){
			datalist.clear();
			for(Province province:provincelist){
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentlevel=LEVEL_PROVINCE;
		}
		else{
			queryFromServer(null,"province");
		}
	}
	//query all cities in selected province, from database first,then from server.
	private void queryCities(){
		citylist=coolweatherDB.loadCities(selectedprovince.getId());
		if(citylist.size()>0){
			datalist.clear();
			for(City city:citylist){
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedprovince.getProvinceName());
			currentlevel=LEVEL_CITY;
		}
		else{
			queryFromServer(selectedprovince.getProvinceCode(),"city");
		}
	}
	//query all counties in selected city, from database first, then from server.
	private void queryCounties(){
		countylist=coolweatherDB.loadCounties(selectedcity.getId());
		if(countylist.size()>0){
			datalist.clear();
			for(County county:countylist){
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedcity.getCityName());
			currentlevel=LEVEL_CITY;
		}
		else{
			queryFromServer(selectedcity.getCityCode(),"county");
		}
	}
	//query data from server
	private void queryFromServer(final String code, final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}
		else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			public void onFinish(String response){
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolweatherDB, response);
				}
				else if("city".equals(type)){
					result=Utility.handleCityResponse(coolweatherDB, response, selectedprovince.getId());
				}
				else if("county".equals(type)){
					result=Utility.handleCountyResponse(coolweatherDB, response, selectedcity.getId());
				}
				if(result){
					runOnUiThread(new Runnable(){
						public void run(){
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}
							else if("city".equals(type)){
								queryCities();
							}
							else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			public void onError(Exception e){
				runOnUiThread(new Runnable(){
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	//show progress dialog
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	//close progress dialog
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	//capture the event of "back" button to choose the province list or city list or quit
	public void onBackPressed(){
		if(currentlevel==LEVEL_COUNTY){
			queryCities();
		}
		else if(currentlevel==LEVEL_CITY){
			queryProvinces();
		}
		else{
			finish();
		}
	}
}
