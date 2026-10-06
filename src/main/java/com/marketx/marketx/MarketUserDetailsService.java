package com.marketx.marketx;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MarketUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public MarketUserDetailsService(
            UserRepository userRepository,
            AdminRepository adminRepository) {

        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        /*
         * =========================================
         * FIRST CHECK ADMIN
         * =========================================
         */

        Admin admin = adminRepository
                .findByUsername(username)
                .orElse(null);

        if (admin != null) {

            return org.springframework.security.core.userdetails.User
                    .withUsername(admin.getUsername())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }


        /*
         * =========================================
         * THEN CHECK NORMAL USER
         * =========================================
         */

        user normalUser = userRepository
                .findByEmail(username)
                .orElse(null);

        if (normalUser != null) {

            String role = normalUser.getRole();

            /*
             * If old users don't have a role,
             * make them BUYER by default.
             */

            if (role == null ||
                role.trim().isEmpty()) {

                role = "BUYER";
            }

            role = role.trim().toUpperCase();


            /*
             * Only allow valid roles
             */

            if (!role.equals("BUYER") &&
                !role.equals("SELLER") &&
                !role.equals("ADMIN")) {

                throw new UsernameNotFoundException(
                        "Invalid user role"
                );
            }


            /*
             * Create Spring Security user
             *
             * BUYER  -> ROLE_BUYER
             * SELLER -> ROLE_SELLER
             * ADMIN  -> ROLE_ADMIN
             */

            return org.springframework.security.core.userdetails.User
                    .withUsername(normalUser.getEmail())
                    .password(normalUser.getPassword())
                    .roles(role)
                    .build();
        }


        /*
         * =========================================
         * USER NOT FOUND
         * =========================================
         */

        throw new UsernameNotFoundException(
                "User not found"
        );
    }
}