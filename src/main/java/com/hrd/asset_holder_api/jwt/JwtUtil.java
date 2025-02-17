package com.hrd.asset_holder_api.jwt;

import com.hrd.asset_holder_api.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {
    public static final long JWT_TOKEN_TIME = 24 * 60 * 60 ;//5 hour
    public static final String SECRET = "5465464bcd3967c1859c1c9eeb365dc8ebd62e782dbfa7e094b6e40404dcdb8b15f4bcd3967c1859c1c9eeb365dc8ebd62e782dbfa7e094b6e40404dcdb8b15f";
    private String createToken(Map<String, Object> claim, String subject){
        return Jwts.builder()
                .setClaims(claim)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_TIME * 1000))
                .signWith(getSignKey() , SignatureAlgorithm.HS256).compact();
    }


    public Key getSignKey(){
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    //2. generate token for user
    public String generateToken(User appUser){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", appUser.getUserId());
        claims.put("role",appUser.getRoles());
        claims.put("password",appUser.getPassword());
        return createToken(claims, appUser.getUsername());
    }

    //3. retrieving any information from token we will need the secret key
    private Claims extractAllClaim(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //4. extract a specific claim from the JWT token's claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaim(token);
        return claimsResolver.apply(claims);
    }

    //5. retrieve username from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //6. retrieve expiration date from JWT token
    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //7. check expired token
    private Boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    //8. validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
