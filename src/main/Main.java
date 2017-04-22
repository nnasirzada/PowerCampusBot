package main;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class Main {

	public static void main(String[] args) {
		ApiContextInitializer.init();
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		try {
			telegramBotsApi.registerBot(new PowerCampusHandler());
			System.out.println("Start time -> " + new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy").format(Calendar.getInstance().getTime()));
		} catch (TelegramApiRequestException e) {
			e.printStackTrace();
		}
	}
}
