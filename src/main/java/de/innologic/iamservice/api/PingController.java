package de.innologic.iamservice.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/v1/ping")
    public String ping() {
        return "ok";
    }
}
