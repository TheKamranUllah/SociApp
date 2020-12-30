package com.example.sociapp;

public class Comments {

   public  String comment, date, time, uid,profileimage, username;

   public Comments( )
   {

   }

    public Comments(String comment, String date, String time, String uid, String profileimage, String username)
    {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.profileimage = profileimage;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
