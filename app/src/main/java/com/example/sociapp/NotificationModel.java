package com.example.sociapp;

public class NotificationModel {

    private String profileimage, fullname, date, time, receiverid,senderid, usermessage, notificationtype;

    public NotificationModel()
    {

    }

    public NotificationModel(String profileimage, String fullname, String date,
                             String time, String receiverid, String senderid, String usermessage, String notificationtype) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.date = date;
        this.time = time;
        this.receiverid = receiverid;
        this.senderid = senderid;
        this.usermessage = usermessage;
        this.notificationtype = notificationtype;
    }

    public NotificationModel(String profileimage, String fullname,
                             String date, String time, String receiverid, String senderid, String notificationtype)
    {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.date = date;
        this.time = time;
        this.receiverid = receiverid;
        this.senderid = senderid;
        this.notificationtype = notificationtype;
    }

    public String getUsermessage() {
        return usermessage;
    }

    public void setUsermessage(String usermessage) {
        this.usermessage = usermessage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public String getReceiverid() {
        return receiverid;
    }

    public void setReceiverid(String receiverid) {
        this.receiverid = receiverid;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
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

    public String getNotificationtype() {
        return notificationtype;
    }

    public void setNotificationtype(String notificationtype) {
        this.notificationtype = notificationtype;
    }
}
