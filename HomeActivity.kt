package com.lqkj.location.slug.library.view

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Build.*
import android.os.Build.VERSION.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.NonNull
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.TtsMode
import com.githang.statusbar.StatusBarCompat
import com.joanzapata.android.BaseAdapterHelper
import com.joanzapata.android.QuickAdapter
import com.lqkj.gis.creepers.library.R
import com.lqkj.gis.creepers.library.floor.OnBuildingFloorChange
import com.lqkj.gis.creepers.library.floor.OnBuildingStateChange
import com.lqkj.gis.creepers.library.map.VectorMapView
import com.lqkj.gis.creepers.library.route.activity.RouteActivity
import com.lqkj.gis.creepers.library.route.adapter.RoutePathAdapter
import com.lqkj.gis.creepers.library.route.dialog.RouteProgressDialog
import com.lqkj.gis.creepers.library.route.graph.NodeWeighting
import com.lqkj.gis.creepers.library.route.graph.RoutePath
import com.lqkj.gis.creepers.library.route.presenter.RoutePresenter
import com.lqkj.gis.creepers.library.route.vi.RouteInterface
import com.lqkj.gis.creepers.library.search.POIComponent
import com.lqkj.gis.creepers.library.search.POIPoint
import com.lqkj.gis.creepers.library.search.activity.SearchActivity
import com.lqkj.gis.creepers.library.utils.DensityUtils
import com.lqkj.gis.creepers.library.utils.JSON
import com.lqkj.location.slug.library.ApplicationData
import com.lqkj.location.slug.library.analog.RnalogCall
import com.lqkj.location.slug.library.analog.RnalogNavigation
import com.lqkj.location.slug.library.baiduVoice.BaiDuVoiceInit
import com.lqkj.location.slug.library.baiduVoice.MainHandlerConstant.*
import com.lqkj.location.slug.library.baiduVoice.control.InitConfig
import com.lqkj.location.slug.library.baiduVoice.control.MySyntherizer
import com.lqkj.location.slug.library.baiduVoice.control.NonBlockSyntherizer
import com.lqkj.location.slug.library.baiduVoice.listener.UiMessageListener
import com.lqkj.location.slug.library.baiduVoice.util.AutoCheck
import com.lqkj.location.slug.library.baiduVoice.util.OfflineResource
import com.lqkj.location.slug.library.bean.BeaconDevice
import com.lqkj.location.slug.library.bezier.bezier
import com.lqkj.location.slug.library.device.LocationDevice
import com.lqkj.location.slug.library.find.LocationFinder
import com.lqkj.location.slug.library.http.MapRequest
import com.lqkj.location.slug.library.location.InertialLocationEngine
import com.lqkj.location.slug.library.location.LocationCall
import com.lqkj.location.slug.library.scanner.Scanner
import com.lqkj.location.slug.library.track.listener.DirectionListener
import com.lqkj.location.slug.library.view.adapter.*
import com.lqkj.location.slug.library.view.bean.gridBean
import com.lqkj.location.slug.library.view.bean.hosptialFunctionBean
import com.lqkj.location.slug.library.view.direction.GetDirection
import com.lqkj.location.slug.library.view.fragement.Fragement1
import com.lqkj.location.slug.library.view.fragement.Fragement2
import com.lqkj.location.slug.library.view.fragement.Fragement3
import com.lqkj.location.slug.library.view.service.ReOpenService
import com.lqkj.location.slug.library.view.x5utils.X5WebView
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.android.telemetry.location.LocationEngineListener
import com.mapbox.services.commons.geojson.*
import com.mapbox.services.commons.models.Position
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import com.tencent.smtt.sdk.WebChromeClient
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import okhttp3.FormBody
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 路径规划界面
 */
open class HomeActivity : AppCompatActivity(), OnMapReadyCallback, MapboxMap.OnMapClickListener,
        RouteInterface, View.OnClickListener, RoutePathAdapter.OnItemClickListener, OnBuildingFloorChange,
        OnBuildingStateChange, Scanner.ScannerCallback, LocationCall.TrackCallBack,
        LocationEngineListener,LocationCall.BleLocationCallBack,LocationCall.InitBleCallBack,LocationCall.DistanceCallBack,
        LocationCall.nosingleCallback, LocationCall.ZhixinCallBack, LocationCall.outDoorCall, POIComponent.SearchCallback,
        SwipyRefreshLayout.OnRefreshListener, DirectionListener, MapboxMap.OnScrollListener, RnalogCall{

    @SuppressLint("MissingPermission")
    override fun rnaFinish() = layerPlugin!!.setLocationLayerEnabled(LocationLayerMode.NONE)

    override fun onDirection(value: Float) {
//        deviceDirection = value.toDouble()
    }

    override fun onAccelerometer(values: FloatArray?) {
        //重力加速度
    }

    var deviceDirection: Double? = 90.0
    private lateinit var poiComponent: POIComponent
    override fun onRefresh(direction: SwipyRefreshLayoutDirection) {
        when (direction) {
            SwipyRefreshLayoutDirection.TOP -> {
                poiComponent.initialization()
                poiComponent.searchAsync()
            }
            SwipyRefreshLayoutDirection.BOTTOM -> {
                if (poiComponent.hasNextPage) {
                    poiComponent.page++
                    poiComponent.searchAsync(true)
                }
            }
        }
    }
    override fun searchFinish(results: List<POIPoint>, isAdd: Boolean) {
        if (input_location.text.isEmpty()){
            search_all.visibility = View.VISIBLE
            srL.visibility = View.GONE
            return
        }

        search_all.visibility = View.GONE

        detail_list.visibility = View.GONE

        srL.visibility = View.VISIBLE

        swipyRefreshLayout_search.isRefreshing = false

        val adapter = result_list_search.adapter as QuickAdapter<POIPoint>

        if (isAdd) {
            adapter.addAll(results)
        } else {
            adapter.replaceAll(results)
        }
    }

    override fun searchNoResult() {
        swipyRefreshLayout_search.isRefreshing = false

        val adapter = result_list_search.adapter as QuickAdapter<POIPoint>

        Toast.makeText(context,"未查询到对应的地点",Toast.LENGTH_SHORT).show()

        adapter.clear()
    }

    override fun searchError(e: Exception) {
        swipyRefreshLayout_search.isRefreshing = false

        Toast.makeText(this, "搜索失败:$e", Toast.LENGTH_SHORT).show()
    }

    override fun noneInput() {
        srL.visibility = View.GONE
        swipyRefreshLayout_search.isRefreshing = false
    }

    override fun outDoor(outDoorLocation: LatLng?) {
        //室外回调
        if (ApplicationData.OutDoor) {
            if (zhiXinMarker != null) {
                mapbox?.removeMarker(zhiXinMarker!!)
            }
            if (resetMarker != null) {
                mapbox?.removeMarker(resetMarker!!)
            }
            if (locationMarker != null) {
                mapbox?.removeMarker(locationMarker!!)
            }

            if (baiduMarker != null) {
                mapbox?.removeMarker(baiduMarker!!)
            }

            if (mapbox != null) {
                if (vectorMapView.floorComponent!!.nowLevelIndex == nowLocationFloor) {
                    nowLocationFloor = 0
                    val markerOptions = MarkerOptions()
                    markerOptions.position = LatLng(outDoorLocation!!.latitude, outDoorLocation.longitude)
                    markerOptions.icon = IconFactory.getInstance(applicationContext)
                            .fromResource(R.drawable.baidu)
                    baiduMarker = mapbox?.addMarker(markerOptions)
                }
            }
/*
            locationEngine?.setOutDoorLocation(outDoorLocation)
            Log.d("百度定位：","经度="+outDoorLocation?.longitude+"，纬度="+outDoorLocation?.latitude)*/
        }
    }

    override fun stepXYZ(values: FloatArray?) {
//        stepXyz.text = "X="+ values!![0]+",Y="+values!![1]+",Z="+values!![2]
    }

    @SuppressLint("SetTextI18n")
    override fun stepDistanceback(step: Double, allDistance: Double) {
        /*var msg = Message.obtain()
        msg.what = 2
        val bundle = Bundle()
        bundle.putDouble("step", step)
        bundle.putDouble("allstep", allDistance)
        msg.data = bundle
        handler.sendMessage(msg)*/
    }

    private var zhiXinLatlng: LatLng?= null

    private var zhixinTime: Long?=0

    override fun initZhiXinBack(latLng: LatLng?) {
        if (zhiXinMarker != null) {
            mapbox?.removeMarker(zhiXinMarker!!)
        }
        if (latLng!=null) {
            if (ApplicationData.OutDoor){
                nowLocationFloor = 0
                if (this.OutisFirst!!){
                    nowLocationFloor = 0
                    vectorMapView.floorComponent?.setIndex(0)
                    vectorMapView.floorComponent?.nowLevelIndex = 0
                    OutisFirst = false
                    val position2 = CameraPosition.Builder()
                            .target(LatLng(latLng.latitude, latLng.longitude)) // Sets the new camera position位置
                            //                    .zoom(mapbox.getCameraPosition().zoom) // Sets the zoom大小
                            .bearing(0.0) // Rotate the camera方向
                            .tilt(0.0) // Set the camera tilt倾斜
                            .build() // Creates a CameraPosition from the builder
                    mapbox!!.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)
                    presenter.setStartLocation(Point.fromCoordinates(Position.fromCoordinates(latLng.longitude, latLng.latitude))
                            , "当前位置", this.nowLocationFloor!!)
                }
            }

            zhixinTime = System.currentTimeMillis()
            zhiXinLatlng = latLng
            if (mapbox != null) {
                if (vectorMapView.floorComponent!!.nowLevelIndex == nowLocationFloor) {
                    val markerOptions = MarkerOptions()
                    markerOptions.position = LatLng(latLng.latitude, latLng.longitude)
                    markerOptions.icon = IconFactory.getInstance(applicationContext)
                            .fromResource(R.drawable.mapbox_marker_icon_reset)
                    zhiXinMarker = mapbox?.addMarker(markerOptions)
                }
            }
        }
    }

    override fun nosingleCallback(hasSingle: Boolean) {
        if (single_rl!=null) {
            if (hasSingle) {
                handler.sendEmptyMessage(0)
            } else {
                handler.sendEmptyMessage(1)
            }
        }
    }

    var reNavigationFlag: Boolean?=false

    override fun resNavigation(location: LatLng?, floor: Double) {
        synthesizer!!.speak("路径重新规划")
        reNavigationFlag = true
        presenter.setStartLocation(Coordinate(location!!.longitude, location.latitude, floor))
    }

    override fun onScanOnce(device: LocationDevice?) {
    }

    override fun onScanList(devices: MutableList<LocationDevice>?) {
    }

    override fun onLocationChanged(location: Location?) {
    }

    override fun onConnected() {
    }

    var isFirst: Boolean?=true
    var OutisFirst: Boolean?=true

    var nowLocationFloor: Int?=0

    override fun initBleBack(initPoint: BeaconDevice?) {
        if (resetMarker != null) {
            mapbox?.removeMarker(resetMarker!!)
        }

        if (baiduMarker != null) {
            mapbox?.removeMarker(baiduMarker!!)
        }
        if (initPoint!=null) {
            if (mapbox != null) {
                if (vectorMapView.floorComponent!!.nowLevelIndex == nowLocationFloor) {
                    val markerOptions = MarkerOptions()
                    markerOptions.position = LatLng(initPoint.lat, initPoint.lon)
                    markerOptions.icon = IconFactory.getInstance(applicationContext)
                            .fromResource(R.drawable.ble)
//                mapbox_marker_icon_reset
                    resetMarker = mapbox?.addMarker(markerOptions)
                }
            }
            nowLocationFloor = initPoint.floor
//            Toast.makeText(context,"置信点楼层"+initPoint!!.floor,Toast.LENGTH_SHORT).show()
            presenter.setStartLocation(Point.fromCoordinates(Position.fromCoordinates(initPoint.lon, initPoint.lat))
                    , "当前位置", initPoint.floor)
            if (theChangeFloor == initPoint.floor) {
                if (needChangeFloor) {
                    vectorMapView.setBuildingLevel(initPoint.floor)
                    needChangeFloor = false
                    theChangeFloor = 999
                    navigationStep = true
                    var floor = initPoint.floor
                    if (floor < 0) {
                        floor -= 1
                    } else {
                        floor += 1
                    }
                    if (floor < 0) {
                        checkResult(synthesizer!!.speak("到达负" + floor + "楼"), "speak")
                    } else {
                        checkResult(synthesizer!!.speak("到达" + floor + "楼"), "speak")
                    }
                    location_now_floor.text = "(" + floor + "F)"
                    locationFinder!!.changeLineString(initPoint.floor)
                    changeFloorOk = true
                }
            }
        }else{
            if (zhiXinLatlng!=null){
                if (System.currentTimeMillis()- this.zhixinTime!! <3000){
                    presenter.setStartLocation(Point.fromCoordinates(Position.fromCoordinates(zhiXinLatlng!!.longitude, zhiXinLatlng!!.latitude))
                            , "当前位置", this.nowLocationFloor!!)
                }
            }
        }
    }

    var juli: String?="0米"
    var toEndDistance: Double? = 1000.0

    override fun distanceCallBack(distance: Double?, enddistance: Double?) {
        toEndDistance = enddistance
        if (distance != null) {
            if (distance<0) {
                textCountMeter.text = "0米"
                juli = "0米"
            }else {
                textCountMeter.text = String.format("%.1f", distance) + "米"
                juli = String.format("%.1f", distance) + "米"
            }
        }
        if (text_detail.text.contains("米")) {
            val s = text_detail.text.split("米")
            if (s[0].contains("直行")){
                text_detail.text = "直行"+enddistance?.toInt().toString()+ "米"
            }else if (s[1].contains("终点")) {
                text_detail.text = enddistance?.toInt().toString() + "米" + s[1]
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun timeCallBack(time: Double?) {
        Log.d("timecallback",time.toString())
        var shi = time!!.toInt()
        var ge = time-shi
        var gewei = (ge * 60).toInt()
        var time: String?="0秒"
        if (shi == 0){
            textCountTime.text = (ge*60).toInt().toString()+"秒"
            time = (ge*60).toInt().toString()+"秒"
        }else {
            if (gewei != 0) {
                textCountTime.text = shi.toString() + "分" + gewei.toString() + "秒"
                time = shi.toString() + "分" + gewei.toString() + "秒"
            }else{
                textCountTime.text = shi.toString() + "分钟"
                time = shi.toString() + "分钟"
            }
        }

        location_now_description.text = "距离目的地约$juli  耗时约 $time"
    }

    var navigationStep: Boolean? = true

    var changeFloorOk: Boolean?=false

    override fun bleLocationBack(point: LatLng?) {
        if (point != null) {
            ApplicationData.bleReLocation = LatLng(point.latitude,point.longitude,point.altitude)
        }
        if (!ApplicationData.stopBleLocation && !ApplicationData.OutDoor) {
            if (mapbox != null) {
                if (locationMarker != null) {
                    mapbox?.removeMarker(locationMarker!!)
                }

                if (baiduMarker != null) {
                    mapbox?.removeMarker(baiduMarker!!)
                }
                if (vectorMapView.floorComponent!!.nowLevelIndex == nowLocationFloor) {
                    val markerOptions = MarkerOptions()
                    markerOptions.position = point
                    markerOptions.icon = IconFactory.getInstance(applicationContext)
                            .fromResource(R.drawable.mapbox_marker_icon_default)
                    locationMarker = mapbox?.addMarker(markerOptions)
                }
//                Toast.makeText(context,"蓝牙回调楼层"+point!!.altitude.toInt(),Toast.LENGTH_SHORT).show()
                Log.d("tag222", "蓝牙回调" + point!!.altitude.toInt())
                count = if (point.altitude.toInt() != vectorMapView.floorComponent!!.getLevel()) {
                    count!! + 1
                } else {
                    0
                }
                if (count!! > 2 && this.isFirst!!) {
                    count = 0
                    if (this.isFirst!!) {
                        isFirst = false
                        val position2 = CameraPosition.Builder()
                                .target(LatLng(point.latitude, point.longitude)) // Sets the new camera position位置
                                //                    .zoom(mapbox.getCameraPosition().zoom) // Sets the zoom大小
                                .bearing(0.0) // Rotate the camera方向
                                .tilt(0.0) // Set the camera tilt倾斜
                                .build() // Creates a CameraPosition from the builder
                        mapbox!!.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)
                        nowLocationFloor = point.altitude.toInt()
                        presenter.setStartLocation(Point.fromCoordinates(Position.fromCoordinates(point.longitude, point.latitude))
                        , "当前位置", this.nowLocationFloor!!)
                    }
                    vectorMapView.setBuildingLevel(point.altitude.toInt())
                } else if (theChangeFloor == point.altitude.toInt()){
                    if (!ApplicationData.daohangFinish) {
                        if (needChangeFloor) {
                            vectorMapView.setBuildingLevel(point.altitude.toInt())
                            needChangeFloor = false
                            theChangeFloor = 999
                            navigationStep = true
                            locationFinder!!.changeLineString(point.altitude.toInt())
                            var floor = point.altitude.toInt()
                            if (floor <= 0) {
                                floor -= 1
                            } else {
                                floor += 1
                            }
                            if (floor<0) {
                                checkResult(synthesizer!!.speak("到达负"+floor+"楼"),"speak")
                            }else{
                                checkResult(synthesizer!!.speak("到达"+floor+"楼"),"speak")
                            }
                            location_now_floor.text = "(" + floor + "F)"
                            changeFloorOk = true
                        }
                    }
                } else {
                    /*if (count!! > 5) {
                        count = 0
                        vectorMapView.setBuildingLevel(point!!.altitude.toInt())
                        *//*if (theChangeFloor != 999) {
                            if (needChangeFloor && theChangeFloor == point!!.altitude.toInt()) {
                                needChangeFloor = false
                                locationFinder!!.changeLineString(point!!.altitude.toInt())
                                location_now_floor.text = "(" + point!!.altitude.toInt().toString() + "F)"
                            }
                        }*//*
                    }*/
                }
            }
        }
    }

    var floorBle: Int?=0
    var savefloorBle: Int?=0
    var shouldChangeFloor: Boolean = false

    override fun trackCallBack(nowLocation: com.vividsolutions.jts.geom.Point, minIndex: Int) {
        if (!ApplicationData.stop) {
            if (ApplicationData.OutDoor){
                nowLocationFloor = 0
                if (this.OutisFirst!!){
                    nowLocationFloor = 0
                    vectorMapView.floorComponent?.setIndex(0)
                    vectorMapView.floorComponent?.nowLevelIndex = 0
                    OutisFirst = false
                    val position2 = CameraPosition.Builder()
                            .target(LatLng(nowLocation.x, nowLocation.y)) // Sets the new camera position位置
                            //                    .zoom(mapbox.getCameraPosition().zoom) // Sets the zoom大小
                            .bearing(0.0) // Rotate the camera方向
                            .tilt(0.0) // Set the camera tilt倾斜
                            .build() // Creates a CameraPosition from the builder
                    mapbox!!.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)
                    presenter.setStartLocation(Point.fromCoordinates(Position.fromCoordinates(nowLocation.y, nowLocation.x))
                            , "当前位置", this.nowLocationFloor!!)
                }
            }
            if (ApplicationData.monidaohang) {
                var floorBle = nowLocation.coordinates[0].z.toInt()
                if (shouldChangeFloor) {
                    val distanceTo1 = LatLng(coordinates!![floorBle].x, coordinates!![floorBle].y).distanceTo(LatLng(nowLocation.x, nowLocation.y))
                    if (distanceTo1 < 0.3) {
                        shouldChangeFloor = false
                        ApplicationData.changeFloor = true
                        var floorNum: Int? = 0
                        if (savefloorBle!! < floors.size - 1) {
                            floorNum = floors[savefloorBle!! + 1] + 1
                        } else if (savefloorBle == floors.size - 1) {
                            floorNum = floors[savefloorBle!! - 1] + 1
                        }
                        if (presenter.priorityType == NodeWeighting.SearchType.ELEVATOR_FIRST) {
                            synthesizer!!.speak("进入电梯，请到" + floorNum + "楼")
                            text_detail.text = "进入电梯，请到" + floorNum + "楼"
                        } else {
                            synthesizer!!.speak("进入楼梯，请到" + floorNum + "楼")
                            text_detail.text = "进入楼梯，请到" + floorNum + "楼"
                        }
                        text_detail.text = "进入楼梯，请到" + floorNum + "楼"
                        image_type.setImageResource(R.drawable.dainti)
                        vectorMapView.setBuildingLevel(floors[savefloorBle!! + 1])
//                        Thread.sleep(1500)
                        ApplicationData.changeFloor = false
                    }
                }
                if (floorBle!=savefloorBle){
                    if (floorBle == endString.size) {
                        text_detail.text="到达终点附近"
                        image_type.visibility = View.GONE
                        onBackPressed()
                    } else {
                        if (!image_type.isShown){
                            image_type.visibility = View.VISIBLE
                        }
                        text_detail.text= endString[floorBle-1]
                    }
                    if (floorBle<endString.size) {
                        if (endString[floorBle - 1].contains("直行") || endString[floorBle - 1].contains("按道路行走") || endString[floorBle - 1].contains("米后到达")) {
                            image_type.setImageResource(R.drawable.zhixing)
                        } else if (endString[floorBle - 1].contains("左转")) {
                            image_type.setImageResource(R.drawable.zuozhuan)
                        } else if (endString[floorBle - 1].contains("右转")) {
                            image_type.setImageResource(R.drawable.youzhuan)
                        }
                    }
                    savefloorBle = floorBle
                    if (floorBle<floors.size-1) {
                        if (vectorMapView.floorComponent!!.nowLevelIndex != floors[floorBle+1]) {
                            var floorNum = floors[floorBle+1]+1
                            Log.d("进入电梯", floorNum.toString())
                            /*if (presenter.priorityType == NodeWeighting.SearchType.ELEVATOR_FIRST) {
                                synthesizer!!.speak("进入电梯，请到" + floorNum + "楼")
                            } else {
                                synthesizer!!.speak("进入楼梯，请到" + floorNum + "楼")
                            }*/
                            shouldChangeFloor = true
                            /*ApplicationData.changeFloor = true
                            vectorMapView.setBuildingLevel(floorNum-1)
                            Thread.sleep(1500)
                            ApplicationData.changeFloor = false*/
                        }
                    }else if (floorBle==floors.size-1){
                        if (vectorMapView.floorComponent!!.nowLevelIndex != floors[floorBle - 1]) {
                            shouldChangeFloor = true
//                            Log.d("进入电梯", "后"+floorNum.toString())
                        }
                    }
                    /*if (shouldChangeFloor && LatLng(coordinates!![floorBle].x, coordinates!![floorBle].y).distanceTo(LatLng(nowLocation.x,nowLocation.y))<1){
                        shouldChangeFloor = false
                        ApplicationData.changeFloor = true
                        var floorNum: Int?=0
                        if (floorBle<floors.size-1) {
                            floorNum = floors[floorBle+1]+1
                        }else if (floorBle==floors.size-1){
                            floorNum = floors[floorBle-1]+1
                        }
                        if (presenter.priorityType == NodeWeighting.SearchType.ELEVATOR_FIRST) {
                            synthesizer!!.speak("进入电梯，请到" + floorNum + "楼")
                            text_detail.text="进入电梯，请到" + floorNum + "楼"
                        } else {
                            synthesizer!!.speak("进入楼梯，请到" + floorNum + "楼")
                            text_detail.text="进入楼梯，请到" + floorNum + "楼"
                        }
                        text_detail.text="进入楼梯，请到" + floorNum + "楼"
                        image_type.setImageResource(R.drawable.dainti)
                        vectorMapView.setBuildingLevel(floors[floorBle+1])
//                        Thread.sleep(1500)
                        ApplicationData.changeFloor = false
                    }*/
                }else{
                    val distanceTo = LatLng(nowLocation.x, nowLocation.y).distanceTo(LatLng(coordinates!![savefloorBle!!].x, coordinates!![savefloorBle!!].y))
                    if (distanceTo<5 && distanceTo>1){
                        if (savefloorBle!! < endString.size) {
                            if (savefloorBle == endString.size - 1) {
                                text_detail.text= "前方到达终点附近"
                            } else if (savefloorBle!! < endString.size){
                                text_detail.text= "前方" + endString[savefloorBle!!]
                                if (endString[savefloorBle!!].contains("直行")||endString[savefloorBle!!].contains("按道路行走")||endString[savefloorBle!!].contains("米后到达")){
                                    image_type.setImageResource(R.drawable.zhixing)
                                }else if (endString[savefloorBle!!].contains("左转")){
                                    image_type.setImageResource(R.drawable.zuozhuan)
                                }else if (endString[savefloorBle!!].contains("右转")){
                                    image_type.setImageResource(R.drawable.youzhuan)
                                }
                            }
                        }
//                        text_detail.text= "前方"+endString[savefloorBle!!]
                    }
                }
                locationEngine?.setLocation(nowLocation)
            }else if(!ApplicationData.daohangFinish){
                if (vectorMapView.floorComponent!!.nowLevelIndex == floors[minIndex] && navigationStep!!) {
                    locationEngine?.setLocation(nowLocation)
                }else if (this.changeFloorOk!!){
                    locationEngine?.setLocation(nowLocation)
                }
                voiceDaohang(nowLocation,minIndex)
            }else{
                locationEngine?.setLocation(nowLocation)
            }
        }
    }
    var savefloorBleMoni: Int?=0
    var shouldChangeFloorMoni: Boolean = false
    override fun moniCall(nowLocation: LatLng, minIndex: Double, nowfloor: Double) {

        Log.d("距离","距离终点距离："+minIndex+"当前floor="+nowfloor)
        var floorBle = nowLocation.altitude.toInt()
        if (shouldChangeFloorMoni) {
            if (minIndex < 1.4) {
                shouldChangeFloorMoni = false
                ApplicationData.changeFloor = true
                var floorNum: Int? = 0
                if (savefloorBleMoni!! < floors.size - 1) {
                    floorNum = floors[savefloorBleMoni!! + 1] + 1
                } else if (savefloorBleMoni == floors.size - 1) {
                    floorNum = floors[savefloorBleMoni!! - 1] + 1
                }
                if (presenter.priorityType == NodeWeighting.SearchType.ELEVATOR_FIRST) {
                    synthesizer!!.speak("进入电梯，请到" + floorNum + "楼")
                    text_detail.text = "进入电梯，请到" + floorNum + "楼"
                } else {
                    synthesizer!!.speak("进入楼梯，请到" + floorNum + "楼")
                    text_detail.text = "进入楼梯，请到" + floorNum + "楼"
                }
                text_detail.text = "进入楼梯，请到" + floorNum + "楼"
                image_type.setImageResource(R.drawable.dainti)
            }
        }
        if (floors[floorBle]==nowfloor.toInt() && vectorMapView.floorComponent!!.nowLevelIndex!=nowfloor.toInt() && ApplicationData.changeFloor){
            vectorMapView.setBuildingLevel(floors[savefloorBleMoni!! + 1])
            ApplicationData.changeFloor = false
        }
        if (floorBle!=savefloorBleMoni){
            if (floorBle == endString.size-1) {
                text_detail.text="到达终点附近"
                image_type.visibility = View.GONE
                onBackPressed()
                savefloorBleMoni=0
                shouldChangeFloorMoni = false
            } else {
                if (!image_type.isShown){
                    image_type.visibility = View.VISIBLE
                }
                text_detail.text= endString[floorBle-1]
            }
            if (floorBle<endString.size) {
                if (endString[floorBle - 1].contains("直行") || endString[floorBle - 1].contains("按道路行走") || endString[floorBle - 1].contains("米后到达")) {
                    image_type.setImageResource(R.drawable.zhixing)
                } else if (endString[floorBle - 1].contains("左转")) {
                    image_type.setImageResource(R.drawable.zuozhuan)
                } else if (endString[floorBle - 1].contains("右转")) {
                    image_type.setImageResource(R.drawable.youzhuan)
                }
            }
            savefloorBleMoni = floorBle
            if (floorBle<floors.size-1) {
                if (vectorMapView.floorComponent!!.nowLevelIndex != floors[floorBle+1]) {
                    var floorNum = floors[floorBle+1]+1
                    Log.d("进入电梯", floorNum.toString())
                    shouldChangeFloorMoni = true
                }
            }else if (floorBle==floors.size-1){
                if (vectorMapView.floorComponent!!.nowLevelIndex != floors[floorBle - 1]) {
                    shouldChangeFloorMoni = true
                }
            }
        }else{
            val distanceTo = nowLocation.distanceTo(LatLng(coordinates!![savefloorBleMoni!!].x, coordinates!![savefloorBleMoni!!].y))
            if (distanceTo<5 && distanceTo>1){
                if (savefloorBleMoni!! < endString.size) {
                    if (savefloorBleMoni == endString.size - 1) {
                        text_detail.text= "前方到达终点附近"
                    } else if (savefloorBleMoni!! < endString.size){
                        text_detail.text= "前方" + endString[savefloorBleMoni!!]
                        if (endString[savefloorBleMoni!!].contains("直行")||endString[savefloorBleMoni!!].contains("按道路行走")||endString[savefloorBleMoni!!].contains("米后到达")){
                            image_type.setImageResource(R.drawable.zhixing)
                        }else if (endString[savefloorBleMoni!!].contains("左转")){
                            image_type.setImageResource(R.drawable.zuozhuan)
                        }else if (endString[savefloorBleMoni!!].contains("右转")){
                            image_type.setImageResource(R.drawable.youzhuan)
                        }
                    }
                }
//                        text_detail.text= "前方"+endString[savefloorBle!!]
            }
        }
        locationEngine?.setLocation(GeometryFactory().createPoint(
                Coordinate(nowLocation.latitude, nowLocation.longitude, nowLocation.altitude)))
    }

    override fun moniFinish() {
        text_detail.text="到达终点附近"
        image_type.visibility = View.GONE
        onBackPressed()
        savefloorBleMoni=0
        shouldChangeFloorMoni = false
    }
    override fun moniCall2(nowLocation: LatLng?, minIndex: Double, nowFloor: Double, floorBle: Int) {
        var nowLocationFloor = nowLocation!!.altitude.toInt()
        if (nowLocationFloor!=nowFloor.toInt() && minIndex<1.5 && !shouldChangeFloor) {
            var sayFloor = nowFloor.toInt()
            if (sayFloor>=0) sayFloor += 1
            if (presenter.priorityType == NodeWeighting.SearchType.ELEVATOR_FIRST) {
                synthesizer!!.speak("进入电梯，请到" + sayFloor + "楼")
                text_detail.text = "进入电梯，请到" + sayFloor + "楼"
                image_type.setImageResource(R.drawable.dainti)
            } else {
                synthesizer!!.speak("进入楼梯，请到" + sayFloor + "楼")
                text_detail.text = "进入楼梯，请到" + sayFloor + "楼"
                image_type.setImageResource(R.drawable.dainti)
            }
            shouldChangeFloor = true
        }
        if (shouldChangeFloor){
            if (nowLocationFloor == nowFloor.toInt()) {
                var showFloor = nowLocationFloor
                if (showFloor>=0) showFloor += 1
                location_now_floor.text = "(${showFloor}F)"
                vectorMapView.floorComponent!!.setIndex(nowLocationFloor)
                vectorMapView.floorComponent!!.floorView.notifyDataSetChanged()
                shouldChangeFloor = false
            }
        }
        if (floorBle!=savefloorBleMoni && floorBle>0){
            if (floorBle == endString.size) {
                text_detail.text="到达终点附近"
                image_type.visibility = View.GONE
                onBackPressed()
                savefloorBleMoni=0
                shouldChangeFloorMoni = false
            } else {
                if (!image_type.isShown){
                    image_type.visibility = View.VISIBLE
                }
                text_detail.text= endString[floorBle-1]
            }
            if (floorBle<endString.size) {
                if (endString[floorBle - 1].contains("直行") || endString[floorBle - 1].contains("按道路行走") || endString[floorBle - 1].contains("米后到达")) {
                    image_type.setImageResource(R.drawable.zhixing)
                } else if (endString[floorBle - 1].contains("左转")) {
                    image_type.setImageResource(R.drawable.zuozhuan)
                } else if (endString[floorBle - 1].contains("右转")) {
                    image_type.setImageResource(R.drawable.youzhuan)
                }
            }
            savefloorBleMoni = floorBle
            if (floorBle<floors.size-1) {
                if (vectorMapView.floorComponent!!.nowLevelIndex != floors[floorBle+1]) {
                    var floorNum = floors[floorBle+1]+1
                    Log.d("进入电梯", floorNum.toString())
                    shouldChangeFloorMoni = true
                }
            }else if (floorBle==floors.size-1){
                if (vectorMapView.floorComponent!!.nowLevelIndex != floors[floorBle - 1]) {
                    shouldChangeFloorMoni = true
                }
            }
        }else{
            val distanceTo = nowLocation.distanceTo(LatLng(coordinates!![savefloorBleMoni!!].x, coordinates!![savefloorBleMoni!!].y))
            if (distanceTo<5 && distanceTo>1){
                if (savefloorBleMoni!! < endString.size) {
                    if (savefloorBleMoni == endString.size - 1) {
                        text_detail.text= "前方到达终点附近"
                    } else if (savefloorBleMoni!! < endString.size){
                        text_detail.text= "前方" + endString[savefloorBleMoni!!]
                        if (endString[savefloorBleMoni!!].contains("直行")||endString[savefloorBleMoni!!].contains("按道路行走")||endString[savefloorBleMoni!!].contains("米后到达")){
                            image_type.setImageResource(R.drawable.zhixing)
                        }else if (endString[savefloorBleMoni!!].contains("左转")){
                            image_type.setImageResource(R.drawable.zuozhuan)
                        }else if (endString[savefloorBleMoni!!].contains("右转")){
                            image_type.setImageResource(R.drawable.youzhuan)
                        }
                    }
                }
//                        text_detail.text= "前方"+endString[savefloorBle!!]
            }
        }
        locationEngine?.setLocation(GeometryFactory().createPoint(
                Coordinate(nowLocation.latitude, nowLocation.longitude, nowLocation.altitude)))
    }

    companion object {
        const val MAP_ID = "mapId"

        const val ROUTE_SOURCE = "routeSource"
        const val ROUTE_LINE_LAYER = "routeFillLayer"

        const val OVER_SOURCE = "overSource"
        const val OVER_LAYER = "overLayer"

        const val DASH_SOURCE = "dashSource"
        const val DASH_LAYER = "dashLayer"
    }

    private var count: Int?=0

    private var locationEngine: InertialLocationEngine?=null

    private var layerPlugin: LocationLayerPlugin?= null

    private var mapbox: MapboxMap?=null

    private var resetMarker: Marker? = null
    private var locationMarker: Marker? = null
    private var zhiXinMarker: Marker? = null
    private var baiduMarker: Marker? = null
    private var nowMarker: Marker? = null
    private var gridepage: Int? = 0

    private var presenter = RoutePresenter(this)
    private var rnalogNavigation: RnalogNavigation?=null
    var df = DecimalFormat("######0.00")

    val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what==0){
                single_rl.visibility = View.VISIBLE
            }else if (msg.what==1){
                single_rl.visibility = View.GONE
            }else if (msg.what == 2){
                var bundle = msg.data
                stepdistance.text = "步长="+df.format(bundle.getDouble("step"))+"，总长="+ df.format(bundle.getDouble("allstep"))
            }else if (msg.what == 3){
                var bundle = msg.data
                if (page_search.visibility == View.VISIBLE){
                    diturl.visibility = View.VISIBLE
                    imm?.hideSoftInputFromWindow(input_location.windowToken, 0) //强制隐藏键盘
                    timerCount = 30000
                    returnSearchCount = 60000
                    lock_ditu.visibility = View.GONE
                    page_search.visibility = View.GONE
                    lock_ditu.visibility = View.GONE
                    val floor = bundle.getString("floor").toInt()
                    vectorMapView.floorComponent?.setIndex(floor)
                    vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
                    val lat = bundle.getDouble("lat")
                    val lon = bundle.getDouble("lon")
                    val name = bundle.getString("name")
                    querySelectGeometry(LatLng(lat, lon),name,floor)
                }
            }else if (msg.what == 4){
                page_search.visibility = View.VISIBLE
                var requestBody = FormBody.Builder()
                        .add("mapId", "3")
                        .build()
                MapRequest.getInstance().post2("https://xz.parkbobo.com/hospService/v1/getAllService/",requestBody, object : MapRequest.succCallback {

                    override fun onError(e: Exception, errorCode: Int) {
                        lock_ditu.visibility = View.GONE
                        loading_data.visibility = View.VISIBLE
                        loada_text.text = "数据加载失败，点击重新加载"
                        loading_data.isEnabled = true
                        vp_tab.visibility = View.GONE
                    }

                    override fun onSuccess(result: String, isCache: Boolean) {
                        vp_tab.visibility = View.VISIBLE
                        lock_ditu.visibility = View.VISIBLE
                        loading_data.visibility = View.GONE
                        loading_data.isEnabled = false
                        var resultObject = JSONObject(result)
                        if (resultObject.getString("status").equals("true")){
                            var data = resultObject.getJSONArray("data")
                            if (data.length()>0){
                                for (n in 0 until data.length()){
                                    var dataobject = data.getJSONObject(n)
                                    var hospArray = dataobject.getJSONArray("hospServiceFunction")
                                    var hospFunctions = mutableListOf<hosptialFunctionBean>()
                                    if (hospArray.length()>0){
                                        for (m in 0 until hospArray.length()){
                                            var hoapitem = hospArray.getJSONObject(m)
                                            var areaName = hoapitem.getString("areaName")
                                            var floorId = hoapitem.getString("floorId")
                                            var iconImg = hoapitem.getString("iconImg")
                                            var latitude = hoapitem.getDouble("latitude")
                                            var longitude = hoapitem.getDouble("longitude")
                                            var hospbean = hosptialFunctionBean(areaName,floorId,iconImg,latitude,longitude)
                                            hospFunctions.add(hospbean)
                                        }
                                    }
                                    var iconImg = dataobject.getString("iconImg")
                                    var mapId = dataobject.getString("mapId")
                                    var serviceName = dataobject.getString("serviceName")
                                    var serviceType = dataobject.getInt("serviceType")
                                    var serviceTypeId = dataobject.getString("serviceTypeId")
                                    var zoneId = dataobject.getString("zoneId")
                                    var gridbean = gridBean(hospFunctions,iconImg,mapId,serviceName,
                                            serviceType,zoneId,serviceTypeId)
                                    if (serviceType == 1){
                                        gridepage = gridepage?.plus(1)
                                        mGridBeans1.add(gridbean)
                                    }else{
                                        mGridBeans2.add(gridbean)
                                    }
                                }
                            }
                            initGridViewData()
                        }
                    }
                })
            }else if (msg.what==5){
                if (!ApplicationData.monidaohang && !overShow!!) {
                    timerCount = timerCount!! - 1000
                    returnSearchCount = returnSearchCount!! - 1000
                    if (timerCount!! <= 0) {
                        timerCount = 30000
//                        90.0?.let { mapbox?.setBearing(it) }
                        if (vectorMapView.floorComponent?.nowLevelIndex != 0) {
                            vectorMapView.floorComponent?.setIndex(0)
                            vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
                        }
                        val position2 = CameraPosition.Builder()
                                .target(ApplicationData.screenLocation) // Sets the new camera position位置
                                .zoom(19.5) // Sets the zoom大小
                                .bearing(ApplicationData.screenBearing) // Rotate the camera方向
                                .tilt(0.0) // Set the camera tilt倾斜
                                .build() // Creates a CameraPosition from the builder
                        mapbox?.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)
                    }
                    if (returnSearchCount!! <=0){
                        timerCount = 60000
                        if (diturl.visibility == View.VISIBLE ) {
                            autoBackPress()
                            com.lqkj.location.slug.library.view.show.showDialog.hideMessage()
                            diturl.visibility = View.GONE
                            page_search.visibility = View.VISIBLE
                            lock_ditu.visibility = View.VISIBLE
                        }
                    }
                }
            }else if (msg.what == 6){
                timerCount = 30000
                returnSearchCount = 60000
                var bundle = msg.data
                val lat = bundle.getDouble("lat")
                val lon = bundle.getDouble("lon")
                val name = bundle.getString("name")
                val floor = bundle.getString("floor").toInt()
                vectorMapView.floorComponent?.setIndex(floor)
                vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
                querySelectGeometry(LatLng(lat,lon),name,floor)
            }
        }
    }

    var overShow: Boolean?=false
    private val vectorMapView by lazy {
        findViewById<VectorMapView>(R.id.vectorMap)
    }
    private val coordinatorLayout by lazy {
        findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
    }
    private val linearBottomSheet by lazy {
        findViewById<LinearLayout>(R.id.linear_bottom_sheet)
    }
    private val rl_rl_location by lazy {
        findViewById<RelativeLayout>(R.id.rl_rl_location)
    }
    private val textStartLocation by lazy {
        findViewById<TextView>(R.id.text_start_location)
    }
    private val textEndLocation by lazy {
        findViewById<TextView>(R.id.text_end_location)
    }
    private val location_now_floor by lazy {
        findViewById<TextView>(R.id.location_now_floor)
    }
    private val daohang_destation by lazy {
        findViewById<TextView>(R.id.daohang_destation)
    }
    private val location_now_description by lazy {
        findViewById<TextView>(R.id.location_now_description)
    }
    private val close_location by lazy {
        findViewById<ImageView>(R.id.close_location)
    }
    private val imageFootType by lazy {
        findViewById<ImageView>(R.id.btn_foot_type)
    }
    private val imageCarType by lazy {
        findViewById<ImageView>(R.id.btn_car_type)
    }
    private val imageElevatorType by lazy {
        findViewById<ImageView>(R.id.btn_elevator_type)
    }
    private val imageStairsType by lazy {
        findViewById<ImageView>(R.id.btn_stairs_type)
    }
    private val linearChooseLocation by lazy {
        findViewById<CardView>(R.id.linear_choose_location)
    }
    private val daohangview by lazy {
        findViewById<LinearLayout>(R.id.daohangview)
    }
    private val pathListView by lazy {
        findViewById<RecyclerView>(R.id.path_view)
    }
    private val relativeBottomToolBar by lazy {
        findViewById<RelativeLayout>(R.id.relative_bottom_tool_bar)
    }
    private val textCountMeter by lazy {
        findViewById<TextView>(R.id.count_meter)
    }
    private val textCountTime by lazy {
        findViewById<TextView>(R.id.count_time)
    }
    private val text_detail by lazy {
        findViewById<TextView>(R.id.text_detail)
    }
    private val image_type by lazy {
        findViewById<ImageView>(R.id.image_type)
    }
    private val textFeatureName by lazy {
        findViewById<TextView>(R.id.feature_name)
    }
    private val imageRouteToFeature by lazy {
        findViewById<ImageView>(R.id.route_to_feature)
    }
    private val relativeFeatureInfo by lazy {
        findViewById<RelativeLayout>(R.id.relative_feature_info)
    }
    private val frameBottomFeatureInfo by lazy {
        findViewById<FrameLayout>(R.id.frame_bottom_feature_info)
    }
    private val cardSearch by lazy {
        findViewById<CardView>(R.id.card_search)
    }
    private val startNavigation by lazy {
        findViewById<Button>(R.id.btn_start_navigation)
    }
    private val startMoNiNavigation by lazy {
        findViewById<RelativeLayout>(R.id.btn_moni_navigation)
    }
    private val toolbar_back by lazy {
        findViewById<Button>(R.id.toolbar_back)
    }
    private val name_navigation by lazy {
        findViewById<TextView>(R.id.name_navigation)
    }
    private val image_close by lazy {
        findViewById<ImageView>(R.id.image_close)
    }
    private val image_swap by lazy {
        findViewById<ImageView>(R.id.image_swap)
    }
    private val route_path_menu by lazy {
        findViewById<ImageView>(R.id.route_path_menu)
    }
    private val singlemessage by lazy {
        findViewById<TextView>(R.id.singlemessage)
    }
    private val bottomsheet_text by lazy {
        findViewById<TextView>(R.id.bottomsheet_text)
    }
    private val guanbimessage by lazy {
        findViewById<ImageView>(R.id.guanbimessage)
    }
    private val single_rl by lazy {
        findViewById<RelativeLayout>(R.id.single_rl)
    }
    private val toolbar_voice_input by lazy {
        findViewById<Button>(R.id.toolbar_voice_input)
    }
    private val stepdistance by lazy {
        findViewById<TextView>(R.id.stepdistance)
    }
    private val toolbar_search_input by lazy {
        findViewById<TextView>(R.id.toolbar_search_input)
    }
    private val loada_text by lazy {
        findViewById<TextView>(R.id.loada_text)
    }
    private val stepXyz by lazy {
        findViewById<TextView>(R.id.stepXyz)
    }
    private val viewPager by lazy {
        findViewById<ViewPager>(R.id.viewPager)
    }
    private val group by lazy {
        findViewById<LinearLayout>(R.id.points)
    }
    private val listView by lazy {
        findViewById<ListView>(R.id.listView)
    }
    private val detail_listview by lazy {
        findViewById<ListView>(R.id.detail_listview)
    }
    private val result_list_search by lazy {
        findViewById<ListView>(R.id.result_list_search)
    }
    private val search_all by lazy {
        findViewById<LinearLayout>(R.id.search_all)
    }
    private val page_search by lazy {
        findViewById<LinearLayout>(R.id.page_search)
    }
    private val detail_list by lazy {
        findViewById<LinearLayout>(R.id.detail_list)
    }
    private val srL by lazy {
        findViewById<LinearLayout>(R.id.srL)
    }
    private val diturl by lazy {
        findViewById<RelativeLayout>(R.id.diturl)
    }
    private val loading_data by lazy {
        findViewById<RelativeLayout>(R.id.loading_data)
    }
    private val lock_ditu by lazy {
        findViewById<RelativeLayout>(R.id.lock_ditu)
    }
    private val textview_all by lazy {
        findViewById<TextView>(R.id.textview_all)
    }
    private val input_location by lazy {
        findViewById<EditText>(R.id.input_location)
    }
    private val swipyRefreshLayout_search by lazy {
        findViewById<SwipyRefreshLayout>(R.id.swipyRefreshLayout_search)
    }
    private val search_home_back by lazy {
        findViewById<Button>(R.id.search_home_back)
    }
    private val vp_hospital by lazy {
        findViewById<ViewPager>(R.id.vp_hospital)
    }
    private val tab by lazy {
        findViewById<TabLayout>(R.id.tab)
    }
    private val vp_tab by lazy {
        findViewById<LinearLayout>(R.id.vp_tab)
    }
    //起点和结束点Marker
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var startLatlng: LatLng?=null
    private var endLatlng: LatLng?=null
    private var baiDuVoiceInit: BaiDuVoiceInit?=null
    //底部上拉菜单
    private var bottomSheet: BottomSheetBehavior<LinearLayout>? = null
    //进度弹窗
    private var progressDialog: RouteProgressDialog? = null
    private var locationFinder: LocationFinder?=null
    private var context: Context?=null
    val mGridBeans1 = mutableListOf<gridBean>()
    val mGridBeans2 = mutableListOf<gridBean>()
    var viewPagerList = mutableListOf<View>()
    var timerTask: Timer? = null

    private lateinit var receiver: HomeKeyEventBroadCastReceiver

    private var imm: InputMethodManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(-0x80000000, -0x80000000)
        setContentView(R.layout.layout_homeactivity)
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#1BCCC6"), true)

        context = this

        receiver = HomeKeyEventBroadCastReceiver()// 注册监听HOME键的广播

        receiver.setContext(this)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        registerReceiver(receiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

        MapRequest.setContext(context)

        initMapConfig()

        initView(savedInstanceState)

        initialTts()

        initPOIComponent()

        initListAdapter()

        initTimer()
        hideNavKey(window)
        window.decorView.setOnSystemUiVisibilityChangeListener { hideNavKey(window) }

        initFragementData()
//        initData()
    }

    private fun initFragementData() {

        var lists = mutableListOf<Fragment>()
        lists.add(Fragement1())
        lists.add(Fragement2())
        lists.add(Fragement3())
        var names = mutableListOf<String>()
        var images = mutableListOf<Int>()
        var images2 = mutableListOf<Int>()
        names.add("医院简介")
        images.add(R.drawable.hospital_instruction1)
        images2.add(R.drawable.hospital_instruction2)
        names.add("科室介绍")
        images.add(R.drawable.section_introduction2)
        images2.add(R.drawable.section_introduction1)
        names.add("专家团队")
        images.add(R.drawable.expert_team1)
        images2.add(R.drawable.expert_team2)
        var framentadapter = FragementAdapter(supportFragmentManager,names,context,lists,images)
        vp_hospital.adapter = framentadapter
        //绑定
//        tab.setupWithViewPager(vp_hospital)
        (0 until framentadapter.count).forEach { m ->
            /*var tabitem = tab.getTabAt(m)
            tabitem?.customView = framentadapter.getTabView(m)
            tab.addTab(tabitem!!)*/
            /*val tabitem = tab.newTab()
            tabitem.customView = framentadapter.getTabView(m)*/
            if (m==1){
                val tabitem = tab.newTab()
                val view = LayoutInflater.from(context).inflate(R.layout.tab_main, null)
                val title = view.findViewById<TextView>(R.id.tab_main_textview)
                val image = view.findViewById<ImageView>(R.id.tab_main_image)
                title.text = names[m]
                title.setTextColor(Color.parseColor("#E4007F"))
                image.setImageResource(images2[m])
                tabitem.customView = view
                tab.addTab(tabitem,true)
            }else{
                val tabitem = tab.newTab()
                tabitem.customView = framentadapter.getTabView(m)
                tab.addTab(tabitem,false)
            }
        }
        vp_hospital.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))
//        tab.setTabsFromPagerAdapter(framentadapter)
//        tab.addTab()
        vp_hospital.currentItem = 1
        tab.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab1: TabLayout.Tab) {
                vp_hospital.currentItem = tab1.position
                when {
                    tab1.position == 2 -> {
                        val fragment = lists[tab1.position] as Fragement3
                        fragment.initData()
                    }
                    tab1.position==1 -> {
                        val fragment = lists[tab1.position] as Fragement2
                        fragment.initdata()
                    }
                    tab1.position==0 -> {
                        val fragment = lists[tab1.position] as Fragement1
                        fragment.initdata()
                    }
                }
                val viewById = tab1.customView?.findViewById<View>(R.id.tab_main_textview) as TextView
                viewById.setTextColor(Color.parseColor("#E4007F"))
                val imageView = tab1.customView?.findViewById<View>(R.id.tab_main_image) as ImageView
                imageView.setImageResource(images2[tab1.position])
            }

            override fun onTabUnselected(tab1: TabLayout.Tab) {
                val viewById = tab1.customView?.findViewById<View>(R.id.tab_main_textview) as TextView
                viewById.setTextColor(Color.parseColor("#B0BBFD"))
                val imageView = tab1.customView?.findViewById<View>(R.id.tab_main_image) as ImageView
                imageView.setImageResource(images[tab1.position])
            }

            override fun onTabReselected(tab1: TabLayout.Tab) {
                if (tab1.position == 2) {
                    val fragment = lists[tab1.position] as Fragement3
                    fragment.initData()
                }else if (tab1.position==1){
                    val fragment = lists[tab1.position] as Fragement2
                    fragment.initdata()
                }else if (tab1.position==0){
                    val fragment = lists[tab1.position] as Fragement1
                    fragment.initdata()
                }
                val viewById = tab1.customView?.findViewById<View>(R.id.tab_main_textview) as TextView
                viewById.setTextColor(Color.parseColor("#E4007F"))
                val imageView = tab1.customView?.findViewById<View>(R.id.tab_main_image) as ImageView
                imageView.setImageResource(images2[tab1.position])
            }
        })
    }

    var timerCount: Int?=30000
    var returnSearchCount: Int?=60000
    private fun initTimer() {
        if (timerTask==null){
            timerTask = Timer()
            timerTask!!.schedule(object : TimerTask() {
                override fun run() {
                    handler.sendEmptyMessage(5)
                }
            }, 0, 1000)// 定时任务
        }
    }

    private fun initPOIComponent() {
        poiComponent = POIComponent(this, input_location,
                ApplicationData.mapid.toLong(),
                SearchActivity.MAP_TYPE_2D)
        poiComponent.callback = this
    }


    private fun initListAdapter() {
        val adapter = object : QuickAdapter<POIPoint>(this,
                R.layout.search_search_item) {
            override fun convert(helper: BaseAdapterHelper, item: POIPoint) {
                val name = item.tags?.get("name")
                var address = item.tags?.get("level")?.toInt()
                if (address!=null) {
                    if (address!! >= 0) {
                        address++
                    }
                }

                if (name != null) {
                    helper.setText(R.id.text_name_name, name)
                }
                if (address != null) {
                    helper.setText(R.id.text_address_floor, "在"+address+"楼")
                }
                //TODO 计算距离
//                helper.setText(R.id.text_distance_floor, "距离"+item.distance+"米")
            }
        }

        result_list_search.adapter = adapter

        result_list_search.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            diturl.visibility = View.VISIBLE
            imm?.hideSoftInputFromWindow(input_location.windowToken, 0) //强制隐藏键盘
            timerCount = 30000
            returnSearchCount = 60000
            search_all.visibility = View.VISIBLE
            page_search.visibility = View.GONE
            lock_ditu.visibility = View.GONE
            srL.visibility = View.GONE
            input_location.setText("")
            setSearchData(adapter.getItem(i))
        }
    }


    var totalpage: Int?=0

    private lateinit var ivPoints: Array<ImageView?>

    private fun initGridViewData() {
        totalpage =Math.ceil(mGridBeans1.size *1.0/8).toInt()
        viewPagerList = ArrayList<View>()
        for (k in 0 until totalpage!!){
            val grideview = View.inflate(context, R.layout.item_gride, null) as GridView
            grideview.adapter = GrigViewAdapger(context,mGridBeans1,k,8)
            grideview.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val obj = grideview.getItemAtPosition(i)
                if (obj !=null && obj is gridBean) {
//                    Toast.makeText(context, obj.serivceName, Toast.LENGTH_SHORT).show()
                    if (search_all.visibility == View.VISIBLE){
                        search_all.visibility = View.GONE
                        detail_list.visibility = View.VISIBLE
                    }
                    var adapter = DetailListAdapter(context,obj.hosptialFunctionBeans)
                    detail_listview.adapter = adapter
                    detail_listview.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        lock_ditu.visibility = View.GONE
                        page_search.visibility = View.GONE
                        search_all.visibility = View.VISIBLE
                        detail_list.visibility = View.GONE
                        diturl.visibility = View.VISIBLE
                        imm?.hideSoftInputFromWindow(input_location.windowToken, 0) //强制隐藏键盘
//                        querySelectGeometry(LatLng(obj.hosptialFunctionBeans[i].lat,obj.hosptialFunctionBeans[i].lon))
                        var msg = Message.obtain()
                        val bundle = Bundle()
                        bundle.putDouble("lat", obj.hosptialFunctionBeans[i].lat)
                        bundle.putDouble("lon", obj.hosptialFunctionBeans[i].lon)
                        bundle.putString("floor", obj.hosptialFunctionBeans[i].floor)
                        bundle.putString("name",obj.hosptialFunctionBeans[i].areaName)
                        msg.what = 6
                        msg.data = bundle
                        handler.sendMessage(msg)
                    }
                }
            }
            viewPagerList.add(grideview)
        }
        viewPager.adapter = pagerAdapter(viewPagerList)

        //添加小圆点
        ivPoints = arrayOfNulls<ImageView>(totalpage!!)
        for (n in 0 until totalpage!!){
            ivPoints[n] = ImageView(context)
            var params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            //为小圆点左右添加间距
            params.leftMargin = 10
            params.rightMargin = 10
            //手动给小圆点一个大小
            params.height = 20
            params.width = 20

            if (n == 0){
                ivPoints[n]?.setImageResource(R.drawable.blue_scircle)
            }else{
                ivPoints[n]?.setImageResource(R.drawable.gray_circle)
            }
//            ivPoints[n]?.setPadding(8,8,8,8)
            group.addView(ivPoints[n],params)
        }
        viewPager.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                // TODO Auto-generated method stub
                //currentPage = position;
                for (i in 0 until totalpage!!) {
                    if (i == position) {
                        ivPoints[i]?.setImageResource(R.drawable.blue_scircle)
                    }else{
                        ivPoints[i]?.setImageResource(R.drawable.gray_circle)
                    }
                }
            }
        })

        initListViewData()
    }

    private fun initListViewData() {
//        listView.layoutManager = LinearLayoutManager(this,LinearLayout.VERTICAL,true)
//        listView.layoutManager = GridLayoutManager(this, 3)
        var adapter = ListViewAdapter(context,mGridBeans2)
        /*var mData = getSampleData()
        val sectionAdapter = recycleView(R.layout.list_view_item, R.layout.def_section_head, mData).apply {
            setOnItemClickListener { adapter, view, position ->
                val mySection = mData.get(position)
                if (mySection.isHeader)
                    Toast.makeText(context, mySection.header, Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(context, mySection.t.areaName, Toast.LENGTH_LONG).show()
            }
            setOnItemChildClickListener { adapter, view, position ->
                Toast.makeText(context, "onItemChildClick$position", Toast.LENGTH_LONG).show() }
        }
        */
        listView.adapter = adapter
    }

    /**
     * 设置地图id
     */
    private fun initMapConfig() {
        this.presenter.mapId = intent.getStringExtra("mapid").toLong()
        name_navigation.text = intent.getStringExtra("name")
    }

    private val mBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet1: View,
                                    @BottomSheetBehavior.State newState: Int) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED){
                bottomsheet_text.text = "地图显示"
                route_path_menu.setImageResource(R.drawable.map)
            }else if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                bottomsheet_text.text = "路径详情"
                route_path_menu.setImageResource(R.drawable.creeper_sdk_route_path_menu)
            }
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
    }
    private fun initView(savedInstanceState: Bundle?) {
        bottomSheet = BottomSheetBehavior.from(linearBottomSheet)

        (bottomSheet as BottomSheetBehavior<LinearLayout>?)?.setBottomSheetCallback(mBottomSheetCallback)

        coordinatorLayout.visibility = View.GONE

        imageFootType.setOnClickListener(typeImageClickListener)
        imageCarType.setOnClickListener(typeImageClickListener)
        imageElevatorType.setOnClickListener(typeImageClickListener)
        imageStairsType.setOnClickListener(typeImageClickListener)
        startNavigation.setOnClickListener(typeImageClickListener)
        startMoNiNavigation.setOnClickListener(typeImageClickListener)
        image_close.setOnClickListener(typeImageClickListener)
        image_swap.setOnClickListener(typeImageClickListener)
        singlemessage.setOnClickListener(typeImageClickListener)
        guanbimessage.setOnClickListener(typeImageClickListener)
        single_rl.setOnClickListener(typeImageClickListener)
        close_location.setOnClickListener(typeImageClickListener)
        toolbar_back.setOnClickListener(typeImageClickListener)
        toolbar_voice_input.setOnClickListener(typeImageClickListener)
        toolbar_search_input.setOnClickListener(typeImageClickListener)
        lock_ditu.setOnClickListener(typeImageClickListener)
        search_home_back.setOnClickListener(typeImageClickListener)

        pathListView.layoutManager = LinearLayoutManager(applicationContext,
                LinearLayoutManager.VERTICAL, false)

        imageRouteToFeature.setOnClickListener(this)

        cardSearch.setOnClickListener(this)

        vectorMapView.onCreate(savedInstanceState)
        vectorMapView.getMapAsync(this)
        vectorMapView.loadMap(presenter.mapId)
        loading_data.visibility = View.VISIBLE
        com.lqkj.location.slug.library.view.show.showDialog.showMessage(context,"数据加载中...")
    }


    @RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    private fun startLocation(mapboxMap: MapboxMap) {
        locationEngine = InertialLocationEngine(mapboxMap)
        locationEngine!!.interval = 2000
        locationEngine!!.fastestInterval = 2000
        locationEngine!!.smallestDisplacement = 5f
        locationEngine!!.addLocationEngineListener(this)
        locationEngine!!.activate()

        mapboxMap.myLocationViewSettings.isEnabled = false
        mapbox = mapboxMap

        mapboxMap.uiSettings.isTiltGesturesEnabled = false
        mapboxMap.uiSettings.isCompassEnabled = false

        layerPlugin = LocationLayerPlugin(vectorMapView, mapboxMap, locationEngine)
        layerPlugin?.isLinearAnimation = true
//        layerPlugin?.setLocationLayerEnabled(LocationLayerMode.COMPASS)
        layerPlugin?.onStart()

        if (vectorMapView.floorComponent!!.nowLevelIndex == 0) {
            val markerOptions = MarkerOptions()
            markerOptions.position = ApplicationData.screenLocation
            markerOptions.icon = IconFactory.getInstance(applicationContext)
                    .fromResource(R.drawable.creeper_sdk_route_start_marker)
            nowMarker = mapbox?.addMarker(markerOptions)
        }

        GetDirection(this,this)

//        initScanner()
    }

    @RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
    private fun initScanner() {
        locationFinder = LocationFinder(this, MAP_ID, this, this,
                this,this,this,this, this)
        locationFinder?.start()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_HOME == keyCode) {
            Toast.makeText(context, "HOME 键已被禁用...", Toast.LENGTH_SHORT).show()
            return true//同理
        }
        return super.onKeyDown(keyCode, event)
    }

    @RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
    override fun onMapReady(mapboxMap: MapboxMap) {
        vectorMapView.floorComponent?.onFloorChanges?.add(this)
        vectorMapView.floorComponent?.onStateChanges?.add(this)


        val wm = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels         // 屏幕宽度（像素）
        val height = dm.heightPixels       // 屏幕高度（像素）
        val density = dm.density         // 屏幕密度（0.75 / 1.0 / 1.5）
        val densityDpi = dm.densityDpi     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        /*val screenWidth = (width / density).toInt()  // 屏幕宽度(dp)
        val screenHeight = (height / density).toInt()// 屏幕高度(dp)*/

        val compassView = vectorMapView.findViewById<ImageView>(com.mapbox.mapboxsdk.R
                .id.compassView)
//        compassView.setPadding(0,400,screenWidth-compassView.width/2,0)
        compassView.x = -20.0f
        compassView.y = height/5.toFloat()
/*
        mapboxMap.setZoom(20.0)
        mapboxMap.setBearing(0.0)
        mapboxMap.setTilt(0.0)
        vectorMapView.setBuildingLevel(0)
        vectorMapView.floorComponent?.nowLevelIndex = 0
        mapboxMap.animateCamera(CameraUpdateFactory.zoomBy(20.0))*/
        com.lqkj.location.slug.library.view.show.showDialog.hideMessage()
        startLocation(mapboxMap)
        mapboxMap.addOnMapClickListener(this)
        mapboxMap.addOnScrollListener(this)

        mapboxMap.setBearing(ApplicationData.screenBearing)
        val position2 = CameraPosition.Builder()
                .target(ApplicationData.screenLocation) // Sets the new camera position位置
                .zoom(19.5) // Sets the zoom大小
                .bearing(ApplicationData.screenBearing) // Rotate the camera方向
                .tilt(0.0) // Set the camera tilt倾斜
                .build() // Creates a CameraPosition from the builder
        mapbox?.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)

        diturl.visibility = View.GONE
        loading_data.visibility = View.GONE
        handler.sendEmptyMessage(4)
    }
    override fun onScroll() {
        timerCount = 30000
        returnSearchCount = 60000
    }
    override fun onMapClick(point: LatLng) {
        timerCount = 30000
        returnSearchCount = 60000
        FingerQuerySelectGeometry(point)
    }

    private fun querySelectGeometry(point: LatLng,name: String,floor: Int) {
        val zoom = 19.5
        val position2 = CameraPosition.Builder()
                .target(point) // Sets the new camera position位置
                .zoom(zoom) // Sets the zoom大小
                .bearing(ApplicationData.screenBearing) // Rotate the camera方向
                .tilt(0.0) // Set the camera tilt倾斜
                .build() // Creates a CameraPosition from the builder
        mapbox!!.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 500, false)
        if (linearChooseLocation.visibility == View.GONE) {
            textFeatureName.text = name
            frameBottomFeatureInfo.visibility = View.VISIBLE
            pullUpControlGroup()
            presenter.setEndLocation(point,name,floor)
        }
    }
    private fun FingerQuerySelectGeometry(point: LatLng) {
        val mapboxMap = vectorMapView.mapboxMap

        mapboxMap?.animateCamera(CameraUpdateFactory.newLatLng(point),
                object : MapboxMap.CancelableCallback {
                    override fun onFinish() {
                        val projection = mapboxMap.projection

                        val features = mapboxMap.queryRenderedFeatures(projection
                                ?.toScreenLocation(point) ?: PointF())

                        if (features.isEmpty()) {
                            frameBottomFeatureInfo.visibility = View.GONE
                            return
                        }

                        val feature = features[features.size - 1]

                        if (!feature.hasProperty("name")) {
                            frameBottomFeatureInfo.visibility = View.GONE
                            return
                        }
                        if (linearChooseLocation.visibility == View.GONE) {
                            textFeatureName.text = feature.getStringProperty("name")
                            frameBottomFeatureInfo.visibility = View.VISIBLE
                            pullUpControlGroup()
                        }

                        presenter.setLocation(feature.geometry,
                                feature.getStringProperty("name"),
                                vectorMapView.floorComponent?.getLevel() ?: 0)
                    }

                    override fun onCancel() {}
                })
    }

    override fun setStartLocationName(name: String) {
        textStartLocation.text = name
    }

    override fun setEndLocationName(name: String) {
        textEndLocation.text = name
    }

    override fun showMessage(message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    override fun setStartMarker(centerLatLng: Coordinate) {
        if (startMarker != null) {
            vectorMapView.floorMarkerComponent?.removeMarker(startMarker)
        }
        checkResult(synthesizer!!.speak("。"),"speaking")
        val markerOptions = MarkerOptions()
        markerOptions.icon = IconFactory.getInstance(applicationContext)
                .fromResource(R.drawable.creeper_sdk_route_start_marker)
        markerOptions.position = LatLng(centerLatLng.y, centerLatLng.x)

        startLatlng = LatLng(centerLatLng.y, centerLatLng.x)

//        locationFinder?.setNowLocation(startLatlng)
        ApplicationData.daohangFinish = false
        locationEngine?.setLocation(GeometryFactory().createPoint(
                Coordinate(startLatlng!!.latitude, startLatlng!!.longitude,
                        vectorMapView.floorComponent!!.getLevel().toDouble())))

        if (!this.reNavigationFlag!!) {
            ApplicationData.stopBleLocation = true
            ApplicationData.daohangFinish = true
            ApplicationData.stop = true
        }
        startMarker = vectorMapView.floorMarkerComponent?.addMarker(markerOptions)
        endString.clear()
        crossTpyes.clear()
        floors.clear()
        positionsm.clear()
        daohangcurrent = 0
    }

    //重新设置图标图层顺序
    private fun reorderMarkerLayer() {
        val mapboxMap = vectorMapView.mapboxMap ?: return

        val markerLayer = mapboxMap.getLayer("com.mapbox.annotations.points")
        mapboxMap.removeLayer(markerLayer ?: return)
        mapboxMap.addLayer(markerLayer)
    }

    override fun setEndMarker(centerLatLng: Coordinate) {
        if (endMarker != null) {
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)
        }
        val markerOptions = MarkerOptions()
        markerOptions.icon = IconFactory.getInstance(applicationContext)
                .fromResource(R.drawable.creeper_sdk_route_end_marker)
        markerOptions.position = LatLng(centerLatLng.y, centerLatLng.x)
        endLatlng = LatLng(centerLatLng.y, centerLatLng.x)

        endMarker = vectorMapView.floorMarkerComponent?.addMarker(markerOptions)

    }

    private var lineString: com.vividsolutions.jts.geom.LineString? = null

    private var coordinates: LinkedList<Coordinate>?=LinkedList<Coordinate>()

    val positionsm = mutableListOf<Position>()
    val floors = mutableListOf<Int>()

    override fun showRouteLine(isIndoor: Boolean?) {
        var indoor = isIndoor

        var first: Boolean ? = true
        if (indoor == null) {
            indoor = isIndoor()
        }

        val mapboxMap = vectorMapView.mapboxMap ?: return

        val features = mutableListOf<Feature>()

        if(presenter.showPaths.size>0){
            (0 until presenter.showPaths.size).forEach { m ->
                var pathRange = presenter.showPaths[m]
                if (pathRange.endIndex + 1 <= presenter.paths.size) {
                    val subPaths = presenter.paths.subList(pathRange.startIndex, pathRange.endIndex + 1)

                    val positions = mutableListOf<Position>()

                    for ((index, it) in subPaths.withIndex()) {
                        if (indoor) {
                            var turntype = it.turnType
                            if (turntype == RoutePath.NAVIGATION_START || turntype == RoutePath.NAVIGATION_END){

                            }else if (turntype == RoutePath.NAVIGATION_LEFT || turntype == RoutePath.NAVIGATION_RIGHT) {
                                var pos1 = mutableListOf<Position>()
                                var pos2 = mutableListOf<Position>()
                                if (index+1 == subPaths.size){
                                    /*pos1 = presenter.pathToPosition(it).toMutableList()
                                    pos2 = presenter.pathToPosition(subPaths[index]).toMutableList()*/
                                    if (index>0){
                                        if (subPaths[index - 1].turnType == RoutePath.NAVIGATION_LEFT || subPaths[index - 1].turnType == RoutePath.NAVIGATION_RIGHT){
                                            positions.add(presenter.pathToPosition(it)[presenter.pathToPosition(it).size-1])
                                        }else{
                                            positions.addAll(presenter.pathToPosition(it))
                                        }
                                    }else {
                                        positions.addAll(presenter.pathToPosition(it))
                                    }
                                }else{
                                    pos1 = presenter.pathToPosition(it).toMutableList()
                                    pos2 = presenter.pathToPosition(subPaths[index + 1]).toMutableList()
                                    if (first!!){
                                        positions.add(pos1[0])
                                        first = false
                                    }
                                    var bezier = bezier(pos1[0],pos1[pos1.size - 1],pos2[pos2.size - 1],mapboxMap)
                                    positions.addAll(bezier.positions)
                                }
                            }else if (turntype == RoutePath.NAVIGATION_LAST){
                                if (index>0){
                                    if (subPaths[index - 1].turnType == RoutePath.NAVIGATION_LEFT || subPaths[index - 1].turnType == RoutePath.NAVIGATION_RIGHT){
                                        positions.add(presenter.pathToPosition(it)[presenter.pathToPosition(it).size-1])
                                    }else{
                                        positions.addAll(presenter.pathToPosition(it))
                                    }
                                }else {
                                    positions.addAll(presenter.pathToPosition(it))
                                }
//                                positions.addAll(presenter.pathToPosition(it))
                            }else{
                                if (index>0){
                                    if (subPaths[index - 1].turnType == RoutePath.NAVIGATION_LEFT || subPaths[index - 1].turnType == RoutePath.NAVIGATION_RIGHT){

                                    }else{
                                        positions.addAll(presenter.pathToPosition(it))
                                    }
                                }else {
                                    positions.addAll(presenter.pathToPosition(it))
                                }
                            }
                        } else if (!indoor && it.indoor == false) {
                            var turntype = it.turnType
                            if (turntype == RoutePath.NAVIGATION_START || turntype == RoutePath.NAVIGATION_END){

                            }else if (turntype == RoutePath.NAVIGATION_LEFT || turntype == RoutePath.NAVIGATION_RIGHT) {
                                var pos1 = mutableListOf<Position>()
                                var pos2 = mutableListOf<Position>()
                                if (index+1 == subPaths.size){
                                    /*pos1 = presenter.pathToPosition(it).toMutableList()
                                    pos2 = presenter.pathToPosition(subPaths[index]).toMutableList()*/
                                    positions.addAll(presenter.pathToPosition(it))
                                }else{
                                    pos1 = presenter.pathToPosition(it).toMutableList()
                                    pos2 = presenter.pathToPosition(subPaths[index + 1]).toMutableList()
                                    if (first!!){
                                        positions.add(pos1[0])
                                        first = false
                                    }
                                    var bezier = bezier(pos1[0],pos1[pos1.size - 1],pos2[pos2.size - 1],mapboxMap)
                                    positions.addAll(bezier.positions)
                                }
                            }else if (turntype == RoutePath.NAVIGATION_LAST){
                                if (index>0){
                                    if (subPaths[index - 1].turnType == RoutePath.NAVIGATION_LEFT || subPaths[index - 1].turnType == RoutePath.NAVIGATION_RIGHT){
                                        positions.add(presenter.pathToPosition(it)[presenter.pathToPosition(it).size-1])
                                    }else{
                                        positions.addAll(presenter.pathToPosition(it))
                                    }
                                }else {
                                    positions.addAll(presenter.pathToPosition(it))
                                }
//                                positions.addAll(presenter.pathToPosition(it))
                            }else{
                                if (index>0){
                                    if (subPaths[index - 1].turnType == RoutePath.NAVIGATION_LEFT || subPaths[index - 1].turnType == RoutePath.NAVIGATION_RIGHT){

                                    }else{
                                        positions.addAll(presenter.pathToPosition(it))
                                    }
                                }else {
                                    positions.addAll(presenter.pathToPosition(it))
                                }
                            }
                        }
                    }
                    if (locationFinder!=null){
                        locationFinder!!.positions = positions
                    }
                    features.add(Feature.fromGeometry(LineString.fromCoordinates(positions)))
                }
            }
        }

        var source = mapboxMap.getSourceAs<GeoJsonSource>(ROUTE_SOURCE)

        if (source == null) {
            source = GeoJsonSource(ROUTE_SOURCE, FeatureCollection.fromFeatures(features))
            mapboxMap.addSource(source)
        } else {
            source.setGeoJson(FeatureCollection.fromFeatures(features))
        }
        setLayer(mapboxMap)
        reorderMarkerLayer()
    }

    private fun setLayer(mapboxMap: MapboxMap){

        var layer = mapboxMap.getLayerAs<LineLayer>(ROUTE_LINE_LAYER)

        val layerSize = mapboxMap.layers.size
        if (layer == null) {
            layer = LineLayer(ROUTE_LINE_LAYER, ROUTE_SOURCE)
            //TODO 写死背景图片
            layer.withProperties(PropertyFactory.lineWidth(8.0f))
                    .withProperties(PropertyFactory.linePattern("location3"))
                    .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_ROUND))
                    .withProperties(PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND))
            mapboxMap.addLayerAt(layer,layerSize-1)
        }
    }

    private var endString: MutableList<String> = ArrayList()
    private var crossTpyes: MutableList<Int> = ArrayList()
    private var dialog1: Dialog? = null

    private fun setAnalogDate(path: RoutePresenter.OnLineRoutePath){
        val distance = path.distance.toInt()
        var crossType = path.crossType
        when(path.turnType){
            RoutePath.NAVIGATION_START -> {
                /*crossTpyes.add(crossType)
                endString.add("请直行")
                positionsm.addAll(presenter.pathToPositionMo(path))
                floors.add(path.startFloor)*/
            }
            RoutePath.NAVIGATION_FORWARD -> {
                var list = presenter.pathToPositionMo(path)
                for (n in 0 until list.size){
                    if (n==0) {
                        if (distance<=2){
                            endString.add("")
                            crossTpyes.add(crossType)
                        }else if (distance<5){
                            endString.add("请按道路行走")
                            crossTpyes.add(crossType)
                        }else {
                            endString.add("直行" + distance.toString() + "米")
                            crossTpyes.add(crossType)
                        }
                        positionsm.add(list[n])
                    }else{
                        if (path.crossType==RoutePath.CROSS_ELEVATOR_IN || path.crossType==RoutePath.CROSS_STAIRS_IN){
                            endString.add("")
                        }else {
                            endString.add("直行")
                        }
                        crossTpyes.add(crossType)
                        positionsm.add(list[n])
                    }
                    floors.add(path.startFloor)
                }
            }
            RoutePath.NAVIGATION_LEFT -> {
                var list = presenter.pathToPositionMo(path)
                for (n in 0 until list.size){
                    if (n==0) {
                        if (distance<=2){
                            endString.add("")
                            crossTpyes.add(crossType)
                        }else if (distance<5){
                            endString.add("请按道路行走")
                            crossTpyes.add(crossType)
                        }else {
                            endString.add("直行"+distance.toString() + "米")
                            crossTpyes.add(crossType)
                        }
                        positionsm.add(list[n])
                    }else{
                        if (path.crossType==RoutePath.CROSS_ELEVATOR_IN || path.crossType==RoutePath.CROSS_STAIRS_IN){
                            endString.add("")
                        }else {
                            endString.add("左转")
                        }
                        crossTpyes.add(crossType)
                        positionsm.add(list[n])
                    }
                    floors.add(path.startFloor)
                }
            }
            RoutePath.NAVIGATION_RIGHT -> {
                var list = presenter.pathToPositionMo(path)
                for (n in 0 until list.size){
                    if (n==0) {
                        if (distance<=2){
                            endString.add("")
                            crossTpyes.add(crossType)
                        }else if (distance<5){
                            endString.add("请按道路行走")
                            crossTpyes.add(crossType)
                        }else {
                            endString.add("直行"+distance.toString() + "米")
                            crossTpyes.add(crossType)
                        }
                        positionsm.add(list[n])
                    }else{
                        if (path.crossType==RoutePath.CROSS_ELEVATOR_IN || path.crossType==RoutePath.CROSS_STAIRS_IN){
                            endString.add("")
                        }else {
                            endString.add("右转")
                        }
                        crossTpyes.add(crossType)
                        positionsm.add(list[n])
                    }
                    floors.add(path.startFloor)
                }
            }
            RoutePath.NAVIGATION_END -> {
                /*var list = presenter.pathToPositionMo(path)
                endString.add("到达终点")
                crossTpyes.add(crossType)
                positionsm.addAll(list)
                for (n in 0 until list.size){
                    floors.add(path.startFloor)
                }*/
            }
            RoutePath.NAVIGATION_LAST-> {
                /*var list = presenter.pathToPositionMo(path)
                for (n in 0 until list.size){
                    if (n==0) {
                        crossTpyes.add(crossType)
                        endString.add(distance.toString() + "米后" + "到达终点附近")
                        positionsm.add(list[n])
                    }else{
                        endString.add("到达终点附近,导航结束")
                        crossTpyes.add(crossType)
                        positionsm.add(list[n])
                    }
                    floors.add(path.startFloor)
                }*/
            }
        }
    }

    private fun clearRouteLine() {
        val mapboxMap = vectorMapView.mapboxMap ?: return

        clearRouteData(mapboxMap)
        mapboxMap.removeLayer(DASH_LAYER)
    }

    private fun clearRouteData(mapboxMap: MapboxMap){

        val features = mutableListOf<Feature>()
        var source = mapboxMap.getSourceAs<GeoJsonSource>(RouteActivity.ROUTE_SOURCE)

        if (source == null) {
            source = GeoJsonSource(RouteActivity.ROUTE_SOURCE, FeatureCollection.fromFeatures(features))
            mapboxMap.addSource(source)
        } else {
            source.setGeoJson(FeatureCollection.fromFeatures(features))
        }
        setLayer(mapboxMap)
    }
    override fun runChooseLocationAnim(isClose: Boolean) {
        if (presenter.hasRouteResult()) {
            return
        }
        var dpOne = DensityUtils.dp2px(applicationContext, 5.0f)

        if (isClose) {
            dpOne = -dpOne
        }

        val thread = Thread {
            for (index in 0..32) {
                runOnUiThread {
                    linearChooseLocation
                            .offsetTopAndBottom(dpOne)
                }
                Thread.sleep(3)
            }
        }
        thread.start()
    }

    override fun setPathListView(paths: MutableList<RoutePresenter.OnLineRoutePath>,
                                 startLocationName: String, endLocationName: String) {
        val adapter = RoutePathAdapter(this, paths
                , startLocationName, endLocationName)

        adapter.onItemClickListener = this

        pathListView.adapter = adapter

        coordinatorLayout.visibility = View.VISIBLE
        relativeBottomToolBar.visibility = View.VISIBLE
    }

    override fun setCountInfo(lengthInfo: String, timeInfo: String) {
        textCountMeter.text = lengthInfo
        textCountTime.text = timeInfo
        val subSequence: String = lengthInfo.substring(0, lengthInfo.length - 1)
        location_now_description.text = "距离目的地约"+lengthInfo+"  耗时约 "+timeInfo
    }

    var isStair: Boolean? = false

    @SuppressLint("MissingPermission")
    private val typeImageClickListener = View.OnClickListener {
        when (it.id) {
            R.id.btn_foot_type -> {
                imageFootType.setImageResource(R.drawable.creeper_sdk_btn_press_foot)
                imageCarType.setImageResource(R.drawable.creeper_sdk_btn_normal_car)
                presenter.roadType = NodeWeighting.SearchType.FOOT_FIRST
                com.lqkj.location.slug.library.view.show.showDialog.showMessage(context,"路径规划中...")
                presenter.startNavigation()
            }
            R.id.btn_car_type -> {
                imageFootType.setImageResource(R.drawable.creeper_sdk_btn_normal_foot)
                imageCarType.setImageResource(R.drawable.creeper_sdk_btn_press_car)
                presenter.roadType = NodeWeighting.SearchType.CAR_FIRST
                com.lqkj.location.slug.library.view.show.showDialog.showMessage(context,"路径规划中...")
                presenter.startNavigation()
            }
            R.id.image_close -> {
                onBackPressed()
            }
            R.id.guanbimessage -> {
                single_rl.visibility = View.GONE
            }
            R.id.lock_ditu -> {
                if (diturl.visibility == View.VISIBLE){
                    diturl.visibility = View.GONE
                    timerCount = 30000
                    returnSearchCount = 60000
                    page_search.visibility = View.VISIBLE
                }else if (page_search.visibility == View.VISIBLE){
                    timerCount = 30000
                    returnSearchCount = 60000
                    input_location.setText("")
                    diturl.visibility = View.VISIBLE
                    imm?.hideSoftInputFromWindow(input_location.windowToken, 0) //强制隐藏键盘
                    detail_list.visibility = View.GONE
                    search_all.visibility = View.VISIBLE
                    page_search.visibility = View.GONE
                    lock_ditu.visibility = View.GONE
                }
            }
            R.id.toolbar_back -> {
                autoBackPress()
                diturl.visibility = View.GONE
                page_search.visibility = View.VISIBLE
                lock_ditu.visibility = View.VISIBLE
            }
            R.id.search_home_back -> {
                when {
                    srL.visibility == View.VISIBLE -> {
                        srL.visibility = View.GONE
                        input_location.setText("")
                        search_all.visibility = View.VISIBLE
                    }
                    detail_list.visibility == View.VISIBLE -> {
                        detail_list.visibility = View.GONE
                        search_all.visibility = View.VISIBLE
                    }
                }
            }
            R.id.singlemessage -> {
                showDialogMessage(0)
            }
            R.id.close_location -> {
                onBackPressed()
            }
            R.id.toolbar_voice_input -> {
                /*startActivityForResult(Intent(this, SearchActivity::class.java)
                        .putExtra(SearchActivity.MAP_ID, presenter.mapId)
                        .putExtra(SearchActivity.MAP_TYPE, SearchActivity.MAP_TYPE_2D), 1)*/
                diturl.visibility = View.GONE
                page_search.visibility = View.VISIBLE
                lock_ditu.visibility = View.VISIBLE
            }
            R.id.toolbar_search_input -> {
                /*startActivityForResult(Intent(this, SearchActivity::class.java)
                        .putExtra(SearchActivity.MAP_ID, presenter.mapId)
                        .putExtra(SearchActivity.MAP_TYPE, SearchActivity.MAP_TYPE_2D), 1)*/
                diturl.visibility = View.GONE
                page_search.visibility = View.VISIBLE
                lock_ditu.visibility = View.VISIBLE
            }
            R.id.image_swap -> {
                if (startLatlng!=null && endLatlng!=null) {
                    if (endMarker != null) {
                        vectorMapView.floorMarkerComponent?.removeMarker(endMarker)
                    }
                    if (startMarker != null) {
                        vectorMapView.floorMarkerComponent?.removeMarker(startMarker)
                    }
                    presenter.changeStartAndEnd(this.startLatlng!!, this.endLatlng!!)
                    com.lqkj.location.slug.library.view.show.showDialog.showMessage(context,"路径规划中...")
                    presenter.startNavigation()
                }
            }
            R.id.btn_elevator_type -> {
                if (this.isStair!!) {
                    isStair = false
                    imageElevatorType.setImageResource(R.drawable.creeper_sdk_btn_press_elevator)
                    imageStairsType.setImageResource(R.drawable.creeper_sdk_btn_normal_stairs)
                    presenter.priorityType = NodeWeighting.SearchType.ELEVATOR_FIRST
                    com.lqkj.location.slug.library.view.show.showDialog.showMessage(context, "路径规划中...")
                    presenter.startNavigation()
                }
            }
            R.id.btn_stairs_type -> {
                if (!this.isStair!!) {
                    isStair = true
                    imageElevatorType.setImageResource(R.drawable.creeper_sdk_btn_normal_elevator)
                    imageStairsType.setImageResource(R.drawable.creeper_sdk_btn_press_stairs)
                    presenter.priorityType = NodeWeighting.SearchType.STAIRS_FIRST
                    com.lqkj.location.slug.library.view.show.showDialog.showMessage(context, "路径规划中...")
                    presenter.startNavigation()
                }
            }
            R.id.btn_start_navigation -> {
                Log.d("tag","开始导航按钮点击")
                synthesizer!!.speak("开始导航")
                rnalogNavigation?.endAnalog()
//                locationFinder?.setNowLocation(startLatlng)
                ApplicationData.daohangFinish = false
                locationEngine?.setLocation(GeometryFactory().createPoint(
                        Coordinate(startLatlng!!.latitude, startLatlng!!.longitude,
                                vectorMapView.floorComponent!!.getLevel().toDouble())))
                ApplicationData.monidaohang = false
                ApplicationData.startDaohang = true
                ApplicationData.stop = false
                needChangeFloor = true
                ApplicationData.stopBleLocation = false
                speaked = false
                daohangcurrent = 0
                layerPlugin?.setLocationLayerEnabled(LocationLayerMode.NONE)
                layerPlugin?.setLocationLayerEnabled(LocationLayerMode.COMPASS)
                layerPlugin?.isLinearAnimation = true
                rl_rl_location.visibility = View.VISIBLE
                linearBottomSheet.visibility = View.GONE
                relativeBottomToolBar.visibility = View.GONE
                if(linearChooseLocation.visibility == View.VISIBLE){
                    linearChooseLocation.visibility = View.GONE
                }
                daohangview.visibility = View.VISIBLE
                if (!image_type.isShown){
                    image_type.visibility = View.VISIBLE
                    image_type.setImageResource(R.drawable.zhixing)
                    text_detail.text = "请直行"
                }
                vectorMapView.setBuildingLevel(floors[0])
                val mapboxMap = vectorMapView.mapboxMap
                if (mapboxMap != null) {
                    setLayer(mapboxMap)
                }
            }
            R.id.btn_moni_navigation -> {
                Log.d("tag","模拟导航按钮点击")
//                synthesizer!!.speak("开始出发")
//                layerPlugin?.setLocationLayerEnabled(LocationLayerMode.NONE)
                layerPlugin?.setLocationLayerEnabled(LocationLayerMode.NAVIGATION)
                layerPlugin?.isLinearAnimation = true
                val options = layerPlugin?.locationLayerOptions?.toBuilder()
                        ?.navigationDrawable(R.drawable.navigation)
                        ?.enableStaleState(true)
                        ?.build()
                layerPlugin?.applyStyle(options)
                linearChooseLocation.visibility = View.GONE
                val mapboxMap = vectorMapView.mapboxMap
                if (mapboxMap != null) {
                    setLayer(mapboxMap)
                }
                shouldChangeFloor = false
                rl_rl_location.visibility = View.VISIBLE
                this.bottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
                linearBottomSheet.visibility = View.GONE
                relativeBottomToolBar.visibility = View.GONE
                daohangview.visibility = View.VISIBLE
                vectorMapView.setBuildingLevel(floors[0])
                rnalogNavigation?.setStart()
            }
        }
    }

    private fun showDialogMessage(flag: Int) {
        if (flag == 0) {
            if (dialog1 == null || !dialog1!!.isShowing) {
                dialog1 = Dialog(context, R.style.MyDialog)
                val view = View.inflate(context, R.layout.singel_message, null)
                val guanbifinish = view.findViewById<ImageView>(R.id.guanbisingel)
                guanbifinish.setOnClickListener { dialog1!!.dismiss() }
                dialog1!!.setContentView(view)
                hideNavKey(dialog1!!.window)
                dialog1!!.show()
            }
        }else if (flag == 1){
            var dialog = Dialog(context, R.style.MyDialog)
            val view = View.inflate(context, R.layout.show_dialog_startlocation, null)
            val cancel_message = view.findViewById<TextView>(R.id.cancel_message)
            val sure_message = view.findViewById<TextView>(R.id.sure_message)
            cancel_message.setOnClickListener(View.OnClickListener {
                Toast.makeText(context,"请选择起点",Toast.LENGTH_SHORT).show()
                presenter.clickStart = true
                dialog.dismiss()
            })
            sure_message.setOnClickListener(View.OnClickListener {
                presenter.kaishidaohang()
                dialog.dismiss()
            })
            dialog.setContentView(view)
            hideNavKey(dialog!!.window)
            dialog.show()
        }else if (flag == 2){
            overShow = true
            var location2 = IntArray(2)
            textview_all.getLocationOnScreen(location2)

            var dialog = Dialog(context, R.style.MyDialogAllOver)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)

            var mWindow = dialog.window
            var mParams = mWindow.attributes
            mParams.alpha = 1f
            mParams.gravity = Gravity.CENTER_HORIZONTAL
            mParams.width = WindowManager.LayoutParams.MATCH_PARENT
            mParams.height = resources.displayMetrics.heightPixels/2
            mParams.y = -200
            mParams.x = 0
            mWindow.attributes = mParams
            mWindow.decorView.setBackgroundColor(Color.TRANSPARENT)
            mWindow.decorView.setPadding(0,location2[1],0,location2[1])
            mWindow.decorView.minimumWidth = resources.displayMetrics.widthPixels
            dialog.setOnDismissListener {
                val mWindow = window
                val mParams = mWindow.attributes
                mParams.alpha = 1.0f
                mWindow.attributes = mParams
            }
            val view = View.inflate(context, R.layout.over_all_layout, null)
            val over_all_web = view.findViewById<X5WebView>(R.id.over_all_web)
            val close_overall = view.findViewById<ImageView>(R.id.close_overall)
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            over_all_web.loadUrl("https://720yun.com/t/285jvrmusw6")
            over_all_web.setWebViewClient(object : com.tencent.smtt.sdk.WebViewClient() {
                override fun shouldOverrideUrlLoading(webView: com.tencent.smtt.sdk.WebView?, url: String?): Boolean {
                    progressBar.visibility = View.VISIBLE
                    webView!!.loadUrl(url)
                    return super.shouldOverrideUrlLoading(webView, url)
                }

                override fun onPageFinished(webView: com.tencent.smtt.sdk.WebView?, s: String?) {
                    progressBar.visibility = View.GONE
                }

            })
            over_all_web.setWebChromeClient(object : WebChromeClient() {
                override fun onProgressChanged(webView: com.tencent.smtt.sdk.WebView?, i: Int) {
                    super.onProgressChanged(webView, i)
                    if (i < 100) {
                        progressBar.progress = i
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }
            })
            /*var webSetting = over_all_web.settings
            webSetting.javaScriptEnabled = true
            webSetting.javaScriptCanOpenWindowsAutomatically = true
            webSetting.setAppCacheEnabled(true)
            webSetting.setSupportZoom(true)
            webSetting.useWideViewPort = true
            webSetting.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            webSetting.displayZoomControls = true
            webSetting.defaultFixedFontSize = 12*/
            close_overall.setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                over_all_web.goBack()
                over_all_web.destroy()
                overShow = false
            })
            dialog.setContentView(view)
            hideNavKey(dialog!!.window)
            dialog.show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onBackPressed() {

        if (frameBottomFeatureInfo.visibility == View.VISIBLE
                && relativeFeatureInfo.visibility == View.VISIBLE) {
            frameBottomFeatureInfo.visibility = View.GONE
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)
            pullDownControlGroup()
        } else if (linearChooseLocation.visibility == View.VISIBLE
                && coordinatorLayout.visibility == View.GONE) {
            linearChooseLocation.visibility = View.GONE
            frameBottomFeatureInfo.visibility = View.GONE
            relativeFeatureInfo.visibility = View.VISIBLE
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)
            presenter.clearLocation()
        } else if (coordinatorLayout.visibility == View.VISIBLE) {
            bottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
            coordinatorLayout.visibility = View.GONE
            relativeBottomToolBar.visibility = View.GONE
            linearChooseLocation.visibility = View.GONE
            relativeFeatureInfo.visibility = View.VISIBLE
            frameBottomFeatureInfo.visibility = View.GONE
            pullDownControlGroup()

            presenter.clearLocation()

            vectorMapView.floorMarkerComponent?.removeMarker(startMarker)
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)

            clearRouteLine()

            textStartLocation.text = ""
            textEndLocation.text = ""

            if (ApplicationData.monidaohang){
                rnalogNavigation?.endAnalog()
            }

            ApplicationData.daohangFinish = true
            ApplicationData.startDaohang = true
            ApplicationData.stop = false
            rl_rl_location.visibility = View.GONE
            daohangview.visibility = View.GONE
            startLatlng=null
            endLatlng=null
            presenter.clickStart = false
            layerPlugin?.setLocationLayerEnabled(LocationLayerMode.NONE)
            locationFinder?.initPaths(null,0.0,0,null)
            endString.clear()
            crossTpyes.clear()
            floors.clear()
            positionsm.clear()
            daohangcurrent = 0
            turnSpeak = false
            reNavigationFlag = false

        } else {
            when {
                srL.visibility == View.VISIBLE -> {
                    srL.visibility = View.GONE
                    input_location.setText("")
                    search_all.visibility = View.VISIBLE
                }
                detail_list.visibility == View.VISIBLE -> {
                    detail_list.visibility = View.GONE
                    search_all.visibility = View.VISIBLE
                }
                diturl.visibility == View.VISIBLE -> {
                    diturl.visibility = View.GONE
                    page_search.visibility = View.VISIBLE
                    lock_ditu.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun autoBackPress(){
        if (frameBottomFeatureInfo.visibility == View.VISIBLE
                && relativeFeatureInfo.visibility == View.VISIBLE) {
            frameBottomFeatureInfo.visibility = View.GONE
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)
            pullDownControlGroup()
        } else if (linearChooseLocation.visibility == View.VISIBLE
                && coordinatorLayout.visibility == View.GONE) {
            linearChooseLocation.visibility = View.GONE
            frameBottomFeatureInfo.visibility = View.GONE
            relativeFeatureInfo.visibility = View.VISIBLE
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)
            presenter.clearLocation()
        } else if (coordinatorLayout.visibility == View.VISIBLE) {
            bottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
            coordinatorLayout.visibility = View.GONE
            relativeBottomToolBar.visibility = View.GONE
            linearChooseLocation.visibility = View.GONE
            relativeFeatureInfo.visibility = View.VISIBLE
            frameBottomFeatureInfo.visibility = View.GONE
            pullDownControlGroup()

            presenter.clearLocation()

            vectorMapView.floorMarkerComponent?.removeMarker(startMarker)
            vectorMapView.floorMarkerComponent?.removeMarker(endMarker)

            clearRouteLine()

            textStartLocation.text = ""
            textEndLocation.text = ""

            if (ApplicationData.monidaohang){
                rnalogNavigation?.endAnalog()
            }

            ApplicationData.daohangFinish = true
            ApplicationData.startDaohang = true
            ApplicationData.stop = false
            rl_rl_location.visibility = View.GONE
            daohangview.visibility = View.GONE
            startLatlng=null
            endLatlng=null
            presenter.clickStart = false
            layerPlugin?.setLocationLayerEnabled(LocationLayerMode.NONE)
            locationFinder?.initPaths(null,0.0,0,null)
            endString.clear()
            crossTpyes.clear()
            floors.clear()
            positionsm.clear()
            daohangcurrent = 0
            turnSpeak = false
            reNavigationFlag = false

        }
    }
    var daohangcurrent: Int?=0
    var turnSpeak: Boolean?=false
    var speaked: Boolean?=false
    var saveIndex: Int?=0

    var needChangeFloor: Boolean = true

    var speakonce: Boolean?=true

    var changeIndex: Int?=-2

    var theChangeFloor: Int? = 999

    private fun voiceDaohang(nowLocation: com.vividsolutions.jts.geom.Point?, minIndex: Int){
        var location = LatLng(nowLocation!!.x, nowLocation.y)
//        presenter.setNowPath(presenter.paths[minIndex], minIndex)
        if (minIndex!=-1 && minIndex>= this.saveIndex!!) {
            if (minIndex!=saveIndex){
                speaked = false
            }
            val distanceTo = location.distanceTo(LatLng(coordinates!![minIndex].x, coordinates!![minIndex].y))
            if (distanceTo < 2 && !speaked!!) {
                if (minIndex == endString.size-1){
                    synthesizer!!.speak(endString[minIndex])
                    speakonce = false
                    onBackPressed()
                    daohangview.visibility = View.GONE
                    text_detail.text = "您已到达目的地"
                }else if(vectorMapView.floorComponent!!.nowLevelIndex != floors[minIndex+1]){
                    var floor = floors[minIndex+1]
                    if (floor<=0){
                        if (presenter.priorityType == NodeWeighting.SearchType.STAIRS_FIRST){
                            synthesizer!!.speak("前方进入楼梯，前往负" + floor + "楼")
                        }else {
                            synthesizer!!.speak("前方进入电梯，前往负" + floor + "楼")
                        }
                    }else{
                        floor+=1
                        if (presenter.priorityType == NodeWeighting.SearchType.STAIRS_FIRST){
                            synthesizer!!.speak("前方进入楼梯，前往" + floor + "楼")
                        }else {
                            synthesizer!!.speak("前方进入电梯，前往" + floor + "楼")
                        }
                    }
                    changeFloorOk = false
                    changeIndex = minIndex
                    image_type.visibility = View.VISIBLE
                    text_detail.text = "前方进入电梯，前往"+floor+"楼"
                    image_type.setImageResource(R.drawable.dainti)
                    needChangeFloor = true
                    theChangeFloor = floors[minIndex+1]
                    navigationStep = false
                } else {
                    if (!image_type.isShown){
                        image_type.visibility = View.VISIBLE
                    }
                    if (endString[minIndex].contains("直行")||endString[minIndex].contains("按道路行走")){
                        image_type.setImageResource(R.drawable.zhixing)
                    }else if (endString[minIndex].contains("左转")){
                        image_type.setImageResource(R.drawable.zuozhuan)
                    }else if (endString[minIndex].contains("右转")){
                        image_type.setImageResource(R.drawable.youzhuan)
                    }
                    text_detail.text = endString[minIndex]
                    if (!TextUtils.isEmpty(endString[minIndex])) {
                        synthesizer!!.speak(endString[minIndex])
                        speakonce = false
                    }
                }
                saveIndex=minIndex
                speaked = true
            }
            if (minIndex == this.saveIndex!! && !needChangeFloor) {
                val distanceTo = location.distanceTo(LatLng(coordinates!![this.saveIndex!!].x, coordinates!![this.saveIndex!!].y))
                if (distanceTo < 1.5 && distanceTo > 1) {
                    if (saveIndex!! < endString.size) {
                        if (saveIndex == endString.size - 1) {
                            text_detail.text = "前方到达终点附近"
                            if (!speakonce!!) {
                                synthesizer!!.speak("前方到达终点附近")
                                speakonce = true
                            }
                        } else {
                            if (endString[saveIndex!!+1].contains("直行") || endString[saveIndex!!+1].contains("按道路行走") || endString[saveIndex!!+1].contains("米后到达")) {
                                image_type.setImageResource(R.drawable.zhixing)
                            } else if (endString[saveIndex!!+1].contains("左转")) {
                                image_type.setImageResource(R.drawable.zuozhuan)
                            } else if (endString[saveIndex!!+1].contains("右转")) {
                                image_type.setImageResource(R.drawable.youzhuan)
                            }
                            if (!TextUtils.isEmpty(endString[saveIndex!!+1])) {
                                text_detail.text = "前方" + endString[saveIndex!! + 1]
                                if (!speakonce!!) {
                                    synthesizer!!.speak("前方" + endString[saveIndex!! + 1])
                                    speakonce = true
                                }
                            }
                        }
                    }
                } else {
                }
            }else if(minIndex == this.saveIndex!! && needChangeFloor){
                val distanceTo = LatLng(nowLocation.x, nowLocation.y).distanceTo(LatLng(coordinates!![this.saveIndex!!].x, coordinates!![this.saveIndex!!].y))
                if (distanceTo < 5 && distanceTo > 1) {
                    if (saveIndex!! < endString.size) {
                    }
                }
            }
        }else{
            speaked = false
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.route_to_feature -> {
                presenter.setStartLocation(Point.fromCoordinates(Position.fromCoordinates(ApplicationData.screenLocation.longitude, ApplicationData.screenLocation.latitude))
                        , "大厅", 0)
                if (textEndLocation.text == "大厅"){
                    synthesizer?.speak("当前位置正在大厅")
                    val position2 = CameraPosition.Builder()
                            .target(ApplicationData.screenLocation) // Sets the new camera position位置
                            .zoom(19.5) // Sets the zoom大小
                            .bearing(ApplicationData.screenBearing) // Rotate the camera方向
                            .tilt(0.0) // Set the camera tilt倾斜
                            .build() // Creates a CameraPosition from the builder
                    mapbox!!.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)
                    onBackPressed()
                }else {
                    relativeFeatureInfo.visibility = View.GONE
                    linearChooseLocation.visibility = View.VISIBLE
                    presenter.routeMode = true
//                showDialogMessage(1)
                    pullDownControlGroup()
//                presenter.setStartLocation(Coordinate(103.961176310343, 30.6796164750057))
                    if (this!!.isStair!!) {
                        presenter.priorityType = NodeWeighting.SearchType.STAIRS_FIRST
                    } else {
                        presenter.priorityType = NodeWeighting.SearchType.ELEVATOR_FIRST
                    }
                    com.lqkj.location.slug.library.view.show.showDialog.showMessage(context, "路径规划中...")
                    presenter.startNavigation()
                }
            }
            R.id.card_search -> {
                /*startActivityForResult(Intent(this, SearchActivity::class.java)
                        .putExtra(SearchActivity.MAP_ID, presenter.mapId)
                        .putExtra(SearchActivity.MAP_TYPE, SearchActivity.MAP_TYPE_2D), 1)*/
                onBackPressed()
                diturl.visibility = View.GONE
                page_search.visibility = View.VISIBLE
                lock_ditu.visibility = View.VISIBLE
            }
            R.id.top_info ->{

            }
            R.id.cover_route_detail ->{

            }
            R.id.frame_bottom_feature_info ->{

            }
            R.id.loading_data ->{
                handler.sendEmptyMessage(4)
            }
            R.id.route_detail->{
                if (this.bottomSheet?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    this.bottomSheet?.state = BottomSheetBehavior.STATE_EXPANDED
                    bottomsheet_text.text = "地图显示"
                    route_path_menu.setImageResource(R.drawable.map)
                }else{
                    this.bottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
                    bottomsheet_text.text = "路径详情"
                    route_path_menu.setImageResource(R.drawable.creeper_sdk_route_path_menu)
                }
            }
            R.id.search_home_image->{
                poiComponent.initialization()
                poiComponent.searchAsync()
            }
            R.id.toolbar_search_input_all->{
                poiComponent.initialization()
                poiComponent.searchAsync()
            }
            R.id.rl_all_over->{
                //全景
                if (!overShow!!) {
                    showDialogMessage(2)
                }
            }
        }
    }

    override fun onClick(view: View, position: Int, path: RoutePresenter.OnLineRoutePath) {
        val mapboxMap = vectorMapView.mapboxMap ?: return
        val geometry = JSON.getInstance().fromJson<Geometry<*>>(path.geometry,
                Geometry::class.java)
/*

        mapboxMap.removeLayer(OVER_LAYER)
        mapboxMap.removeSource(OVER_SOURCE)

        val source = GeoJsonSource(OVER_SOURCE, Feature.fromGeometry(geometry))

        val layer = LineLayer(OVER_LAYER, OVER_SOURCE)
        layer.withProperties(PropertyFactory.lineColor(Color.YELLOW))
                .withProperties(PropertyFactory.lineOpacity(0.6f))
                .withProperties(PropertyFactory.lineWidth(8.0f))

        mapboxMap.addSource(source)
        mapboxMap.addLayer(layer)
*/

        bottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED

        moveToFeature(geometry, path, mapboxMap)

        //楼层切换
        vectorMapView.floorComponent?.setIndex(path.endFloor)
        vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
        presenter.setNowPath(path, position)
    }

    private fun moveToFeature(geometry: Geometry<*>, path: RoutePresenter.OnLineRoutePath,
                              mapboxMap: MapboxMap) {
        if (geometry is Point) {
            val coordinates = geometry.coordinates

            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(coordinates.latitude,
                    coordinates.longitude)), object : MapboxMap.CancelableCallback {
                override fun onFinish() {
                    vectorMapView.floorComponent?.setIndex(path.endFloor)
                    vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
                }

                override fun onCancel() {}
            })
            return
        }

        val bounds = LatLngBounds.Builder()

        if (geometry is LineString) {
            val coordinates = geometry.coordinates.map {
                LatLng(it.latitude, it.longitude)
            }
            bounds.includes(coordinates)
        } else if (geometry is MultiLineString) {
            geometry.coordinates.forEach {
                bounds.includes(it.map {
                    LatLng(it.latitude, it.longitude)
                })
            }
        }

        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),
                DensityUtils.dp2px(applicationContext, 100.0f)),
                object : MapboxMap.CancelableCallback {
                    override fun onFinish() {
                        vectorMapView.floorComponent?.setIndex(path.endFloor)
                        vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
                    }

                    override fun onCancel() {}
                })
    }

    override fun outBuilding(buildingId: String) {
        showRouteLine(false)
    }

    override fun inBuilding(buildingId: String) {
        showRouteLine(true)
    }

    var locationLayerMode: Int? = 9999

    @SuppressLint("MissingPermission")
    override fun onChange(oldFloor: Int, floor: Int) {
        presenter.findPathForLevel(floor)

        timerCount = 30000
        returnSearchCount = 60000

        val mapboxMap = vectorMapView.mapboxMap ?: return

        if (nowLocationFloor != floor && nowLocationFloor!=99999){

            if (!ApplicationData.monidaohang && ApplicationData.daohangFinish) {
                locationLayerMode = layerPlugin!!.locationLayerMode
                layerPlugin!!.setLocationLayerEnabled(LocationLayerMode.NONE)
            }
            if (locationMarker != null) {
                mapbox?.removeMarker(locationMarker!!)
            }
            if (zhiXinMarker != null) {
                mapbox?.removeMarker(zhiXinMarker!!)
            }
            if (nowMarker != null) {
                mapbox?.removeMarker(nowMarker!!)
            }
        }else{
            /*if (locationLayerMode!=9999 && layerPlugin!!.locationLayerMode == LocationLayerMode.NONE) {
                if (locationLayerMode == LocationLayerMode.NAVIGATION){
                    layerPlugin?.setLocationLayerEnabled(LocationLayerMode.NAVIGATION)
                }else if (locationLayerMode == LocationLayerMode.COMPASS){
                    layerPlugin?.setLocationLayerEnabled(LocationLayerMode.COMPASS)
                }else{
                    layerPlugin?.setLocationLayerEnabled(LocationLayerMode.COMPASS)
                }
            }*/
        }


        if (floor == 0) {
            val markerOptions = MarkerOptions()
            markerOptions.position = ApplicationData.screenLocation
            markerOptions.icon = IconFactory.getInstance(applicationContext)
                    .fromResource(R.drawable.creeper_sdk_route_start_marker)
            nowMarker = mapbox?.addMarker(markerOptions)
        }

        /*mapboxMap.removeLayer(OVER_LAYER)
        mapboxMap.removeSource(OVER_SOURCE)*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == SearchActivity.RESULT_CODE) {
            val poiPoint = data?.getSerializableExtra(SearchActivity.RESULT_CONTENT)
                    as POIPoint

            val name = poiPoint.tags?.get("name")
            val level = (poiPoint.tags?.get("level") ?: "0").toInt()

            relativeFeatureInfo.visibility = View.GONE
            linearChooseLocation.visibility = View.VISIBLE

            val lat = poiPoint.coordinate?.get(1) ?: 0.0
            val lon = poiPoint.coordinate?.get(0) ?: 0.0
//            val zoom = if (level > 0) 19.0 else 14.0
            val zoom = 19.5

            if (name != null) {
                if (level > 0) {
                    vectorMapView.floorComponent?.floorView?.visibility = View.VISIBLE
                }
                vectorMapView.mapboxMap?.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(LatLng(lat, lon), zoom), object : MapboxMap.CancelableCallback {
                    override fun onFinish() {
                        vectorMapView.floorComponent?.setIndex(level)
                        vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
                        presenter.routeMode = true
                        presenter.setLocation(Point.fromCoordinates(Position.fromCoordinates(lon, lat))
                                , name, level)
                    }

                    override fun onCancel() {}
                })
            } else {
                Toast.makeText(this, "数据缺失", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setSearchData(poiPoint: POIPoint){

        val name = poiPoint.tags?.get("name")
        val level = (poiPoint.tags?.get("level") ?: "0").toInt()

//        relativeFeatureInfo.visibility = View.GONE
//        linearChooseLocation.visibility = View.VISIBLE

        val lat = poiPoint.coordinate?.get(1) ?: 0.0
        val lon = poiPoint.coordinate?.get(0) ?: 0.0
        var msg = Message.obtain()
        val bundle = Bundle()
        bundle.putDouble("lat", lat)
        bundle.putDouble("lon", lon)
        bundle.putString("floor", poiPoint.tags?.get("level") ?: "0")
        bundle.putString("name", name)
        msg.what = 6
        msg.data = bundle
        handler.sendMessage(msg)

    }

    override fun routeError() {
        vectorMapView.floorMarkerComponent?.removeMarker(startMarker)
        vectorMapView.floorMarkerComponent?.removeMarker(endMarker)

        com.lqkj.location.slug.library.view.show.showDialog.hideMessage()
        Toast.makeText(this, "路径规划失败", Toast.LENGTH_SHORT).show()
//        baiDuVoiceInit?.synthesizer!!.speak("路径规划失败")
        checkResult(synthesizer!!.speak("路径规划失败"),"speaking")

        onBackPressed()

        progressDialog?.dismiss()
    }

    override fun routeSuccess() {
        endString.clear()
        crossTpyes.clear()
        positionsm.clear()
        floors.clear()
        coordinates!!.clear()
        speaked=false
        saveIndex=0
        com.lqkj.location.slug.library.view.show.showDialog.hideMessage()
        Toast.makeText(this, "路径规划成功", Toast.LENGTH_SHORT).show()
//        baiDuVoiceInit?.synthesizer!!.speak("路径规划成功")
        checkResult(synthesizer!!.speak("路径规划成功"),"speaking")
        ApplicationData.stop = true
        progressDialog?.dismiss()
        locationFinder?.initPre(presenter)
        setRnalogNavigationDate()
        if(lineString!=null) {
//            locationFinder?.initPaths(lineString,ApplicationData.allDistance,ApplicationData.allTimes,coordinates)
            rnalogNavigation = RnalogNavigation(endString, coordinates,this,lineString,
                    this,synthesizer,this,ApplicationData.allDistance,ApplicationData.allTimes,this)
        }
        daohang_destation.text = presenter.getEndLocationName()
        var floo = floors[0]
        if (floo>=0){
            floo +=1
            location_now_floor.text = "("+floo+"F)"
        }else{
            location_now_floor.text = "("+floo+"F)"
        }
        vectorMapView.floorComponent!!.setIndex(floors[0])
        vectorMapView.floorComponent?.floorView?.notifyDataSetChanged()
        val position2 = CameraPosition.Builder()
                .target(ApplicationData.screenLocation) // Sets the new camera position位置
                .zoom(19.5) // Sets the zoom大小
                .bearing(ApplicationData.screenBearing) // Rotate the camera方向
                .tilt(0.0) // Set the camera tilt倾斜
                .build() // Creates a CameraPosition from the builder
        mapbox!!.easeCamera(CameraUpdateFactory.newCameraPosition(position2), 200, false)
        linearBottomSheet.visibility = View.VISIBLE
        pullUpControlGroup()
    }

    private fun setRnalogNavigationDate(){
        if (presenter.paths.size>1) {
//            endString.add("开始出发")
            (0 until presenter.paths.size).forEach { t ->
                var pathname = presenter.paths[t]
                if (t == presenter.paths.size - 2){
                    var list = presenter.pathToPositionMo(pathname)
                    for (n in 0 until list.size){
                        if (n==0) {
                            crossTpyes.add(pathname.crossType)
                            endString.add(pathname.distance.toInt().toString() + "米后" + "到达终点附近")
                            positionsm.add(list[n])
                        }else{
                            endString.add("到达终点附近,导航结束")
                            crossTpyes.add(pathname.crossType)
                            positionsm.add(list[n])
                        }
                        floors.add(pathname.startFloor)
                    }
                }else{
                    setAnalogDate(pathname)
                }
            }
//            coordinates = LinkedList<Coordinate>()
            (0 until positionsm.size).forEach { j ->
                val coordinate = Coordinate(positionsm[j].latitude, positionsm[j].longitude, floors[j].toDouble())
                /*if(coordinates!!.size>0){
                    if (coordinates!![coordinates!!.size-1].x == coordinate.x && coordinates!![coordinates!!.size-1].y==coordinate.y){

                    }else{
                        coordinates!!.add(coordinate)
                    }
                }else {
                    coordinates!!.add(coordinate)
                }*/
                coordinates!!.add(coordinate)
            }
            lineString = null
            val factory = GeometryFactory()
            lineString = factory.createLineString(coordinates!!.toArray(arrayOfNulls(coordinates!!.size)))
        }
    }

    override fun startProgressDialog() {
        progressDialog = RouteProgressDialog(this)
        progressDialog?.show()
    }

    /**
     * 地图控制组件拉升
     */
    fun pullUpControlGroup() {
        vectorMapView.controlGroup.setMargin(0, 0, 0,
                resources.getDimensionPixelSize(R.dimen.dp_132))
    }

    /**
     * 地图控制组件下降
     */
    fun pullDownControlGroup() {
        vectorMapView.controlGroup.setMargin(0, 0, 0, 0)
    }

    /**
     * 是否在室内
     */
    override fun isIndoor(): Boolean {
        return (vectorMapView.floorComponent?.floorView?.visibility ?: View.VISIBLE) == View.VISIBLE
    }

    fun pullUpBottomSheet(view: View? = null) {
        if (this.bottomSheet?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            this.bottomSheet?.state = BottomSheetBehavior.STATE_EXPANDED
            bottomsheet_text.text = "地图显示"
            route_path_menu.setImageResource(R.drawable.map)
        }else{
            this.bottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomsheet_text.text = "路径详情"
            route_path_menu.setImageResource(R.drawable.creeper_sdk_route_path_menu)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationEngine != null) {
            locationEngine?.deactivate()
        }
        if (layerPlugin != null) {
            layerPlugin?.onStop()
        }
        if (rnalogNavigation!=null){
            rnalogNavigation?.destory()
        }
        if (locationFinder!=null){
            locationFinder?.destroy()
        }
        if (synthesizer!=null){
            synthesizer!!.release()
        }
        vectorMapView.floorComponent?.onFloorChanges?.remove(this)
        vectorMapView.floorComponent?.onStateChanges?.remove(this)
        vectorMapView.onDestroy()
        unregisterReceiver(receiver)
        val intentOne = Intent(this, ReOpenService::class.java)

        startService(intentOne)
    }

    override fun onResume() {
        super.onResume()
        ownerFlag = false
        vectorMapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        vectorMapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        vectorMapView.onPause()
        if (!ownerFlag!!) {
            startActivitym()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        vectorMapView.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        vectorMapView.onStart()
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected fun hideBottomUIMenu(window: Window) {
        //保持布局状态
        var uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN
        if (SDK_INT >= 19) {
            window.decorView.systemUiVisibility = uiOptions
        } else {
            window.decorView.systemUiVisibility = View.GONE
        }
        window.decorView.systemUiVisibility = uiOptions
    }

    fun hideNavKey(window: Window) {
        if (SDK_INT in 12..18) {
            window.decorView.systemUiVisibility = View.GONE
        } else if (SDK_INT >= 19) {
            //for new api versions.
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            window.decorView.systemUiVisibility = uiOptions
        }
    }

    /**
     * 语音
     */


    private val appId = "11641231"

    private val appKey = "uqGpOg8T41BarzGvA631hz4x"

    private val secretKey = "EpRgzkgHvPhg6qwXuiIygflM0TzCqTQ4"

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private val ttsMode = TtsMode.MIX

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    private val offlineVoice = OfflineResource.VOICE_FEMALE

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    var synthesizer: MySyntherizer? = null

    private val mainHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what = msg.what
            when (what) {
                PRINT -> print(msg)
                UI_CHANGE_INPUT_TEXT_SELECTION -> {
                }
                UI_CHANGE_SYNTHES_TEXT_SELECTION -> {
                }
                else -> {
                }
            }/*if (msg.arg1 <= mInput.getText().length()) {
                        mInput.setSelection(0, msg.arg1);
                    }*///                    SpannableString colorfulText = new SpannableString(mInput.getText().toString());
            /*    if (msg.arg1 <= colorfulText.toString().length()) {
                        colorfulText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, msg.arg1, Spannable
                                .SPAN_EXCLUSIVE_EXCLUSIVE);
                        mInput.setText(colorfulText);
                    }*/
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     *
     *
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    fun initialTts() {
        LoggerProxy.printable(true) // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        val listener = UiMessageListener(mainHandler)

        val params = getParams()


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        val initConfig = InitConfig(appId, appKey, secretKey, ttsMode, params, listener)

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(context).check(initConfig, object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == 100) {
                    val autoCheck = msg.obj as AutoCheck
                    synchronized(autoCheck) {
                        val message = autoCheck.obtainDebugMessage()
                        toPrint(message) // 可以用下面一行替代，在logcat中查看代码
                        Log.w("AutoCheckMessage", message)
                    }
                }
            }

        })
        if (synthesizer != null) {
            if (!synthesizer!!.isInnitied) {
                Log.w("AutoCheckMessage", "synthesizer!=null" + "isInitied=false")
                synthesizer = NonBlockSyntherizer(context, initConfig, mainHandler) // 此处可以改为MySyntherizer 了解调用过程
            }
            Log.w("AutoCheckMessage", "synthesizer!=null" + "isInitied=false")
            /* synthesizer.release();
            synthesizer = new NonBlockSyntherizer(context, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程*/
        } else {
            Log.w("AutoCheckMessage", "synthesizer=null")
            synthesizer = NonBlockSyntherizer(context, initConfig, mainHandler) // 此处可以改为MySyntherizer 了解调用过程
        }
    }


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0")
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "7")
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5")
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5")

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK)
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        val offlineResource = createOfflineResource(offlineVoice)
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource!!.textFilename)
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.modelFilename)
        return params
    }


    protected fun createOfflineResource(voiceType: String): OfflineResource? {
        var offlineResource: OfflineResource? = null

        try {
            offlineResource = OfflineResource(context, voiceType)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("AutoCheckMessage", "【error】:copy files from assets failed." + e.message)
            toPrint("【error】:copy files from assets failed." + e.message)
        }

        return offlineResource
    }

    private fun toPrint(str: String) {
        val msg = Message.obtain()
        msg.obj = str
        mainHandler.sendMessage(msg)
    }

    fun checkResult(result: Int, method: String) {
        if (result != 0) {
            toPrint("error code :$result method:$method, 错误码文档:http://yuyin.baidu.com/docs/tts/122 ")
        }
    }

    /**
     * 程序是否在前台运行
     *
     */
    fun isAppOnForeground(): Boolean {

        val activityManager = applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = applicationContext.packageName
        /**
         * 获取Android设备中所有正在运行的App
         */
        /**
         * 获取Android设备中所有正在运行的App
         */
        val appProcesses = activityManager
                .runningAppProcesses ?: return false

        for (appProcess in appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName == packageName && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }

        return false
    }

    private fun startActivitym() {
        if (!isAppOnForeground()) {

        } else {
            val intentOne = Intent(this, ReOpenService::class.java)
            startService(intentOne)
        }
    }

    var ownerFlag: Boolean?=false

    fun changeFlag(flag: Boolean){
        ownerFlag = flag
    }

    internal class HomeKeyEventBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context1: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra(SYSTEM_REASON)
                if (reason != null) {
                    bcontext.changeFlag(true)
                    if (reason == SYSTEM_HOME_KEY) {
                        val intentOne = Intent(bcontext, ReOpenService::class.java)
                        bcontext.startService(intentOne)
                    }
                }
            }
        }

        companion object {
            val SYSTEM_REASON = "reason"
            val SYSTEM_HOME_KEY = "homekey"
            val SYSTEM_RECEN_KEY = "recentapps"
            var bcontext = HomeActivity()
        }

        fun setContext(context: Context){
            bcontext = context as HomeActivity
        }

    }
}