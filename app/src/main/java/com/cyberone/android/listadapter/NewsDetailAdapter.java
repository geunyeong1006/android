package com.cyberone.android.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cyberone.android.listitem.NewsDetailItem;
import com.cyberone.android.R;

import java.util.ArrayList;

public class NewsDetailAdapter extends BaseAdapter {
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<NewsDetailItem> news;

    public NewsDetailAdapter(Context context, ArrayList<NewsDetailItem> data) {
        mContext = context;
        news = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public NewsDetailItem getItem(int i) {
        return news.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.newsdetail, null);
        }

        TextView regDtime = (TextView) view.findViewById(R.id.textViewRegDtime);
        TextView bbsTit = (TextView) view.findViewById(R.id.textViewBbsTit);

        regDtime.setText(news.get(i).getRegDtime());
        bbsTit.setText(news.get(i).getBbsTit());

        return view;
    }
}
