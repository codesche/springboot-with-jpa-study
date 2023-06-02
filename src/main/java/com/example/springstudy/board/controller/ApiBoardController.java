package com.example.springstudy.board.controller;

import com.example.springstudy.board.entity.BoardType;
import com.example.springstudy.board.model.BoardTypeCount;
import com.example.springstudy.board.model.BoardTypeInput;
import com.example.springstudy.board.model.BoardTypeUsing;
import com.example.springstudy.board.model.ServiceResult;
import com.example.springstudy.board.service.BoardService;
import com.example.springstudy.notice.model.ResponseError;
import com.example.springstudy.user.model.ResponseMessage;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ApiBoardController {

    private final BoardService boardService;

    /**
     * 61. 게시판타입을 추가하는 API를 작성해 보기
     * - 동일한 게시판 제목이 있는 경우 status:200, result:false, message에 이미 동일한 게시판이 존재한다는 메시지 리턴
     * - 게시판이름은 필수항목에 대한 부분 체크
     */

    @PostMapping("/api/board/type")
    public ResponseEntity<?> addBoardType(@RequestBody @Valid BoardTypeInput boardTypeInput, Errors errors) {

        if (errors.hasErrors()) {
            List<ResponseError> responseErrors = ResponseError.of(errors.getAllErrors());

            return new ResponseEntity<>(ResponseMessage.fail("입력값이 정확하지 않습니다",
                responseErrors), HttpStatus.BAD_REQUEST);
        }

        ServiceResult result = boardService.addBoard(boardTypeInput);

        if (!result.isResult()) {
            return ResponseEntity.ok().body(ResponseMessage.fail(result.getMessage()));
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 62. 게시판타입명을 수정하는 API를 작성해 보기
     * - 게시판명이 동일할 경우 "수정할 이름이 동일한 게시판명 입니다." 리턴
     */

    @PutMapping("/api/board/type/{id}")
    public ResponseEntity<?> updateBoardType(@PathVariable Long id,
        @RequestBody @Valid BoardTypeInput boardTypeInput, Errors errors) {

        if (errors.hasErrors()) {
            List<ResponseError> responseErrors = ResponseError.of(errors.getAllErrors());

            return new ResponseEntity<>(ResponseMessage.fail("입력값이 정확하지 않습니다",
                responseErrors), HttpStatus.BAD_REQUEST);
        }

        ServiceResult result = boardService.updateBoard(id, boardTypeInput);

        if (!result.isResult()) {
            return ResponseEntity.ok().body(ResponseMessage.fail(result.getMessage()));
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 63. 게시판타입 삭제하는 API를 작성해 보기
     * - 삭제시는 게시글이 있는 경우 삭제 안됨
     */

    @DeleteMapping("/api/board/type/{id}")
    public ResponseEntity<?> deleteBoardType(@PathVariable Long id) {

        ServiceResult result = boardService.deleteBoard(id);

        if (!result.isResult()) {
            return ResponseEntity.ok().body(ResponseMessage.fail(result.getMessage()));
        }

        return ResponseEntity.ok().body(ResponseMessage.success());
    }

    /**
     * 64. 게시판타입의 목록을 리턴하는 API를 작성해 보기
     */

    @GetMapping("/api/board/type")
    public ResponseEntity<?> boardType() {

        List<BoardType> boardTypeList = boardService.getAllBoardType();

        return ResponseEntity.ok().body(ResponseMessage.success(boardTypeList));
    }

    /**
     * 65. 게시판타입의 사용여부를 설정하는 API를 작성해 보기
     */

    @PatchMapping("/api/board/type/{id}/using")
    public ResponseEntity<?> enableBoardType(@PathVariable Long id, @RequestBody BoardTypeUsing boardTypeUsing) {

        ServiceResult result = boardService.setBoardTypeUsing(id, boardTypeUsing);

        if (!result.isResult()) {
            return ResponseEntity.ok().body(ResponseMessage.fail(result.getMessage()));
        }

        return ResponseEntity.ok().body(ResponseMessage.success());
    }

    /**
     * 66. 게시판별 작성된 게시글의 개수를 리턴하는 API를 작성해 보기
     */

    @GetMapping("/api/board/type/count")
    public ResponseEntity<?> boardTypeCount() {

        List<BoardTypeCount> list = boardService.getBoardTypeCount();

        return ResponseEntity.ok().body(list);
    }

    /**
     * 67. 게시된 게시글을 최상단에 배치하는 API를 작성해 보기
     */

    @PatchMapping("/api/board/{id}/top")
    public ResponseEntity<?> boardPostTop(@PathVariable Long id) {

        ServiceResult result = boardService.setBoardTop(id, true);

        return ResponseEntity.ok().body(result);
    }

}
