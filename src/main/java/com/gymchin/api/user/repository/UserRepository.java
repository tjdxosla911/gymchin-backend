package com.gymchin.api.user.repository;

import com.gymchin.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    java.util.Optional<User> findByEmail(String email);
}
