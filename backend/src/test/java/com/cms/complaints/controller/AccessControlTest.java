package com.cms.complaints.controller;

import com.cms.complaints.entity.*;
import com.cms.complaints.repository.CategoryRepository;
import com.cms.complaints.repository.ComplaintRepository;
import com.cms.complaints.repository.PriorityRepository;
import com.cms.complaints.repository.UserRepository;
import com.cms.complaints.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String complainantToken;
    private String staffToken;
    private String adminToken;
    private Complaint otherUserComplaint;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        complaintRepository.deleteAll();

        User complainant = new User("Complainant", "complainant@cms.local",
                passwordEncoder.encode("password123"), "9876543210", Role.COMPLAINANT);
        User complainant2 = new User("Other Complainant", "other@cms.local",
                passwordEncoder.encode("password123"), "9876543210", Role.COMPLAINANT);
        User staff = new User("Staff", "staff@cms.local",
                passwordEncoder.encode("password123"), "9876543210", Role.STAFF);
        User admin = new User("Admin", "admin@cms.local",
                passwordEncoder.encode("password123"), "9876543210", Role.ADMIN);

        complainant = userRepository.save(complainant);
        complainant2 = userRepository.save(complainant2);
        staff = userRepository.save(staff);
        admin = userRepository.save(admin);

        complainantToken = tokenProvider.generateToken(complainant.getId(),
                complainant.getEmail(), complainant.getRole().name());
        staffToken = tokenProvider.generateToken(staff.getId(),
                staff.getEmail(), staff.getRole().name());
        adminToken = tokenProvider.generateToken(admin.getId(),
                admin.getEmail(), admin.getRole().name());

        Category category = new Category("Test", "Test");
        categoryRepository.save(category);

        Priority priority = new Priority("High", 3, 24);
        priorityRepository.save(priority);

        otherUserComplaint = new Complaint("CMP-2026-000001", "Title", "Description",
                category, priority, complainant2);
        complaintRepository.save(otherUserComplaint);
    }

    @Test
    void testComplainantCannotViewOthersComplaint() throws Exception {
        mockMvc.perform(get("/api/complaints/" + otherUserComplaint.getId())
                .header("Authorization", "Bearer " + complainantToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAdminCanViewAnyComplaint() throws Exception {
        mockMvc.perform(get("/api/complaints/" + otherUserComplaint.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthenticatedCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/complaints/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testComplainantCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", "Bearer " + complainantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStaffCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
