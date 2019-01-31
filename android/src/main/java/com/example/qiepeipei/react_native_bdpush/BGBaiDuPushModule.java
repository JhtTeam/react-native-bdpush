package com.example.qiepeipei.react_native_bdpush;

/**
 * Created by qiepeipei on 2017/6/27.
 */
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.android.pushservice.BasicPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;

import java.util.HashMap;
import java.util.Map;

public class BGBaiDuPushModule extends ReactContextBaseJavaModule {

    static public BGBaiDuPushModule myPush;
    static public String DidReceiveMessage = "DidReceiveMessage";
    static public String DidOpenMessage = "DidOpenMessage";
    static public String channelId = "";
    static public String userId = "";
    public BGBaiDuPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        myPush = this;
        this.initialise();
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DidReceiveMessage, DidReceiveMessage);
        constants.put(DidOpenMessage, DidOpenMessage);
        return constants;
    }

    @Override
    public String getName() {
        return "RCTBaiDuPushManager";
    }

    //初始化
    public void initialise() {
        Log.d("百度推送", "initialise");
        Log.d("百度推送", "sound uri: " + Settings.System.DEFAULT_NOTIFICATION_URI.toString());

        PushManager.startWork(getReactApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(getReactApplicationContext(), "api_key"));

        BasicPushNotificationBuilder bBuilder = new BasicPushNotificationBuilder();
        bBuilder.setChannelId("default_channel");
        bBuilder.setChannelName("TMS_CHANNEL");
        bBuilder.setNotificationDefaults(Notification.DEFAULT_ALL
                | Notification.DEFAULT_SOUND);
        bBuilder.setStatusbarIcon(R.drawable.ic_notification);

        bBuilder.setNotificationSound(Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        PushManager.setNotificationBuilder(getReactApplicationContext(), 1, bBuilder);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TMS_CHANNEL";
            String description = "CHANNEL FOR TMS";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default_channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getReactApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //函数执行状态返回
    public void sendMsg(String title,String description,String customContentString,String type){


        WritableMap params = Arguments.createMap();
        params.putString("title",title);
        params.putString("description",description);
        params.putString("customContentString",customContentString);

        if(type.equals(DidReceiveMessage)){
            sendEvent(getReactApplicationContext(), DidReceiveMessage, params);
        }else{
            sendEvent(getReactApplicationContext(), DidOpenMessage, params);
        }


    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    //恢复推送
    @ReactMethod
    public void getChannelId(Promise promise){
        try {

            promise.resolve(BGBaiDuPushModule.channelId);

        } catch (IllegalViewOperationException e) {

            promise.reject(e.getMessage());

        }
    }

    @ReactMethod
    public void getUserId(Promise promise){
        try {

            promise.resolve(BGBaiDuPushModule.userId);

        } catch (IllegalViewOperationException e) {

            promise.reject(e.getMessage());

        }
    }

    @ReactMethod
    public void testPrint(String name) {
        Log.i("momomo", name);
    }


}
