package com.mcart.customer.web;

import com.mcart.customer.domain.Customer;
import com.mcart.customer.repo.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    CustomerRepo repo;

    @GetMapping("/me")
    public Customer me(@AuthenticationPrincipal Jwt jwt) {
        String uid = jwt.getSubject(); // Firebase/IdP UID in 'sub'
        return repo.findById(uid).orElse(null);
    }

    @PostMapping("/me")
    public Customer save(@AuthenticationPrincipal Jwt jwt, @RequestBody Customer body) {
        String uid = jwt.getSubject();
        Customer c = repo.findById(uid).orElse(new Customer(uid, body.getEmail()));
        c.setName(body.getName());
        c.setPhone(body.getPhone());
        c.setAddress(body.getAddress());
        return repo.save(c);
    }

}
