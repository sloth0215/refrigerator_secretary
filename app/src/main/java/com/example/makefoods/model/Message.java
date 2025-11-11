package com.example.makefoods.model;

public class Message {
    public enum Sender { USER, BOT }
    private final String text;
    private final Sender sender;

    public Message(String text, Sender sender) {
        this.text = text; this.sender = sender;
    }
    public String getText(){ return text; }
    public Sender getSender(){ return sender; }
}