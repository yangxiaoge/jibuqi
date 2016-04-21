package com.dikey0602;

import name.bagi.levente.pedometer.WalkingActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.RouteOverlay;
import com.baidu.mapapi.TransitOverlay;
import com.nuist.yjn.R;

public class RouteLineActivity extends MapActivity {

	private static final String TAG = "MainActivity";
	static double DEF_PI = 3.14159265359; // PI
	static double DEF_2PI = 6.28318530712; // 2*PI
	static double DEF_PI180 = 0.01745329252; // PI/180.0
	static double DEF_R = 6370693.5; // radius of earth

	double startLongitude, startLatitude, endLongitude, endLatitude, distance;

	Button mBtnDrive = null; // 驾车搜索
	Button mBtnWalk = null; // 步行搜索

	MapView mMapView = null; // 地图View（布局中的组件）
	MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	LocationListener mLocationListener = null; // onResume时注册此listener，onPause时需要Remove
	MyLocationOverlay mLocationOverlay = null; // 定位图层

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate()");
		setContentView(R.layout.map);

		BMapApiDemoApp app = (BMapApiDemoApp) this.getApplication();
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication()); //百度地图API管理类实例化对象
			app.mBMapMan.init(app.mStrKey,
					new BMapApiDemoApp.MyGeneralListener());  //验证百度地图授权key
		}
		app.mBMapMan.start();  //开启地图服务
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(app.mBMapMan);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);  //设置启用内置的缩放控件（“-”“+”）
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.setDrawOverlayWhenZooming(true);

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);

		// 注册定位事件
		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				Log.v(TAG, "onLocationChanged() : location = " + location);
				if (location != null) {
					GeoPoint pt = new GeoPoint(
							(int) (location.getLatitude() * 1e6), //纬度
							(int) (location.getLongitude() * 1e6)); //经度
					mMapView.getController().animateTo(pt);
				}
			}
		};

		// 初始化搜索模块，注册事件监听
		mSearch = new MKSearch();
		mSearch.init(app.mBMapMan, new MKSearchListener() {

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
												int error) {
				Log.v(TAG, "onGetDrivingRouteResult() : res = " + res
						+ " ,error = " + error);
				// 错误号可参考MKEvent中的定义
				if (error != 0 || res == null) {
					Toast.makeText(RouteLineActivity.this, "对不起，未找到结果！",
							Toast.LENGTH_SHORT).show();
					return;
				}
				RouteOverlay routeOverlay = new RouteOverlay(
						RouteLineActivity.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(routeOverlay);
				mMapView.invalidate();

				mMapView.getController().animateTo(res.getStart().pt);
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
												int error) {
				Log.v(TAG, "onGetTransitRouteResult() : res = " + res
						+ " ,error = " + error);
				if (error != 0 || res == null) {
					Toast.makeText(RouteLineActivity.this, "对不起，未找到结果！",
							Toast.LENGTH_SHORT).show();
					return;
				}
				TransitOverlay routeOverlay = new TransitOverlay(
						RouteLineActivity.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(routeOverlay);
				mMapView.invalidate();

				mMapView.getController().animateTo(res.getStart().pt);
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
												int error) {
				Log.v(TAG, "onGetWalkingRouteResult() : res = " + res
						+ " ,error = " + error);
				if (error != 0 || res == null) {
					Toast.makeText(RouteLineActivity.this, "对不起，未找到结果！",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.v(TAG, "numPlan = " + res.getNumPlan());
				startLongitude = res.getStart().pt.getLongitudeE6();
				startLatitude = res.getStart().pt.getLatitudeE6();
				endLongitude = res.getEnd().pt.getLongitudeE6();
				endLatitude = res.getEnd().pt.getLatitudeE6();

				Log.v(TAG, "start : Longitude = " + startLongitude
						+ ", Latitude = " + startLatitude);
				Log.v(TAG, "End : Longitude = " + endLongitude
						+ ", Latitude = " + endLatitude);

				distance = getShortDistance(startLongitude, startLatitude,
						endLongitude, endLatitude);

				String d = String.valueOf(distance);
				// distanceTV.setText("到目标地点的距离为：" + d.substring(0,
				// d.lastIndexOf(".") + 2) + "千米。");

				Log.v(TAG, "distance = " + distance);

				RouteOverlay routeOverlay = new RouteOverlay(
						RouteLineActivity.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(routeOverlay);
				mMapView.invalidate();

				mMapView.getController().animateTo(res.getStart().pt);
			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
				Log.v(TAG, "onGetAddrResult() : res = " + res + " ,error = "
						+ error);
			}

			public void onGetPoiResult(MKPoiResult res, int arg1, int arg2) {
				Log.v(TAG, "onGetPoiResult() : res = " + res + ", arg1" + arg1
						+ " ,arg2 = " + arg2);
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
				Log.v(TAG, "onGetBusDetailResult() : res = " + result
						+ " ,error = " + iError);
			}

			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.v(TAG, "onGetPoiDetailSearchResult() : arg0" + arg0
						+ " ,arg1 = " + arg1);
			}

			@Override
			public void onGetRGCShareUrlResult(String arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.v(TAG, "onGetRGCShareUrlResult() : arg0 = " + arg0
						+ ", arg1" + arg1);
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub
				Log.v(TAG, "onGetSuggestionResult() : arg0 = " + arg0
						+ ", arg1" + arg1);
			}
		});

		// 设定搜索按钮的响应
		mBtnDrive = (Button) findViewById(R.id.drive);
		// mBtnTransit = (Button)findViewById(R.id.transit);
		mBtnWalk = (Button) findViewById(R.id.walk);
		// distanceTV = (TextView) findViewById(R.id.distance);

		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				SearchButtonProcess(v);
			}
		};

		mBtnDrive.setOnClickListener(clickListener);
		// mBtnTransit.setOnClickListener(clickListener);
		mBtnWalk.setOnClickListener(clickListener);

		// mBtnDrive.setVisibility(View.GONE);
		// mBtnTransit.setVisibility(View.GONE);
		app.mBMapMan.getLocationManager().requestLocationUpdates(
				mLocationListener);
	}

	void SearchButtonProcess(View v) {
		// 处理搜索按钮响应
		EditText editSt = (EditText) findViewById(R.id.start);
		EditText editEn = (EditText) findViewById(R.id.end);

		// 对起点终点的name进行赋值，也可以直接对坐标赋值，赋值坐标则将根据坐标进行搜索
		MKPlanNode stNode = new MKPlanNode();
		stNode.name = editSt.getText().toString();
		MKPlanNode enNode = new MKPlanNode();
		enNode.name = editEn.getText().toString();

		// 实际使用中请对起点终点城市进行正确的设定
		if (mBtnDrive.equals(v)) {
			mSearch.drivingSearch("北京", stNode, "上海", enNode);  //开车搜索
		} else if (mBtnWalk.equals(v)) {
			mSearch.walkingSearch("北京", stNode, "北京", enNode);  //步行搜索
		}
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause()");
		BMapApiDemoApp app = (BMapApiDemoApp) this.getApplication();
		app.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass();
		app.mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume()");
		BMapApiDemoApp app = (BMapApiDemoApp) this.getApplication();
		mLocationOverlay.enableMyLocation(); // 启用定位
		mLocationOverlay.enableCompass(); // 打开指南针
		app.mBMapMan.start();
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		Log.v(TAG, "isRouteDisplayed()");
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onDestroy()");
		BMapApiDemoApp app = (BMapApiDemoApp) this.getApplication();
		app.mBMapMan.destroy();
		super.onDestroy();
	}

	public double getShortDistance(double lon1, double lat1, double lon2,
								   double lat2) {
		double ew1, ns1, ew2, ns2;
		double dx, dy, dew;
		double distance;
		// 角度转换为弧度
		ew1 = lon1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lon2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 经度差
		dew = ew1 - ew2;
		// 若跨东经和西经180 度，进行调整
		if (dew > DEF_PI)
			dew = DEF_2PI - dew;
		else if (dew < -DEF_PI)
			dew = DEF_2PI + dew;
		dx = DEF_R * Math.cos(ns1) * dew;
		// 东西方向长度(在纬度圈上的投影长度)
		dy = DEF_R * (ns1 - ns2);
		// 南北方向长度(在经度圈上的投影长度)
		// 勾股定理求斜边长
		distance = Math.sqrt(dx * dx + dy * dy);
		return distance;

	}

	/*
	 * 点击Button计步器跳转WalkingActivity
	 */
	public void jbq(View v) {
		startActivity(new Intent(this, WalkingActivity.class));
	}
}
