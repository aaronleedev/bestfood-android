package com.aaronlee.bestfood;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronlee.bestfood.item.MemberInfoItem;
import com.aaronlee.bestfood.lib.EtcLib;
import com.aaronlee.bestfood.lib.MyLog;
import com.aaronlee.bestfood.lib.MyToast;
import com.aaronlee.bestfood.lib.StringLib;
import com.aaronlee.bestfood.remote.RemoteService;
import com.aaronlee.bestfood.remote.ServiceGenerator;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 사용자 프로필을 설정할 수 있는 Activity
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    Context mContext;

    ImageView mProfileIconImage;
    ImageView mProfileIconChangeImage;
    EditText mNameEdit;
    EditText mSextypeEdit;
    EditText mBirthdayEdit;
    EditText mPhoneEdit;

    MemberInfoItem mCurrentItem;

    /**
     * Activity를 생성하고 화면을 구성한다.
     * @param savedInstanceState Activity가 새로 생성되었을 경우, 이전 상태 값을 가지는 객체
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mContext = this;

        mCurrentItem = ((MyApp)getApplication()).getMemberInfoItem();

        setToolbar();
        setView();
    }

    /**
     * 사용자 정보를 기반으로 Profile Icon을 설정한다.
     */
    @Override
    protected void onResume() {
        super.onResume();

        MyLog.d(TAG, RemoteService.MEMBER_ICON_URL + mCurrentItem.memberIconFilename);

        if (StringLib.getInstance().isBlank(mCurrentItem.memberIconFilename)) {
            Picasso.with(this).load(R.drawable.ic_person).into(mProfileIconImage);
        } else {
            Picasso.with(this).load(RemoteService.MEMBER_ICON_URL + mCurrentItem.memberIconFilename).into(mProfileIconImage);
        }
    }

    /**
     * Activity Toolbar를 설정
     */
    private void setToolbar() {
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.profile_setting);
        }
    }

    /**
     * Activity 화면을 설정
     */
    private void setView() {
        mProfileIconImage = (ImageView)findViewById(R.id.profile_icon);
        mProfileIconImage.setOnClickListener(this);

        mProfileIconChangeImage = (ImageView)findViewById(R.id.profile_icon_change);
        mProfileIconChangeImage.setOnClickListener(this);

        mNameEdit = (EditText)findViewById(R.id.profile_name);
        mNameEdit.setText(mCurrentItem.name);

        mSextypeEdit = (EditText)findViewById(R.id.profile_sextype);
        mSextypeEdit.setText(mCurrentItem.sextype);
        mSextypeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSexTypeDialog();
            }
        });

        mBirthdayEdit = (EditText)findViewById(R.id.profile_birth);
        mBirthdayEdit.setText(mCurrentItem.birthday);
        mBirthdayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBirthdayDialog();
            }
        });

        String phoneNumber = EtcLib.getInstance().getPhoneNumber(mContext);

        mPhoneEdit = (EditText)findViewById(R.id.profile_phone);
        mPhoneEdit.setText(phoneNumber);

        TextView phoneStateEdit = (TextView)findViewById(R.id.phone_state);
        if (phoneNumber.startsWith("0")) {
            phoneStateEdit.setText("(" + getResources().getString(R.string.device_number) + ")");
        } else {
            phoneStateEdit.setText("(" + getResources().getString(R.string.phone_number) + ")");
        }
    }

    /**
     * Sex Type을 선택한 수 있는 Dialog를 출력
     */
    private void setSexTypeDialog() {
        final String[] sexTypes = new String[2];
        sexTypes[0] = getResources().getString(R.string.sex_man);
        sexTypes[1] = getResources().getString(R.string.sex_woman);

        new AlertDialog.Builder(this).setItems(sexTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i >= 0) {
                    mSextypeEdit.setText(sexTypes[i]);
                }
                dialogInterface.dismiss();
            }
        }).show();
    }

    /**
     * Birthday를 선택할 수 있는 Dialog를 출력
     */
    private void setBirthdayDialog() {
        GregorianCalendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String myMonth;
                if (month + 1 < 10) {
                    myMonth = "0" + (month + 1);
                } else {
                    myMonth = "" + month + 1;
                }

                String myDay;
                if (day < 10) {
                    myDay = "0" + day;
                } else {
                    myDay = "" + day;
                }

                String date = year + " " + myMonth + " " + myDay;
                mBirthdayEdit.setText(date);
            }
        }, year, month, day).show();
    }

    /**
     * 오른쪽 상단 Options Menu를 구성
     * @param menu 메뉴 객체
     * @return 메뉴를 보여준다면 true, 보여주지 않으면 false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_submit, menu);
        return true;
    }

    /**
     * 왼쫀 상단 Home Menu와 오른쪽 상단 Options Menu를
     * 클릭했을 때의 동작을 지정
     * @param item Menu Item 객체
     * @return Menu Item을 처리 한다면 true, 그렇지 않는다면 false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close();
                break;
            case R.id.action_submit:
                save();
                break;
        }

        return true;
    }

    /**
     * 사용자가 입력한 정보를 MemberInfoItem 객체에 저장해서 반환
     * @return MemberInfoItem 객체
     */
    private MemberInfoItem getMemberInfoItem() {
        MemberInfoItem item = new MemberInfoItem();
        item.phone = EtcLib.getInstance().getPhoneNumber(mContext);
        item.name = mNameEdit.getText().toString();
        item.sextype = mSextypeEdit.getText().toString();
        item.birthday = mBirthdayEdit.getText().toString();

        return item;
    }

    /**
     * 기존 사용자 정보와 새로 입력한 사용자 정보를 비교해 변경되었는지 파악
     * @param newItem 새로 입력한 MemberInfoItem 객체
     * @return 변경되었다면 true, 변경되지 않았다면 false
     */
    private boolean isChanged(MemberInfoItem newItem) {
        if (newItem.name.trim().equals(mCurrentItem.name)
                && newItem.sextype.trim().equals(mCurrentItem.sextype)
                && newItem.birthday.trim().equals(mCurrentItem.birthday)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 사용자 정보에 이름을 입력했는지를 확인
     * @param newItem 새로 입력한 MemberInfoItem 객체
     * @return 입력하지 않았다면 true, 입력했다면 false
     */
    private boolean isNoName(MemberInfoItem newItem) {
        if (StringLib.getInstance().isBlank(newItem.name)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Activity를 종료하기 전에 변경 유무를 확인하고
     * 변경사항이 있다면 저장할지 Dialog를 출력하고 없다면 Activity를 종료
     */
    private void close() {
        MemberInfoItem newItem = getMemberInfoItem();

        if (!isChanged(newItem) && !isNoName(newItem)) {
            finish();
        } else if (isNoName(newItem)) {
            MyToast.s(mContext, R.string.name_need);
        } else {
            new AlertDialog.Builder(this).setTitle(R.string.change_save)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            save();
                        }
                    })
                    .setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
        }
    }

    /**
     * 사용자가 입력한 정보를 서버에 전달
     */
    private void save() {
        final MemberInfoItem newItme = getMemberInfoItem();

        if (!isChanged(newItme)) {
            MyToast.s(this, R.string.no_change);
            finish();
            return;
        }

        MyLog.d(TAG, "insertItem " + newItme.toString());

        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertMemberInfo(newItme);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String seq = response.body();
                    try {
                        mCurrentItem.seq = Integer.parseInt(seq);
                        if (mCurrentItem.seq == 0) {
                            MyToast.s(mContext, R.string.member_insert_fail_message);
                            return;
                        }
                    } catch (Exception e) {
                        MyToast.s(mContext, R.string.member_insert_fail_message);
                        return;
                    }
                    mCurrentItem.name = newItme.name;
                    mCurrentItem.sextype = newItme.sextype;
                    mCurrentItem.birthday = newItme.birthday;
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyToast.s(mContext, R.string.member_insert_fail_message);
            }
        });
    }

    /**
     * 뒤로가기 버튼을 눌렀을 때 close() 메소드를 호출
     */
    @Override
    public void onBackPressed() {
        close();
    }

    /**
     * 프로필 아이콘이나 프로필 변경 아이콘을 눌렀을 때
     * startProfileIconChange() 메소드를 호출
     * @param view 누른 뷰 객체
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_icon || view.getId() == R.id.profile_icon_change) {
            startProfileIconChange();
        }
    }

    /**
     * 프로필 아이콘을 변경할 수 있도록 ProfileIconActivity를 실행
     */
    private void startProfileIconChange() {
        Intent intent = new Intent(this, ProfileIconActivity.class);
        startActivity(intent);
    }
}
