package com.example.cat300;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private boolean status;

    public Chat(String sender, String receiver, String message, boolean status) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.status = status;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
