package com.ppl.crimezone.classes;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created by adesudiman on 4/5/2014.
 */
public class SearchLocationUI extends AutoCompleteTextView {

    public SearchLocationUI(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Returns the Place Description corresponding to the selected item */
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        /** Each item in the autocompetetextview suggestion list is a hashmap object */
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("description");
    }
}