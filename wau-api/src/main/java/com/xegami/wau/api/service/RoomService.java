package com.xegami.wau.api.service;

import com.xegami.wau.api.controller.RoomReply;
import com.xegami.wau.api.dto.*;
import com.xegami.wau.api.service.exception.Exceptions;
import com.xegami.wau.api.service.exception.WauException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Service
public class RoomService {

    private static final int MAX_LOBBY_SIZE = 10;
    private static final int MIN_LOBBY_SIZE = 3;
    private static final int LAST_ROUND_INDEX = 19;
    private static final int AFK_TIMER_DELAY = 30000; // 30 segundos
    private static final int ROOM_CLEANER_TIMER_PERIOD = 3600000; // 1 hora
    private static final int ROOM_LIFETIME = 10800000; // 3 horas

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SimpMessagingTemplate simp;

    private final Map<Integer, RoomDTO> rooms = new HashMap<>();
    private Random random = new Random();
    private List<QuestionDTO> allQuestions, pussyQuestions;

    public void loadRooms() {
        for (int i = 128; i <= 1024; i++) {
            rooms.put(i, new RoomDTO());
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                rooms.forEach((k, v) -> {
                    if (v.getCreateDate() != null && System.currentTimeMillis() - v.getCreateDate() > ROOM_LIFETIME) {
                        rooms.put(k, new RoomDTO());
                        log.info("Se ha limpiado una sala vieja -> " + v.getCode() + ".");
                    }
                });
            }
        }, 0, ROOM_CLEANER_TIMER_PERIOD);
    }

    public void refreshQuestions() {
        try {
            questionService.loadQuestions();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        allQuestions = questionService.findAllDTOs();
        pussyQuestions = questionService.findAllByHotFalse();
    }

    public RoomDTO create(PlayerDTO playerDTO, boolean hotMode) throws WauException {
        for (Map.Entry<Integer, RoomDTO> r : rooms.entrySet()) {
            if (r.getValue().getPlayers().size() == 0) {
                String password = String.valueOf(new Random().nextInt(9000) + 1000);
                Long createDate = System.currentTimeMillis();
                String token = createToken(createDate, r.getKey(), password);

                r.getValue().setCode(r.getKey());
                r.getValue().getPlayers().add(playerDTO);
                r.getValue().setPassword(password);
                r.getValue().setPartyLeader(playerDTO);
                r.getValue().setHotMode(hotMode);
                r.getValue().setCreateDate(createDate);
                r.getValue().setToken(token);

                return r.getValue();
            }
        }

        throw new WauException(Exceptions.NO_AVAILABLE_ROOMS);
    }

    public RoomDTO join(PlayerDTO newPlayer, Integer code, String password, boolean rejoining, String roomToken) throws WauException {
        RoomDTO roomDTO;

        if (roomToken == null) {
            roomDTO = rooms.getOrDefault(code, new RoomDTO());
        } else {
            roomDTO = rooms.values().stream().filter(r -> roomToken.equals(r.getToken())).findAny().orElse(null);
            if (roomDTO == null) throw new WauException(Exceptions.INVALID_LINK);
        }

        if (rejoining && roomDTO.getPlayers().contains(newPlayer)) {
            if (!password.equals(roomDTO.getPassword())) throw new WauException(Exceptions.UNKNOWN_ROOM);

            return roomDTO;
        }

        if (roomDTO.getPlayers().size() == 0) throw new WauException(Exceptions.UNKNOWN_ROOM);
        if (roomToken == null && !roomDTO.getPassword().equals(password)) throw new WauException(Exceptions.WRONG_PASSWORD);
        if (roomDTO.getPlayers().size() == MAX_LOBBY_SIZE) throw new WauException(Exceptions.MAX_LOBBY_SIZE);
        for (PlayerDTO p : roomDTO.getPlayers()) {
            if (p.getNickname().equals(newPlayer.getNickname())) throw new WauException(Exceptions.NICKNAME_EXISTS);
        }

        roomDTO.getPlayers().add(newPlayer);

        simp.convertAndSend("/topic/room/" + roomDTO.getCode() + "/playerJoined", new RoomReply(roomDTO, newPlayer));

        return rooms.put(roomDTO.getCode(), roomDTO);
    }

    public void leave(PlayerDTO oldPlayer, Integer code, boolean kicked) {
        RoomDTO roomDTO = rooms.get(code);

        roomDTO.getPlayers().removeIf(p -> p.equals(oldPlayer));

        if (roomDTO.getPlayers().size() > 0) {
            roomDTO.getVotingSkipPlayers().removeIf(p -> p.equals(oldPlayer));
            roomDTO.getVotingCancelSkipPlayers().removeIf(p -> p.equals(oldPlayer));
            roomDTO.getVotingNextPlayers().removeIf(p -> p.equals(oldPlayer));
            roomDTO.getAfkPlayers().removeIf(p -> p.equals(oldPlayer));

            if (roomDTO.getPartyLeader().equals(oldPlayer)) {
                roomDTO.setPartyLeader(roomDTO.getPlayers().get(0));
            }

            if (roomDTO.getPlayers().size() < MIN_LOBBY_SIZE) {
                switch (roomDTO.getScreen()) {
                    case "game":
                    case "votes":
                        roomDTO.setScreen("endgame");
                        simp.convertAndSend("/topic/room/" + code + "/gameEndedEarly", new RoomReply(roomDTO, oldPlayer, kicked));
                        break;
                    default:
                        simp.convertAndSend("/topic/room/" + code + "/playerLeft", new RoomReply(roomDTO, oldPlayer, kicked));
                }
            } else {
                simp.convertAndSend("/topic/room/" + code + "/playerLeft", new RoomReply(roomDTO, oldPlayer, kicked));
            }

            switch (roomDTO.getScreen()) {
                case "game":
                    if (calcVotesCount(roomDTO).equals(roomDTO.getPlayers().size())) {
                        roomDTO.setScreen("votes");
                        simp.convertAndSend("/topic/room/" + code + "/roundEnded", new RoomReply(roomDTO, oldPlayer));
                        roomDTO.clearVotingPlayers();
                    } else if (roomDTO.getVotingSkipPlayers().size() > roomDTO.getPlayers().size() / 2) {
                        roomDTO.increaseRound();
                        simp.convertAndSend("/topic/room/" + code + "/skipRound", new RoomReply(roomDTO, oldPlayer));
                        roomDTO.clearVotingPlayers();
                    }
                    break;
                case "votes":
                    if (roomDTO.getVotingNextPlayers().size() == roomDTO.getPlayers().size()) {
                        roomDTO.setScreen("game");
                        roomDTO.increaseRound();
                        simp.convertAndSend("/topic/room/" + code + "/votesEnded", new RoomReply(roomDTO, oldPlayer));
                        roomDTO.clearVotingPlayers();
                    }
                    break;
            }

            rooms.put(code, roomDTO);

        } else {
            rooms.put(roomDTO.getCode(), new RoomDTO());
        }

    }

    public void startGame(Integer code) throws WauException {
        RoomDTO roomDTO = rooms.get(code);

        if (roomDTO.getPlayers().size() < MIN_LOBBY_SIZE) {
            throw new WauException(Exceptions.MIN_LOBBY_SIZE);
        }

        List<QuestionDTO> questions = new ArrayList<>(roomDTO.isHotMode() ? allQuestions : pussyQuestions);

        // TODO: default gonna be 19 counting from 0
        for (int i = 0; i <= LAST_ROUND_INDEX; i++) {
            RoundDTO roundDTO = new RoundDTO();
            int randomIndex = random.nextInt(questions.size());

            roundDTO.setQuestion(questions.get(randomIndex));
            questions.remove(randomIndex);
            roomDTO.getRounds().add(roundDTO);
        }

        roomDTO.setScreen("game");

        simp.convertAndSend("/topic/room/" + code + "/gameStarted", new RoomReply(roomDTO));

        rooms.put(code, roomDTO);
    }

    public void votePlayer(PlayerDTO voting, PlayerDTO voted, Integer code) {
        RoomDTO roomDTO = rooms.get(code);

        roomDTO.getRounds().get(roomDTO.getRoundNumber()).addVote(voted, voting);

        Integer votesCount = calcVotesCount(roomDTO);
        if (votesCount.equals(roomDTO.getPlayers().size())) {
            roomDTO.setScreen("votes");
            roomDTO.getRounds().get(roomDTO.getRoundNumber()).getVotedPlayers().sort(Comparator.comparingInt(VotedPlayerDTO::getVoteCount).reversed());
            simp.convertAndSend("/topic/room/" + code + "/roundEnded", new RoomReply(roomDTO, voting));
        } else {
            simp.convertAndSend("/topic/room/" + code + "/playerVoted", new RoomReply(roomDTO, voting));
        }

        rooms.put(code, roomDTO);
    }

    public void voteSkip(PlayerDTO votingSkipPlayer, Integer code, Integer roundNumber) {
        RoomDTO roomDTO = rooms.get(code);

        if (!roomDTO.getRoundNumber().equals(roundNumber)) return;

        roomDTO.getVotingSkipPlayers().add(votingSkipPlayer);

        if (roomDTO.getVotingSkipPlayers().size() > roomDTO.getPlayers().size() / 2) {
            roomDTO.getRounds().get(roomDTO.getRoundNumber()).getVotedPlayers().clear();

            if (roomDTO.getRoundNumber() < LAST_ROUND_INDEX) {
                roomDTO.increaseRound();
                simp.convertAndSend("/topic/room/" + code + "/skipRound", new RoomReply(roomDTO, votingSkipPlayer));
            } else {
                roomDTO.setScreen("endgame");
                simp.convertAndSend("/topic/room/" + code + "/gameEnded", new RoomReply(roomDTO, votingSkipPlayer));
            }

            roomDTO.clearVotingPlayers();

        } else {
            simp.convertAndSend("/topic/room/" + code + "/skipVoted", new RoomReply(roomDTO, votingSkipPlayer));
        }

        rooms.put(code, roomDTO);
    }

    public void voteCancelSkip(PlayerDTO votingCancelSkipPlayer, Integer code, Integer roundNumber) {
        RoomDTO roomDTO = rooms.get(code);

        if (!roomDTO.getRoundNumber().equals(roundNumber)) return;

        roomDTO.getVotingCancelSkipPlayers().add(votingCancelSkipPlayer);

        if (roomDTO.getVotingCancelSkipPlayers().size() >= Math.round(roomDTO.getPlayers().size() / 2.0)) {
            simp.convertAndSend("/topic/room/" + code + "/skipNotRound", new RoomReply(roomDTO, votingCancelSkipPlayer));
            roomDTO.clearVotingPlayers();
        } else {
            simp.convertAndSend("/topic/room/" + code + "/cancelSkipVoted", new RoomReply(roomDTO, votingCancelSkipPlayer));
        }

        rooms.put(code, roomDTO);
    }

    public void voteNext(PlayerDTO votingNextPlayer, Integer code) {
        RoomDTO roomDTO = rooms.get(code);

        roomDTO.getVotingNextPlayers().add(votingNextPlayer);

        if (roomDTO.getVotingNextPlayers().size() == roomDTO.getPlayers().size()) {
            if (roomDTO.getRoundNumber() < LAST_ROUND_INDEX) {
                roomDTO.increaseRound();
                roomDTO.setScreen("game");
                simp.convertAndSend("/topic/room/" + code + "/votesEnded", new RoomReply(roomDTO, votingNextPlayer));
            } else {
                roomDTO.setScreen("endgame");
                simp.convertAndSend("/topic/room/" + code + "/gameEnded", new RoomReply(roomDTO, votingNextPlayer));
            }

            roomDTO.clearVotingPlayers();
        } else {
            simp.convertAndSend("/topic/room/" + code + "/nextVoted", new RoomReply(roomDTO, votingNextPlayer));
        }

        rooms.put(code, roomDTO);
    }

    public void playAgain(Integer code) {
        RoomDTO roomDTO = rooms.get(code);

        roomDTO.getRounds().clear();
        roomDTO.setRoundNumber(0);
        roomDTO.setScreen("lobby");
        roomDTO.clearVotingPlayers();

        simp.convertAndSend("/topic/room/" + code + "/playAgain", new RoomReply(roomDTO));

        rooms.put(code, roomDTO);
    }

    private Integer calcVotesCount(RoomDTO roomDTO) {
        Integer count = 0;
        for (VotedPlayerDTO v : roomDTO.getRounds().get(roomDTO.getRoundNumber()).getVotedPlayers()) {
            for (int i = 0; i < v.getVoting().size(); i++) {
                count++;
            }
        }

        return count;
    }

    public void afk(PlayerDTO playerDTO, Integer code, boolean afk) {
        RoomDTO roomDTO = rooms.get(code);

        if (afk) {
            roomDTO.getAfkPlayers().add(playerDTO);
            kickCountDown(playerDTO, code);
        } else {
            roomDTO.getAfkPlayers().remove(playerDTO);
        }

        rooms.put(code, roomDTO);
    }

    private void kickCountDown(PlayerDTO playerDTO, Integer code) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (rooms.get(code).getAfkPlayers().contains(playerDTO)) {
                    leave(playerDTO, code, false);
                }
            }
        }, AFK_TIMER_DELAY);
    }

    private String createToken(Long createDate, Integer code, String password) {
        try {
            byte[] bytes = (createDate + "-" + code + "-" + password).getBytes(StandardCharsets.UTF_8);
            return MessageDigest.getInstance("MD5").digest(bytes).toString();
        } catch (Exception e) {
            return createDate + "pogchamp";
        }
    }
}
