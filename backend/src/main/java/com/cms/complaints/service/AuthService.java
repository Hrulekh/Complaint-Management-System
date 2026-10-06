package com.cms.complaints.service;

import com.cms.complaints.dto.AuthResponse;
import com.cms.complaints.dto.LoginRequest;
import com.cms.complaints.dto.RegisterRequest;
import com.cms.complaints.dto.UserDto;
import com.cms.complaints.entity.Role;
import com.cms.complaints.entity.User;
import com.cms.complaints.exception.BadRequestException;
import com.cms.complaints.repository.UserRepository;
import com.cms.complaints.security.JwtTokenProvider;
import com.cms.complaints.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.COMPLAINANT);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(),
                                                   savedUser.getRole().name());

        UserDto userDto = new UserDto(savedUser.getId(), savedUser.getFullName(),
                                      savedUser.getEmail(), savedUser.getPhone(),
                                      savedUser.getRole(), savedUser.getActive());

        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserDto userDto = new UserDto(user.getId(), user.getFullName(), user.getEmail(),
                                      user.getPhone(), user.getRole(), user.getActive());

        return new AuthResponse(token, userDto);
    }

    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(),
                          user.getPhone(), user.getRole(), user.getActive());
    }
}
