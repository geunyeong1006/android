package com.cyberone.android.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cyberone.android.listitem.NewsItem;
import com.cyberone.android.R;

import java.util.ArrayList;

public class NewsAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<NewsItem> news;

    public NewsAdapter(Context context, ArrayList<NewsItem> data) {
        mContext = context;
        news = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public NewsItem getItem(int i) {
        return news.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.news, null);
        }

        TextView newsTitle = (TextView) view.findViewById(R.id.textViewNewsTitle);

        newsTitle.setText(news.get(i).getNewsTitle());

        return view;
    }
}
