package com.ppl.crimezone.classes;

import java.io.Serializable;

/**
 * Created by ajiutamaa on 08/05/14.
 */
public class NewsFeedItem implements Serializable {
    private String title;
    private String username;
    private String reportID;
    private String timestamp;
    private String rating;
    private int[] categories;

    public NewsFeedItem(String title, String username, String reportID, String timestamp, String rating, int [] categories) {
        this.title = title;
        this.username = username;
        this.reportID = reportID;
        this.timestamp = timestamp;
        this.rating = rating;
        this.categories = categories;
    }
}
