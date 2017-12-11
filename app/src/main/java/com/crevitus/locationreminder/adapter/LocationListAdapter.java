package com.crevitus.locationreminder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.model.LocationListItem;

import java.util.List;

public class LocationListAdapter extends BaseAdapter {

    private List<LocationListItem> _listData;
    private Context _context;
    ColorGenerator _generator = ColorGenerator.MATERIAL;

    public LocationListAdapter(Context context, List<LocationListItem> listData)
    {
        _listData = listData;
        _context = context;
    }

    @Override
    public int getCount() {
        return _listData.size();
    }

    @Override
    public Object getItem(int position) {
        return _listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(int position)
    {
        _listData.remove(position);
    }

    public void insert(int position, LocationListItem item)
    {
        _listData.add(position, item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationListItem itemData = (LocationListItem) getItem(position);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.loc_list_item, null);
        }

        TextView txtLocTitle = (TextView) convertView.findViewById(R.id.txtLocAddress);
        TextView txtInfo = (TextView) convertView.findViewById(R.id.txtExtraInfo);

        if(itemData != null) {
            //set random color on icon
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(itemData.getAddress().substring(0,1), _generator.getRandomColor());
            ImageView image = (ImageView) convertView.findViewById(R.id.imgLstIcon);
            image.setImageDrawable(drawable);
            txtLocTitle.setText(itemData.getAddress());
            txtInfo.setText(itemData.getLat() + ", " + itemData.getLng());
        }
        return convertView;
    }
}