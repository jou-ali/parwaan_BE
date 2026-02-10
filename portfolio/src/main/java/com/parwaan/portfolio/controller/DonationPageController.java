package com.parwaan.portfolio.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/donations")
public class DonationPageController {

    @GetMapping("/success")
    public ResponseEntity<String> success() {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <title>Donation Success</title>
                </head>
                <body style="font-family: Arial, sans-serif; margin: 32px;">
                  <h1>Thank you!</h1>
                  <p>Your donation was successful.</p>
                  <p>You can close this page now.</p>
                </body>
                </html>
                """;
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/failure")
    public ResponseEntity<String> failure() {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <title>Donation Failed</title>
                </head>
                <body style="font-family: Arial, sans-serif; margin: 32px;">
                  <h1>Payment failed</h1>
                  <p>Your donation was not completed.</p>
                  <p>Please try again.</p>
                </body>
                </html>
                """;
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
