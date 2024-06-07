package com.nam.cafe.serviceimpl;

import com.nam.cafe.JWT.CustomerUserDetailsService;
import com.nam.cafe.JWT.JwtUtil;
import com.nam.cafe.constants.CafeConstants;
import com.nam.cafe.dao.UserDao;
import com.nam.cafe.entity.User;
import com.nam.cafe.service.UserService;
import com.nam.cafe.utils.CafeUtils;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

  @Autowired private CustomerUserDetailsService customerUserDetailsService;
  @Autowired private UserDao userDao;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private JwtUtil jwtUtil;

  @Override
  public ResponseEntity<String> signUp(Map<String, String> requestMap) {
    log.info("Request received for signUp: {}", requestMap);
    try {
      if (validateSignUpMap(requestMap)) {
        User user = userDao.findByEmail(requestMap.get("email"));
        if (Objects.isNull(user)) {
          userDao.save(getUserFromMap(requestMap));
          return CafeUtils.getResponseEntity(
              CafeConstants.USER_REGISTERED_SUCCESSFULLY, HttpStatus.OK);
        } else {
          return CafeUtils.getResponseEntity(
              CafeConstants.USER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
      } else {
        return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> logIn(Map<String, String> requestMap) {
    log.info("Request received for logIn: {}", requestMap);
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  requestMap.get("email"), requestMap.get("password")));
      if (authentication.isAuthenticated()) {
        if (customerUserDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")) {
          return new ResponseEntity<>(
              "{\"token\":\""
                  + jwtUtil.generateToken(
                      customerUserDetailsService.getUserDetails().getEmail(),
                      customerUserDetailsService.getUserDetails().getRole())
                  + "\"}",
              HttpStatus.OK);
        } else {
          return CafeUtils.getResponseEntity(CafeConstants.USER_NOT_ACTIVE, HttpStatus.BAD_REQUEST);
        }
      }
    } catch (AuthenticationException e) {
      log.info("Exception occurred while login: {}", e.getMessage());
    }
    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST);
  }

  private boolean validateSignUpMap(Map<String, String> requestMap) {
    return requestMap.containsKey("name")
        && requestMap.containsKey("contactNumber")
        && requestMap.containsKey("email")
        && requestMap.containsKey("password");
  }

  private User getUserFromMap(Map<String, String> requestMap) {
    User user = new User();
    user.setName(requestMap.get("name"));
    user.setContactNumber(requestMap.get("contactNumber"));
    user.setEmail(requestMap.get("email"));
    user.setPassword(requestMap.get("password"));
    user.setStatus("ACTIVE");
    user.setRole("USER");
    return user;
  }
}
