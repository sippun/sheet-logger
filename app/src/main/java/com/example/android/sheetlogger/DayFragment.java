package com.example.android.sheetlogger;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Joel on 3/20/2017.
 */

/**
 * Encapsulates fetching the items for the day and displaying them in a {@link ListView} layout.
 */
public class DayFragment extends Fragment {

    private ArrayAdapter<String> mDayAdapter;

    public DayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
