package com.cms.complaints.repository;

import com.cms.complaints.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ComplaintRepositoryIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_cms")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    private User complainant;
    private User staff;
    private Category category;
    private Priority priority;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        priorityRepository.deleteAll();
        complaintRepository.deleteAll();

        complainant = userRepository.save(new User("User", "user@cms.local", "hash", "9876543210", Role.COMPLAINANT));
        staff = userRepository.save(new User("Staff", "staff@cms.local", "hash", "9876543210", Role.STAFF));
        category = categoryRepository.save(new Category("Test", "Test"));
        priority = priorityRepository.save(new Priority("High", 3, 24));
    }

    @Test
    void testFindComplaintsByStatus() {
        Complaint submitted = new Complaint("CMP-2026-000001", "Title 1", "Desc", category, priority, complainant);
        submitted.setStatus(ComplaintStatus.SUBMITTED);
        complaintRepository.save(submitted);

        Complaint assigned = new Complaint("CMP-2026-000002", "Title 2", "Desc", category, priority, complainant);
        assigned.setStatus(ComplaintStatus.ASSIGNED);
        complaintRepository.save(assigned);

        Page<Complaint> submittedComplaints = complaintRepository.findByStatus(ComplaintStatus.SUBMITTED, PageRequest.of(0, 10));
        assertEquals(1, submittedComplaints.getTotalElements());
        assertEquals("CMP-2026-000001", submittedComplaints.getContent().get(0).getTicketId());
    }

    @Test
    void testFindOverdueComplaints() {
        Complaint overdue = new Complaint("CMP-2026-000001", "Title", "Desc", category, priority, complainant);
        overdue.setDueAt(LocalDateTime.now().minusHours(1));
        overdue.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.save(overdue);

        Complaint notOverdue = new Complaint("CMP-2026-000002", "Title", "Desc", category, priority, complainant);
        notOverdue.setDueAt(LocalDateTime.now().plusHours(1));
        notOverdue.setStatus(ComplaintStatus.ASSIGNED);
        complaintRepository.save(notOverdue);

        List<Complaint> overdueList = complaintRepository.findOverdueNotEscalated(LocalDateTime.now());
        assertEquals(1, overdueList.size());
        assertEquals("CMP-2026-000001", overdueList.get(0).getTicketId());
    }

    @Test
    void testFindComplaintsWithFilters() {
        Complaint complaint1 = new Complaint("CMP-2026-000001", "Test Search", "Desc", category, priority, complainant);
        complaint1.setStatus(ComplaintStatus.SUBMITTED);
        complaintRepository.save(complaint1);

        Complaint complaint2 = new Complaint("CMP-2026-000002", "Another", "Desc", category, priority, complainant);
        complaint2.setStatus(ComplaintStatus.ASSIGNED);
        complaintRepository.save(complaint2);

        Page<Complaint> results = complaintRepository.findWithFilters(
                ComplaintStatus.SUBMITTED, category.getId(), priority.getId(), null,
                null, null, "Search", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("CMP-2026-000001", results.getContent().get(0).getTicketId());
    }

    @Test
    void testCountByStatus() {
        for (int i = 0; i < 3; i++) {
            Complaint c = new Complaint("CMP-2026-00000" + (i+1), "Title", "Desc", category, priority, complainant);
            c.setStatus(ComplaintStatus.SUBMITTED);
            complaintRepository.save(c);
        }

        long count = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        assertEquals(3, count);
    }
}
