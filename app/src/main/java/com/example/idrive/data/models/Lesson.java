package com.example.idrive.data.models;

public class Lesson {
    private String id, userId, title, date, time, notes;

    public Lesson() {}

    public Lesson(String userId, String title, String date, String time, String notes) {
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getNotes() {
        return notes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
