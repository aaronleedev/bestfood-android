package com.aaronlee.bestfood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aaronlee.bestfood.item.MemberInfoItem;
import com.aaronlee.bestfood.lib.EtcLib;
import com.aaronlee.bestfood.lib.GeoLib;
import com.aaronlee.bestfood.lib.MyLog;
import com.aaronlee.bestfood.lib.RemoteLib;
import com.aaronlee.bestfood.lib.StringLib;
import com.aaronlee.bestfood.remote.RemoteService;
import com.aaronlee.bestfood.remote.ServiceGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 시작 Activity이며, 이 Activity에서 사용자 정보를 조회해서
 * MainActivity를 실행할지, ProfileActivity를 실행할지를 결정한다.
 */
public class IndexActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    /**
     * 레이아웃을 설정하고 인터넷에 연결되어 있는지를 확인한다.
     * 만약 인터넷에 연결되어 있지 않다면 showNoService() 메소드를 호출한다.
     * @param savedInstanceState Activity가 새로 생성되었을 경우에 이전 상태 값을 가지는 객체
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        context = this;

        if (!RemoteLib.getInstance().isConnected(context)) {
            showNoService();
            return;
        }
    }

    /**
     * 일정 시간(1.2초) 이후에 startTask() 메소드를 호출해서
     * 서버에서 사용자 정보를 조회한다.
     */
    @Override
    protected void onStart() {
        super.onStart();

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTask();
            }
        }, 1200);
    }

    /**
     * 현재 인터넷에 접속할 수 없기 때문에 서비스를 사용할 수 없다는 메시지와
     * 화면 종료 버튼을 보여준다.
     */
    private void showNoService() {
        TextView messageText = (TextView) findViewById(R.id.message);
        messageText.setVisibility(View.VISIBLE);

        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);
    }

    /**
     * 현재 폰의 전화번호와 동일한 사용자 정보를 조회할 수 있도록
     * selectMemberInfo() 메소드를 호출한다.
     * 그리고 setLastKnownLocation() 메소드를 호출해서 현재 위치 정보를 설정한다.
     */
    public void startTask() {
        String phone = EtcLib.getInstance().getPhoneNumber(this);
        selectMemberInfo(phone);
        GeoLib.getInstance().setLastKnownLocation(this);
    }

    /**
     * retrofit을 활용해서 서버로부터 사용자 정보를 조회한다.
     * 사용자 정보가 조회되었다면 setMemberInfoItem() 메소드를 호출하고,
     * 그렇지 않다면 goProfileActivity() 메소드를 호출한다.
     * @param phone phone의 전화번호
     */
    public void selectMemberInfo(String phone) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);
        Call<MemberInfoItem> call = remoteService.selectMemberInfo(phone);
        call.enqueue(new Callback<MemberInfoItem>() {
            @Override
            public void onResponse(Call<MemberInfoItem> call, Response<MemberInfoItem> response) {
                MemberInfoItem item = response.body();

                if (response.isSuccessful() && !StringLib.getInstance().isBlank(item.name)) {
                    MyLog.d(TAG, "success " + response.body().toString());
                    setMemberInfoItem(item);
                } else {
                    MyLog.d(TAG, "not success");
                    goProfileActivity(item);
                }
            }

            @Override
            public void onFailure(Call<MemberInfoItem> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    /**
     * 전달받은 MemberInfoItem을 Application 객체에 저장하고, startMain() 메소드를 호춯한다.
     * @param item 사용자 정보
     */
    private void setMemberInfoItem(MemberInfoItem item) {
        ((MyApp)getApplicationContext()).setMemberInfoItem(item);

        startMain();
    }

    /**
     * MainActivity를 실행하고 현재 Activity를 종료한다.
     */
    public void startMain() {
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    /**
     * 사용자 정보를 조회하지 못했다면 insertMemberPhone() 메소드를 통해
     * 전화번호를 서버에 저장하고, MainActivity를 실행한 후에 ProfileActivity를 실행하고,
     * 현재 Activity를 종료한다.
     * @param item 사용자 정보
     */
    private void goProfileActivity(MemberInfoItem item) {
        if (item == null || item.seq <= 0) {
            insertMemberPhone();
        }

        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        Intent intent2 = new Intent(this, ProfileActivity.class);
        startActivity(intent2);

        finish();
    }

    private void insertMemberPhone() {
        String phone = EtcLib.getInstance().getPhoneNumber(context);
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertMemberPhone(phone);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    MyLog.d(TAG, "success insert id " + response.body().toString());
                } else {
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    MyLog.d(TAG, "fail " + statusCode + errorBody.toString());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
            }
        });
    }
}
