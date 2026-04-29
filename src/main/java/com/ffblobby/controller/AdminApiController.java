package com.ffblobby.controller;

import com.ffblobby.dto.BackendServerRequest;
import com.ffblobby.dto.BackendServerResponse;
import com.ffblobby.service.BackendServerService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/backends")
public class AdminApiController {

    private final BackendServerService backendServerService;

    public AdminApiController(BackendServerService backendServerService) {
        this.backendServerService = backendServerService;
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody BackendServerRequest request) {
        try {
            BackendServerResponse response = backendServerService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Backend with name '" + request.getName() + "' already exists");
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deregister(@PathVariable String name) {
        backendServerService.deregister(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BackendServerResponse>> listAll() {
        return ResponseEntity.ok(backendServerService.listAll());
    }

    @PutMapping("/{name}/primary")
    public ResponseEntity<BackendServerResponse> setPrimary(@PathVariable String name) {
        BackendServerResponse response = backendServerService.setPrimary(name);
        return ResponseEntity.ok(response);
    }
}
