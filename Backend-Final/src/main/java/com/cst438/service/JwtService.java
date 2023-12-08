package com.cst438.service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.net.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

@Component
public class JwtService {
    static final long EXPIRATION_TIME = 86400000; // 1 day in ms
    static final String PREFIX = "Bearer";
    private final Key key;

    public JwtService() {
        // Generate secret key from the provided string
        final String secretString = "21825474757a1a6de4d4fe60d8826aebb132127b6baa8152a1210277ef6da3921825474757a1a6de4d4fe60d8826aebb132127b6baa8152a1210277ef6da39";
        byte[] decodedSecret = new byte[secretString.length() / 2];
        for (int i = 0; i < decodedSecret.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(secretString.substring(index, index + 2), 16);
            decodedSecret[i] = (byte) j;
        }
        key = Keys.hmacShaKeyFor(decodedSecret);
    }

    // Method to generate a JWT token based on the username
    public String generateToken(String username) {
        if (StringUtils.hasText(username)) {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } else {
            throw new IllegalArgumentException("Username cannot be empty or null.");
        }
    }

    // Method to extract username from the JWT token in the request header
    public String getAuthUser(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith(PREFIX)) {
            token = token.replace(PREFIX, "").trim();
            String user = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            if (user != null) {
                return user;
            }
        }

        return null;
    }

}
