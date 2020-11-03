package com.example.cat300;

public class Profile {
    private String id;
    private String username;
    private String phonenumber;
    private String password;
    private String email;
    private String birthday;
    private String image;
    private String gender;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRaces() {
        return races;
    }

    public void setRaces(String races) {
        this.races = races;
    }

    public String getReligions() {
        return religions;
    }

    public void setReligions(String religions) {
        this.religions = religions;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    private String target;
    private String races;
    private String religions;
    private String hobby;

    public Profile(String id,String username,String phonenumber,String password,String email,String birthday,String image){
        this.id = id;
        this.username = username;
        this.phonenumber = phonenumber;
        this.password=password;
        this.email = email;
        this.birthday = birthday;
        this.image = image;
    }

    public Profile(){

    }

    public String getBirthday() {
        return birthday;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getUsername() {
        return username;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
