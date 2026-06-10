package auth.service.xflow_auth_service.repositories;

import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.active = :active)")
    Page<User> findWithFilters(
        @Param("email") String email,
        @Param("role") UserRole role,
        @Param("active") Boolean active,
        Pageable pageable
    );
}