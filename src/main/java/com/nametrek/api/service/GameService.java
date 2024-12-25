package com.nametrek.api.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.nametrek.api.dto.AnswerDto;
import com.nametrek.api.dto.QuestionDto;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.model.Topics;

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
    private final String gameStartMessage = "Name";
    private final String questionTopicFormat = "/rooms/%s/question";
    private final String answerTopicFormat = "/rooms/%s/answer";
    private final String roomUpdatesTopicFormat = "/rooms/%s";
    private final Integer scoreStep = 10;
    private final RedisService redisService;
    private final CountDownService countDownService;
    private final String playerAnswerKeyFormat = "player:%s:currentAnswer";
    private final String startRoundMessage = "\"Let Go!\"";

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
    public void play(String roomId, String uri) {

        AtomicInteger round = new AtomicInteger(1);

        Room room = roomService.get(roomId);
        Topics topics = getTopics(roomId);
        List<Player> players = playerService.getPlayersOrderBy("DESC", room.getId());

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // schedule the rounds and call it
        scheduler.scheduleAtFixedRate(() -> { 
            if (round.getAndIncrement() <= room.getMaxRounds()) {
                startRound(players, room, topics).join();
            } else {
                scheduler.shutdown();
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * Start a new round
     *
     * @param players the players in the room
     * @param room the room
     * @param topics containing topics for room updates, answer and question
     *
     * @return A promise to resolve when the round is completed
     */
    public CompletableFuture<Void> startRound(List<Player> players, Room room, Topics topics) {
        room.incrementRound();
        sendRoundMessage(topics.roomUpdatesTopic, room.getCurrentRound());

        AtomicBoolean isGameActive = new AtomicBoolean(true);

        String category = chooseCategory();  // Select category at random for each round

        Queue<Player> inGamePlayers = new LinkedList<>(players);

        QuestionDto questionDto = CreateQuestion(category);

        return countDownService.startCountDown(3, null, count -> {
            sendCountDownMessage(topics.roomUpdatesTopic, count);
        }).thenCompose(__ -> {
            sendStartRoundMessage(topics.roomUpdatesTopic);

            return processPlayerTurns(inGamePlayers, questionDto, topics, isGameActive);
        });
    }

    /**
     * Process players turn
     *
     * @param inGamePlayers the queue containing active players
     * @param questionDto th question to ask the players
     * @param topics containing topics for room updates, answer and question
     * @param isGameActive statuss flag
     *
     * @return A promise that runs on a background process synchronous
     */
    private CompletableFuture<Void> processPlayerTurns(
            Queue<Player> inGamePlayers, 
            QuestionDto questionDto,
            Topics topics,
            AtomicBoolean isGameActive) {

        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);  // wait 1 seconds before starting round
                                     
                while (inGamePlayers.size() > 1 && isGameActive.get()) {
                    Player player = inGamePlayers.poll();

                    boolean turnSucess = nextTurn(questionDto, player, topics);
                    if (turnSucess && isGameActive.get()) {
                        inGamePlayers.offer(player);
                    }
                    Thread.sleep(1000);  // wait for 1 second before moving to the next player
                }
            } catch (Exception e) {
               // handle exceptions 
            }
            if (isGameActive.get()) {
                System.out.println(inGamePlayers.peek() + " wins!");
            }
        });

    }

    /**
     * Ask player a question, start count down and validates answer
     *
     * @param questionDto the question to ask
     * @param player the currnet player
     * @param Topics topics,
     *
     * @return true if answer is correct otherwise false
     */
    private boolean nextTurn(QuestionDto questionDto, Player player, Topics topics) {
        questionDto.setPlayerId(player.getId());

        askQuestion(questionDto, topics.questionTopic);

        Boolean isAnswerAvailable = countDownService.startCountDown(7, player.getRoomId(), currentcount -> {
            notificationService.sendMessageToTopic(topics.roomUpdatesTopic, currentcount);
        }).join();

        if (!isAnswerAvailable) {
            return false;
        }
            
        try {
            AnswerDto answerDto = (AnswerDto) redisService.getValue(String.format(playerAnswerKeyFormat, player.getId())); 
            if (!answerDto.getCategory().equals(questionDto.getCategory())) {
                throw new IllegalArgumentException("Not the same category");
            }
            return validate(answerDto, topics.answerTopic);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Constructs and sends the question to the connected clients
     *
     * @param questionDto the dto that will store the question
     * @param questionTopic the question topic
     */
    public void askQuestion(QuestionDto questionDto, String questionTopic) {
        String question = String.format("Name of %s that you know", questionDto.getCategory());
        notificationService.sendMessageToTopic(questionTopic, questionDto);
    }

    /**
     * Select a category at random
     * @return the category
     */
    private String chooseCategory() {
        int randomIndex = ThreadLocalRandom.current().nextInt(categories.length);
        return categories[randomIndex];
    }

    /**
     * Retrive all topics where clients are subscribed to 
     *
     * @param roomId the room id use to construct the topic
     *
     * @return Topics, containing topics for room updates, answer and question
     */
    private Topics getTopics(String roomId) {
        return new Topics(
                String.format(questionTopicFormat, roomId), 
                String.format(answerTopicFormat, roomId),
                String.format(roomUpdatesTopicFormat, roomId));
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
     * Validates answer
     *
     * @param answerDto the answer
     * @param answerTopic the answer topic
     *
     * @return true if the answer is correct otherwise false
     */
    private boolean validate(AnswerDto answerDto, String answerTopic) {
        if (checkAnswer(answerDto)) {
            // increment player score
            playerService.incrementScore(answerDto.getPlayerId(), scoreStep);
            answerDto.markAsCorrect();  // mark answer as correct
                                       
            notificationService.sendMessageToTopic(answerTopic, "Correct");
        } else {
            answerDto.markAsIncorrect();  // mark answer as correct
            notificationService.sendMessageToTopic(answerTopic, "Wrong");
        }

        return answerDto.getIsCorrect();
    }

    /**
     * Create a question
     *
     * @param category the category which the question is based on
     * @return A question dto
     */
    private QuestionDto CreateQuestion(String category) {
        String question = String.format("Name of %s that you know", category);

        return new QuestionDto(category, question);
    }

    /**
     * Send the current round to connected clients
     *
     * @param roomUpdatesTopic topic for room updates
     * @param round current game round
     */
    private void sendRoundMessage(String roomUpdatesTopic, Integer round) {
        notificationService.sendMessageToTopic(
                roomUpdatesTopic, 
                "\"Round: " + round + "\"");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a count down message to connectd client 
     *
     * @param roomUpdatesTopic topic for room updates
     * @param count current count 
     */
    private void sendCountDownMessage(String roomUpdatesTopic, Integer count) {
        notificationService.sendMessageToTopic(roomUpdatesTopic, count);
    }

    /**
     * Send a message to connected clients when the game for that round starts
     *
     * @param roomUpdatesTopic topic for room updates
     */
    private void sendStartRoundMessage(String roomUpdatesTopic) {
        notificationService.sendMessageToTopic(roomUpdatesTopic, startRoundMessage); 
    }

    /**
     * Store answer received from player in redis
     *
     * @param answerDto the answer
     */
    public void saveAnswer(AnswerDto answerDto) {
        redisService.setValue(String.format(playerAnswerKeyFormat, answerDto.getPlayerId()), answerDto);
    }

    public CountDownService getCountDownService() {
        return countDownService;
    }
    
}
