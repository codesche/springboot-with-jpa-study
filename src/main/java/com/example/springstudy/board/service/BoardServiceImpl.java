package com.example.springstudy.board.service;

import com.example.springstudy.board.entity.Board;
import com.example.springstudy.board.entity.BoardBadReport;
import com.example.springstudy.board.entity.BoardHits;
import com.example.springstudy.board.entity.BoardLike;
import com.example.springstudy.board.entity.BoardScrap;
import com.example.springstudy.board.entity.BoardType;
import com.example.springstudy.board.model.BoardBadReportInput;
import com.example.springstudy.board.model.BoardPeriod;
import com.example.springstudy.board.model.BoardTypeCount;
import com.example.springstudy.board.model.BoardTypeInput;
import com.example.springstudy.board.model.BoardTypeUsing;
import com.example.springstudy.board.model.ServiceResult;
import com.example.springstudy.board.repository.BoardBadReportRepository;
import com.example.springstudy.board.repository.BoardHitsRepository;
import com.example.springstudy.board.repository.BoardLikeRepository;
import com.example.springstudy.board.repository.BoardRepository;
import com.example.springstudy.board.repository.BoardScraptRepository;
import com.example.springstudy.board.repository.BoardTypeCustomRepository;
import com.example.springstudy.board.repository.BoardTypeRepository;
import com.example.springstudy.user.entity.User;
import com.example.springstudy.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardTypeRepository boardTypeRepository;
    private final BoardTypeCustomRepository boardTypeCustomRepository;
    private final BoardRepository boardRepository;
    private final BoardHitsRepository boardHitsRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardBadReportRepository boardBadReportRepository;
    private final BoardScraptRepository boardScraptRepository;

    @Override
    public ServiceResult addBoard(BoardTypeInput boardTypeInput) {

        BoardType boardType = boardTypeRepository.findByBoardName(boardTypeInput.getName());

        if (boardType != null && boardTypeInput.getName().equals(boardType.getBoardName())) {
            // 동일한 게시판 제목이 있음
            return ServiceResult.fail("이미 동일한 게시판이 존재합니다.");
        }

        BoardType addBoardType = BoardType.builder()
            .boardName(boardTypeInput.getName())
            .regDate(LocalDateTime.now())
            .build();

        boardTypeRepository.save(addBoardType);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult updateBoard(long id, BoardTypeInput boardTypeInput) {

        Optional<BoardType> optionalBoardType = boardTypeRepository.findById(id);
        if (!optionalBoardType.isPresent()) {
            return ServiceResult.fail("수정할 게시판타입이 없습니다.");
        }

        BoardType boardType = optionalBoardType.get();

        if (boardType.getBoardName().equals(boardTypeInput.getName())) {
            return ServiceResult.fail("수정할 이름이 동일한 게시판명 입니다.");
        }

        boardType.setBoardName(boardTypeInput.getName());
        boardType.setUpdateDate(LocalDateTime.now());
        boardTypeRepository.save(boardType);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult deleteBoard(Long id) {

        Optional<BoardType> optionalBoardType = boardTypeRepository.findById(id);
        if (!optionalBoardType.isPresent()) {
            return ServiceResult.fail("삭제할 게시판타입이 없습니다.");
        }

        BoardType boardType = optionalBoardType.get();

        if (boardRepository.countByBoardType(boardType) > 0) {
            return ServiceResult.fail("삭제할 게시판타입의 게시글이 존재합니다.");
        }

        boardTypeRepository.delete(boardType);

        return ServiceResult.success();
    }

    @Override
    public List<BoardType> getAllBoardType() {
        return boardTypeRepository.findAll();
    }

    @Override
    public ServiceResult setBoardTypeUsing(Long id, BoardTypeUsing boardTypeUsing) {

        Optional<BoardType> optionalBoardType = boardTypeRepository.findById(id);
        if (!optionalBoardType.isPresent()) {
            return ServiceResult.fail("삭제할 게시판타입이 없습니다.");
        }

        BoardType boardType = optionalBoardType.get();

        boardType.setUsingYn(boardTypeUsing.isUsingYn());
        boardTypeRepository.save(boardType);

        return ServiceResult.success();
    }

    @Override
    public List<BoardTypeCount> getBoardTypeCount() {

        return boardTypeCustomRepository.getBoardTypeCount();

    }

    @Override
    public ServiceResult setBoardTop(Long id, boolean topYn) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();
        if (board.isTopYn() == topYn) {
            if (topYn) {
                return ServiceResult.fail("이미 게시글이 최상단에 배치되어 있습니다.");
            } else {
                return ServiceResult.fail("이미 게시글이 최상단에 배치가 해제되어 있습니다.");
            }
        }

        board.setTopYn(true);
        boardRepository.save(board);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult setBoardPeriod(Long id, BoardPeriod boardPeriod) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();

        board.setPublishStartDate(boardPeriod.getStartDate());
        board.setPublishEndDate(boardPeriod.getEndDate());
        boardRepository.save(board);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult setBoardHits(Long id, String email) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return ServiceResult.fail("회원 정보가 존재하지 않습니다.");
        }
        User user = optionalUser.get();

        if (boardHitsRepository.countByBoardAndUser(board, user) > 0) {
            return ServiceResult.fail("이미 조회수가 있습니다.");
        }

        boardHitsRepository.save(BoardHits.builder()
                .board(board)
                .user(user)
                .regDate(LocalDateTime.now())
                .build());

        return ServiceResult.success();
    }

    @Override
    public ServiceResult setBoardLike(Long id, String email) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return ServiceResult.fail("회원 정보가 존재하지 않습니다.");
        }
        User user = optionalUser.get();

        long boardLikeCount = boardLikeRepository.countByBoardAndUser(board, user);
        if (boardLikeCount > 0) {
            return ServiceResult.fail("이미 좋아요한 내용이 있습니다.");
        }

        boardLikeRepository.save(BoardLike.builder()
                        .board(board)
                        .user(user)
                        .regDate(LocalDateTime.now()).build());

        return ServiceResult.success();
    }

    @Override
    public ServiceResult setBoardUnLike(Long id, String email) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return ServiceResult.fail("회원 정보가 존재하지 않습니다.");
        }
        User user = optionalUser.get();


        Optional<BoardLike> optionalBoardLike = boardLikeRepository.findByBoardAndUser(board, user);
        if (!optionalBoardLike.isPresent()) {
            return ServiceResult.fail("좋아요한 내용이 없습니다.");
        }
        BoardLike boardLike = optionalBoardLike.get();

        boardLikeRepository.delete(boardLike);
        return ServiceResult.success();
    }

    @Override
    public ServiceResult addBadReport(Long id, String email,
        BoardBadReportInput boardBadReportInput) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return ServiceResult.fail("회원 정보가 존재하지 않습니다.");
        }
        User user = optionalUser.get();

        BoardBadReport boardBadReport = BoardBadReport.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .userEmail(user.getEmail())
                .boardId(board.getId())
                .boardUserId(board.getUser().getId())
                .boardTitle(board.getTitle())
                .boardContents(board.getContents())
                .boardRegDate(board.getRegDate())
                .comments(boardBadReportInput.getComments())
                .build();

        boardBadReportRepository.save(boardBadReport);

        return ServiceResult.success();
    }

    @Override
    public List<BoardBadReport> badReportList() {
        return boardBadReportRepository.findAll();
    }

    @Override
    public ServiceResult scrapBoard(Long id, String email) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (!optionalBoard.isPresent()) {
            return ServiceResult.fail("게시글이 존재하지 않습니다.");
        }

        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return ServiceResult.fail("회원 정보가 존재하지 않습니다.");
        }
        User user = optionalUser.get();

        BoardScrap boardScrap = BoardScrap.builder()
                                        .user(user)
                                        .boardTypeId(board.getBoardType().getId())
                                        .boardTitle(board.getTitle())
                                        .boardContents(board.getContents())
                                        .boardRegDate(board.getRegDate())
                                        .regDate(LocalDateTime.now())
                                        .build();

        boardScraptRepository.save(boardScrap);

        return ServiceResult.success();
    }

}
