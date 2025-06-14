package com.example.idrive.data.models;

public class Suggestion {
    private String id, userId, lessonId, oldDate, newDate, oldTime, newTime, reason;

    public Suggestion() {}

    public Suggestion(String userId, String lessonId, String oldDate, String oldTime, String newDate, String newTime, String reason) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.oldDate = oldDate;
        this.oldTime = oldTime;
        this.newDate = newDate;
        this.newTime = newTime;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public String getOldDate() {
        return oldDate;
    }

    public String getOldTime() {
        return oldTime;
    }

    public String getNewDate() {
        return newDate;
    }

    public String getNewTime() {
        return newTime;
    }

    public String getReason() {
        return reason;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public void setOldDate(String oldDate) {
        this.oldDate = oldDate;
    }

    public void setOldTime(String oldTime) {
        this.oldTime = oldTime;
    }

    public void setNewDate(String newDate) {
        this.newDate = newDate;
    }

    public void setNewTime(String newTime) {
        this.newTime = newTime;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
