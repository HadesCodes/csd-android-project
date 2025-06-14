package com.example.idrive.data.models;

public class Lesson {
    private String id, userId, date, time, notes;

    public Lesson() {}

    public Lesson(String id, String userId, String date, String time, String notes) {
        this.id = id;
        this.userId = userId;
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
