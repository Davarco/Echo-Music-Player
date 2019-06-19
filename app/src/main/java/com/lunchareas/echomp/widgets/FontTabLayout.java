package com.lunchareas.echomp.widgets;


import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lunchareas.echomp.utils.Constants;


public final class FontTabLayout extends TabLayout {

    private Typeface mTypeface;

    public FontTabLayout(Context context) {
        super(context);
        init();
    }

    public FontTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTypeface = Typeface.createFromAsset(getContext().getAssets(), Constants.FONT_PATH);
    }

    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        super.addTab(tab, position, setSelected);

        ViewGroup mainView = (ViewGroup) getChildAt(0);
        ViewGroup tabView = (ViewGroup) mainView.getChildAt(tab.getPosition());
        int tabChildCount = tabView.getChildCount();
        for (int i = 0; i < tabChildCount; i++) {
            View tabViewChild = tabView.getChildAt(i);
            if (tabViewChild instanceof TextView) {
                ((TextView) tabViewChild).setTypeface(mTypeface, Typeface.NORMAL);
            }
        }
    }
}