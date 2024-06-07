package com.nam.cafe.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  Claims claims = null;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private CustomerUserDetailsService customerUserDetailsService;
  private String userName = null;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request
        .getServletPath()
        .matches("/user/signup | /user/login | /user/logout | /user/forgotPassword")) {
      filterChain.doFilter(request, response);
    } else {
      String authorizationHeader = request.getHeader("Authorization");
      String token = null;

      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        token = authorizationHeader.substring(7);
        claims = jwtUtil.extractAllClaims(token);
        userName = jwtUtil.extractUsername(token);
      }

      if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(userName);
        if (jwtUtil.isTokenValid(token, userDetails)) {
          UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          usernamePasswordAuthenticationToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
      }
      filterChain.doFilter(request, response);
    }
  }

  public boolean isAdmin() {
    return claims.get("role").equals("admin");
  }

  public boolean isUser() {
    return claims.get("role").equals("user");
  }

  public String getCurrentUser(){
    return userName;
  }
}
