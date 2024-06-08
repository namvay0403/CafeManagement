package com.nam.cafe.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

  Claims claims = null;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private CustomerUserDetailsService customerUserDetailsService;
  private String userName = "";

  //  @Override
  //  protected void doFilterInternal(
  //      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
  //      throws ServletException, IOException {
  //    //NOTE: regex for the servlet path don't contain " " (space) between the pipes
  //    if (request
  //        .getServletPath()
  //        .matches("/user/signup|/user/login|/user/logout|/user/forgotPassword")) {
  //      filterChain.doFilter(request, response);
  //      return;
  //    }
  //
  //    String authorizationHeader = request.getHeader("Authorization");
  //    String token = null;
  //    String userName = null;
  //
  //    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
  //      token = authorizationHeader.substring(7);
  //      claims = jwtUtil.extractAllClaims(token);
  //      userName = jwtUtil.extractUsername(token);
  //    }
  //
  //    if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
  //      UserDetails userDetails = customerUserDetailsService.loadUserByUsername(userName);
  //      if (jwtUtil.isTokenValid(token, userDetails)) {
  //        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
  //            new UsernamePasswordAuthenticationToken(
  //                userDetails, null, userDetails.getAuthorities());
  //        usernamePasswordAuthenticationToken.setDetails(
  //            new WebAuthenticationDetailsSource().buildDetails(request));
  //
  //
  // SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
  //      }
  //
  //      filterChain.doFilter(request, response);
  //    }
  //  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String servletPath = request.getServletPath();
    if (servletPath.matches("/user/signup|/user/login|/user/logout|/user/forgotPassword")) {
      filterChain.doFilter(request, response);
      return;
    }

    String authorizationHeader = request.getHeader("Authorization");
    String token = null;
    userName = null;

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      token = authorizationHeader.substring(7);
      try {
        claims = jwtUtil.extractAllClaims(token);
        userName = jwtUtil.extractUsername(token);
      } catch (Exception e) {
        // Log the exception for debugging
        logger.error("JWT extraction error: " + e.getMessage());
        // Handle the exception and send an appropriate response
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        return;
      }
    }

    if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = customerUserDetailsService.loadUserByUsername(userName);
      if (jwtUtil.isTokenValid(token, userDetails)) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      } else {
        // Handle invalid token scenario
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  public boolean isAdmin() {
    return "admin".equalsIgnoreCase((String) claims.get("role"));
  }

  public boolean isUser() {
    return "user".equalsIgnoreCase((String) claims.get("role"));
  }

  public String getCurrentUser() {
    return userName;
  }
}
