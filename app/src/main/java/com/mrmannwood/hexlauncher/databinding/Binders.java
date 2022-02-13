package com.mrmannwood.hexlauncher.databinding;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

    @BindingAdapter("android:layout_alignParentStart")
    public static void alignParentStart(View view, boolean alignParentLeft) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (alignParentLeft) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        }
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_alignParentEnd")
    public static void alignParentEnd(View view, boolean alignParentRight) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (alignParentRight) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_toStartOf")
    public static void toStartOf(View view, Integer viewId) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (viewId != null) {
            layoutParams.addRule(RelativeLayout.START_OF, viewId);
        }
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_toEndOf")
    public static void toEndOf(View view, Integer viewId) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (viewId != null) {
            layoutParams.addRule(RelativeLayout.END_OF, viewId);
        }
        view.setLayoutParams(layoutParams);
    }
}
