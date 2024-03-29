package com.example.springstudy.user.repository;

import com.example.springstudy.user.entity.User;
import com.example.springstudy.user.model.UserNoticeCount;
import com.example.springstudy.user.model.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    int countByEmail(String email);

    Optional<User> findByIdAndPassword(Long id, String password);

    Optional<User> findByUserNameAndPhone(String userName, String phone);

    Optional<User> findByEmail(String email);

     List<User> findByEmailContainsOrPhoneContainsOrUserNameContains(String email, String phone,
        String userName);

    long countByStatus(UserStatus userStatus);

    @Query("select u from User u where u.regDate between :startDate and :endDate ")
    List<User> findToday(LocalDateTime startDate, LocalDateTime endDate);

}
