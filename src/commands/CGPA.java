package commands;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import database.DatabaseManager;
import main.Constants;
import main.CustomConnection;

public class CGPA {
	/* Input: Cumulative GPA: 2.45 Cumulative GPA: 3.01
	 * Find only numbers and return as formatted string */
	private static String extractCGPA(String input) {
		StringBuilder sb = new StringBuilder();
		Pattern pattern = Pattern.compile(Constants.Selector.CGPA_REGEX);
		Matcher matcher = pattern.matcher(input);

		int matched = 0;

		while (matcher.find()) {
			if (matched == 0){
				sb.append("Your Cumulative Grade Point Average\n");
				sb.append("EAPP: " + matcher.group() + "\n");
			}
			if (matched == 1)
				sb.append("General Education: " + matcher.group());
			matched++;
		}

		if (matched == 0) {
			sb.append("I found nothing. " + Constants.Replies.CHECK_ACCOUNT);
		}

		return sb.toString();
	}

	public static SendMessage execute(Message message) throws IOException {
		DatabaseManager databaseManager = DatabaseManager.getInstance();
		String CGPA = null;
		
		//Get username and password from database and try to login
		Connection.Response auth_response = CustomConnection.connect(
				databaseManager.getUsername(message.getFrom().getId()),
				databaseManager.getPassword(message.getFrom().getId()));

		//If login is successful find GPA and format it.
		if (CustomConnection.isLoggedIn(auth_response, databaseManager.getUsername(message.getFrom().getId()))) {
			Map<String, String> loginCookies = CustomConnection.getCookies(auth_response);

			// Create connection with transcript web page and get its content
			Connection.Response transcript = Jsoup.connect(Constants.Url.PC_TRANSCRIPT).method(Connection.Method.GET)
					.cookies(loginCookies).execute();

			// Select tr>td which contains Cumulative GPA information
			CGPA = extractCGPA(transcript.parse().select(Constants.Selector.CGPA).toString());
		} else {
			CGPA = Constants.Replies.CHECK_ACCOUNT;
		}

		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		sendMessageRequest.setText(CGPA);
		return sendMessageRequest;
	}
}
