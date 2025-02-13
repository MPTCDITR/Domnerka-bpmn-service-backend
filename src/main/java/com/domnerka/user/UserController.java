package com.domnerka.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public List<User> getAllUser() {
        return userService.getUsers();
    }

    @PostMapping("")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user.getName(), user.getGender(), user.getEmail(), user.getPassword());
        return ResponseEntity.ok(createdUser);
    }


}
