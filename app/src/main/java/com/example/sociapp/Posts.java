
package com.example.sociapp;

import android.net.Uri;

import com.google.firebase.database.Exclude;

public class Posts {

    public String uid,time, date,description,postimage,profileimage,fullname, storageName, type;
    public long counter;

    String backgrounduri ,userstatus, statusBg;
    long textcolor, textsize;

    public Posts ( )
    {

    }

    public Posts(String uid, String time, String date, String description, String postimage,
                 String profileimage, String fullname, String storageName, long counter, String type) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.description = description;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.storageName = storageName;
        this.counter = counter;
        this.type = type;
    }

    public Posts(String uid, String fullname, String time, String backgrounduri, String userstatus,
                 String statusBg, long textsize, long textcolor, long counter, String profileimage,
                 String type, String date)
    {

         this.uid = uid;
         this.backgrounduri = backgrounduri;
         this.fullname = fullname;
         this.time = time;
         this.userstatus = userstatus;
         this.statusBg = statusBg;
         this.textsize = textsize;
         this.textcolor = textcolor;
         this.counter = counter;
         this.profileimage = profileimage;
         this.type = type;
         this.date = date;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBackgrounduri() {
        return backgrounduri;
    }

    public void setBackgrounduri(String backgrounduri) {
        this.backgrounduri = backgrounduri;
    }

    public String getUserstatus() {
        return userstatus;
    }

    public void setUserstatus(String userstatus) {
        this.userstatus = userstatus;
    }

    public String getStatusBg() {
        return statusBg;
    }

    public void setStatusBg(String statusBg) {
        this.statusBg = statusBg;
    }

    public long getTextcolor() {
        return textcolor;
    }

    public void setTextcolor(long textcolor) {
        this.textcolor = textcolor;
    }

    public long getTextsize() {
        return textsize;
    }

    public void setTextsize(long textsize) {
        this.textsize = textsize;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}

