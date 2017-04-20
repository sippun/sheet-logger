package com.example.android.sheetlogger;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
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
    private ToDoAdapter mDayAdapter;

    // TODO create local list of items to use offline, then sync when available

    // A type to tell the AsyncTask what operation to perform on the API
    private enum Request {GET, SET}

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
                new ToDoAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_day, // The name of the layout ID.
                        R.id.list_item_day_textview, // The ID of the textview to populate.
                        new ArrayList<ToDoItem>());

        // Get a reference to the ListView, and attach this adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_today);
        listView.setAdapter(mDayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Integer[] pos = new Integer[1];
                pos[0] = position;
                new MakeRequestTask(MainActivity.getCredential(),
                        Request.SET).execute(pos);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        new MakeRequestTask(MainActivity.getCredential(),
                Request.GET)
                .execute(new Integer[1]);
    }

    /**
     * An asynchronous task that handles the Google Sheets API calls.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Integer, Void, List<ToDoItem>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private Request request;
        private final String spreadsheetId = "1JXj2kexyTpmym_WZ52zOGkVOsgzFxf3lB1jNxqjSXNE";

        MakeRequestTask(GoogleAccountCredential credential, Request req) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Sheet Logger")
                    .build();
            request = req;
        }

        /**
         * Background task to call Google Sheets API.
         * @param params If request type = SET, array of Integers denoting which
         *               columns to be written to.
         *               If request type = GET, not needed.
         */
        @Override
        protected List<ToDoItem> doInBackground(Integer... params) {
            try {
                if (request == Request.GET)
                    return getDataFromApi();
                else if (request == Request.SET)
                    updateSheet(params[0]);
                return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of tasks to be done for the day,
         * @return List of tasks as Strings
         * @throws IOException
         */
        private List<ToDoItem> getDataFromApi() throws IOException {
            // TODO create method to construct range string
            List<String> findRanges = new ArrayList<>();
            findRanges.add("Tracking Log!B1:K2");
            String row = Integer.toString(getTodayRow());
            String range = "Tracking Log!B" + row + ":K" + row;
            findRanges.add(range);

            BatchGetValuesResponse response = this.mService.spreadsheets().values()
                    .batchGet(spreadsheetId)
                    .setRanges(findRanges)
                    .setMajorDimension("ROWS")
                    .execute();
            List<ValueRange> ranges = response.getValueRanges();

            // Response containing names of the tasks
            List<List<Object>> tasks = ranges.get(0).getValues();
            List<Object> userNames = tasks.get(0);
            List<Object> taskNames = tasks.get(1);
            String user = "Sip"; // TODO make this a preference
            boolean found = false;
            while (!found) {
                if (userNames.get(0).toString().equals(user)) {
                    found = true;
                } else {
                    taskNames.remove(0);
                    userNames.remove(0);
                }
            }
            int taskIndex = 0;
            for (int i = 0; userNames.get(i).toString().equals("")
                    || userNames.get(i).toString().equals(user); i++, taskIndex++) {}
            taskIndex--; //Remove Weekly Goal column

            // Response containing data entered for today
            List<List<Object>> data = ranges.get(1).getValues();
            // TODO truncate data to match tasks

            List<ToDoItem> results = new ArrayList<>();
            if (tasks != null) {
                for (int i = 0; i < taskIndex; i++) {
                    Object n = taskNames.get(i); // Name of task
                    ToDoItem item = new ToDoItem(n.toString());
                    if (data != null && i < data.get(0).size()) {
                        Object d = data.get(0).get(i); // Data entered for today's task
                        // TODO remove hardcoded inputs
                        item.setDone(d.toString().equals("✔"));
                    } else {
                        item.setDone(false);
                    }
                    results.add(item);
                }
            }

            return results;
        }

        private void updateSheet(int position) throws IOException {
            // Get column by incrementing base char representing column with
            // position of item clicked in list
            // TODO change base column based on user
            // TODO get column as part of a class representing the item
            String column = String.valueOf(
                    Character.toChars((int)('B') + position));
            // B represents first column of user Sip's tasks on spreadsheet
            // Get row (currently offset of date - 2)
            int row = getTodayRow();
            String cell = column + row;
            String range = "Tracking Log!" + cell;

            // Create values to update spreadsheet with
            // TODO get and use data validation source
            ValueRange valueRange = new ValueRange();
            List<List<Object>> values = new ArrayList<>();
            List<Object> in = new ArrayList<>();
            // TODO remove hardcoded inputs
            in.add("✔");
            values.add(in);
            valueRange.setValues(values);
            valueRange.setMajorDimension("ROWS");
            mService.spreadsheets().values()
                    .update(spreadsheetId, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute();

            // Update local list item
            mDayAdapter.getItem(position).setDone(true);
        }

        /**
         * Get the row corresponding to today's date on the spreadsheet
          */
        private int getTodayRow() throws IOException{
            Calendar c = Calendar.getInstance();
            int todayDate = c.get(Calendar.DAY_OF_MONTH);
            int logDate = 0;

            String cell = "Tracking Log!A3";
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, cell)
                    .execute();
            List<List<Object>> values = response.getValues();
            String firstDay = "";
            if (values != null) {
                firstDay = values.get(0).toString();
            }
            // Parse date to get int for date of month
            String[] split = firstDay.split("/");
            logDate = Integer.parseInt(split[1]);
            int offset = 3 - logDate; // Dates start from row 3
            return todayDate + offset; // if today is the 27th, we want row 25
        }

//        @Override
//        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
//        }

        @Override
        protected void onPostExecute(List<ToDoItem> output) {
//            mProgress.hide();
            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
            } else {
//                output.add(0, "Data retrieved using the Google Sheets API:");
//                mOutputText.setText(TextUtils.join("\n", output));
                mDayAdapter.clear();
                for (ToDoItem task : output) {
                    mDayAdapter.add(task);
                }
            }
            mDayAdapter.notifyDataSetChanged();
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
