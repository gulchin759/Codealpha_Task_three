package org.example.stocktradingplatform.Security;

import org.example.stocktradingplatform.Entity.Userr;
import org.example.stocktradingplatform.Reposity.UserReposity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserReposity userReposity;

    public CustomUserDetailsService(UserReposity userReposity) {
        this.userReposity = userReposity;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Userr user = userReposity.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = (user.getRole() != null) ? user.getRole().name() : "ROLE_USER";

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}
