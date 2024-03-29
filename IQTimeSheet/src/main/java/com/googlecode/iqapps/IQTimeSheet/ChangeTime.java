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
import android.widget.Toast;
import com.googlecode.iqapps.TimeHelpers;
import com.googlecode.iqapps.TimePicker;

/**
 * Activity that provides an interface to change the time of an entry.
 *
 * @author Paul Kronenwetter <kronenpj@gmail.com>
 */
public class ChangeTime extends Activity {
    private TimePicker timeChange;
    private final static String TAG = "ChangeTime";
    private long timeMillis = -1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Choose a time");
        setContentView(R.layout.changehourmin);

        Bundle extras = getIntent().getExtras();
        timeMillis = extras.getLong("time");

        timeChange = (TimePicker) findViewById(R.id.TimePicker01);

        if (timeChange != null) {
            timeChange.setIs24HourView(true);
            timeChange.setCurrentHour(TimeHelpers.millisToHour(timeMillis));
            timeChange.setCurrentMinute(TimeHelpers.millisToMinute(timeMillis));

            if (TimeSheetActivity.prefs.getAlignTimePicker())
                timeChange.setInterval(TimeSheetActivity.prefs
                        .getAlignMinutes());
        }

        Button[] child = new Button[]{(Button) findViewById(R.id.changeok),
                (Button) findViewById(R.id.changecancel)};

        for (Button aChild : child) {
            try {
                aChild.setOnClickListener(mButtonListener);
            } catch (NullPointerException e) {
                Toast.makeText(ChangeTime.this, "NullPointerException",
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
            long newTime = TimeHelpers.millisSetTime(timeMillis,
                    timeChange.getCurrentHour(), timeChange.getCurrentMinute());

            Log.d(TAG, "onClickListener view id: " + v.getId());
            Log.d(TAG, "onClickListener defaulttask id: " + R.id.defaulttask);

            switch (v.getId()) {
                case R.id.changecancel:
                    setResult(RESULT_CANCELED, (new Intent()).setAction("cancel"));
                    finish();
                    break;
                case R.id.changeok:
                    setResult(RESULT_OK,
                            new Intent().setAction(Long.toString(newTime)));
                    finish();
                    break;
            }
        }
    };
}