package com.company.discussion.controllers;

import com.company.discussion.config.JwtToken;
import com.company.discussion.models.JwtRequest;
import com.company.discussion.models.JwtResponse;
import com.company.discussion.services.JwtUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private JwtUserDetailService jwtUserDetailService;

    @RequestMapping(value="/authenticate", method= RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        // Check the database if username exists
        // Retrieve the user with the same username
        final UserDetails USER_DETAILS = jwtUserDetailService.loadUserByUsername(authenticationRequest.getUsername());

        // Generate the jwt including the username and user id
        final String token = jwtToken.generateToken(USER_DETAILS);

        // Respond with the JWT in the client (e.g. ghd1uikh3iurhi12.f32huilhflku32)
        return ResponseEntity.ok(new JwtResponse(token));

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
