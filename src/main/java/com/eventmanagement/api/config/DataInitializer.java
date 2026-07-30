package com.eventmanagement.api.config;

import com.eventmanagement.api.entity.EventInquiry;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import com.eventmanagement.api.entity.Role;
import com.eventmanagement.api.entity.User;
import com.eventmanagement.api.repository.EventInquiryRepository;
import com.eventmanagement.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final EventInquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            logger.info("Initializing seed database users and sample event inquiries...");

            // Create Admin User
            User admin = User.builder()
                    .fullName("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);

            // Create Standard User 1
            User user1 = User.builder()
                    .fullName("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user1);

            // Create Standard User 2
            User user2 = User.builder()
                    .fullName("Jane Smith")
                    .email("jane@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user2);

            // Create Sample Event Inquiries for John Doe
            EventInquiry inquiry1 = EventInquiry.builder()
                    .user(user1)
                    .customerName("John Doe")
                    .customerEmail("john@example.com")
                    .customerPhone("+15550192834")
                    .eventType(EventType.WEDDING)
                    .eventDate(LocalDate.now().plusMonths(6))
                    .location("Grand Palace Hotel, New York")
                    .estimatedBudget(new BigDecimal("25000.00"))
                    .guestCount(150)
                    .status(InquiryStatus.PENDING)
                    .specialRequests("Requires vegetarian catering options and stage setup.")
                    .build();

            EventInquiry inquiry2 = EventInquiry.builder()
                    .user(user1)
                    .customerName("John Doe")
                    .customerEmail("john@example.com")
                    .customerPhone("+15550192834")
                    .eventType(EventType.CORPORATE)
                    .eventDate(LocalDate.now().plusMonths(2))
                    .location("Tech Center Convention Hall, San Francisco")
                    .estimatedBudget(new BigDecimal("12000.00"))
                    .guestCount(80)
                    .status(InquiryStatus.CONFIRMED)
                    .specialRequests("Audio/Visual equipment required for tech presentations.")
                    .build();

            // Create Sample Event Inquiry for Jane Smith
            EventInquiry inquiry3 = EventInquiry.builder()
                    .user(user2)
                    .customerName("Jane Smith")
                    .customerEmail("jane@example.com")
                    .customerPhone("+15559876543")
                    .eventType(EventType.BIRTHDAY)
                    .eventDate(LocalDate.now().plusMonths(1))
                    .location("Seaside Pavilion, Miami")
                    .estimatedBudget(new BigDecimal("5000.00"))
                    .guestCount(40)
                    .status(InquiryStatus.UNDER_REVIEW)
                    .specialRequests("Beachfront setup with DJ table.")
                    .build();

            inquiryRepository.save(inquiry1);
            inquiryRepository.save(inquiry2);
            inquiryRepository.save(inquiry3);

            logger.info("Sample database initialization completed successfully.");
        }
    }
}
