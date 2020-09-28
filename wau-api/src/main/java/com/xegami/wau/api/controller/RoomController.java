package com.xegami.wau.api.controller;

import com.xegami.wau.api.dto.RoomDTO;
import com.xegami.wau.api.service.RoomService;
import com.xegami.wau.api.service.exception.WauException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @MessageExceptionHandler
    @SendToUser("/topic/error")
    public String handleException(Throwable exception) {
        log.error(exception.getMessage());

        return exception.getMessage();
    }

    @MessageMapping("/create-mm")
    @SendToUser("/topic/create")
    public RoomReply create(RoomMessage message) throws WauException {
        RoomDTO roomDTO = roomService.create(message.getAgent(), message.isHotMode());

        return new RoomReply(roomDTO, message.getAgent());
    }

    @MessageMapping("/join-mm")
    @SendToUser("/topic/join")
    public RoomReply join(RoomMessage message) throws WauException {
        RoomDTO roomDTO = roomService.join(message.getAgent(), message.getCode(), message.getPassword(), message.isRejoining(), message.getRoomToken());

        return new RoomReply(roomDTO, message.getAgent());
    }

    @MessageMapping("/leave-mm")
    public void leave(RoomMessage message) {
        roomService.leave(message.getAgent(), message.getCode(), message.isKicked());
    }

    @MessageMapping("/startGame-mm")
    public void startGame(RoomMessage message) throws WauException {
        roomService.startGame(message.getCode());
    }

    @MessageMapping("/votePlayer-mm")
    public void votePlayer(RoomMessage message) {
        roomService.votePlayer(message.getAgent(), message.getTarget(), message.getCode());
    }

    @MessageMapping("/voteNext-mm")
    public void voteNext(RoomMessage message) {
        roomService.voteNext(message.getAgent(), message.getCode());
    }

    @MessageMapping("/voteSkip-mm")
    public void voteSkip(RoomMessage message) {
        roomService.voteSkip(message.getAgent(), message.getCode(), message.getRoundNumber());
    }

    @MessageMapping("/voteCancelSkip-mm")
    public void voteCancelSkip(RoomMessage message) {
        roomService.voteCancelSkip(message.getAgent(), message.getCode(), message.getRoundNumber());
    }

    @MessageMapping("/playAgain-mm")
    public void playAgain(RoomMessage message) {
        roomService.playAgain(message.getCode());
    }

    @MessageMapping("/afk-mm")
    public void afk(RoomMessage message) {
        roomService.afk(message.getAgent(), message.getCode(), message.isAfk());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/refreshQuestions")
    public void refreshQuestions() {
        roomService.refreshQuestions();
    }

}
