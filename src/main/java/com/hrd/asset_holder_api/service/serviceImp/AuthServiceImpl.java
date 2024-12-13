package com.hrd.asset_holder_api.service.serviceImp;

import com.hrd.asset_holder_api.exception.NotFoundException;
import com.hrd.asset_holder_api.jwt.JwtUtil;
import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.response.LoginReq;
import com.hrd.asset_holder_api.model.response.LoginRes;
import com.hrd.asset_holder_api.repository.EnrollmentRepository;
import com.hrd.asset_holder_api.repository.UserRepository;
import com.hrd.asset_holder_api.service.AuthService;
import com.hrd.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtService;
    private final UserRepository appUserRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginRes login(LoginReq authRequest) throws Exception {


        authRequest.setUsername(authRequest.getUsername().toLowerCase());

        final User userDetails = appUserRepository.findUserByUsername(authRequest.getUsername());

        if (userDetails == null){
            throw new NotFoundException( "Username not exists");
        }

        try {
            authenticate(authRequest.getUsername(), authRequest.getPassword());
        } catch (Exception e) {
            if(e.getMessage().contains("USER_DISABLED")) {
                throw new NotFoundException("Account Disabled");
            }
            throw new NotFoundException("Password incorrect");
        }


        String token = jwtService.generateToken(userDetails);

        // Construct and populate LoginRes
        LoginRes loginRes = new LoginRes();
        loginRes.setToken(token);
//        loginRes.setUsername(userDetails.getUsername());
//        loginRes.setRoles(userDetails.getRoles()); // assuming `roles` is a list or similar structure in User

        return loginRes;
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
