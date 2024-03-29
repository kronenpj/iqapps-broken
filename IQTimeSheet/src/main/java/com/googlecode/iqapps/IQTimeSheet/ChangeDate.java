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

package com.googlecode.iqapps.IQTimeSheet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;
import android.widget.Toast;
import com.googlecode.iqapps.TimeHelpers;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * Activity that provides an interface to change the date of an entry.
 *
 * @author Paul Kronenwetter <kronenpj@gmail.com>
 */
public class ChangeDate extends Activity {
    private DatePicker dateChange;
    private final static String TAG = "ChangeDate";
    private TextView dateText;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Choose a date");
        setContentView(R.layout.changedate);
        dateText = (TextView) findViewById(R.id.DateText);

        Bundle extras = getIntent().getExtras();
        long timeMillis = extras.getLong("time");

        dateChange = (DatePicker) findViewById(R.id.DatePicker01);

        DatePicker.OnDateChangedListener mDateChangedListener = new OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                updateDateText(year, monthOfYear, dayOfMonth);
            }
        };

        dateChange.init(TimeHelpers.millisToYear(timeMillis),
                TimeHelpers.millisToMonthOfYear(timeMillis),
                TimeHelpers.millisToDayOfMonth(timeMillis),
                mDateChangedListener);
        updateDateText(TimeHelpers.millisToYear(timeMillis),
                TimeHelpers.millisToMonthOfYear(timeMillis),
                TimeHelpers.millisToDayOfMonth(timeMillis));

        Button[] child = new Button[]{(Button) findViewById(R.id.changeok),
                (Button) findViewById(R.id.changecancel)};

        for (Button aChild : child) {
            try {
                aChild.setOnClickListener(mButtonListener);
            } catch (NullPointerException e) {
                Toast.makeText(ChangeDate.this, "NullPointerException",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method is what is registered with the button to cause an action to
     * occur when it is pressed.
     */
    private OnClickListener mButtonListener = new OnClickListener() {
        public void onClick(View v) {
            long newDate = TimeHelpers.millisSetDate(dateChange.getYear(),
                    dateChange.getMonth() + 1, dateChange.getDayOfMonth());

            Log.d(TAG, "onClickListener view id: " + v.getId());
            Log.d(TAG, "onClickListener defaulttask id: " + R.id.defaulttask);

            switch (v.getId()) {
                case R.id.changecancel:
                    setResult(RESULT_CANCELED, (new Intent()).setAction("cancel"));
                    finish();
                    break;
                case R.id.changeok:
                    setResult(RESULT_OK,
                            new Intent().setAction(Long.toString(newDate)));
                    finish();
                    break;
            }
        }
    };

    private void updateDateText(int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar date = new GregorianCalendar(year, monthOfYear,
                dayOfMonth);
        SimpleDateFormat simpleDate = new SimpleDateFormat("E, MMM d, yyyy");
        dateText.setText(simpleDate.format(date.getTime()));
    }
}
