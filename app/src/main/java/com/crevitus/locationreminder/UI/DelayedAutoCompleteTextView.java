package com.crevitus.locationreminder.UI;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

//autocomplete textview with query delay
public class DelayedAutoCompleteTextView extends AutoCompleteTextView {
    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final Handler _Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayedAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        _Handler.removeMessages(0);
        //send input to be queried every 750 milliseconds (stops query quota being used up)
        _Handler.sendMessageDelayed(_Handler.obtainMessage(0, keyCode, 0, text), 750);
    }
}