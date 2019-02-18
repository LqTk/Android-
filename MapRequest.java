package location.wbkj.com.newnavigation.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Tang on 2017/3/15 0015.
 */
public class MapRequest {

    private static MapRequest mapRequest;

    protected OkHttpClient client;

    protected Handler handler;

    private CacheControl cacheControl;

    private static Context context;

    public static void setContext(Context context){
        MapRequest.context=context;
    }

    /**
     *
     * @return map请求
     */
    public static synchronized MapRequest getInstance(){
        if (mapRequest==null){
            mapRequest=new MapRequest(context);
        }

        return mapRequest;
    }

    private MapRequest(Context context) {
        Cache cache = new Cache(ContextCompat.getExternalCacheDirs(context)[0], 30 * 1024 * 1024);

        cacheControl = new CacheControl.Builder()
                .maxAge(0, TimeUnit.SECONDS)
                .build();

        client = new OkHttpClient.Builder()
                .cache(cache)
                .addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
                .addInterceptor(REWRITE_RESPONSE_INTERCEPTOR_OFFLINE)
                .build();

        handler = new Handler(Looper.getMainLooper());
    }

    /**
     *
     * @param url url
     * @param body 请求体
     * @param callback 回调
     */
    public void post(String url, RequestBody body, final Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .post(body).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(new Exception("服务器请求失败"), 500);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                if (response.isSuccessful()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(result, false);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new Exception("请求出错"), response.code());
                        }
                    });
                }
            }
        });
    }

    /**
     *
     * @param url url
     * @param body 请求体
     * @param callback 回调
     */
    public void post2(String url, RequestBody body, final succCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .post(body).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e, 500);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                if (response.isSuccessful()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(result, false);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new Exception("请求出错"), response.code());
                        }
                    });
                }
            }
        });

    }

    /**
     *
     * @param url url
     * @param callback 回调
     */

    public void get(String url, final Callback callback){
        get(url,callback,null);
    }

    /**
     *
     * @param url url
     * @param callback 回调
     * @param cacheControl 缓存控制
     */
    public void get(String url, final Callback callback,CacheControl cacheControl) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        if (cacheControl==null){
            requestBuilder.cacheControl(this.cacheControl);
        }

        client.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e, 500);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                boolean isCache=response.cacheResponse()!=null;
                if (isCache){
                    String nowTag=response.cacheResponse().header("ETag");
                    String cacheTag=response.header("ETag");
                    if (nowTag!=null && cacheTag!=null) {
                        isCache = response.cacheResponse().header("ETag").equals(response.header("ETag"));
                    }
                }
                if (response.isSuccessful()) {
                    final boolean finalIsCache = isCache;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            callback.onSuccess(result, finalIsCache);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new Exception("请求出错"), response.code());
                        }
                    });
                }
            }
        });
    }

    public void put(String url, final Callback callback, FormBody formBody){
        put(url,callback,null,formBody);
    }

    public interface UpdataCallBack{
        void onErrorB(Exception e,int errorCode);
        void onSuccess(String result);
    }

    public void putUpdata(String url,final UpdataCallBack updataCallBack,FormBody formBody){
        putUpdataC(url,updataCallBack,formBody);
    }

    private void putUpdataC(String url, final UpdataCallBack updataCallBack, FormBody formBody) {
        Request requestBuilder = new Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .post(formBody).build();

        client.newCall(requestBuilder).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updataCallBack.onErrorB(e, 500);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                if (response.isSuccessful()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updataCallBack.onSuccess(result);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updataCallBack.onErrorB(new Exception("请求出错"), response.code());
                        }
                    });
                }
            }
        });
    }


    /**
     *
     * @param url url
     * @param callback 回调
     * @param cacheControl 缓存控制
     */
    public void put(String url, final Callback callback,CacheControl cacheControl,FormBody formBody) {

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type","application/json")
                .addHeader("Accept","application/json")
                .put(formBody);


        if (formBody != null){
            requestBuilder.put(formBody);
        }

        if (cacheControl==null){
            requestBuilder.cacheControl(this.cacheControl);
        }

        client.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e, 500);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                boolean isCache=response.cacheResponse()!=null;
                if (isCache){
                    String nowTag=response.cacheResponse().header("ETag");
                    String cacheTag=response.header("ETag");
                    if (nowTag!=null && cacheTag!=null) {
                        isCache = response.cacheResponse().header("ETag").equals(response.header("ETag"));
                    }
                }
                if (response.isSuccessful()) {
                    final boolean finalIsCache = isCache;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            callback.onSuccess(result, finalIsCache);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new Exception("请求出错"), response.code());
                        }
                    });
                }
            }
        });
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void onError(Exception e, int errorCode);

        void onSuccess(String result, boolean isCache);
    }

    /**
     * 回调接口
     */
    public interface succCallback {
        void onError(Exception e, int errorCode);

        void onSuccess(String result, boolean isCache);
    }

    /**
     * 网络缓存拦截器
     */
    private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            String cacheControl1 = originalResponse.header("Cache-Control");
            if (cacheControl1 == null) {
                //如果cache没值，缓存时间为TIMEOUT_CONNECT，有的话就为cache的值
                originalResponse = originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=0")
                        .build();

                return originalResponse;
            } else {
                return originalResponse;
            }
        }
    };

    /**
     * 缓存拦截器
     */
    private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR_OFFLINE = new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            try {
                Response response = chain.proceed(request);

                return response;
            } catch (UnknownHostException | SocketTimeoutException | ConnectException e) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();

                return chain.proceed(request);
            }
        }
    };

/*请求方式
FormBody formBody = new FormBody.Builder()
                .add("type",type+"").build();
        MapRequest.getInstance().post("https://xz.parkbobo.com/location/locMapZone/v1/findByType?", formBody, new MapRequest.Callback() {
            @Override
            public void onError(Exception e, int errorCode) {
                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                handler.postDelayed(runnable,10000);
            }

            @Override
            public void onSuccess(String result, boolean isCache) {
                analyseData(JSON.Companion.getInstance().fromJson(result,JsonObject.class));
            }
        });
*/
}
