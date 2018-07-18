package com.aaronlee.bestfood;

import android.app.Application;

import com.aaronlee.bestfood.item.MemberInfoItem;

/**
 * App 전역에서 정보를 저장하고 반환해 사용할 수 있는 클래스
 */
public class MyApp extends Application {
    private MemberInfoItem memberInfoItem;

    public MemberInfoItem getMemberInfoItem() {
        if (memberInfoItem == null) memberInfoItem = new MemberInfoItem();
        return memberInfoItem;
    }

    public void setMemberInfoItem(MemberInfoItem item) {
        this.memberInfoItem = item;
    }
}
