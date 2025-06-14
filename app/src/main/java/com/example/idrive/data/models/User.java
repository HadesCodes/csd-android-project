package com.example.idrive.data.models;

public class User {
    private String _uid;
    private String _firstName;
    private String _lastName;
    private String _email;
    private String _phone;
    private String _password;
    private boolean _isAdmin;

    public User() {}
    public User(String _firstName, String _lastName, String _phone, String _email, String _password) {
        this._firstName = _firstName;
        this._lastName = _lastName;
        this._email = _email;
        this._phone = _phone;
        this._password = _password;
        this._isAdmin = false;
    }

    public String getUid() {
        return _uid;
    }

    public String getFirstName() {
        return _firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public String getPhone() {
        return _phone;
    }

    public String getEmail() {
        return _email;
    }

    public String getPassword() {
        return _password;
    }

    public boolean getIsAdmin() {
        return _isAdmin;
    }

    public void setUid(String _uid) {
        this._uid = _uid;
    }

    public void setFirstName(String _firstName) {
        this._firstName = _firstName;
    }

    public void setLastName(String _lastName) {
        this._lastName = _lastName;
    }

    public void setPhone(String _phone) {
        this._phone = _phone;
    }

    public void setEmail(String _email) {
        this._email = _email;
    }

    public void setPassword(String _password) {
        this._password = _password;
    }

    public void setIsAdmin(boolean _isAdmin) {
        this._isAdmin = _isAdmin;
    }
}
