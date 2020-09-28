package com.xegami.wau.android.rest.controller

import android.util.Log
import com.google.gson.Gson
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.dto.RoomDTO
import com.xegami.wau.android.rest.event.*
import io.reactivex.CompletableObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.LifecycleEvent

class RoomController private constructor() {

    private val serverUri = "ws://127.0.0.1:8080/ws"

    companion object {
        val instance = RoomController()
    }

    private lateinit var room: RoomDTO
    private lateinit var localPlayer: PlayerDTO
    private var inRoom: Boolean = false
    private val clientDisposables = CompositeDisposable()
    private val preGameDisposables = CompositeDisposable()
    private val gameDisposables = CompositeDisposable()
    private val tag = this::class.simpleName
    private val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUri)
    private val co = object : CompletableObserver {
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
            ErrorEvent("Error de conexión.").post()
        }
    }

    private fun subscribeLifecycle() {
        clientDisposables.clear()

        val lifecycleDisposable = client.lifecycle().subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d(tag, "Connection opened.")

                    subscribePreGameTopics()

                    if (inRoom) {
                        afk(false)
                        join(localPlayer, room.code, room.password, true)
                    }

                    ConnectionOpenedEvent().post()
                }
                LifecycleEvent.Type.ERROR -> {
                    Log.e(
                        tag,
                        "Connection error.",
                        lifecycleEvent.exception
                    )

                    ErrorEvent("Error de conexión.").post()
                }
                LifecycleEvent.Type.CLOSED -> {
                    Log.d(tag, "Connection closed.")

                    ConnectionClosedEvent().post()
                }
            }
        }

        clientDisposables.add(lifecycleDisposable)
    }

    private fun subscribePreGameTopics() {
        preGameDisposables.clear()

        val errorDisposable = client.topic("/user/topic/error").subscribe { message ->
            ErrorEvent(message.payload).post()
        }

        val createDisposable = client.topic("/user/topic/create").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room
            localPlayer = roomReply.agent
            inRoom = true

            subscribeGameTopics(room.code)
            CreateRoomEvent().post()
        }

        val joinDisposable = client.topic("/user/topic/join").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room
            localPlayer = roomReply.agent
            inRoom = true

            subscribeGameTopics(room.code)
            JoinRoomEvent().post()
        }

        preGameDisposables.addAll(
            errorDisposable,
            createDisposable,
            joinDisposable
        )
    }

    private fun subscribeGameTopics(code: Int) {
        gameDisposables.clear()

        val playerJoinedDisposable = client.topic("/topic/room/$code/playerJoined").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            if (localPlayer == roomReply.agent) return@subscribe

            PlayerJoinedEvent(roomReply.agent).post()
        }

        val playerLeftDisposable = client.topic("/topic/room/$code/playerLeft").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            if (localPlayer == roomReply.agent && roomReply.kicked) {
                inRoom = false
                gameDisposables.clear()
            }

            PlayerLeftEvent(roomReply.agent, roomReply.kicked).post()
        }

        val gameStartedDisposable = client.topic("/topic/room/$code/gameStarted").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            GameStartedEvent().post()
        }

        val playerVotedDisposable = client.topic("/topic/room/$code/playerVoted").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            PlayerVotedEvent(roomReply.agent).post()
        }

        val roundEndedDisposable = client.topic("/topic/room/$code/roundEnded").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            RoundEndedEvent(roomReply.agent).post()
        }

        val nextVotedDisposable = client.topic("/topic/room/$code/nextVoted").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            NextVotedEvent(roomReply.agent).post()
        }

        val skipVotedDisposable = client.topic("/topic/room/$code/skipVoted").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            SkipVotedEvent(roomReply.agent).post()
        }

        val cancelSkipVotedDisposable = client.topic("/topic/room/$code/cancelSkipVoted").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            CancelSkipVotedEvent(roomReply.agent).post()
        }

        val skipRoundDisposable = client.topic("/topic/room/$code/skipRound").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            SkipRoundEvent(roomReply.agent).post()
        }

        val skipNotRoundDisposable = client.topic("/topic/room/$code/skipNotRound").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            SkipNotRoundEvent(roomReply.agent).post()
        }

        val votesEndedDisposable = client.topic("/topic/room/$code/votesEnded").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            VotesEndedEvent(roomReply.agent).post()
        }

        val gameEndedDisposable = client.topic("/topic/room/$code/gameEnded").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            GameEndedEvent(roomReply.agent).post()
        }

        val gameEndedEarlyDisposable = client.topic("/topic/room/$code/gameEndedEarly").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            if (localPlayer == roomReply.agent && roomReply.kicked) {
                inRoom = false
                gameDisposables.clear()
            }

            GameEndedEarlyEvent(roomReply.agent, roomReply.kicked).post()
        }

        val playAgainDisposable = client.topic("/topic/room/$code/playAgain").subscribe { message ->
            val roomReply = Gson().fromJson(message.payload, RoomReply::class.java)
            room = roomReply.room

            PlayAgainEvent().post()
        }

        gameDisposables.addAll(
            playerJoinedDisposable,
            playerLeftDisposable,
            gameStartedDisposable,
            playerVotedDisposable,
            roundEndedDisposable,
            nextVotedDisposable,
            skipVotedDisposable,
            cancelSkipVotedDisposable,
            skipRoundDisposable,
            skipNotRoundDisposable,
            votesEndedDisposable,
            gameEndedDisposable,
            gameEndedEarlyDisposable,
            playAgainDisposable
        )
    }

    fun create(playerDTO: PlayerDTO, hotMode: Boolean) {
        client.send(
            "/topic/create-mm", RoomMessage(
                playerDTO,
                hotMode = hotMode
            ).toJson()
        )
            .subscribe(co)
    }

    fun join(playerDTO: PlayerDTO, roomToken: String) {
        client.send(
            "/topic/join-mm", RoomMessage(
                playerDTO,
                roomToken = roomToken
            ).toJson()
        )
            .subscribe(co)
    }

    fun join(playerDTO: PlayerDTO, code: Int, password: String, rejoining: Boolean) {
        client.send(
            "/topic/join-mm", RoomMessage(
                playerDTO,
                code,
                password,
                rejoining = rejoining
            ).toJson()
        )
            .subscribe(co)
    }

    fun leave() {
        inRoom = false
        gameDisposables.clear()

        client.send(
            "/topic/leave-mm", RoomMessage(
                localPlayer,
                room.code
            ).toJson()
        )
            .subscribe(co)
    }

    fun startGame() {
        client.send(
            "/topic/startGame-mm", RoomMessage(
                localPlayer,
                room.code
            ).toJson()
        )
            .subscribe(co)
    }

    fun votePlayer(playerDTO: PlayerDTO) {
        client.send(
            "/topic/votePlayer-mm", RoomMessage(
                localPlayer,
                room.code,
                target = playerDTO
            ).toJson()
        )
            .subscribe(co)
    }

    fun voteNext() {
        client.send(
            "/topic/voteNext-mm", RoomMessage(
                localPlayer,
                room.code
            ).toJson()
        )
            .subscribe(co)
    }

    fun voteSkip() {
        client.send(
            "/topic/voteSkip-mm", RoomMessage(
                localPlayer,
                room.code,
                roundNumber = room.roundNumber
            ).toJson()
        )
            .subscribe(co)
    }

    fun voteCancelSkip() {
        client.send(
            "/topic/voteCancelSkip-mm", RoomMessage(
                localPlayer,
                room.code,
                roundNumber = room.roundNumber
            ).toJson()
        )
            .subscribe(co)
    }

    fun playAgain() {
        client.send(
            "/topic/playAgain-mm", RoomMessage(
                localPlayer,
                room.code
            ).toJson()
        )
            .subscribe(co)
    }

    fun afk(afk: Boolean) {
        client.send(
            "/topic/afk-mm", RoomMessage(
                localPlayer,
                room.code,
                afk = afk
            ).toJson()
        )
            .subscribe(co)
    }

    fun kick(target: PlayerDTO) {
        client.send(
            "/topic/leave-mm", RoomMessage(
                target,
                room.code,
                kicked = true
            ).toJson()
        )
            .subscribe(co)
    }

    fun connect() {
        if (!client.isConnected) {
            subscribeLifecycle()
            client.connect()
        }
    }

    fun disconnect() {
        if (inRoom) afk(true)

        client.disconnect()
    }

    fun isConnected(): Boolean {
        return client.isConnected
    }

    fun getLocalPlayer(): PlayerDTO {
        return localPlayer
    }

    fun getRoom(): RoomDTO {
        return room
    }

    fun isInRoom(): Boolean {
        return inRoom
    }

}