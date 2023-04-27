package com.example.springstudy.user.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.example.springstudy.notice.entity.Notice;
import com.example.springstudy.notice.entity.NoticeLike;
import com.example.springstudy.notice.model.NoticeResponse;
import com.example.springstudy.notice.model.ResponseError;
import com.example.springstudy.notice.repository.NoticeLikeRepository;
import com.example.springstudy.notice.repository.NoticeRepository;
import com.example.springstudy.user.entity.User;
import com.example.springstudy.user.exception.ExistsEmailException;
import com.example.springstudy.user.exception.PasswordNotMatchException;
import com.example.springstudy.user.exception.UserNotFoundException;
import com.example.springstudy.user.model.ResponseMessage;
import com.example.springstudy.user.model.UserInput;
import com.example.springstudy.user.model.UserInputFind;
import com.example.springstudy.user.model.UserInputPassword;
import com.example.springstudy.user.model.UserLogin;
import com.example.springstudy.user.model.UserLoginToken;
import com.example.springstudy.user.model.UserResponse;
import com.example.springstudy.user.model.UserUpdate;
import com.example.springstudy.user.repository.UserRepository;
import com.example.springstudy.util.JWTUtils;
import com.example.springstudy.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ApiAdminUserController {

    private final UserRepository userRepository;

    /**
     * 48. 사용자 목록 과 사용자 수를 함께 내리는 REST API를 작성해 보기
     * - ResponseData의 구조를 아래와 같이 형식으로 작성해서 결과 리턴
     * ```
     * {
     *     "totalCount": N
     *     , "data": user목록 컬렉션
     * }
     */

    @GetMapping("/api/admin/user")
    public ResponseMessage userList() {

        List<User> userList = userRepository.findAll();
        long totalUserCount = userRepository.count();

        return ResponseMessage.builder()
                    .totalCount(totalUserCount)
                    .data(userList)
                    .build();
    }

}
