package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class UserRepositoryAdapterTest {

    @Inject
    UserRepositoryAdapter adapter;

    private final PageRequest defaultPage = new PageRequest(1, 10);

    private void createDefaultUsers() {
        adapter.save(new User(UserId.newId(), new Username("Alice"), "pw_a", Role.ADMIN));
        adapter.save(new User(UserId.newId(), new Username("Bob"), "pw_b", Role.MEMBER));
        adapter.save(new User(UserId.newId(), new Username("Charlie"), "pw_c", Role.TECHNICAL_USER));
    }

    @Test
    void findById_idExists_returnsUser() {
        UserId userId = UserId.newId();
        User user = new User(userId, new Username("testuser"), "hashed_pw_123", Role.MEMBER);

        adapter.save(user);

        Optional<User> result = adapter.findById(userId);
        assertTrue(result.isPresent());

        User loaded = result.get();
        assertEquals(user.getId(), loaded.getId());
        assertEquals(user.getUsername().value(), loaded.getUsername().value());
        assertEquals(user.getRole(), loaded.getRole());
    }

    @Test
    void findById_idMissing_returnsEmpty() {
        UserId nonExistentId = UserId.newId();
        Optional<User> result = adapter.findById(nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_exists_returnsUser() {
        createDefaultUsers();

        Optional<User> result = adapter.findByUsername(new Username("Alice"));

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getUsername().value());
    }

    @Test
    void findByUsername_missing_returnsEmpty() {
        createDefaultUsers();

        Optional<User> result = adapter.findByUsername(new Username("Ghost"));

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_returnsAllUsers() {
        createDefaultUsers();

        Page<User> result = adapter.findAll(defaultPage);

        assertEquals(3, result.content().size());
        assertEquals(3, result.totalElements());

        List<String> names = result.content().stream()
                .map(u -> u.getUsername().value())
                .toList();

        assertTrue(names.containsAll(List.of("Alice", "Bob", "Charlie")));
    }

    @Test
    void findAll_secondPage_returnsSecondUser() {
        createDefaultUsers();

        PageRequest secondPageRequest = new PageRequest(2, 1);

        Page<User> result = adapter.findAll(secondPageRequest);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());

        assertFalse(result.content().isEmpty());
    }

    @Test
    void findAll_pageOutOfBounds_returnsEmptyPage() {
        createDefaultUsers();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);
        Page<User> result = adapter.findAll(outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validUser_persists() {
        UserId id = UserId.newId();
        User user = new User(id, new Username("ValidUser"), "secret", Role.TECHNICAL_USER);

        adapter.save(user);

        Optional<User> entity = adapter.findById(id);
        assertTrue(entity.isPresent());

        User loaded = entity.get();
        assertEquals(id, loaded.getId());
        assertEquals("ValidUser", loaded.getUsername().value());
        assertEquals(Role.TECHNICAL_USER, loaded.getRole());
    }

    @Test
    void save_duplicateUsername_throwsException() {
        Username sharedName = new Username("UniqueUser");
        adapter.save(new User(UserId.newId(), sharedName, "pw1", Role.MEMBER));

        User duplicate = new User(UserId.newId(), sharedName, "pw2", Role.ADMIN);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void delete_idExists_removesUser() {
        UserId id = UserId.newId();
        adapter.save(new User(id, new Username("UserToDelete"), "pw", Role.MEMBER));

        assertTrue(adapter.findById(id).isPresent());

        adapter.delete(id);

        Optional<User> result = adapter.findById(id);
        assertTrue(result.isEmpty());

        Optional<User> byName = adapter.findByUsername(new Username("UserToDelete"));
        assertTrue(byName.isEmpty());
    }

    @Test
    void delete_idMissing_doesNothing() {
        UserId existingId = UserId.newId();
        adapter.save(new User(existingId, new Username("Test"), "pw", Role.MEMBER));

        UserId nonExistentId = UserId.newId();
        adapter.delete(nonExistentId);

        assertTrue(adapter.findById(existingId).isPresent());
    }
}