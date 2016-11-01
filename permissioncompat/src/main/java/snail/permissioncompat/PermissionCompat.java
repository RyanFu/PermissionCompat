package snail.permissioncompat;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.annotation.annotation.OnGrantedListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: hzlishang
 * Data: 16/10/28 下午5:39
 * Des:
 * version:
 */
public class PermissionCompat {

    private static int sNextRequestCode;
    static final Map<Class<?>, OnGrantedListener<BasePermissionCompatActivity>> BINDERS = new LinkedHashMap<>();

    // 分批次请求权限
    public static void requestPermission(BasePermissionCompatActivity target, String[] permissions) {

        Class<?> targetClass = target.getClass();
        try {
            OnGrantedListener<BasePermissionCompatActivity> listener = findOnGrantedListenerForClass(targetClass, permissions);
            if (PermissionUtils.hasSelfPermissions(target, permissions)) {
                listener.onGranted(target, permissions);
            } else if (PermissionUtils.shouldShowRequestPermissionRationale(target, permissions)) {
                // 拒绝过，再次请求的时候,这个函数是否有必要，不在询问后，返回false，第一次返回false，
                //listener.onShowRationale(target, permissions);
                startRequest(target, listener, permissions);
            } else {
                startRequest(target, listener, permissions);
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to bind views for " + targetClass.getName(), e);
        }
    }

    private static OnGrantedListener<BasePermissionCompatActivity>
    findOnGrantedListenerForClass(Class<?> cls, String[] permissions)
            throws IllegalAccessException, InstantiationException {
        OnGrantedListener<BasePermissionCompatActivity> listener = BINDERS.get(cls);
        if (listener != null) {
            return listener;
        }
        String clsName = cls.getName();
        try {
            Class<?> listenerClass = Class.forName(clsName + "$OnGrantedListener");
            listener = (OnGrantedListener<BasePermissionCompatActivity>) listenerClass.newInstance();
        } catch (ClassNotFoundException e) {
            listener = findOnGrantedListenerForClass(cls.getSuperclass(), permissions);
        }
        BINDERS.put(cls, listener);
        return listener;
    }

    private static void startRequest(BasePermissionCompatActivity target, OnGrantedListener listener, final @NonNull String[] permissions) {
        target.setOnGrantedListener(listener);
        ActivityCompat.requestPermissions(target, permissions, 100);
    }

    private static int getNextRequestCode() {
        return sNextRequestCode++;
    }
}
