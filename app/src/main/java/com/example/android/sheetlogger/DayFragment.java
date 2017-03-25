package com.example.android.sheetlogger;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mDayAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_day, // The name of the layout ID.
                        R.id.list_item_day_textview, // The ID of the textview to populate.
                        new ArrayList<String>());

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_today);
        listView.setAdapter(mDayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Integer[] pos = new Integer[1];
                pos[0] = position;
                new MakeUpdateTask(MainActivity.getCredential()).execute(pos);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        new MakeRequestTask(MainActivity.getCredential()).execute();
    }

    /**
     * An asynchronous task to handle updating the spreadsheet
     * TODO merge to use one AsyncTask (?)
     */
    private class MakeUpdateTask extends AsyncTask<Integer, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeUpdateTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Sheet Logger")
                    .build();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                updateSheet(params[0]);
                return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private void updateSheet(int position) throws IOException {
            String spreadsheetId = "1JXj2kexyTpmym_WZ52zOGkVOsgzFxf3lB1jNxqjSXNE";
            // Get column by incrementing base char representing column with
            // position of item clicked in list
            // TODO change base column based on user
            // TODO get column as part of a class representing the item
            String column = String.valueOf(
                    Character.toChars((int)('B') + position));
            // Get row (currently offset of date - 2)
            // TODO find row offset by comparing date in col A to today's date
            Calendar c = Calendar.getInstance();
            int date = c.get(Calendar.DAY_OF_MONTH);
            int row = date - 2;
            String cell = column + row;
            String range = "Tracking Log!" + cell;

            // Create values to update spreadsheet with
            // TODO get and use data validation source
            ValueRange vals = new ValueRange();
            List<List<Object>> inputs = new ArrayList<>();
            List<Object> in = new ArrayList<>();
            in.add("✔");
            vals.setValues(inputs);
            mService.spreadsheets().values().update(spreadsheetId, range, vals).execute();
        }
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Sheet Logger")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            String spreadsheetId = "1JXj2kexyTpmym_WZ52zOGkVOsgzFxf3lB1jNxqjSXNE";
            // Access current day's tasks
//            Calendar c = Calendar.getInstance();
//            int date = c.get(Calendar.DAY_OF_MONTH);
//            int row = date - 2;
//            String range = "Tracking Log!" + "B" + row + ":F";
            String range = "Tracking Log!B2:G2";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (Object s : values.get(0)) {
                    results.add(s.toString());
                }
            }
            return results;
        }

//        @Override
//        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
//        }

        @Override
        protected void onPostExecute(List<String> output) {
//            mProgress.hide();
            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
            } else {
//                output.add(0, "Data retrieved using the Google Sheets API:");
//                mOutputText.setText(TextUtils.join("\n", output));
                mDayAdapter.clear();
                for (String task : output) {
                    mDayAdapter.add(task);
                }
                mDayAdapter.notifyDataSetChanged();
            }
        }

//        @Override
//        protected void onCancelled() {
//            mProgress.hide();
//            if (mLastError != null) {
//                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
//                    showGooglePlayServicesAvailabilityErrorDialog(
//                            ((GooglePlayServicesAvailabilityIOException) mLastError)
//                                    .getConnectionStatusCode());
//                } else if (mLastError instanceof UserRecoverableAuthIOException) {
//                    startActivityForResult(
//                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
//                            MainActivity.REQUEST_AUTHORIZATION);
//                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage());
//                }
//            } else {
//                mOutputText.setText("Request cancelled.");
//            }
//        }
    }
}
