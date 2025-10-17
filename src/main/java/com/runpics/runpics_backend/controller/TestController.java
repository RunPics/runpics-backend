package com.runpics.runpics_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/login")
    public String testLogin() {
        return "스프링 시큐리티 테스트 성공!";
    }
}
