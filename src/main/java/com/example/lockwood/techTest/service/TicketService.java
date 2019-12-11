package com.example.lockwood.techTest.service;

import com.example.lockwood.techTest.aws.DynamoDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    DynamoDb dynamoDb;

    public String createNewTicket() {
        UUID ticket = UUID.randomUUID();
        while(!dynamoDb.createId(ticket.toString())){
            ticket = UUID.randomUUID();
        }
        return null;
    }

    public boolean validateTicket(String ticket){
        try{
            UUID.fromString(ticket);
        }catch (IllegalArgumentException e) {
            return false;
        }
        if(dynamoDb.findValidTicket(ticket)){
            return true;
        }
        return false;

    }

    public boolean invalidateTicket(String ticket){
        try{
            UUID.fromString(ticket);
        }catch (IllegalArgumentException e) {
            return false;
        }
        if(dynamoDb.invalidateTicket(ticket)){
            return true;
        }
        return false;

    }

}
