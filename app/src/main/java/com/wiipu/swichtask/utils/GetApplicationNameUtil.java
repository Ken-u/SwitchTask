package com.wiipu.swichtask.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Ken~Jc on 2016/4/3.
 */
public class GetApplicationNameUtil {
    /**
     * 通过包名获取应用程序的名称。
     * @param context
     *            Context对象。
     * @param packageName
     *            包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static String getProgramNameByPackageName(Context context,
                                                     String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }
}
