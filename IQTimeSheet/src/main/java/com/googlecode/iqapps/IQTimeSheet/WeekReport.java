/*
 * Copyright 2010 TimeSheet authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Display a report for a week's worth of entries.
 *
 * @author Paul Kronenwetter <kronenpj@gmail.com>
 */
package com.googlecode.iqapps.IQTimeSheet;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.googlecode.iqapps.TimeHelpers;

/**
 * Activity to display a summary report of tasks and their cumulative durations
 * for a selected week.
 *
 * @author Paul Kronenwetter <kronenpj@gmail.com>
 */
public class WeekReport extends ListActivity {
    private static final String TAG = "WeekReport";
    //private final int FOOTER_ID = -1;
    private ListView reportList;
    private TimeSheetDbAdapter db;
    private Cursor timeEntryCursor;
    private TextView footerView = null;
    private long day = TimeHelpers.millisNow();
    private float weekHours = -1;
    private float dayHours = -1;

    /**
     * Called when the activity is resumed or created.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "In onResume.");

        // FIXME: I bet these cause a FC when resuming from the home menu.
        weekHours = TimeSheetActivity.prefs.getHoursPerWeek();
        dayHours = TimeSheetActivity.prefs.getHoursPerDay();

        try {
            showReport();
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString() + " calling showReport");
        }
        Log.d(TAG, "Back from showReport.");
        setTitle("Week Report");

        db = new TimeSheetDbAdapter(this);
        try {
            setupDB();
        } catch (Exception e) {
            Log.e(TAG, "setupDB: " + e.toString());
            finish();
        }

        try {
            fillData();
        } catch (Exception e) {
            Log.e(TAG, "fillData: " + e.toString());
            finish();
        }
        Log.d(TAG, "Back from fillData.");
    }

    /**
     * Called when the activity destroyed.
     */
    @Override
    public void onDestroy() {
        try {
            timeEntryCursor.close();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.toString());
        }
        db.close();
        super.onDestroy();
    }

    /**
     * Encapsulate what's needed to open the database and make sure something is
     * in it.
     */
    private void setupDB() {
        try {
            db.open();
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            finish();
        }
    }

    /**
     * Populate the elements displayed in the view.
     */
    private void fillData() {
        Log.d(TAG, "In fillData.");

        // Cheat a little on the date. This was originally referencing timeIn
        // from the cursor below.
        // TODO: Make the option to report from week-end or week-start an
        // option. This may include changing the usage of 'date' further down so
        // that it's interpreted as a beginning rather than an end date.
        String date = TimeHelpers.millisToDate(TimeHelpers
                .millisToEndOfWeek(day));
        setTitle("Week Report - W/E: " + date);

        footerView
                .setText("Hours worked this week: 0\nHours remaining this week: "
                        + String.format("%.2f", weekHours)
                        + "\nDays remaining this week: "
                        + String.format("%.2f", weekHours / dayHours));

        try {
            timeEntryCursor.close();
        } catch (NullPointerException e) {
            // Do nothing, this is expected sometimes.
        } catch (Exception e) {
            Log.e(TAG, "timeEntryCursor.close: " + e.toString());
            return;
        }

        // If the week being reported is the current week, most probably where
        // the current open task exists, then include it, otherwise omit.
        if (day >= TimeHelpers.millisToStartOfWeek(TimeHelpers.millisNow())
                && day <= TimeHelpers
                .millisToEndOfWeek(TimeHelpers.millisNow())) {
            timeEntryCursor = db.weekSummary(day, false);
        } else {
            timeEntryCursor = db.weekSummary(day, true);
        }
        // startManagingCursor(timeEntryCursor);

        try {
            timeEntryCursor.moveToFirst();
        } catch (NullPointerException e) {
            return;
        } catch (Exception e) {
            Log.e(TAG, "timeEntryCursor.moveToFirst: " + e.toString());
            return;
        }

        float accum = 0;
        while (!timeEntryCursor.isAfterLast()) {
            accum = accum
                    + timeEntryCursor.getFloat(timeEntryCursor
                    .getColumnIndex(TimeSheetDbAdapter.KEY_TOTAL));
            timeEntryCursor.moveToNext();
        }

        footerView.setText("Hours worked this week: "
                + String.format("%.2f", accum)
                + "\nHours remaining this week: "
                + String.format("%.2f", weekHours - accum)
                + "\nDays remaining this week: "
                + String.format("%.2f", (weekHours - accum) / dayHours));

        try {
            reportList.setAdapter(new ReportCursorAdapter(this,
                    R.layout.mysimple_list_item_2, timeEntryCursor,
                    new String[]{TimeSheetDbAdapter.KEY_TASK,
                            TimeSheetDbAdapter.KEY_TOTAL}, new int[]{
                    android.R.id.text1, android.R.id.text2}));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            finish();
        }
    }

    /**
     * Change the view to the one we need and set up references to the children
     * we're interested in manipulating.
     */
    protected void showReport() {
        Log.d(TAG, "Changing to report layout.");

        try {
            setContentView(R.layout.report);
        } catch (Exception e) {
            Log.e(TAG, "Caught " + e.toString()
                    + " while calling setContentView(R.layout.report)");
        }

        // reportList = (ListView) findViewById(R.id.reportlist);
        reportList = (ListView) findViewById(android.R.id.list);
        footerView = (TextView) findViewById(R.id.reportfooter);
        footerView.setTextSize(TimeSheetActivity.prefs
                .getTotalsFontSize());

        Button[] child = new Button[]{(Button) findViewById(R.id.previous),
                (Button) findViewById(R.id.today),
                (Button) findViewById(R.id.next)};

        for (Button aChild : child) {
            try {
                aChild.setOnClickListener(mButtonListener);
            } catch (NullPointerException e) {
                Log.e(TAG, "setOnClickListener: " + e.toString());
            }
        }
    }

    /**
     * Method registered with a button to cause an action to occur when it is
     * pressed.
     */
    private OnClickListener mButtonListener = new OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "onClickListener view id: " + v.getId());

            switch (v.getId()) {
                case R.id.previous:
                    day = TimeHelpers.millisToStartOfWeek(day) - 1000;
                    break;
                case R.id.today:
                    day = TimeHelpers.millisNow();
                    break;
                case R.id.next:
                    day = TimeHelpers.millisToEndOfWeek(day) + 1000;
                    break;
            }
            fillData();
        }
    };
}
