package com.bonnarotec.taskmanager.controller;

import com.bonnarotec.taskmanager.dto.MessageReturn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/tasks")
public class TaskController {
    @GetMapping()
    public ResponseEntity<MessageReturn> getMessage() {
        return ResponseEntity.ok(new MessageReturn("OK"));
    }
}
