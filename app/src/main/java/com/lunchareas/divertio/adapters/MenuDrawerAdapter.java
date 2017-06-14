package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;

import java.util.List;

public class MenuDrawerAdapter extends BaseAdapter {

    private String[] titleList;
    private int[] iconIdList;
    private LayoutInflater layoutInflater;
    private RelativeLayout menuItemLayout;
    private Activity activity;

    public MenuDrawerAdapter(Activity activity, String[] titles, int[] icons) {
        this.titleList = titles;
        this.iconIdList = icons;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return titleList.length;
    }

    // Not used
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    // Not used
    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {

        // Get the parts
        menuItemLayout = (RelativeLayout) layoutInflater.inflate(R.layout.list_item_menu_drawer, parentView, false);
        ImageView imageView = (ImageView) menuItemLayout.findViewById(R.id.menu_icon);
        TextView textView = (TextView) menuItemLayout.findViewById(R.id.menu_title);

        // Set the parts equal to the corresponding part
        Drawable icon = activity.getResources().getDrawable(iconIdList[position]);
        String title = titleList[position];
        imageView.setImageDrawable(icon);
        textView.setText(title);

        // Set position as tag
        menuItemLayout.setTag(position);
        return menuItemLayout;
    }
}
