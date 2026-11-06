package epam.springboottask.controller;

import epam.springboottask.dto.AdminStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String ADMIN_STATUS = "admin access granted";

    @GetMapping("/status")
    public ResponseEntity<AdminStatusResponse> status() {
        return ResponseEntity.ok(new AdminStatusResponse(ADMIN_STATUS));
    }
}
