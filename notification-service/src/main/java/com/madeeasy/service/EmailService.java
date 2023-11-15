package com.madeeasy.service;

import com.madeeasy.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;
    private boolean emailSent = false; // Flag to track whether the email has been sent
    private volatile boolean stopThread = false; // Flag to indicate when the thread should stop

    @Value("${spring.mail.username:pabitrabera2001@gmail.com}")
    private String fromEmail;

    public void checkAndSendEmail(EmailRequest emailRequest) {
        // Scheduled task to run every 3 seconds
        new Thread(() -> {
            while (!stopThread) {
                try {
                    Thread.sleep(3000);
                    if (netIsAvailable() && !emailSent) {
                        sendEmail(emailRequest);
                        emailSent = true; // Set the flag to true after sending the email
                        stopThread = true;
                        System.out.println("Email sent successfully");
                    } else if (!netIsAvailable()) {
                        emailSent = false; // Reset the flag if internet connectivity is lost
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // this two is set to false  for next network calls
        this.stopThread = false;
        this.emailSent = false;
        System.out.println("Thread has done its work");
    }

    private boolean netIsAvailable() {
        try {
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            System.out.println("Internet is connected");
            return true;
        } catch (MalformedURLException e) {
            System.out.println("Internet is not connected");
        } catch (IOException e) {
            System.out.println("Internet is not connected");
        }
        return false;
    }

    public void sendEmail(EmailRequest emailRequest) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromEmail);
        simpleMailMessage.setTo(emailRequest.getToEmail());
        simpleMailMessage.setSubject(emailRequest.getSubject());
        simpleMailMessage.setText(emailRequest.getText());
        javaMailSender.send(simpleMailMessage);
    }
}
