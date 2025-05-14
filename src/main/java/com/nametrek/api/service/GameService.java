package com.nametrek.api.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.processing.Completions;

import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.nametrek.api.dto.AnswerDto;
import com.nametrek.api.dto.GameEvent;
import com.nametrek.api.dto.GameEventType;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.QuestionDto;
import com.nametrek.api.exception.GameAlreadyStartedException;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.utils.FormattedKeysAndTopics;
import com.nametrek.api.utils.GameInfo;
import com.nametrek.api.utils.RedisKeys;
import com.nametrek.api.utils.TopicsFormatter;

import io.netty.util.concurrent.CompleteFuture;



/**
 * Service handling the complete game flow and operations.
 */
@Service
public class GameService {

    private final NotificationService notificationService;
    private final RoomService roomService;
    private final CategoryService categoryService;
    private final String[] categories; 
    private final PlayerService playerService;
    private final Integer scoreStep = 10;
    private final RedisService redisService;
    private final CountDownService countDownService;
    private final Integer ANSWER_COUNTDOWN = 10;

    @Autowired
    public GameService(
            NotificationService notificationService,
            CategoryService categoryService,
            RoomService roomService,
            PlayerService playerService,
            RedisService redisService,
            CountDownService countDownService) {
        this.notificationService = notificationService;
        this.categoryService = categoryService;
        this.roomService = roomService;
        this.playerService = playerService;
        this.redisService = redisService;
        this.countDownService = countDownService;
        categories = categoryService.getCategories();
    }

    /**
     * Starts the game on a separate thread to avoid blocking.
     *
     * @param roomId the ID of the room
     * @param url the URI of the game
     */
    public void play(UUID roomId, String uri) {
        if (Boolean.TRUE.equals((Boolean) redisService.getField(RedisKeys.formatRoomKey(roomId), RedisKeys.IN_GAME))) {
            throw new GameAlreadyStartedException("A game is already in progress. Finish it before starting a new one.");
        }
        Room room = roomService.getRoomById(roomId);

		FormattedKeysAndTopics keysAndTopics = new FormattedKeysAndTopics();
		keysAndTopics.setKeysAndTopics(roomId);

		String gameUpdateTopic = keysAndTopics.gameUpdateTopic;
		String roomKey = keysAndTopics.roomKey;
		GameInfo gameInfo = new GameInfo(roomId);

		if (redisService.sortedSetLength(keysAndTopics.inGamePlayersKey) < 2) {
			throw new IllegalArgumentException("Atleast two players must be present in the room");
		}
        sendGameStartMessage(gameUpdateTopic, roomId);

        AtomicInteger rounds = new AtomicInteger(1);
        // start the rounds on a seperate thread
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> { 

            Integer round = rounds.getAndIncrement();
            if (round <= room.getRounds()) {
				gameInfo.setRound(round);
                startRound(gameInfo, keysAndTopics).join();
            } else {
				resetGame(keysAndTopics, gameInfo);
                scheduler.shutdown();
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

	/**
	 * Resets game state
	 *
     * @param keysAndTopics Stores game keys and topics
	 * @param roomId The room id
	 */
	private void resetGame(FormattedKeysAndTopics keysAndTopics, GameInfo gameInfo) {
		String roomKey = keysAndTopics.roomKey;
		UUID roomId = gameInfo.getRoomId();

		Map<String, Object> fields = new HashMap<>();
		fields.put(RedisKeys.ROUND, 0);
		fields.put(RedisKeys.IN_GAME, false);

		redisService.setFields(roomKey, fields);
		List<PlayerDto> players = playerService.getPlayers("DESC", gameInfo.getRoomId());

		players.forEach((player) -> {
			player.setLost(false);
		});

		sendGameEndMessage(keysAndTopics.gameUpdateTopic, players);
		players.forEach((player) -> {
			redisService.addToSortedSet(keysAndTopics.inGamePlayersKey, player.getId(), 0D);
		});
	}

    /**
     * Start a new round
     *
     * @param players the players in the room
	 * @param gameInfo Contains real time game informations (e.g roomId, round, current player and so on)
     * @param keysAndTopics Stores game keys and topics
     *
     * @return A promise to resolve when the round is completed
     */
    public CompletableFuture<Void> startRound(GameInfo gameInfo, FormattedKeysAndTopics keysAndTopics) {
		keysAndTopics.setUsedAnswerKey(gameInfo.getRoomId(), gameInfo.getRound());
		redisService.setField(keysAndTopics.roomKey, RedisKeys.IN_GAME, true);
        String gameUpdateTopic = keysAndTopics.gameUpdateTopic;

        try {
            Thread.sleep(2000);  // wait 2 seconds before starting round
            sendGameMessage(gameUpdateTopic, gameInfo.getRound(), GameEventType.ROUND_STARTED);
            Thread.sleep(2000);  // wait 2 seconds after starting round

			// send game messages with 2 second intervals
			for (int i = 0; i < 2; i++) {
				sendGameMessage(gameUpdateTopic, "Name name name name", GameEventType.GAME_MESSAGE);
				Thread.sleep(2000);
			}

        } catch (InterruptedException e) {
            //TODO: handle exception
			return null;
        }
		redisService.setField(keysAndTopics.roomKey, RedisKeys.ROUND, gameInfo.getRound());


		Queue<Long> inGamePlayers = playerService.getInGamePlayersIds("DESC", gameInfo.getRoomId());
		return processPlayerTurns(inGamePlayers, gameInfo, keysAndTopics);
    }

    /**
     * Process players turn
     *
     * @param inGamePlayers The queue containing active players
     * @param keysAndTopics Stores game keys and topics
	 * @param gameInfo Contains real time game informations (e.g roomId, round, current player and so on)
     *
     * @return A promise that runs on a background process synchronous
     */
    @Async("asyncExecutor")
    private CompletableFuture<Void> processPlayerTurns(Queue<Long> inGamePlayers, GameInfo gameInfo, FormattedKeysAndTopics keysAndTopics) {
		try {
			String category = chooseCategory();
			QuestionDto questionDto = CreateQuestion(category);

			while (inGamePlayers.size() > 1) {
				Long player = inGamePlayers.poll();

				if (playerService.isInGame(gameInfo.getRoomId(), player)) {
					gameInfo.setPlayer(player);
					redisService.setField(keysAndTopics.roomKey, RedisKeys.PLAYER_TURN, player);
					Boolean turnSuccess = nextTurn(questionDto, gameInfo, keysAndTopics);
					// Answer is correct
					if (turnSuccess) {
						inGamePlayers.offer(player);
					}
					Thread.sleep(1000);  // wait for 1 second before moving to the next player
				} 
			}

			Long player = inGamePlayers.peek();
			gameInfo.setPlayer(player);
			celebrateWin(gameInfo, keysAndTopics);
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
            return CompletableFuture.failedFuture(e);
		}
		return CompletableFuture.completedFuture(null);
    }

    /**
     * Ask player a question, start count down and validates answer
     *
     * @param questionDto the question to ask
	 * @param gameInfo Contains real time game informations (e.g roomId, round, current player and so on)
     * @param keysAndTopics Stores game keys and topics
     *
     * @return true if answer is correct otherwise false
     */
    private Boolean nextTurn(QuestionDto questionDto, GameInfo gameInfo, FormattedKeysAndTopics keysAndTopics) {
		Long player = gameInfo.getPlayer();

        questionDto.setPlayerId(player);
        askQuestion(keysAndTopics.questionTopic, questionDto);

        try {
            Thread.sleep(2000);
            Boolean isAnswerAvailable = countDownService.startCountDown(
					ANSWER_COUNTDOWN, gameInfo.getRoomId(), currentcount -> {
                sendGameMessage(keysAndTopics.gameUpdateTopic, currentcount, GameEventType.GAME_COUNTDOWN);
            }).join();
            Thread.sleep(1000);

            AnswerDto answerDto = !isAnswerAvailable 
                ? null
                : (AnswerDto) redisService.getAndDelete(RedisKeys.formatPlayerAnswerKey(player));

			// player did not answer the question
            if (answerDto == null || answerDto.getAnswer() == null || answerDto.getAnswer().isEmpty()) {
				redisService.setField(keysAndTopics.roomKey, RedisKeys.formatPlayerLostStatus(questionDto.getPlayerId()), true);
				sendGameMessage(keysAndTopics.answerTopic, playerService.getPlayers("DESC", gameInfo.getRoomId()), GameEventType.LOSS);
				return false;
            }
			// player is not suppose to answer
            if (answerDto.getPlayerId() != player) {
				return true;
            }

            return validate(answerDto, questionDto, gameInfo, keysAndTopics);
        } catch (Exception e) {
			e.printStackTrace();
            return false;
        }
    }

	/**
	 * Shares 5 points accross players
	 *
     * @param inGamePlayers The queue containing active players
	 * @param gameInfo Contains real time game informations (e.g roomId, round, current player and so on)
	 * @param answerTopic Answer topic for a room
	 */
	public void sharePoints(Queue<Long> inGamePlayers, GameInfo gameInfo, String answerTopic) {
		// player did not answer the question share 0.5 points accross other players
		inGamePlayers.forEach((playerId) -> {
			playerService.incrementScore(gameInfo.getRoomId(), playerId, 0.5);
		});
		sendGameMessage(answerTopic, playerService.getPlayers("DESC", gameInfo.getRoomId()), GameEventType.LOSS);
	}

	/**
	 * Celebrate a win
	 *
     * @param keysAndTopics Stores game keys and topics
	 * @param gameInfo Contains real time game informations (e.g roomId, round, current player and so on)
	 */
	private void celebrateWin(GameInfo gameInfo, FormattedKeysAndTopics keysAndTopics) {
        String roomKey = keysAndTopics.roomKey;
        UUID roomId = gameInfo.getRoomId();

        // winner get extra points
        playerService.incrementScore(roomId, gameInfo.getPlayer(), 0.5);  // 
		// Reset the round
        List<PlayerDto> players = playerService.getPlayers("DESC", roomId);
        players.forEach((player) -> {
            redisService.setField(roomKey, RedisKeys.formatPlayerLostStatus(player.getId()), false);
            player.setLost(false);
        });

		redisService.delete(keysAndTopics.usedAnswersKey);
		sendGameMessage(keysAndTopics.gameUpdateTopic, players, GameEventType.ROUND_ENDED);
	}


    /**
     * Validates answer
     *
     * @param answerDto the answer
     * @param answerTopic the answer topic
     *
     * @return true if the answer is correct otherwise false
     */
    private boolean validate(AnswerDto answerDto, QuestionDto questionDto, GameInfo gameInfo, FormattedKeysAndTopics keysAndTopics) {
		try {
			sendGameMessage(keysAndTopics.answerTopic, answerDto.getAnswer(), GameEventType.ANSWER);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        String playerLostStatusKey = RedisKeys.formatPlayerLostStatus(answerDto.getPlayerId());

        boolean isCorrect = answerDto.getCategory().equals(questionDto.getCategory())
							&& !categoryService.isItemInCategory(keysAndTopics.usedAnswersKey, answerDto.getAnswer())
                            && checkAnswer(answerDto);

		GameEventType type = GameEventType.LOSS; // Default to LOSS
		if (isCorrect) {
			playerService.incrementScore(gameInfo.getRoomId(), answerDto.getPlayerId(), 1D);
			redisService.setField(keysAndTopics.roomKey, playerLostStatusKey, false);
			categoryService.addItemToCategory(keysAndTopics.usedAnswersKey, answerDto.getAnswer().toLowerCase());
			type = GameEventType.WIN;
		} else {
			redisService.setField(keysAndTopics.roomKey, playerLostStatusKey, true);
		}

		sendGameMessage(keysAndTopics.answerTopic, playerService.getPlayers("DESC", gameInfo.getRoomId()), type);
        return isCorrect;
    }

    /**
     * Constructs and sends the question to the connected clients
     *
     * @param questionDto the dto that will store the question
     * @param questionTopic the question topic
     */
    public void askQuestion(String questionTopic, QuestionDto questionDto) {
        notificationService.sendMessageToTopic(questionTopic, questionDto);
    }

    /**
     * Select a category at random
     * @return the category
     */
    private String chooseCategory() {
        return categories[ThreadLocalRandom.current().nextInt(categories.length)];
    }

    /**
     * Checks if the name is in a category
     *
     * @param the answer
     *
     * @return true if name is in category otherwise false
     */
    private boolean checkAnswer(AnswerDto answerDto) {
		return categoryService.isItemInCategory(answerDto.getCategory(), answerDto.getAnswer());
    }

    /**
     * Create a question
     *
     * @param category the category which the question is based on
     *
     * @return A question dto
     */
    private QuestionDto CreateQuestion(String category) {
        return new QuestionDto(category);
    }

    /**
     * Send the current round to connected clients
     *
     * @param round current game round
     */
    private <T> void sendGameMessage(String topic, T message, GameEventType type) {
        notificationService.sendMessageToTopic(topic, new GameEvent<T>(type, message));
    }

    /**
     * Store answer received from player in redis
     *
     * @param answerDto the answer
     */
    public void saveAnswer(AnswerDto answerDto) {
        redisService.setValueExp(
                RedisKeys.formatPlayerAnswerKey(answerDto.getPlayerId()),
                answerDto,
                40,
                TimeUnit.SECONDS);
    }

	/**
	 * 
	 * @param gameUpdateTopic Question topic for a room
     * @param inGamePlayers The queue containing active players
	 */
    public void sendGameEndMessage(String gameUpdateTopic, List<PlayerDto> inGamePlayers) {
        sendGameMessage(gameUpdateTopic, inGamePlayers, GameEventType.GAME_ENDED);
    }

	/**
	 *
	 * @param gameUpdateTopic Question topic for a room
	 * @param roomId The room id
	 */
    public void sendGameStartMessage(String gameUpdateTopic, UUID roomId) {
        sendGameMessage(gameUpdateTopic, playerService.getPlayers("DESC", roomId), GameEventType.GAME_STARTED);
    }

	/**
	 * Return the count down service
	 */
    public CountDownService getCountDownService() {
        return countDownService;
    }

    public Long getPlayerTurn(UUID roomId) {
        return ((Number) redisService.getField(RedisKeys.formatRoomKey(roomId), RedisKeys.PLAYER_TURN)).longValue();
    }
    
}
