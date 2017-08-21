package com.ppl.crimezone.classes;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by adesudiman on 4/6/2014.
 */
public class CrimeReport{

    private String username;
    @SerializedName("reportID")
    private int idReport;
    @SerializedName("data_created")
    private Date reportDate;
    private String description;
    @SerializedName("avg_rating")
    private double avgScore;
    private String title;
    @SerializedName("time")
    private Date crimeTime;
    @SerializedName("CategoryName")
    private List<Byte> categories;

    @SerializedName("x_coordinate")
    private double latitude;
    @SerializedName("y_coordinate")
    private double longitude;

    public CrimeReport(int idReport, String title, Date reportDate, Date time,  String description, List<Byte> categories, double latitude, double longitude, double avgScore){
        this.title = title;
        this.crimeTime= time;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idReport = idReport;
        this.reportDate = reportDate;
        this.description = description;
        this.avgScore = avgScore;
    }

    public CrimeReport(int idReport, String title, Date time, List<Byte> categories, double latitude, double longitude){
        this.idReport = idReport;
        this.title = title;
        this.crimeTime = time;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idReport = idReport;
    }
    public CrimeReport(){
    }


    public void setCrimeTime(long miliseconds){
        crimeTime.setTime(miliseconds);
    }
    public int getIdReport(){
        return idReport;
    }

    public Date getReportDate(){
        return reportDate;
    }

    public String getDescription(){
        return description;
    }

    public double getAvgScore(){
        return avgScore;
    }

    public void setAvgScore(double newAvgScore){
        avgScore = newAvgScore;
    }

    public String getTitle(){
        return title;
    }

    public Date getCrimeTime(){
        return crimeTime;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public List<Byte> getCategories(){
        return categories;
    }

    public void removeCategories(byte category){
        categories.remove(new Byte(category));
    }

    public void addCategories(byte category){
        if(categories.contains(category))categories.add(new Byte(category));
    }


    public String printCategories(){
        String result="";
        for(Byte x:categories){
            result+= x+",";
        }
        return result;
    }

    @Override
    public String toString(){
        return getIdReport()+" "+ getReportDate()+" "+ getDescription()+" "+ getAvgScore()+" " + getCrimeTime()+" "+" "+getLatitude()+" "+getLongitude()+ " "+ printCategories();
    }

    public String printDate(){
        Calendar convertDatetoCalendar = Calendar.getInstance();
        convertDatetoCalendar.setTimeInMillis(crimeTime.getTime());
        int timeInt = convertDatetoCalendar.get(Calendar.DAY_OF_MONTH);
        String timeText = timeInt+"";
        if(timeInt < 10)timeText= "0"+timeText;
        timeText+="/";
        timeInt= (convertDatetoCalendar.get(Calendar.MONTH)+1);
        if(timeInt < 10)timeText= timeText+ "0"+timeInt;
        else timeText = timeText +timeInt;
        timeText+="/"+ convertDatetoCalendar.get(Calendar.YEAR)+ " ";
        timeInt = convertDatetoCalendar.get(Calendar.HOUR_OF_DAY);
        if(timeInt < 10)timeText += "0"+ timeInt+":";
        else timeText +=timeInt+":";
        timeInt = convertDatetoCalendar.get(Calendar.MINUTE);
        if(timeInt < 10)timeText += "0"+ timeInt;
        else timeText +=timeInt;
        return timeText;
    }

    public String getUsername(){
        return username;
    }
}
