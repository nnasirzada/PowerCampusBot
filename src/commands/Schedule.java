package commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import database.DatabaseManager;
import main.Constants;
import main.CustomConnection;

public class Schedule {
	private static String formatResponse(Elements classNames, Elements classTimes, String dayOfWeek) {
		StringBuilder sb = new StringBuilder();
		int count = 0;

		if(dayOfWeek.equals("General")) {
			for(Element classTime : classTimes) {
				sb.append(String.format(Constants.Selector.FORMAT_SCHEDULE, classNames.get(count).text(),
						classTime.text()));
				count++;
			}
		} else {
			for (int i = 0; i < classTimes.size(); i++) {
				if (classTimes.get(i).text().contains(dayOfWeek)) {
					sb.append(String.format(Constants.Selector.FORMAT_SCHEDULE, classNames.get(i).text(),
							classTimes.get(i).text()));
					count++;
				}
			}
		}

		if (count == 0)
			sb.append("You don't have class on <b>" + dayOfWeek + "</b>.");
		else
			sb.insert(0, String.format("You have <b>%d</b> %s:\n\n", count, (count == 1) ? "class" : "classes"));

		return sb.toString();
	}

	private static ReplyKeyboardMarkup getScheduleKeyboard() {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboad(true);

		List<KeyboardRow> keyboard = new ArrayList<>();

		KeyboardRow keyboardFirstRow = new KeyboardRow();
		keyboardFirstRow.add("Monday");
		keyboardFirstRow.add("Tuesday");
		keyboardFirstRow.add("Wednesday");

		KeyboardRow keyboardSecondRow = new KeyboardRow();
		keyboardSecondRow.add("Thursday");
		keyboardSecondRow.add("Friday");
		keyboardSecondRow.add("Saturday");
		
		KeyboardRow keyboardThirdRow = new KeyboardRow();
		keyboardThirdRow.add("General");

		keyboard.add(keyboardFirstRow);
		keyboard.add(keyboardSecondRow);
		keyboard.add(keyboardThirdRow);

		replyKeyboardMarkup.setKeyboard(keyboard);
		return replyKeyboardMarkup;
	}

	public static SendMessage selectDay(Message message) {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyMarkup(getScheduleKeyboard());
		sendMessageRequest.enableHtml(true);
		sendMessageRequest.setText(Constants.Replies.SELECT_DAY);

		return sendMessageRequest;
	}

	public static SendMessage execute(Message message) throws IOException {
		String response = null;
		DatabaseManager databaseManager = DatabaseManager.getInstance();

		//Get username and password from database and try to login
		Connection.Response auth_response = CustomConnection.connect(
				databaseManager.getUsername(message.getFrom().getId()),
				databaseManager.getPassword(message.getFrom().getId()));

		//If login is successful find class names & times.
		if (CustomConnection.isLoggedIn(auth_response, databaseManager.getUsername(message.getFrom().getId()))) {
			Map<String, String> loginCookies = CustomConnection.getCookies(auth_response);

			// Create connection with class schedule web page and get its content
			Connection.Response schedule = Jsoup.connect(Constants.Url.PC_CLASS_SCHEDULE).method(Connection.Method.GET)
					.cookies(loginCookies).execute();

			Elements classNames = schedule.parse().select(Constants.Selector.CLASS_NAMES);
			Elements classTimes = schedule.parse().select(Constants.Selector.CLASS_TIME);

			response = formatResponse(classNames, classTimes, message.getText());
		} else {
			response = Constants.Replies.CHECK_ACCOUNT;
		}

		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChat().getId());
		ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
		replyKeyboardRemove.setSelective(true);
		sendMessageRequest.setReplyMarkup(replyKeyboardRemove);
		sendMessageRequest.enableHtml(true);
		sendMessageRequest.setText(response);
		return sendMessageRequest;
	}
}
