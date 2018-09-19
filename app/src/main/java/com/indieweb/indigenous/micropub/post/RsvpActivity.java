package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Spinner;

import com.indieweb.indigenous.R;

public class RsvpActivity extends BaseCreateActivity {

    Spinner rsvp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "RSVP";
        urlPostKey = "in-reply-to";
        addCounter = true;
        setContentView(R.layout.activity_rsvp);
        super.onCreate(savedInstanceState);

        // Get spinner.
        // TODO make the options translatable
        rsvp = findViewById(R.id.rsvp);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        if (TextUtils.isEmpty(url.getText())) {
            url.setError(getString(R.string.required_field));
        }
        else {
            bodyParams.put("rsvp", rsvp.getSelectedItem().toString());
            sendBasePost(item);
        }
    }

}
