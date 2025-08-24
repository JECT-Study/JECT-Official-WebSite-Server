package org.ject.support.domain;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Operation(hidden = true)
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        System.out.println("Health check endpoint hit2");
        return ResponseEntity.ok("OK");
    }
}
