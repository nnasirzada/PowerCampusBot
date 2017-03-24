package main;

import java.io.IOException;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updateshandlers.SentCallback;
import database.DatabaseManager;
import commands.CGPA;
import commands.Grades;
import commands.Schedule;

public class PowerCampusHandler extends TelegramLongPollingBot {

	@Override
	public void onUpdateReceived(Update update) {
		try {
			handleCommands(update);
		} catch (TelegramApiException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotUsername() {
		return Constants.BotConfig.POWERCAMPUS_USER;
	}

	@Override
	public String getBotToken() {
		return Constants.BotConfig.POWERCAMPUS_TOKEN;
	}

	public void handleCommands(Update update) throws TelegramApiException, IOException {
		Message message = update.getMessage();

		if (message != null && message.hasText()) {
			String messageText = message.getText();
			//If message is command then ...
			if(messageText.startsWith(Constants.Commands.commandInitChar)) {
				if (messageText.startsWith(Constants.Commands.startCommand)) {
					onStartCommand(message);
				} else if (messageText.startsWith(Constants.Commands.setupCommand)) {
					onSetupCommand(message);
				} else if (messageText.startsWith(Constants.Commands.accountCommand)) {
					onAccountCommand(message);
				} else if (messageText.startsWith(Constants.Commands.cgpaCommand)) {
					sendMessage(CGPA.execute(message));
				} else if (messageText.startsWith(Constants.Commands.scheduleCommand)) {
					onScheduleCommand(message);
				} else if (messageText.startsWith(Constants.Commands.helpCommand)) {
					onHelpCommand(message);
				} else if (messageText.startsWith(Constants.Commands.gradesCommand)) {
					onGradesCommand(message);
				} else {
					onInvalidCommand(message);
				}
			}
			// If message is not command...
			else {
				//If message is reply then ...
				if(message.isReply()) {
					// Waiting for reply with username
					if (DatabaseManager.getInstance()
							.getUserStatus(message.getFrom().getId()) == Constants.Status.WAITING_USERNAME
							&& DatabaseManager.getInstance().getUserMessageId(message.getFrom().getId()) == message
									.getReplyToMessage().getMessageId()) {
						onUsernameReceived(message);
					}
					// Waiting for reply with password
					else if (DatabaseManager.getInstance()
							.getUserStatus(message.getFrom().getId()) == Constants.Status.WAITING_PASSWORD
							&& DatabaseManager.getInstance().getUserMessageId(message.getFrom().getId()) == message
									.getReplyToMessage().getMessageId()) {
						onPasswordReceived(message);
					}
				} 
				//If messsage is not reply then ...
				else {
					//If user sent day of week then reply with schedule
					if(DatabaseManager.getInstance().getUserState(message.getFrom().getId()) == Constants.State.WAITING_SCHEDULE) {
						sendMessage(Schedule.execute(message));
					}
					//If user sent course name then reply with activities and points
					else if (DatabaseManager.getInstance().getUserState(message.getFrom().getId()) == Constants.State.WAITING_CLASS_NAME){
						sendMessage(Grades.execute(message));
					}
				}
			}
		}
	}

	private void onInvalidCommand(Message message) throws TelegramApiException {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setText("Command " + message.getText() + " not found. " + Constants.Replies.HELP_COMMAND);
		sendMessage(sendMessageRequest);
	}

	public void onStartCommand(Message message) throws TelegramApiException {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setText(String.format(Constants.Replies.START_COMMAND, message.getChat().getFirstName(),
				Constants.Commands.setupCommand));
		sendMessage(sendMessageRequest);
	}

	public void onSetupCommand(Message message) throws TelegramApiException {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
		forceReplyKeyboard.setSelective(true);
		sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
		sendMessageRequest.setText(Constants.Replies.SETUP_USERNAME);
		
		sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {

			@Override
			public void onResult(BotApiMethod<Message> method, Message sentMessage) {
				if (sentMessage != null) {
					DatabaseManager.getInstance().addUser(message.getFrom().getId(),
							Constants.Status.WAITING_USERNAME, sentMessage.getMessageId(),
							message.getFrom().getFirstName());
				}
			}

			@Override
			public void onException(BotApiMethod<Message> arg0, Exception e) {
			}

			@Override
			public void onError(BotApiMethod<Message> arg0, TelegramApiRequestException e) {
			}
		});
	}

	// Display PowerCampus username and password when /account command is called
	public void onAccountCommand(Message message) throws TelegramApiException {
		String username = DatabaseManager.getInstance().getUsername(message.getFrom().getId());
		String password = DatabaseManager.getInstance().getPassword(message.getFrom().getId());
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setText(String.format(Constants.Replies.ACCOUNT_INFORMATION, username, password));
		sendMessage(sendMessageRequest);
	}

	public void onUsernameReceived(Message message) throws TelegramApiException {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
		forceReplyKeyboard.setSelective(true);
		sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
		sendMessageRequest.setText(Constants.Replies.SETUP_PASSWORD);

		sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {
			@Override
			public void onResult(BotApiMethod<Message> method, Message sentMessage) {
				if (sentMessage != null) {
					DatabaseManager.getInstance().setUsername(message.getFrom().getId(),
							Constants.Status.WAITING_PASSWORD, sentMessage.getMessageId(), message.getText());
				}
			}

			@Override
			public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
			}

			@Override
			public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
			}
		});
	}

	public void onPasswordReceived(Message message) throws TelegramApiException {
		String username = DatabaseManager.getInstance().getUsername(message.getFrom().getId());
		String password = message.getText();
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		sendMessageRequest.setText(String.format(Constants.Replies.SETUP_COMPLETE, username, password));
		
		sendMessageAsync(sendMessageRequest, new SentCallback<Message>() {
			@Override
			public void onResult(BotApiMethod<Message> botApiMethod, Message sentMessage) {
				if (sentMessage != null) {
					DatabaseManager.getInstance().setPassword(message.getFrom().getId(), Constants.Status.SETUP_OK,
							sentMessage.getMessageId(), password);
				}
			}

			@Override
			public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
			}

			@Override
			public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
			}
		});
	}
	
	public void onHelpCommand(Message message) throws TelegramApiException {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		sendMessageRequest.setText(Constants.Replies.HELP_COMMAND);
		sendMessage(sendMessageRequest);
	}

	public void onScheduleCommand(Message message) throws TelegramApiException, IOException {
		sendMessage(Schedule.selectDay(message));
	}
	
	public void onGradesCommand(Message message) throws TelegramApiException, IOException {
		sendMessage(Grades.getClasses(message));
	}
}
