package com.progIII.ms_security.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class Session {
    @Id
    private String _id;
    private String token;
    private int token2FA;
    private Date started_At;
    private Date end_At;
    @DBRef
    private User theUser;

    public Session(){}

    public Session(int token2FA, User theUser){
        this.token2FA = token2FA;
        this.theUser = theUser;
    }

    public String get_id() {
        return _id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getToken2FA() {
        return token2FA;
    }

    public void setToken2FA(int token2FA) {
        this.token2FA = token2FA;
    }

    public Date getStarted_At() {
        return started_At;
    }

    public void setStarted_At(Date started_At) {
        this.started_At = started_At;
    }

    public Date getEnd_At() {
        return end_At;
    }

    public void setEnd_At(Date end_At) {
        this.end_At = end_At;
    }

    public User getTheUser() {
        return theUser;
    }

    public void setTheUser(User theUser) {
        this.theUser = theUser;
    }

}