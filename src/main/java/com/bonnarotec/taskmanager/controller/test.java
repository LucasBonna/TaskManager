package com.bonnarotec.taskmanager.controller;

import com.bonnarotec.taskmanager.dto.hello.HelloWorldResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
public class test {

    @GetMapping()
    public ResponseEntity<HelloWorldResponse> helloWorld() {
        return ResponseEntity.ok(new HelloWorldResponse("Hello World!"));
    }
}
