package com.domnerka.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User createUser(String name, String gender, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setGender(gender);
        user.setEmail(email);
        user.setPassword(password);

        return userRepository.save(user);
    }

}
