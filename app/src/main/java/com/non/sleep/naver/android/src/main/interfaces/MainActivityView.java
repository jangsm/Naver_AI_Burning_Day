package com.non.sleep.naver.android.src.main.interfaces;

import java.io.InputStream;

public interface MainActivityView {

    void validateSuccess(String text);

    void validateFailure(String message);

    void cpvSuccess(InputStream is);
}
