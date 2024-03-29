package com.example.springstudy.common.model.controller;

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
import com.example.springstudy.user.entity.UserLoginHistory;
import com.example.springstudy.user.exception.ExistsEmailException;
import com.example.springstudy.user.exception.PasswordNotMatchException;
import com.example.springstudy.user.exception.UserNotFoundException;
import com.example.springstudy.user.model.ResponseMessage;
import com.example.springstudy.user.model.UserInput;
import com.example.springstudy.user.model.UserInputFind;
import com.example.springstudy.user.model.UserInputPassword;
import com.example.springstudy.user.model.UserLogCount;
import com.example.springstudy.user.model.UserLogin;
import com.example.springstudy.user.model.UserLoginToken;
import com.example.springstudy.user.model.UserNoticeCount;
import com.example.springstudy.user.model.UserResponse;
import com.example.springstudy.user.model.UserSearch;
import com.example.springstudy.user.model.UserStatusInput;
import com.example.springstudy.user.model.UserSummary;
import com.example.springstudy.user.model.UserUpdate;
import com.example.springstudy.user.repository.UserLoginHistoryRepository;
import com.example.springstudy.user.repository.UserRepository;
import com.example.springstudy.user.service.UserService;
import com.example.springstudy.util.JWTUtils;
import com.example.springstudy.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
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

    private final NoticeRepository noticeRepository;

    private final UserLoginHistoryRepository userLoginHistoryRepository;

    private final UserService userService;

    /**
     * 48. 사용자 목록 과 사용자 수를 함께 내리는 REST API를 작성해 보기
     * - ResponseData의 구조를 아래와 같이 형식으로 작성해서 결과 리턴
     * ```
     * {
     *     "totalCount": N
     *     , "data": user목록 컬렉션
     * }
     */

//    @GetMapping("/api/admin/user")
//    public ResponseMessage userList() {
//
//        List<User> userList = userRepository.findAll();
//        long totalUserCount = userRepository.count();
//
//        return ResponseMessage.builder()
//                    .totalCount(totalUserCount)
//                    .data(userList)
//                    .build();
//    }

    /**
     * 49. 사용자 상세 조회를 조회하는 API를 아래 조건에 맞게 구현해 보기
     * - ResponseMessage 클래스로 추상화해서 전송
     * ```
     * {
     *     "header":
     *     {
     *         result: true|false
     *         , resultCode: string
     *         , message: error message or alert message
     *         , status: http result code
     *     },
     *     "body": 내려야 할 데이터가 있는 경우 body를 통해서 전송
     * }
     * ```
     */

    @GetMapping("/api/admin/user/{id}")
    public ResponseEntity<?> userDetail(@PathVariable Long id) {

        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            return new ResponseEntity<>(ResponseMessage.fail(
                "사용자 정보가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().body(ResponseMessage.success(user));
    }

    /**
     * 50. 사용자 목록 조회에 대한 검색을 리턴하는 API를 작성해 보기.
     * - 이메일, 이름, 전화번호에 대한 검색결과를 리턴(각 항목은 or 조건)
     */

    @GetMapping("/api/admin/user/search")
    public ResponseEntity<?> findUser(@RequestBody UserSearch userSearch) {

        // email like '%' || email || '%'
        // email like concat('%', email, '%')

        List<User> userList = userRepository.findByEmailContainsOrPhoneContainsOrUserNameContains(
            userSearch.getEmail(), userSearch.getPhone(), userSearch.getUserName());

        return ResponseEntity.ok().body(ResponseMessage.success(userList));
    }

    /**
     * 51. 사용자의 상태를 변경하는 API를 작성해 보기
     * - 사용자 상태: (정상, 정지)
     * - 이에 대한 플래그값은 사용자상태(정상:Using, 정지:Stop)
     */

    @PatchMapping("/api/admin/user/{id}/status")
    public ResponseEntity<?> userStatus(@PathVariable Long id, @RequestBody UserStatusInput userStatusInput) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(ResponseMessage.fail("사용자 정보가 존재하지 않습니다."),
                HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        user.setStatus(userStatusInput.getStatus());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    /**
     * 52. 사용자 정보를 삭제하는 API를 작성해 보기
     * - 작성된 게시글이 있으면 예외 발생 처리
     */

    @DeleteMapping("/api/admin/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(ResponseMessage.fail("사용자 정보가 존재하지 않습니다."),
                HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();
        if (noticeRepository.countByUser(user) > 0) {
            return new ResponseEntity<>(ResponseMessage.fail("사용자가 작성한 공지사항이 있습니다."),
                HttpStatus.BAD_REQUEST);
        }

        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }

    /**
     * 53. 사용자가 로그인을 했을때 이에 대한 접속 이력이 저장된다고 했을 때, 이에 대한 접속 이력을 조회하는 API를 작성해 보기
     * - 접속이력 정보가 있다는 가정하에 API작성
     * - UserLoginHistory 엔터티를 통해서 구현
     */

    @GetMapping("/api/admin/user/login/{id}/history")
    public ResponseEntity<?> userLoginHistory() {

        List<UserLoginHistory> userLoginHistories = userLoginHistoryRepository.findAll();

        return ResponseEntity.ok().body(userLoginHistories);
    }

    /**
     * 54. 사용자의 접속을 제한하는 API를 구현해 보기
     */

    @PatchMapping("/api/admin/user/{id}/lock")
    public ResponseEntity<?> userLock(@PathVariable Long id) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(ResponseMessage.fail("사용자 정보가 존재하지 않습니다."),
                HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        if (user.isLockYn()) {
            return new ResponseEntity<>(ResponseMessage.fail("이미 접속제한이 된 사용자 입니다.")
                , HttpStatus.BAD_REQUEST);
        }

        user.setLockYn(true);
        userRepository.save(user);

        return ResponseEntity.ok().body(ResponseMessage.success());
    }

    /**
     * 55. 사용자의 접속제한을 해제하는 API를 구현해 보기.
     */
    @PatchMapping("/api/admin/user/{id}/unlock")
    public ResponseEntity<?> userUnLock(@PathVariable Long id) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(ResponseMessage.fail("사용자 정보가 존재하지 않습니다."),
                HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        if (!user.isLockYn()) {
            return new ResponseEntity<>(ResponseMessage.fail("이미 접속제한이 해제된 사용자 입니다.")
                , HttpStatus.BAD_REQUEST);
        }

        user.setLockYn(false);
        userRepository.save(user);

        return ResponseEntity.ok().body(ResponseMessage.success());
    }

    /**
     * ####56. 회원 전체수와 상태별 회원수에 대한 정보를 리턴하는 API를 작성해 보기
     * - 서비스 클래스를 이용해서 작성해 보세요.
     */

    @GetMapping("/api/admin/user/status/count")
    public ResponseEntity<?> userStatusCount() {

        UserSummary userSummary = userService.getUserStatusCount();

        return ResponseEntity.ok().body(ResponseMessage.success(userSummary));
    }

    /**
     * 57. 오늘의 사용자 가입 목록을 리턴하는 API를 작성해 보기
     * - 서비스를 이용해서 REST API를 작성해 보세요.
     */

    @GetMapping("/api/admin/user/today")
    public ResponseEntity<?> todayUser() {

        List<User> users = userService.getTodayUsers();

        return ResponseEntity.ok().body(ResponseMessage.success(users));
    }

    /**
     * 58. 사용자별 공지사항의 게시글수 리턴하는 API를 작성해 보기
     */

    @GetMapping("/api/admin/user/notice/count")
    public ResponseEntity<?> userNoticeCount() {

        List<UserNoticeCount> userNoticeCountList = userService.getUserNoticeCount();

        return ResponseEntity.ok().body(ResponseMessage.success(userNoticeCountList));
    }

    /**
     * 59. 사용자별 게시글수와 좋아요수를 리턴하는 API를 작성해 보기
     */

    @GetMapping("/api/admin/user/log/count")
    public ResponseEntity<?> userLogCount() {

        List<UserLogCount> userLogCounts = userService.getUserLogCount();

        return ResponseEntity.ok().body(ResponseMessage.success(userLogCounts));
    }

    /**
     * 60. 좋아요를 가장 많이 한 사용자 목록(10개)을 리턴하는 API를 작성해 보기
     */

    @GetMapping("/api/admin/user/like/best")
    public ResponseEntity<?> bestLikeCount() {

        List<UserLogCount> userLogCounts = userService.getUserLikeBest();

        return ResponseEntity.ok().body(ResponseMessage.success(userLogCounts));
    }


}
