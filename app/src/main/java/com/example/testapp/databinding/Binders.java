package com.example.testapp.databinding;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;

public class Binders {

    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, float height) {
        setLayoutHeight(view, (int) height);
    }

    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }
}
