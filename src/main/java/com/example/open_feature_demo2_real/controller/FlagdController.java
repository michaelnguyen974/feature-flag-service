package com.example.open_feature_demo2_real.controller;

import com.example.open_feature_demo2_real.service.FlagdFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/flagd")
@CrossOrigin(origins = "*")
public class FlagdController {

    private static final Logger log = LoggerFactory.getLogger(FlagdController.class);
    private final FlagdFileService flagdFileService;

    @Autowired
    public FlagdController(FlagdFileService flagdFileService) {
        this.flagdFileService = flagdFileService;
    }

    @GetMapping("/flags")
    public ResponseEntity<Map<String, Object>> getAllFlags() {
        try {
            Map<String, Object> flags = flagdFileService.getAllFlags();
            log.info("Received GET request from UI, sending all flags: {}", flags);
            return ResponseEntity.ok(flags);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/flags/{flagName}/toggle")
    public ResponseEntity<String> toggleFlag(@PathVariable String flagName) {
        try {
            flagdFileService.toggleFlag(flagName);
            return ResponseEntity.ok("Flag '" + flagName + "' toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to toggle flag: " + e.getMessage());
        }
    }

    @PostMapping("/flags/enable-all")
    public ResponseEntity<String> enableAllFlags() {
        try {
            flagdFileService.enableAllFlags();
            return ResponseEntity.ok("All flags enabled successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to enable all flags: " + e.getMessage());
        }
    }

    @PostMapping("/flags/disable-all")
    public ResponseEntity<String> disableAllFlags() {
        try {
            log.info("Received POST request to disable all flags");

            flagdFileService.disableAllFlags();
            return ResponseEntity.ok("All flags disabled successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to disable all flags: " + e.getMessage());
        }
    }
}
