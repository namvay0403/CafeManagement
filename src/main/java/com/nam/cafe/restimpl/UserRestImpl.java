package com.nam.cafe.restimpl;

import com.nam.cafe.JWT.JwtFilter;
import com.nam.cafe.constants.CafeConstants;
import com.nam.cafe.dao.UserDao;
import com.nam.cafe.entity.User;
import com.nam.cafe.rest.UserRest;
import com.nam.cafe.service.UserService;
import com.nam.cafe.utils.CafeUtils;
import com.nam.cafe.utils.EmailUtils;
import com.nam.cafe.wrapper.UserWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserRestImpl implements UserRest {
  @Autowired private UserDao userDao;

  @Autowired private UserService userService;
  @Autowired private JwtFilter jwtFilter;

  @Override
  public ResponseEntity<String> signUp(Map<String, String> requestMap) {
    try {
      return userService.signUp(requestMap);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> logIn(Map<String, String> requestMap) {
    try {
      return userService.logIn(requestMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<List<UserWrapper>> getAllUsers() {
    try {
      return userService.getAllUsers();
    } catch (Exception e) {
      log.info("Exception in getAllUsers: {}", e.getMessage());
    }
    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> update(Map<String, String> requestMap) {
    try {
      return userService.update(requestMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
            CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> checkToken() {
    try {
      return userService.checkToken();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
        CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
    try {
      return userService.changePassword(requestMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
            CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
    try {
      return userService.forgotPassword(requestMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return CafeUtils.getResponseEntity(
            CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
  }


}
