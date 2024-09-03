package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.service.serviceImp.SingInService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class SingInController{

    private final SingInService service;

    public SingInController(SingInService service) {
        this.service = service;
    }

    @PostMapping("/UserRegistration")
    public String UserRegistration(@RequestParam String userName, @RequestParam String password, @RequestParam String email,
                                   @RequestParam boolean mailConfirmation,@RequestParam Role role, @RequestParam String location) {
        User user = new User(userName, email, password, mailConfirmation, role, LocalDateTime.now(), location);
        return  service.UserRegistration(user);
    }
    @GetMapping("/GetUsers")
    private List<User> GetUsers(){
         return service.GetUsers();
    }

    @GetMapping("/EmailVerificationCheck/{email}/{password}")
    public String EmailVerificationCheck(@PathVariable String email,@PathVariable String password) {
        if(email != null && password != null ){
        return service.EmailVerificationCheck(email,password);
        }
        return "email та password не заповнені";
    }

    @PostMapping("/Login")
    public String Login(@RequestParam String email, @RequestParam String password) {
        return service.Login(email,password);
    }
    @GetMapping("/admin/main")
    public String admin(){
        return "hi admin";
    }
    @GetMapping("/secured/main")
    public String secured() {
        return "hi secured";
    }
}
