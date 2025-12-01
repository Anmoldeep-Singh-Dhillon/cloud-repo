package com.example.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

@GetMapping("/hello")
public String hello() {
    return "Hello World from Anmol via feature branch the ultimate devops engineer(he sucks, he is just living in a bubble)";

}

}
