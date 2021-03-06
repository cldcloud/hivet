package com.acukanov.hivet.data.database.model;


public class Messages {
    public long id;
    public String message;
    //public int dateTime;
    public String dateTime;
    public long userId;

    public Messages() {

    }

    public Messages(int id, String message, /*int*/String dateTime, long userId) {
        this.id = id;
        this.message = message;
        this.dateTime = dateTime;
        this.userId = userId;
    }

    // region getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /*public int getDateTime() {
        return dateTime;
    }

    public void setDateTime(int dateTime) {
        this.dateTime = dateTime;
    }*/

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    // endregion


    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Messages messages = (Messages) o;

        if (id != messages.id) return false;
        if (dateTime != messages.dateTime) return false;
        if (userId != messages.userId) return false;
        return message != null ? message.equals(messages.message) : messages.message == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + dateTime;
        result = 31 * result + userId;
        return result;
    }*/

    @Override
    public String toString() {
        return "Messages{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", user_id=" + userId +
                '}';
    }
}
