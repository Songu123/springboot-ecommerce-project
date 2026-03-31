package com.son.ecommerce.repository;

import com.son.ecommerce.entity.RefreshToken;
import com.son.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    int deleteByToken(String token);
}

