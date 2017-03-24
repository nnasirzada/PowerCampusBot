package commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import database.DatabaseManager;
import main.Constants;
import main.CustomConnection;

public class Grades {
	
	public static ReplyKeyboard getGradesKeyboard(Elements courseNames) {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboad(true);

		List<KeyboardRow> keyboard = new ArrayList<>();

		for(Element e : courseNames) {
			KeyboardRow row = new KeyboardRow();
			row.add(e.text());
			keyboard.add(row);
		}

		replyKeyboardMarkup.setKeyboard(keyboard);
		return replyKeyboardMarkup;
	}
	
	public static SendMessage getClasses(Message message) throws IOException {
		DatabaseManager databaseManager = DatabaseManager.getInstance();
		String coursesLink = null;
		
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChat().getId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		sendMessageRequest.enableHtml(true);
		
		//Get username and password from database and try to login
		Connection.Response auth_response = CustomConnection.connect(
				databaseManager.getUsername(message.getFrom().getId()),
				databaseManager.getPassword(message.getFrom().getId()));

		//If login is successful find GPA and format it.
		if (CustomConnection.isLoggedIn(auth_response)) {
			Map<String, String> loginCookies = CustomConnection.getCookies(auth_response);

			// Create connection with grade report
			Connection.Response gradeReport = Jsoup.connect(Constants.Url.PC_GRADE_REPORT).method(Connection.Method.GET)
					.cookies(loginCookies).execute();
			// Find the url of grades
			coursesLink = gradeReport.parse().select(Constants.Selector.CURRENT_TERM).attr("href");
			//connect to courses pages on grade report
			Connection.Response grades = Jsoup.connect(coursesLink).method(Connection.Method.GET)
					.cookies(loginCookies).execute();
			Elements links = grades.parse().select(Constants.Selector.CLASS_LINKS);
			Elements courseNames = grades.parse().select(Constants.Selector.CLASS_NAMES_GRADE_REPORT);
			JSONObject jsonObject = new JSONObject();
			for(int i = 0; i < links.size(); i++) {
				jsonObject.put(courseNames.get(i).text(), Constants.Url.PREFIX_COURSE_LINK + links.get(i).attr("href"));
			}
			String result = jsonObject.toString();
			//change user status
			databaseManager.addUserState(message.getFrom().getId(), Constants.State.WAITING_CLASS_NAME, result);
			sendMessageRequest.setText(Constants.Replies.SELECT_COURSE);
			sendMessageRequest.setReplyMarkup(getGradesKeyboard(courseNames));
		} else {
			databaseManager.addUserState(message.getFrom().getId(), Constants.State.DEFAULT);
			sendMessageRequest.setText(Constants.Replies.CHECK_ACCOUNT);
		}
		
		return sendMessageRequest;
	}
	
	public static SendMessage execute(Message message) throws IOException {
		String URL = getCourseLink(message.getFrom().getId(), message.getText());
		StringBuilder sb = new StringBuilder();
		
		Connection.Response auth_response = CustomConnection.connect(
				DatabaseManager.getInstance().getUsername(message.getFrom().getId()),
				DatabaseManager.getInstance().getPassword(message.getFrom().getId()));
		
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
		keyboardRemove.setSelective(true);
		sendMessageRequest.setReplyMarkup(keyboardRemove);
		sendMessageRequest.enableHtml(true);

		
		if (CustomConnection.isLoggedIn(auth_response)) {
			Map<String, String> loginCookies = CustomConnection.getCookies(auth_response);

			// Create connection with grade report
			Connection.Response response = Jsoup.connect(URL).method(Connection.Method.GET)
					.cookies(loginCookies).execute();
			//List will have an item if there isn't an activity
			if(!response.parse().getElementsByClass("msgNoData").isEmpty()) {
				sb.append("<b>" + Constants.Replies.NO_ACTIVITY + "</b>");
			//If the list is empty, it means there is at least one activity
			} else {
				//Select cumulative grade of this course
				sb.append(response.parse().select(Constants.Selector.CUMULATIVE_GRADE).toString() + "\n\n");
				Elements activities = response.parse().select(Constants.Selector.ACTIVITIES);

				/* child(0) - Course Name
				 * child(3) - Earned Points
				 * child(4) - Maximum Points */
				for(Element e : activities) {
					sb.append("<b>" + e.child(0).text() + ":</b> " + e.child(3).text() + " " + e.child(4).text()+"\n");
				}
			}
			DatabaseManager.getInstance().addUserState(message.getFrom().getId(), Constants.State.DEFAULT);
			sendMessageRequest.setText(sb.toString());
		} else {
			sendMessageRequest.setText(Constants.Replies.CHECK_ACCOUNT);
		}
		return sendMessageRequest;
	}
	
	private static String getCourseLink(Integer userId, String courseName) {
		JSONObject courses = new JSONObject(DatabaseManager.getInstance().getUserCourses(userId));
		return courses.getString(courseName);
	}
}