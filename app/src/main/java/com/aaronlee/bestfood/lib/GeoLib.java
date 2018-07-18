package com.aaronlee.bestfood.lib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import com.aaronlee.bestfood.item.GeoItem;

/**
 * 위치 정보와 관련된 라이브러리 클래스
 */
public class GeoLib {
    public final String TAG = GeoLib.class.getSimpleName();

    private volatile static GeoLib instance;

    public static GeoLib getInstance() {
        if (instance == null) {
            synchronized (GeoLib.class) {
                if (instance == null) {
                    instance = new GeoLib();
                }
            }
        }
        return instance;
    }

    /**
     * 사용자의 현재 위도, 경도를 GeoItem 객체에 설정한다.
     * 실제로는 최근 측정된 위치 정보다.
     * @param context 컨텍스트 객체
     */
    public void setLastKnownLocation(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if (location != null) {
            GeoItem.knownLatitude = location.getLatitude();
            GeoItem.knownLongitude = location.getLongitude();
        } else {
            // 일본 도쿄 오모리로 기본 설정
            GeoItem.knownLatitude = 35.5861306;
            GeoItem.knownLongitude = 139.7256507;
        }
    }
}
