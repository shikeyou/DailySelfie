package com.ksl.dailyselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SelfieListViewAdapter extends BaseAdapter {

    private List<Selfie> mList = new ArrayList<Selfie>();

    private Context mContext;
    private LayoutInflater inflater = null;

    public SelfieListViewAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        //inflate new view if not recycled
        if (null == view) {
            view = inflater.inflate(R.layout.listitem_selfie, viewGroup, false);
        }

        //get the Selfie instance
        Selfie selfie = (Selfie)getItem(i);

        //load thumbnail Bitmap
        ImageView imageView = (ImageView)view.findViewById(R.id.listitem_selfie_imageView);
        Bitmap thumbnail = BitmapFactory.decodeFile(selfie.getThumbnailFile().toString());
        imageView.setImageBitmap(thumbnail);

        //set text to filename
        TextView textView = (TextView)view.findViewById(R.id.listitem_selfie_textView);
        textView.setText(selfie.getImageFile().getName());

        return view;
    }

    public void add(Selfie selfie) {
        mList.add(selfie);
        notifyDataSetChanged();
    }

    public List<Selfie> getAllItems() {
        return mList;
    }

    public void setAllItems(List<Selfie> list) {
        if (list != null) {
            mList = list;
            notifyDataSetChanged();
        }
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }
}
