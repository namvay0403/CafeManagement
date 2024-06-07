package com.nam.cafe.JWT;

import com.nam.cafe.constants.CafeConstants;
import com.nam.cafe.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
@Slf4j
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    private com.nam.cafe.entity.User userDetails;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Request received for loadUserByUsername: {}", username);
        userDetails = userDao.findByEmail(username);
        if (Objects.isNull(userDetails)) {
            throw new UsernameNotFoundException(CafeConstants.USER_NOT_FOUND + username);
        } else {
            return new User(userDetails.getEmail(), userDetails.getPassword(), new ArrayList<>());
        }
    }

    public com.nam.cafe.entity.User getUserDetails() {
        return userDetails;
    }
}
