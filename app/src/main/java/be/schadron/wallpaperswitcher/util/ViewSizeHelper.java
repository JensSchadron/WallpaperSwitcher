package be.schadron.wallpaperswitcher.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by jenss on 7/05/2016.
 */
public class ViewSizeHelper {
    // http://stackoverflow.com/a/17765520
    public static void setListViewSize(ListView list) {
        ListAdapter myListAdapter = list.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }

        //set listAdapter in loop for getting final size
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(list.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < myListAdapter.getCount(); i++) {
            view = myListAdapter.getView(i, view, list);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }

        //setting listview item in adapter
        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = totalHeight + (list.getDividerHeight() * (myListAdapter.getCount() - 1));
        list.setLayoutParams(params);
    }

    public static void setGridViewSize(GridView grid) {
        ListAdapter myListAdapter = grid.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }

        //set listAdapter in loop for getting final size
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(grid.getWidth()/grid.getNumColumns(), View.MeasureSpec.AT_MOST);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < ((myListAdapter.getCount() / grid.getNumColumns()) + myListAdapter.getCount() % grid.getNumColumns()); i++) {
            view = myListAdapter.getView(i, view, grid);
            if (i == 0) {
                view.setLayoutParams(new GridView.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += view.getMeasuredHeight();
        }

        //setting listview item in adapter
        ViewGroup.LayoutParams params = grid.getLayoutParams();
        params.height = totalHeight + (grid.getHorizontalSpacing() * (((myListAdapter.getCount() - 1) / grid.getNumColumns()) + (myListAdapter.getCount() % grid.getNumColumns())));
        grid.setLayoutParams(params);
    }
}
