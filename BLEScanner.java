package location.wbkj.com.locationlibary.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import location.wbkj.com.locationlibary.bean.BeaconInfo;
import location.wbkj.com.locationlibary.device.LocationBLEDevice;
import location.wbkj.com.locationlibary.device.LocationDevice;
import location.wbkj.com.locationlibary.global.ApplicationData;
import location.wbkj.com.locationlibary.global.GlobalData;
import location.wbkj.com.locationlibary.location.LocationInterface;

/**
 * 蓝牙扫描
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEScanner implements Scanner,BluetoothAdapter.LeScanCallback {
    private final LocationInterface locationInterface;
    private Context context;
    /**
        * 默认扫描间隔
     */
    private static final float defaultInterval = 5.0f;

    private ScannerCallback callback;

    BluetoothAdapter bluetoothAdapter;
    private Thread scanThread;
    private Timer timer;
    private Map<String,BeaconInfo> results;
    private Map<String,LocationBLEDevice> resultDevices;
    private int sleepTime=1;
    private boolean isInit=true;
    private boolean interrupt = false;
    private Handler handler = new Handler();

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            Log.d("timerinto","true");
            if (System.currentTimeMillis() - scanTime > 10000){
//                locationInterface.error("蓝牙扫描超时，重启蓝牙试试",11);
                /*if (bluetoothAdapter!=null) {
                    bluetoothAdapter.disable();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
                destroy();
                resetStrat();
            }

            if (!bluetoothAdapter.isEnabled()){
                locationInterface.error("蓝牙未打开",5);
            }
/*
            if (!GlobalData.isNetWorkAvailable(context)){
                locationInterface.error("请检查网络连接是否可用",10);
            }*/

            if (!interrupt && ApplicationData.startBleScanner) {
                if (callback != null) {
                    List<BeaconInfo> templates = new LinkedList<>();
                    List<LocationDevice> templatesDevice = new LinkedList<>();
                    //map转list
                    for (String key : results.keySet()) {
                        templates.add(results.get(key));
                        templatesDevice.add(resultDevices.get(key));
                    }
                    if (templates.size() > 0) {
                        callback.onScanList(templatesDevice);
                    }
                }
                if (isInit) {
                    results.clear();
                    resultDevices.clear();
                }
            }
            handler.postDelayed(this,1000);
        }
    };
    private long scanTime = System.currentTimeMillis();

    /**
     *
     * @param context 上下文
     */
    public BLEScanner(Context context, LocationInterface locationInterface) {
        this.context = context;
        this.locationInterface = locationInterface;

        results = new ConcurrentHashMap<>();

        resultDevices = new ConcurrentHashMap<>();

        init();
        ApplicationData.BleScanStart = true;
    }

    @Override
    public void start() {
        //TODO 注册蓝牙设备回调
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean b = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            b = bluetoothAdapter.startLeScan(this);
        }
        Log.d("开启结果",b+"");
        if (b){
            locationInterface.complete();
        }
        scanTime = System.currentTimeMillis();
        startScanThread();
        interrupt = false;
    }

    private void resetStrat(){
//        init();
        boolean b = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            b = bluetoothAdapter.startLeScan(this);
        }
        Log.d("开启结果",b+"");
        scanTime = System.currentTimeMillis();
        interrupt = false;
    }

    @Override
    public void destroy() {
        //TODO 取消蓝牙设备回调
        Log.d("步进经纬度","销毁");
        if (bluetoothAdapter!=null){
            Log.d("步进经纬度","停止扫描");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bluetoothAdapter.stopLeScan(this);
            }
        }
        interrupt = true;
//        timer.cancel();
        /*if (scanThread!=null){
            Log.d("步进经纬度","中断");
            scanThread.interrupt();
        }*/
    }

    @Override
    public float interval() {
        return defaultInterval;
    }

    @Override
    public void setCallback(ScannerCallback callback) {
        this.callback=callback;
    }

    /**
     * 初始化
     */
    public void init(){
        BluetoothManager bluetoothManager= (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }else {
                locationInterface.error("请使用android 4.3以上的手机",2);
            }
        }else {
            locationInterface.error("蓝牙服务获取失败，请查看权限是允许",3);
        }

        if (!bluetoothAdapter.isEnabled()){
            locationInterface.error("蓝牙未打开",4);
            bluetoothAdapter.enable();
        }
    }


    /**
     * 启动扫描线程
     */
    private void startScanThread() {
/*


        if (timer==null){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("timerinto","true");

                    if (!interrupt && ApplicationData.startBleScanner) {
                        if (callback != null) {
                            List<BeaconInfo> templates = new LinkedList<>();
                            List<LocationDevice> templatesDevice = new LinkedList<>();
                            //map转list
                            for (String key : results.keySet()) {
                                templates.add(results.get(key));
                                templatesDevice.add(resultDevices.get(key));
                            }
                            if (templates.size() > 0) {
                                callback.onScanList(templatesDevice);
                            }
                        }
                        if (isInit) {
                            results.clear();
                            resultDevices.clear();
                        }
                    }
                }
            },0,1000);
        }
*/

        handler.postDelayed(r,1000);
    }

    private void sleepScanThread() {
        try {
            Thread.sleep(1000 * sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        BeaconInfo beaconInfo = recordToBeaconInfo(scanRecord);
        scanTime = System.currentTimeMillis();
        LocationBLEDevice beaconDevice = recordToBeaconDevice(scanRecord);

//        if (beaconInfo.getMinor() != 0 && beaconInfo.getMajor() != 0) {
            beaconInfo.setRssi(rssi);
            beaconDevice.setRssi(rssi);
            this.results.put(device.getAddress(), beaconInfo);
            this.resultDevices.put(device.getAddress(), beaconDevice);
//        }
    }


    private LocationBLEDevice recordToBeaconDevice(byte[] scanRecord) {
        LocationBLEDevice beaconDevice = new LocationBLEDevice();

        int startByte = 2;

        boolean patternFound = false;
        // 寻找ibeacon
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies
                    // an
                    // iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies
                // correct
                // data
                // length
                patternFound = true;
                break;
            }
            startByte++;
        }
        // 如果找到了的话
        if (patternFound) {
            // ibeacon的Major值
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                    + (scanRecord[startByte + 21] & 0xff);

            // ibeacon的Minor值
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                    + (scanRecord[startByte + 23] & 0xff);

            beaconDevice.setMajor(major);
            beaconDevice.setMinor(minor);
        }

        return beaconDevice;
    }

    private BeaconInfo recordToBeaconInfo(byte[] scanRecord) {
        BeaconInfo beaconInfo = new BeaconInfo();

        int startByte = 2;

        boolean patternFound = false;
        // 寻找ibeacon
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies
                    // an
                    // iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies
                // correct
                // data
                // length
                patternFound = true;
                break;
            }
            startByte++;
        }
        // 如果找到了的话
        if (patternFound) {
            // ibeacon的Major值
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                    + (scanRecord[startByte + 21] & 0xff);

            // ibeacon的Minor值
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                    + (scanRecord[startByte + 23] & 0xff);

            beaconInfo.setMajor(major);
            beaconInfo.setMinor(minor);
        }

        return beaconInfo;
    }

    public boolean isInit() {
        return isInit;
    }

    /**
     *
     * @param init 是否第一次初始化
     */
    public void setInit(boolean init) {
        isInit = init;

        this.sleepTime = 5;
    }
}
