package com.example.livetask.repository;

import com.example.livetask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ユーザー名で検索（ログイン処理などで使う）
    Optional<User> findByUsername(String username);

    // メールアドレスで検索（重複チェックなどで使う）
    Optional<User> findByEmail(String email);

    // ユーザー名の存在確認（バリデーションなどで使う）
    boolean existsByUsername(String username);

    // メールの存在確認
    boolean existsByEmail(String email);
}

