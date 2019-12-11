package com.example.lockwood.techTest.controller;


import com.example.lockwood.techTest.aws.DynamoDb;
import com.example.lockwood.techTest.data.Ticket;
import com.example.lockwood.techTest.service.TicketService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@RestController
public class TicketController {

    @Autowired
    TicketService ticketService;

    @Autowired
    DynamoDb dynamoDb;

    @PostMapping(value = "/ticket")
    @ResponseBody
    public Ticket createNewTicket(HttpServletResponse response){
        Ticket ticketResponse = new Ticket();
        String ticket = ticketService.createNewTicket();
        if(ticket != null){
            ticketResponse.setTicket(ticket);
            return ticketResponse;
        }
        ticketResponse.setError("something went wrong");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, new JSONObject(ticketResponse).toString());

    }

    @GetMapping(value = "/ticket")
    @ResponseBody
    public void validateTicket(@RequestParam final String ticket, HttpServletResponse response){
        Ticket ticketResponse = new Ticket();
        if(ticketService.validateTicket(ticket)){
            response.setStatus(HttpStatus.OK.value());
            return;
        }
        ticketResponse.setError("invalid");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, new JSONObject(ticketResponse).toString());

    }

    @PutMapping(value = "/ticket/invalidate")
    @ResponseBody
    public void invalidateTicket(@RequestParam final String ticket, HttpServletResponse response){
        Ticket ticketResponse = new Ticket();
        if(ticketService.invalidateTicket(ticket)){
            response.setStatus(HttpStatus.OK.value());
            return;
        }
        ticketResponse.setError("something went wrong");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, new JSONObject(ticketResponse).toString());

    }

    @GetMapping(value = "/ticket/total")
    @ResponseBody
    public Long getTicketTotal(HttpServletResponse response){
        return dynamoDb.getTotalTickets();
    }
}
