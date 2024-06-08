package com.nam.cafe.serviceimpl;

import com.google.common.base.Strings;
import com.nam.cafe.JWT.CustomerUserDetailsService;
import com.nam.cafe.JWT.JwtFilter;
import com.nam.cafe.JWT.JwtUtil;
import com.nam.cafe.constants.CafeConstants;
import com.nam.cafe.dao.UserDao;
import com.nam.cafe.entity.User;
import com.nam.cafe.service.UserService;
import com.nam.cafe.utils.CafeUtils;
import com.nam.cafe.utils.EmailUtils;
import com.nam.cafe.wrapper.UserWrapper;

import java.util.*;

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
  @Autowired private JwtFilter jwtFilter;
  @Autowired private EmailUtils emailUtils;


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
    return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST);
  }

  @Override
  public ResponseEntity<List<UserWrapper>> getAllUsers() {
    log.info("Request received for getAllUsers");
    try {
      if (jwtFilter.isAdmin()) {
        return new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
      }
    } catch (Exception e) {
      log.info("Exception occurred while fetching all users: {}", e.getMessage());
    }
    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
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
    user.setStatus("true");
    user.setRole("USER");
    return user;
  }

  @Override
  public ResponseEntity<String> checkToken() {
    return CafeUtils.getResponseEntity("true", HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
    try {
      User user = userDao.findByEmail(jwtFilter.getCurrentUser());
      if (Objects.nonNull(user)) {
        if (user.getPassword().equals(requestMap.get("oldPassword"))) {
          user.setPassword(requestMap.get("newPassword"));
            userDao.save(user);
            return CafeUtils.getResponseEntity(CafeConstants.PASSWORD_UPDATED, HttpStatus.OK);
        } else {
          return CafeUtils.getResponseEntity(CafeConstants.INVALID_PASSWORD, HttpStatus.BAD_REQUEST);
        }
      } else {
        return CafeUtils.getResponseEntity(CafeConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
    try{
      User user = userDao.findByEmail(requestMap.get("email"));
      if (Objects.nonNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
        emailUtils.forgotPassword(user.getEmail(), "Reset Password", user.getPassword());
        userDao.save(user);
        return CafeUtils.getResponseEntity(CafeConstants.CHECK_YOUR_EMAIL, HttpStatus.OK);
      } else {
        return CafeUtils.getResponseEntity(CafeConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> update(Map<String, String> requestMap) {
    try {
      if (jwtFilter.isAdmin()) {
        Optional<User> user = userDao.findById(Integer.parseInt(requestMap.get("id")));
        if (!user.isEmpty()) {
          userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
          sendMailToAdmin(requestMap.get("status"), user.get().getEmail(), userDao.getAllAdmins());
          return CafeUtils.getResponseEntity(CafeConstants.USER_STATUS_UPDATED, HttpStatus.OK);
        } else {
          return CafeUtils.getResponseEntity(CafeConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
      } else {
        return CafeUtils.getResponseEntity(
                CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
            CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private void sendMailToAdmin(String status, String email, List<String> allAdmins) {
    // Send mail to all the admins
    allAdmins.remove(jwtFilter.getCurrentUser());
    if (status != null && status.equalsIgnoreCase("true")) {
      // Send mail to all the admins
      emailUtils.sendEmail(jwtFilter.getCurrentUser(), "Account Approved", "Account Aproved for " + email, allAdmins);
    } else {
      // Send mail to the user
      emailUtils.sendEmail(jwtFilter.getCurrentUser(), "Account Disabled", "Account Aproved for " + email, allAdmins);

    }
  }
}
