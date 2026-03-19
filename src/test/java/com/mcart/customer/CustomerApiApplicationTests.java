package com.mcart.customer;

import com.mcart.customer.domain.Customer;
import com.mcart.customer.repo.CustomerRepo;
import com.mcart.customer.web.CustomerController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerApiApplicationTests {

    @Mock
    private CustomerRepo repo;

    @InjectMocks
    private CustomerController controller;

    @Test
    void saveCreatesCustomerForAuthenticatedUserAndUpdatesAllMutableFields() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .claim("sub", "user-123")
                .build();

        Customer body = new Customer("ignored", "new@example.com");
        body.setName("Alice");
        body.setPhone("9999999999");
        body.setAddress("Delhi");

        when(repo.findById("user-123")).thenReturn(Optional.empty());
        when(repo.save(org.mockito.ArgumentMatchers.any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer saved = controller.save(jwt, body);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repo).save(captor.capture());
        Customer persisted = captor.getValue();

        assertEquals("user-123", persisted.getUid());
        assertEquals("new@example.com", persisted.getEmail());
        assertEquals("Alice", persisted.getName());
        assertEquals("9999999999", persisted.getPhone());
        assertEquals("Delhi", persisted.getAddress());
        assertEquals("user-123", saved.getUid());
    }

    @Test
    void saveUpdatesExistingCustomerEmailAndProfileFields() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .claim("sub", "user-123")
                .build();

        Customer existing = new Customer("user-123", "old@example.com");
        existing.setName("Old Name");

        Customer body = new Customer("ignored", "updated@example.com");
        body.setName("New Name");
        body.setPhone("12345");
        body.setAddress("Pune");

        when(repo.findById("user-123")).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        Customer saved = controller.save(jwt, body);

        assertEquals("updated@example.com", saved.getEmail());
        assertEquals("New Name", saved.getName());
        assertEquals("12345", saved.getPhone());
        assertEquals("Pune", saved.getAddress());
    }

    @Test
    void meReturnsNullWhenCurrentUserHasNoCustomerRecord() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("missing-user")
                .claim("sub", "missing-user")
                .build();

        when(repo.findById("missing-user")).thenReturn(Optional.empty());

        assertNull(controller.me(jwt));
    }

    @Test
    void customerSetsUpdatedAtDuringLifecycleCallbacks() throws Exception {
        Customer customer = new Customer("user-123", "test@example.com");
        Method touchUpdatedAt = Customer.class.getDeclaredMethod("touchUpdatedAt");
        touchUpdatedAt.setAccessible(true);

        touchUpdatedAt.invoke(customer);
        OffsetDateTime firstUpdatedAt = customer.getUpdatedAt();

        assertNotNull(firstUpdatedAt);

        touchUpdatedAt.invoke(customer);
        OffsetDateTime secondUpdatedAt = customer.getUpdatedAt();

        assertNotNull(secondUpdatedAt);
        assertFalse(secondUpdatedAt.isBefore(firstUpdatedAt));
    }
}
