package com.aaronlee.bestfood.lib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * 기타 라이브러리 클래스
 */
public class EtcLib {
    public final String TAG = EtcLib.class.getSimpleName();

    private volatile static EtcLib instance;

    public static EtcLib getInstance() {
        if (instance == null) {
            synchronized (EtcLib.class) {
                if (instance == null) {
                    instance = new EtcLib();
                }
            }
        }
        return instance;
    }

    /**
     * 현재 기기의 전화번호를 반환한다. (+82가 붙은 전화번호로 반환)
     * @param context 컨텍스트 객체
     * @return 전화번호 문자열
     */
    public String getPhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = null;

        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            number = tm.getLine1Number();
        }

        if (number != null && !number.equals("") && number.length() >= 8) {
            if (Locale.getDefault().getCountry().equals("JP")) {
                if (number.startsWith("81")) {
                    number = "+" + number;
                }

                if (number.startsWith("0")) {
                    number = "+81" + number.substring(1, number.length());
                }
            }

            MyLog.d(TAG, "number " + number);

        } else {
            number = getDeviceId(context);
        }

        return number;
    }

    /**
     * 전화번호가 없을 경우 기기 아이디를 반환한다.
     * @param context 컨텍스트 객체
     * @return 기기 아이디 문자열
     */
    private String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        String tmDevice = null;
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            tmDevice = tm.getDeviceId();
        }

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String serial = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) serial = Build.SERIAL;
        if (tmDevice != null) return "01" + tmDevice;
        if (androidId != null) return "02" + androidId;
        if (serial != null) return "03" + serial;

        return null;

    }
}
