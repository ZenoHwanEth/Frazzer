package com.example.cat300;

public class Moment {
    private String id;
    private String profile_pic;
    private String name;
    private String text;
    private String image;
    private String date;

    public Moment(String id,String profile_pic,String name,String text,String image,String date){
        this.id = id;
        this.profile_pic=profile_pic;
        this.name=name;
        this.text=text;
        this.image = image;
        this.date=date;
    }

    public Moment(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
