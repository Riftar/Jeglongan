package com.deviantart.bimbombash.jeglongan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by RIFTAR on 02/01/2016.
 */
public class CustomCommentAdapter extends ArrayAdapter<String> {

    public CustomCommentAdapter(Context context, String[] foods) {
        super(context, R.layout.commentrow, foods);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater commentInflater = LayoutInflater.from(getContext());
        View customView = commentInflater.inflate(R.layout.commentrow, parent, false);

        String namaComment = getItem(position);
        TextView nama = (TextView) customView .findViewById(R.id.namaComment);
        ImageView fotoComment = (ImageView) customView.findViewById(R.id.fotoComment);

        nama.setText(namaComment);
        fotoComment.setImageResource(R.drawable.foto);
        return customView;

    }}
