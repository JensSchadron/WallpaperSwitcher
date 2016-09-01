package be.schadron.wallpaperswitcher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import be.schadron.wallpaperswitcher.R;

/**
 * Created by jenss on 1/09/2016.
 */
public class TimeSettingsAdapter extends ArrayAdapter<String> {
    private static class ViewHolder {
        private TextView title;
        private TextView subtitle;
    }

    public TimeSettingsAdapter(Context context, ArrayList<String> arrayList) {
        super(context, R.layout.listview_timesettings_item, arrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_timesettings_item, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.titel);
            viewHolder.subtitle = (TextView) convertView.findViewById(R.id.subtitel);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(getItem(position).split("-")[0]);
        viewHolder.subtitle.setText(getItem(position).split("-")[1]);
        return convertView;
    }
}
