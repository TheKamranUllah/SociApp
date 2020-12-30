package com.example.sociapp;

public class FindFriends
{
    private String profileimage, Full_Name, Status;


    public FindFriends ( )
    {

    }
    public FindFriends(String profileimage, String fullName, String status)
    {
        this.profileimage = profileimage;
        Full_Name = fullName;
        Status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFull_Name() {
        return Full_Name;
    }

    public void setFull_Name(String fullName) {
        Full_Name = fullName;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
       Status = status;
    }
}
