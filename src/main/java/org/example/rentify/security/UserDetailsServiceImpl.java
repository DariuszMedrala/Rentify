package org.example.rentify.security;

import org.example.rentify.entity.User;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/*
 * UserDetailsServiceImpl class implements the UserDetailsService interface.
 * It is responsible for loading user-specific data during authentication.
 * This class is annotated with @Service to indicate that it is a service component.
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    /**
     * This method loads user details by username.
     * It retrieves the user from the database using the UserRepository.
     *
     * @param username the username of the user
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional // Important for lazy loading of roles if not EAGER fetched
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return user;
    }
}