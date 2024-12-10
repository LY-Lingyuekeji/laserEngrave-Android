package in.co.gorest.grblcontroller;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.orm.SugarApp;
import com.zhy.http.okhttp.OkHttpUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.wandersnail.ble.EasyBLE;
import cn.wandersnail.ble.ScanConfiguration;
import cn.wandersnail.ble.ScannerType;
import cn.wandersnail.commons.base.AppHolder;
import cn.wandersnail.commons.poster.ThreadMode;
import es.dmoral.toasty.Toasty;
import in.co.gorest.grblcontroller.service.BLEService;
import in.co.gorest.grblcontroller.util.NettyClient;
import okhttp3.OkHttpClient;

public class GrblController extends SugarApp  {


    // 单例模式： GrblController实例
    private static GrblController grblController;
    public static List<Activity> Activitylist = new ArrayList();
    private static ExecutorService FULL_TASK_EXECUTOR;

    /**
     * 获取单例实例的方法
     */
    public static synchronized GrblController getInstance() {
        GrblController instance = grblController;
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("Application not created yet!");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化单例实例
        grblController = this;

        // 启用矢量图支持，确保在应用中可以正确显示矢量图形
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // 初始化 Iconify 以支持 FontAwesome 图标
        Iconify.with(new FontAwesomeModule());
        // 配置 Toasty 库
        Toasty.Config.getInstance()
                .tintIcon(true)  // 设置 Toast 图标颜色
                .allowQueue(true)  // 允许 Toast 排队显示
                .apply();  // 应用配置

        AppHolder.initialize(this);
        EasyBLE build = EasyBLE.getBuilder().setScanConfiguration(new ScanConfiguration().setScanSettings(new ScanSettings.Builder().setScanMode(1).build()).setScanPeriodMillis(15000).setAcceptSysConnectedDevice(true).setOnlyAcceptBleDevice(true)).setObserveAnnotationRequired(false).setMethodDefaultThreadMode(ThreadMode.BACKGROUND).setScannerType(ScannerType.LE).build();
        build.setLogEnabled(false);
        build.initialize(this);
        BLEService.Start(this);
        OkHttpUtils.initClient(new OkHttpClient.Builder().connectTimeout(600L, TimeUnit.SECONDS).readTimeout(600L, TimeUnit.SECONDS).build());


        grblController.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() { // from class: makerbase.com.mkslaser.AppContext.1
            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityCreated(Activity activity, Bundle bundle) {
                Log.i("onActivityCreated------", "---------");
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityStarted(Activity activity) {
                Log.i("onActivityStarted------", "---------");
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityResumed(Activity activity) {
                Log.i("onActivityResumed------", "---------");
                NettyClient.getInstance().setIsBackground(false);
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityPaused(Activity activity) {
                Log.i("onActivityPaused------", "---------");
                NettyClient.getInstance().setIsBackground(true);
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityStopped(Activity activity) {
                Log.i("onActivityStopped------", "---------");
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                Log.i("SaveInstanceSt-", "---------");
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityDestroyed(Activity activity) {
                Log.i("onDestroyed------", "---------");
            }
        });
    }




    public static ExecutorService getPrintTask() {
        if (FULL_TASK_EXECUTOR == null) {
            FULL_TASK_EXECUTOR = Executors.newCachedThreadPool();
        }
        return FULL_TASK_EXECUTOR;
    }

    public static void clearBindPhone() {
        Iterator<Activity> it = Activitylist.iterator();
        while (it.hasNext()) {
            it.next().finish();
        }
        Activitylist.clear();
    }

}
