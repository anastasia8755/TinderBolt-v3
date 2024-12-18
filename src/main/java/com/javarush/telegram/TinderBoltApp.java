package com.javarush.telegram;

import com.javarush.telegram.utils.BotInfoReader;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = BotInfoReader.getProperty("TELEGRAM_BOT_NAME");
    public static final String TELEGRAM_BOT_TOKEN = BotInfoReader.getProperty("TELEGRAM_BOT_TOKEN");
    public static final String OPEN_AI_TOKEN = BotInfoReader.getProperty("OPEN_AI_TOKEN");
    public DialogMode mode = DialogMode.MAIN;
    private List<String> chat;
    private UserInfo myInfo;
    private UserInfo personInfo;
    private int questionNum;

    public ChatGPTService gptService = new ChatGPTService(OPEN_AI_TOKEN);

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();

        switch (message) {
            case "/start" -> {
                mode = DialogMode.MAIN;

                showMainMenu(
                        "головне меню бота", "/start",
                        "генерація Tinder-профілю \uD83D\uDE0E", "/profile",
                        "повідомлення для знайомства \uD83E\uDD70", "/opener",
                        "листування від вашого імені \uD83D\uDE08", "/message",
                        "листування із зірками \uD83D\uDD25", "/date",
                        "поставити запитання чату GPT \uD83E\uDDE0", "/gpt",
                        "попрощатися", "/bye"
                );

                String menu = loadMessage("main");
                sendTextMessage(menu);
                sendPhotoMessage("main");

                return;
            }
            case "/profile" -> {
                mode = DialogMode.PROFILE;

                sendPhotoMessage("profile");
                String profileMessage = loadMessage("profile");
                sendTextMessage(profileMessage);

                myInfo = new UserInfo();
                questionNum = 1;
                sendTextMessage("Введіть ім*я");

                return;
            }
            case "/opener" -> {
                mode = DialogMode.OPENER;

                sendPhotoMessage("opener");
                String openerMessage = loadMessage("opener");
                sendTextMessage(openerMessage);

                personInfo = new UserInfo();
                questionNum = 1;
                sendTextMessage("Введіть ім*я");

                return;
            }
            case "/message" -> {
                mode = DialogMode.MESSAGE;

                sendPhotoMessage("message");
                String messageHelper = loadMessage("message");

                sendTextButtonsMessage(messageHelper, "Наступне повідомлення", "message_next",
                        "Запросити на побачення", "message_date");

                chat = new ArrayList<>();

                return;
            }
            case "/date" -> {
                mode = DialogMode.DATE;

                sendPhotoMessage("date");

                String date = loadMessage("date");
                sendTextButtonsMessage(date, "Аріана Гранде \uD83D\uDD25", "date_grande",
                        "Марго Роббі \uD83D\uDD25\uD83D\uDD25", "date_robbie",
                        "Зендея \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25", "date_zendaya",
                        "Райан Гослінг \uD83D\uDE0E", "date_gosling",
                        "Том Харді \uD83D\uDE0E\uD83D\uDE0E", "date_hardy");

                return;

            }
            case "/gpt" -> {
                mode = DialogMode.GPT;

                String gpt = loadMessage("gpt");
                sendTextMessage(gpt);
                sendPhotoMessage("gpt");

                return;
            }
            case "/stop" -> {
                sendTextMessage("*Bye*!");
                sendPhotoMessage("bye");
            }
        }
            switch (mode) {

                case PROFILE -> {
                    if (questionNum <= 6) {
                        askQuestion(message, myInfo, "profile");
                    }
                    return;
                }
                case OPENER -> {
                    if (questionNum <= 6) {
                        askQuestion(message, personInfo, "opener");
                    }
                    return;
                }
                case MESSAGE -> {
                    String buttonId = getCallbackQueryButtonKey();

                    if (buttonId.startsWith("message_")) {
                        String prompt = loadPrompt(buttonId);
                        String history = String.join("/n/n", chat);

                        Message msg = sendTextMessage("Почекай:)");

                        String answ = gptService.sendMessage(prompt, history);
                        updateTextMessage(msg, answ);
                    }

                    chat.add(message);
                    return;
                }

                case GPT -> {
                    String prompt = loadPrompt("gpt");
                    Message msg = sendTextMessage("Почекай:)");
                    String answ = gptService.sendMessage(prompt, message);
                    updateTextMessage(msg, answ);
                    return;
                }

                case DATE -> {
                    String buttonId = getCallbackQueryButtonKey();

                    if (buttonId.startsWith("date_")) {
                        sendPhotoMessage(buttonId);
                        String prompt = loadPrompt(buttonId);
                        gptService.setPrompt(prompt);
                    }

                    Message msg = sendTextMessage("Почекай:)");
                    String answ = gptService.addMessage(message);
                    updateTextMessage(msg, answ);
                    return;
                }
            }
    }

    private void askQuestion(String message, UserInfo user, String profileName) {

        switch (questionNum){
            case 1 -> {
                user.name = message;
                questionNum = 2;
                sendTextMessage("Введіть вік");
            }
            case 2 -> {
                user.age = message;
                questionNum = 3;
                sendTextMessage("Введіть місто");
            }
            case 3 -> {
                user.city = message;
                questionNum = 4;
                sendTextMessage("Введіть професію");
            }
            case 4 -> {
                user.occupation = message;
                questionNum = 5;
                sendTextMessage("Введіть хоббі");
            }
            case 5 -> {
                user.hobby = message;
                questionNum = 6;
                sendTextMessage("Вкажіть ціль знайомства");
            }
            case 6 -> {
                user.goals = message;
                String prompt = loadPrompt(profileName);

                Message msg = sendTextMessage("Почекай:)");
                String answ = gptService.sendMessage(prompt, myInfo.toString());
                updateTextMessage(msg, answ);
            }
        }

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}