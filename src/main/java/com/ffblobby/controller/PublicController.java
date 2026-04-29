package com.ffblobby.controller;

import com.ffblobby.dto.JnlpResponse;
import com.ffblobby.service.JnlpService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final JnlpService jnlpService;

    public PublicController(JnlpService jnlpService) {
        this.jnlpService = jnlpService;
    }

    @GetMapping("/jnlp")
    public ResponseEntity<Void> getJnlp(
            @RequestParam String environment,
            @RequestParam(required = false) String gameName,
            @RequestParam(required = false) Long gameId) {

        JnlpResponse response = jnlpService.resolveJnlp(environment, gameName, gameId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(response.getJnlpUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
