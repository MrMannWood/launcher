package com.mrmannwood.hexlauncher.databinding;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    @BindingAdapter("visibleOrGone")
    public static void setVisibleOrGone(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("bold")
    public static void setBold(TextView view, boolean bold) {
        view.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
    }
}
