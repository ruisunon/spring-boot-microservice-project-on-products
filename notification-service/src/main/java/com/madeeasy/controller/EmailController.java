package com.madeeasy.controller;


import com.madeeasy.dto.EmailRequest;
import com.madeeasy.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/notification-service/")
@CrossOrigin("*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping(value = "/with-no-attachment")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest emailRequest) {
        emailService.checkAndSendEmail(emailRequest);
        return ResponseEntity.ok().headers(httpHeaders -> httpHeaders
                        .add("hello", "message sent"))
                .body("email successfully sent");
    }

}