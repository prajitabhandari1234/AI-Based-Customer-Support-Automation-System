package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserCreateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.ChatRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.RegisterRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests enum parsing aliases and Jakarta Bean Validation rules used by
 * request DTOs and ticket AI score fields.
 */
class EnumAndValidationUnitTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    /**
     * Creates the validator shared by the validation tests.
     */
    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    /**
     * Closes the validator factory after all tests have completed.
     */
    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void userRoleAliasesAreAcceptedCaseInsensitively() {
        assertEquals(UserRole.CLIENT, UserRole.fromValue("customer"));
        assertEquals(UserRole.AGENT, UserRole.fromValue("SUPPORT_AGENT"));
        assertEquals(UserRole.ADMIN, UserRole.fromValue(" admin "));
        assertNull(UserRole.fromValue(null));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromValue("root"));
    }

    @Test
    void ticketStatusAliasAndCaseAreAccepted() {
        assertEquals(TicketStatus.OPEN, TicketStatus.fromValue("new"));
        assertEquals(TicketStatus.IN_PROGRESS, TicketStatus.fromValue("in_progress"));
        assertEquals(TicketStatus.CLOSED, TicketStatus.fromValue(" closed "));
        assertNull(TicketStatus.fromValue(null));
        assertThrows(IllegalArgumentException.class, () -> TicketStatus.fromValue("done"));
    }

    @Test
    void senderTypeAliasesAreAccepted() {
        assertEquals(SenderType.CLIENT, SenderType.fromValue("customer"));
        assertEquals(SenderType.AGENT, SenderType.fromValue("support_agent"));
        assertEquals(SenderType.AI, SenderType.fromValue("ai"));
        assertThrows(IllegalArgumentException.class, () -> SenderType.fromValue("bot-user"));
    }

    @Test
    void ticketMessageRequestPrefersMessageThenContent() {
        TicketMessageRequest request = new TicketMessageRequest(" primary ", "fallback");
        assertEquals(" primary ", request.text());

        request.setMessage("   ");
        assertEquals("fallback", request.text());

        request.setContent(null);
        assertNull(request.text());
    }

    @Test
    void registrationValidationCoversNameEmailAndPasswordBoundaries() {
        RegisterRequest valid = new RegisterRequest("AB", "user@example.com", "12345678");
        assertTrue(validator.validate(valid).isEmpty());

        RegisterRequest invalid = new RegisterRequest("A", "not-an-email", "1234567");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(invalid);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void chatValidationRejectsBlankAndOverTenThousandCharacters() {
        assertTrue(validator.validate(new ChatRequest("hello", null)).isEmpty());
        assertFalse(validator.validate(new ChatRequest(" ", null)).isEmpty());
        assertFalse(validator.validate(new ChatRequest("x".repeat(10001), null)).isEmpty());
        assertTrue(validator.validate(new ChatRequest("x".repeat(10000), null)).isEmpty());
    }

    @Test
    void loginValidationRejectsMissingAndInvalidValues() {
        assertTrue(validator.validate(new LoginRequest("user@example.com", "password")).isEmpty());
        assertFalse(validator.validate(new LoginRequest("bad", "")).isEmpty());
    }


    @Test
    void ticketAiScoreValidationEnforcesDocumentedNumericBoundaries() {
        Ticket ticket = new Ticket();
        ticket.setCustomer(new User());
        ticket.setSentimentScore(-1.0);
        ticket.setAiConfidenceScore(1.0);
        assertTrue(validator.validate(ticket).isEmpty());

        ticket.setSentimentScore(-1.01);
        ticket.setAiConfidenceScore(1.01);
        var violations = validator.validate(ticket);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("sentimentScore")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("aiConfidenceScore")));
    }

    @Test
    void adminUserCreationValidationCoversRequiredFields() {
        AdminUserCreateRequest request = new AdminUserCreateRequest(
                "", "broken", "short", null, UserStatus.ACTIVE);
        assertFalse(validator.validate(request).isEmpty());

        request = new AdminUserCreateRequest(
                "Valid User", "valid@example.com", "Password123!", UserRole.AGENT, UserStatus.ACTIVE);
        assertTrue(validator.validate(request).isEmpty());
    }
}
