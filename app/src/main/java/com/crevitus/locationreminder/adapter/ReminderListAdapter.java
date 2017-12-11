package com.crevitus.locationreminder.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.crevitus.locationreminder.R;
import com.crevitus.locationreminder.model.ReminderListItem;

import java.util.List;

public class ReminderListAdapter extends BaseAdapter {

    private List<ReminderListItem> _listData;
    private Context _context;
    ColorGenerator _generator = ColorGenerator.MATERIAL;

    public ReminderListAdapter(Context context, List<ReminderListItem> listData)
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

    public void insert(int position, ReminderListItem item)
    {
        _listData.add(position, item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReminderListItem itemData = (ReminderListItem) getItem(position);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.rem_list_item, null);
        }

        TextView txtRemTitle = (TextView) convertView.findViewById(R.id.txtRemTitle);
        TextView txtInfo = (TextView) convertView.findViewById(R.id.txtExtraInfo);
        TextView txtTime = (TextView) convertView.findViewById(R.id.txtTime);

        if(itemData != null) {
            //set random color on icon
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(itemData.getTitle().substring(0,1), _generator.getRandomColor());
            ImageView image = (ImageView) convertView.findViewById(R.id.imgLstIcon);
            image.setImageDrawable(drawable);
            txtRemTitle.setText(itemData.getTitle());
            //format preview text
            Spanned text;
            if (itemData.getNote().length() >= 45)
            {
                text = Html.fromHtml("<b>" + itemData.getAddress() + "</b> - " + itemData.getNote().substring(0, 44) + " ...");
            }
            else
            {
                text = Html.fromHtml("<b>" + itemData.getAddress() + "</b> - " + itemData.getNote());
            }
            txtInfo.setText(text);
            txtTime.setText(itemData.getDateTime());
        }
        return convertView;
    }
}