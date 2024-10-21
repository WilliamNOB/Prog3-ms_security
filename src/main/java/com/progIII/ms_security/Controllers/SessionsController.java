package com.progIII.ms_security.Controllers;

import com.progIII.ms_security.Models.Session;
import com.progIII.ms_security.Models.User;
import com.progIII.ms_security.Repositories.SessionRepository;
import com.progIII.ms_security.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/sessions")

public class SessionsController {
    @Autowired
    private SessionRepository theSessionRepository;

    @Autowired
    private UserRepository theUserRepository;

    @GetMapping("")
    public List<Session> find(){
        return this.theSessionRepository.findAll();
    }
    @GetMapping("{id}")
    public Session findById(@PathVariable String id){
        Session theSession=this.theSessionRepository.findById(id).orElse(null);
        return theSession;
    }
    @PostMapping
    public Session create(@RequestBody Session newSession){
        return this.theSessionRepository.save(newSession);
    }

    @PutMapping("{id}")
    public Session update(@PathVariable String id, @RequestBody Session newSession){
        Session actualSession=this.theSessionRepository.findById(id).orElse(null);
        if(actualSession!=null){
            actualSession.setToken(newSession.getToken());
            this.theSessionRepository.save(actualSession);
            return actualSession;
        }else{
            return null;
        }
    }
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id){
        Session theRole=this.theSessionRepository.findById(id).orElse(null);
        if (theRole!=null){
            this.theSessionRepository.delete(theRole);
        }
    }
    @PostMapping("{session_id}/user/{user_id}")
    public Session matchUser(@PathVariable String session_id, @PathVariable String  user_id){
        Session theSession=this.theSessionRepository.findById(session_id).orElse(null);
        User theUser=this.theUserRepository.findById(user_id).orElse(null);
        if (theSession!=null && theUser!=null){
            theSession.setTheUser(theUser);
            this.theSessionRepository.save(theSession);
            return theSession;
        }else{
            return null;
        }
    }
}
