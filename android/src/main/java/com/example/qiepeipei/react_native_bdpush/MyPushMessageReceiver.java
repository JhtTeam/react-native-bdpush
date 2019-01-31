package com.example.qiepeipei.react_native_bdpush;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushMessageReceiver;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by qiepeipei on 16/8/13.
 */
public class MyPushMessageReceiver extends PushMessageReceiver {

    private interface ReactContextInitListener {
        void contextInitialized(ReactApplicationContext context);
    }

    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {

        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        Log.d("百度推送", responseString);

        BGBaiDuPushModule.channelId = channelId;
        BGBaiDuPushModule.userId = userId;
    }

    @Override
    public void onUnbind(Context context, int errorCode, String s) {
        Log.d("百度推送", "onUnbind");
    }

    @Override
    public void onSetTags(Context context, int errorCode, List<String> list, List<String> list1, String s) {
        Log.d("百度推送", "onSetTags");


    }

    @Override
    public void onDelTags(Context context, int errorCode, List<String> list, List<String> list1, String s) {
        Log.d("百度推送", "onDelTags");


    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {
        Log.d("百度推送", "onListTags");
    }

    //接收透传消息
    /*
    *   context 上下文
        message 推送的消息
        customContentString 自定义内容，为空或者json字符串
    * */
    @Override
    public void onMessage(final Context context, final String message, final String customContentString) {
        String responseString = "onMessage message=" + message + " customContentString="
                + customContentString;
        Log.d("百度推送", responseString);
        final JSONObject data = getPushData(message);
        String title = message;
        String description = message;
        try {
            title = (data != null) ? data.getString("title") : message;
            description = (data != null) ? data.getString("description") : message;
        } catch (JSONException e) {
            Log.e("百度推送", e.toString());
        }
        if (!isAppIsInBackground(context)) {
            BGBaiDuPushModule.myPush.sendMsg(title, description, customContentString, BGBaiDuPushModule.DidReceiveMessage);
        } else {
            Context applicationContext = context.getApplicationContext();
            final String _title = message;
            final String _description = message;
            handleEvent(applicationContext, new ReactContextInitListener() {
                @Override
                public void contextInitialized(ReactApplicationContext context) {
                    BGBaiDuPushModule.myPush.sendMsg(_title, _description, customContentString, BGBaiDuPushModule.DidReceiveMessage);
                }
            });
        }
    }

    private JSONObject getPushData(String dataString) {
        try {
            return new JSONObject(dataString);
        } catch (Exception e) {
            return null;
        }
    }

    private void handleEvent(final Context applicationContext, final ReactContextInitListener reactContextInitListener) {
        // We need to run this on the main thread, as the React code assumes that is true.
        // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
        // "Can't create handler inside thread that has not called Looper.prepare()"
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Construct and load our normal React JS code bundle
                if (applicationContext instanceof ReactApplication) {
                    ReactInstanceManager mReactInstanceManager = ((ReactApplication) applicationContext).getReactNativeHost().getReactInstanceManager();
                    com.facebook.react.bridge.ReactContext context = mReactInstanceManager.getCurrentReactContext();
                    // If it's constructed, send a notification
                    if (context != null) {
                        reactContextInitListener.contextInitialized((ReactApplicationContext) context);
                    } else {
                        // Otherwise wait for construction, then send the notification
                        mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                            public void onReactContextInitialized(ReactContext context) {
                                reactContextInitListener.contextInitialized((ReactApplicationContext) context);
                            }
                        });
                        if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
                            // Construct it in the background
                            mReactInstanceManager.createReactContextInBackground();
                        }
                    }
                }
            }
        });
    }

    /*接收通知点击的函数

    *   context 上下文
        title 推送的通知的标题
        description 推送的通知的描述
        customContentString 自定义内容，为空或者json字符串
    * */
    @Override
    public void onNotificationClicked(final Context context, final String title, final String description, final String customContentString) {

        Log.d("百度推送", "onNotificationClicked");
        String packageName = context.getApplicationContext().getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(launchIntent);
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAppIsInBackground(context)) {
                    BGBaiDuPushModule.myPush.sendMsg(title, description, customContentString, BGBaiDuPushModule.DidOpenMessage);
                }
            }
        }, 1000);
    }

    /*接收通知到达的函数
    *
    *   context 上下文
        title 推送的通知的标题
        description 推送的通知的描述
        customContentString 自定义内容，为空或者json字符串

    * */

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {

        Log.d("百度推送", "onNotificationArrived");
        if (!isAppIsInBackground(context)) {
            //发送通知
            BGBaiDuPushModule.myPush.sendMsg(title, description, customContentString, BGBaiDuPushModule.DidReceiveMessage);

        }
    }

    //判断是否在后台
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                //前台程序
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }


}
