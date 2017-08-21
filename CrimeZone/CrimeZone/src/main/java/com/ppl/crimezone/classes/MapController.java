package com.ppl.crimezone.classes;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This is controller for HomeMapUI
 */
public class MapController {

    private Calendar start, end;
    private HashMap<Integer, CrimeReport> filterList;
    private boolean filterCrimeTtype[];
    ;
    private List<CrimeReport> reports;
    private LatLng location;

    private byte filterTime;
    private byte filterType;
    private boolean zoneMode = false;

    private final boolean START = true;
    private final boolean END = false;;

    private List<LatLng> list;



    private HashMap<Marker, CrimeReport> markerToCrimeReport;
    public MapController() {
        filterList = new HashMap<Integer, CrimeReport>();
        filterCrimeTtype = new boolean[8];
        for (int ii = 0; ii < filterCrimeTtype.length; ++ii) {
            filterCrimeTtype[ii] = false;
        }
        markerToCrimeReport = new HashMap<Marker, CrimeReport>();
        start = null;
        end = null;
        filterType = 0;
        filterTime = 0;
        list = new ArrayList<LatLng>();
    }

    public int getIdReport(Marker marker) {
        return markerToCrimeReport.get(marker).getIdReport();
    }

    public boolean getFilterType(int ii) {
        return filterCrimeTtype[ii];
    }

    public void setLocation(double latitude, double longitude) {
        location = new LatLng(latitude, longitude);
    }

    public LatLng getLocation() {
        return location;
    }

    public boolean resetStartDate() {
        if (start != null){
            setFilterStartDate(false);
            return true;
        }
        return false;

    }

    public boolean resetEndDate() {
        if(end != null){
            setFilterEndDate(false);
            return true;
        }return false;
    }



    public boolean setStartDate(int day, int month, int year) {
        if(isValidDate(day, month, year, true))
        {
            if(start != null){
                setFilterStartDate(false);
            }
            start = Calendar.getInstance();
            start.set(Calendar.DAY_OF_MONTH, day);
            start.set(Calendar.MONTH, month);
            start.set(Calendar.YEAR, year);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            if(end == null){
                setFilterStartDate(true);
            }else {
                setFilterRangeDate(true);
            }
            return true;
        }
        return false;
    }

    public void setZonesMode(boolean value){
        this.zoneMode = value;
    }

    public boolean getZonesMode(){
        return zoneMode;
    }

    public boolean getReportList(double distance){
        List<CrimeReport> miniReports= null;
        boolean success = false;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://crimezone.besaba.com/webservice/crimeLocation.php");
        try {
                //try to fetch the data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("distance", distance+""));
            nameValuePairs.add(new BasicNameValuePair("latitude", location.latitude+""));
            nameValuePairs.add(new BasicNameValuePair("longitude", location.longitude+""));
            Log.d("parameter", location.latitude +" "+ location.longitude + " " + distance);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == 200) {
                    //we have an OK response
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    try {
                        //Read the server response and attempt to parse it as JSON
                        Reader reader = new InputStreamReader(content);
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.setDateFormat("dd/MM/yyyy HH:mm");
                        Gson gson = gsonBuilder.create();
                        miniReports = Arrays.asList(gson.fromJson(reader, CrimeReport[].class));
                        content.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        if(miniReports != null){
            reports = miniReports;
            applyCurrentFilterSettings();
            success = true;
        }
        return success;
    }


    public static List<CrimeReport> getReportList(LatLng location){
        List<CrimeReport> miniReports= null;
        double distance = 250;
        boolean success = false;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://crimezone.besaba.com/webservice/crimeLocation.php");
        try {
            //try to fetch the data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("distance", distance+""));
            nameValuePairs.add(new BasicNameValuePair("latitude", location.latitude+""));
            nameValuePairs.add(new BasicNameValuePair("longitude", location.longitude+""));
            Log.d("parameter", location.latitude +" "+ location.longitude + " " + distance);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 200) {
                //we have an OK response
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                try {
                    //Read the server response and attempt to parse it as JSON
                    Reader reader = new InputStreamReader(content);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setDateFormat("dd/MM/yyyy HH:mm");
                    Gson gson = gsonBuilder.create();
                    miniReports = Arrays.asList(gson.fromJson(reader, CrimeReport[].class));
                    content.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return miniReports;
    }

    public String printStartDate(){
            int timeInt = start.get(Calendar.DAY_OF_MONTH);
            String dayString = timeInt+"";
            if(timeInt<10)dayString = "0"+ dayString;
            timeInt = start.get(Calendar.MONTH)+1;
            String monthString = (timeInt)+"";
            if(timeInt<10)monthString= "0"+ monthString;
            return (dayString + "/" + monthString + "/" + start.get(Calendar.YEAR));
    }

    public String printEndDate(){
            int timeInt = end.get(Calendar.DAY_OF_MONTH);
            String dayString = timeInt+"";
            if(timeInt<10)dayString = "0"+ dayString;
            timeInt = end.get(Calendar.MONTH)+1;
            String monthString = (timeInt)+"";
            if(timeInt<10)monthString= "0"+ monthString;
            return (dayString + "/" + monthString + "/" + end.get(Calendar.YEAR));
    }
    private boolean isValidDate(int day, int month, int year, boolean type) {
        Calendar chosen = Calendar.getInstance();
        chosen.set(Calendar.DAY_OF_MONTH, day);
        chosen.set(Calendar.MONTH, month);
        chosen.set(Calendar.YEAR, year);
        Calendar now = Calendar.getInstance();
        if(chosen.getTimeInMillis() > now.getTimeInMillis())return false;
        chosen.set(Calendar.HOUR_OF_DAY, 0);
        chosen.set(Calendar.MINUTE, 0);
        if((type == START && end != null ) && (chosen.getTimeInMillis() > end.getTimeInMillis()) )return false;
        chosen = Calendar.getInstance();
        chosen.set(Calendar.DAY_OF_MONTH, day);
        chosen.set(Calendar.MONTH, month);
        chosen.set(Calendar.YEAR, year);
        if((type == END && start != null ) && (chosen.getTimeInMillis() < start.getTimeInMillis()) )return false;
        return true;
    }

    public boolean setEndDate(int day, int month, int year){
        if(isValidDate(day, month, year, false)) {
            if(end != null) {
                setFilterEndDate(false);
            }
            end = Calendar.getInstance();
            end.set(Calendar.DAY_OF_MONTH, day);
            end.set(Calendar.MONTH, month);
            end.set(Calendar.YEAR, year);
            if(start != null){
                setFilterRangeDate(true);
            }else {
                setFilterEndDate(true);
            }
            return true;
        }
        return false;
    }




    public int getStartDate(int type){
        Calendar cal;
        if(start == null){
            cal = Calendar.getInstance();
        }else {
            cal = start;
        }
        switch(type){
            case 0:
                return cal.get(Calendar.DAY_OF_MONTH);
            case 1:
                return cal.get(Calendar.MONTH);
             default:
                return cal.get(Calendar.YEAR);
        }

    }

    public int getEndDate(int type){
        Calendar cal;
        if(end == null){
            cal = Calendar.getInstance();
        }else {
            cal = end;
        }
        switch(type){
            case 0:
                return cal.get(Calendar.DAY_OF_MONTH);
            case 1:
                return cal.get(Calendar.MONTH);
            default:
                return cal.get(Calendar.YEAR);

        }
    }

    private CrimeReport getFilteredReport(CrimeReport x, Byte initialCrimeType){
        List<Byte> crimeType = new ArrayList<Byte>();
        crimeType.add(initialCrimeType);
        return new CrimeReport(x.getIdReport(), x.getTitle(), x.getCrimeTime(), crimeType, x.getLatitude(), x.getLongitude());
    }


    public void setFilterType(byte type, boolean value){
        if(!value) {
            Log.d("reports size", reports.size()+"");
            for (int ii = 0; ii < reports.size(); ++ii) {
                Log.d("truth false", reports.get(ii).getCategories().contains(type) + " :-> " + reports.get(ii).printCategories());
                if (reports.get(ii).getCategories().contains(type)) {
                   Log.d("truth2", (filterList.get(reports.get(ii).getIdReport()) != null)+"");
                    if (filterList.get(reports.get(ii).getIdReport()) != null) {
                        CrimeReport temp = filterList.get(reports.get(ii).getIdReport());
                        temp.addCategories(type);
                        filterList.put(reports.get(ii).getIdReport(),temp) ;
                    } else {
                        filterList.put(reports.get(ii).getIdReport(), getFilteredReport(reports.get(ii), type));
                    }
                }
            }

            for(int ii=0; ii< reports.size(); ++ii){
                Log.d("after false :=>", reports.get(ii).printCategories());
            }
            Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
            iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                Log.d("after false =>" , entry.getValue().printCategories());
            }
            filterType--;
            filterCrimeTtype[type] = false;
        }else {

            Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                entry.getValue().removeCategories(type);
                if(entry.getValue().getCategories().size() == 0) iterator.remove(); // right way to remove entries from Map,
                // avoids ConcurrentModificationException
            }
            iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                Log.d("true =>", entry.getValue().printCategories());
            }

            for(int ii=0; ii< reports.size(); ++ii){
                Log.d("after true :=>", reports.get(ii).printCategories());
            }
            filterType++;
            filterCrimeTtype[type] = true;
        }
    }

    private CrimeReport copyReport(CrimeReport x) {
        List<Byte> categories = new ArrayList<Byte>();
        for(int ii=0; ii<x.getCategories().size(); ++ii){
            categories.add(new Byte(x.getCategories().get(ii).byteValue()));
        }
        return new CrimeReport(x.getIdReport(),x.getTitle(), x.getCrimeTime(), categories, x.getLatitude(), x.getLongitude());
    }

    private void setFilterStartDate(boolean value){
        if(!value) {
            for(int ii=0; ii<reports.size(); ++ii){
                Log.d("false before report :-> ", reports.get(ii).getCrimeTime().toString());
            }
            Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                Log.d("false before filter :=>", entry.getValue().getCrimeTime().toString());
            }
            for (int ii = 0; ii < reports.size(); ++ii) {
                if (start.getTime().after(reports.get(ii).getCrimeTime())) {
                    filterList.put(reports.get(ii).getIdReport(), copyReport(reports.get(ii)));
                }
            }
            for(int ii=0; ii<reports.size(); ++ii){
                Log.d("false after report :da-> ", reports.get(ii).getCrimeTime().toString());
            }
            iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                Log.d("false after filter :=>", entry.getValue().getCrimeTime().toString());
            }
            start = null;
        }else {
            for(int ii=0; ii<reports.size(); ++ii){
                Log.d("true before report -> ", reports.get(ii).getCrimeTime().toString());
            }
            Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                Log.d("true before filter =>", entry.getValue().getCrimeTime().toString());
            }
            iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                if(start.getTime().after(entry.getValue().getCrimeTime())) iterator.remove(); // right way to remove entries from Map,
            }
            for(int ii=0; ii<reports.size(); ++ii){
                Log.d("true after report -> ", reports.get(ii).getCrimeTime().toString());
            }
            iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                Log.d("true after filter =>", entry.getValue().getCrimeTime().toString());
            }

        }
    }

    private void setFilterEndDate(boolean value){
        if(!value) {
            for (int ii = 0; ii < reports.size(); ++ii) {
                if (reports.get(ii).getCrimeTime().after(end.getTime())) {
                    filterList.put(reports.get(ii).getIdReport(), copyReport(reports.get(ii)));
                }
            }
            end = null;
        }else {
            Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                if(entry.getValue().getCrimeTime().after(end.getTime())) iterator.remove(); // right way to remove entries from Map,
            }

        }
    }


    private void setFilterRangeDate(boolean value){
        if(!value) {
            for (int ii = 0; ii < reports.size(); ++ii) {
                if (reports.get(ii).getCrimeTime().after(end.getTime()) || reports.get(ii).getCrimeTime().before(start.getTime())) {
                    filterList.put(reports.get(ii).getIdReport(), copyReport(reports.get(ii)));
                }
            }
            start = null;
            end = null;
        }else {
            Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
                if(entry.getValue().getCrimeTime().after(end.getTime()) || entry.getValue().getCrimeTime().before(start.getTime())) iterator.remove(); // right way to remove entries from Map,
            }
        }
    }

    public CrimeReport getCrimeReport(int idReport){
        return filterList.get(idReport);
    }

    public void addMarkerToCrimeReport(Marker mark, CrimeReport report){
        markerToCrimeReport.put(mark, report);
    }

    public HashMap<Integer, CrimeReport> getFilteredReports(){return filterList;}


    private void applyCurrentFilterSettings(){
        filterList = new HashMap<Integer, CrimeReport>();

        Log.d("report size", reports.size() + "");
        //copy from report to filterList
        for(int ii=0; ii< reports.size(); ++ii){
            filterList.put(reports.get(ii).getIdReport(), copyReport(reports.get(ii)));
        }
        if(start != null)setFilterStartDate(true);
        if(end != null)setFilterEndDate(true);
        if(filterType > 0) {
            for (byte ii = 0; ii < filterCrimeTtype.length; ++ii) {
                if (filterCrimeTtype[ii]) {
                    setFilterType(ii, true);
                }
            }
        }
        Iterator<HashMap.Entry<Integer, CrimeReport>> iterator = filterList.entrySet().iterator();
        iterator = filterList.entrySet().iterator();
        while(iterator.hasNext()){
            HashMap.Entry<Integer, CrimeReport> entry = iterator.next();
            list.add(new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude()));
            Log.d("after false =>" , entry.getValue().printCategories());
        }


    }
}
