package com.example.livetask.repository;

import com.example.livetask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // メールアドレスで検索（重複チェックなどで使う）（ログイン処理などで使う）
    Optional<User> findByEmail(String email);

    // メールの存在確認
    boolean existsByEmail(String email);
}

