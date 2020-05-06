package com.non.sleep.naver.android.src.menu_list.interfaces;

import com.non.sleep.naver.android.src.recommend.models.ObjectResponse;

import java.util.ArrayList;

public interface MenuListView {

    void retrofitFailure(String message);

    void postWordPositiveSuccess();

    void postWordNegativeSuccess();

    void postWordConfirmName(ObjectResponse objectResponse);

    void postWordConfirmCategory(ArrayList<ObjectResponse> arrayList, String word);
}
