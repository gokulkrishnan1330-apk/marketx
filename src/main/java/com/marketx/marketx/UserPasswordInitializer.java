package com.marketx.marketx;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordInitializer
        implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserPasswordInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        List<user> users =
                userRepository.findAll();

        for (user user : users) {

            String password =
                    user.getPassword();

            if (
                    password != null &&
                    !password.startsWith("$2a$") &&
                    !password.startsWith("$2b$") &&
                    !password.startsWith("$2y$")
            ) {

                user.setPassword(
                        passwordEncoder.encode(password)
                );

                userRepository.save(user);

                System.out.println(
                        "User password converted to BCrypt: "
                        + user.getEmail()
                );
            }
        }
    }
}