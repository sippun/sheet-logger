package com.example.android.sheetlogger;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Joel on 3/29/2017.
 */

public class ToDoAdapter extends ArrayAdapter<ToDoItem> {

    public ToDoAdapter(Context context, int resource, int textViewResourceId,
                       List<ToDoItem> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView myView =  (TextView) super.getView(position, convertView, parent);
        ToDoItem myItem = getItem(position);
        myView.setText(myItem.getName());
        if (myItem instanceof BoolItem && ((BoolItem) myItem).getDone()) {
            myView.setBackgroundColor(Color.DKGRAY);
        } else {
            myView.setBackgroundColor(Color.WHITE);
        }

        return myView;
    }
}
