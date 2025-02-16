package com.qorvia.eventmanagementservice.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class JwtService {
    private final String jwtSecret = "ZmFrbKapka2Y7YWprZGZqYTtsZGZrYW";

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public boolean validateToken(String token) throws Exception {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new Exception("Jwt token is expired or is invalid");
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJWTFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    String jwtToken = cookie.getValue();
                    logger.info("Jwt key : {}", jwtToken);
                    if (StringUtils.hasText(jwtToken)) {
                        return jwtToken;
                    }
                }
            }
        }
        return null;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public Long getUserIdFormRequest(HttpServletRequest request) {
        String token = getJWTFromRequest(request);
        if (token!= null){
            return getUserIdFromToken(token);
        }
        return null;
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userEmail", String.class);
    }

}
