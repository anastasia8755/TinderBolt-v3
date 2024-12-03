package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "little_tg_tinder_helper_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7520928912:AAFhpTwA5rYzB5TYJ3v3C7fBtZfMp1zglro";
    public static final String OPEN_AI_TOKEN = "chat-gpt-token"; //TODO: додай токен ChatGPT у лапках

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();

        if (message.equals("/start")) {
            sendTextMessage("*Hello*!");
            sendPhotoMessage("main");
            return;

        } else if (message.equals("/stop")) {
            sendTextMessage("*Bye*!");
            sendPhotoMessage("bye");
            return;
        }

        sendTextMessage("_" + message + "_");

        sendTextButtonsMessage("Button message",
                "START", "start",
                "STOP", "stop");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
