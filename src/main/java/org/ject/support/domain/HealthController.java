package org.ject.support.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        System.out.println("Health check endpoint hit");
        return ResponseEntity.ok("OK");
    }
}
