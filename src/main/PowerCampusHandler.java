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
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import database.DatabaseManager;
import commands.CGPA;

public class PowerCampusHandler extends TelegramLongPollingBot {
	private static final String LOGTAG = "POWERCAMPUSHANDLERS";

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

			if (messageText.startsWith(Constants.Commands.startCommand)) {
				onStartCommand(message);
			} else if (messageText.startsWith(Constants.Commands.setupCommand)) {
				onSetupCommand(message);
			} else if (messageText.startsWith(Constants.Commands.accountCommand)) {
				onAccountCommand(message);
			} else if (messageText.startsWith(Constants.Commands.cgpaCommand)) {
				sendMessage(CGPA.onCGPACommand(message));
			}
			// If message is not command...
			else if (!messageText.startsWith(Constants.Commands.commandInitChar)) {
				// Waiting for reply with username
				if (DatabaseManager.getInstance()
						.getUserStatus(message.getFrom().getId()) == Constants.Status.WAITING_USERNAME
						&& message.isReply()
						&& DatabaseManager.getInstance().getUserMessageId(message.getFrom().getId()) == message
								.getReplyToMessage().getMessageId()) {
					onUsernameReceived(message);
				}
				// Waiting for reply with password
				else if (DatabaseManager.getInstance()
						.getUserStatus(message.getFrom().getId()) == Constants.Status.WAITING_PASSWORD
						&& message.isReply()
						&& DatabaseManager.getInstance().getUserMessageId(message.getFrom().getId()) == message
								.getReplyToMessage().getMessageId()) {
					onPasswordReceived(message);
				} else {
					// Do nothing yet
				}
			}
		}
	}

	public void onStartCommand(Message message) {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setText(String.format(Constants.Replies.START_COMMAND, message.getChat().getFirstName(),
				Constants.Commands.setupCommand));
		try {
			sendMessage(sendMessageRequest);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void onSetupCommand(Message message) {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
		forceReplyKeyboard.setSelective(true);
		sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
		sendMessageRequest.setText(Constants.Replies.SETUP_USERNAME);
		try {
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
					BotLogger.error(LOGTAG, e);
				}
			});
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	// Display PowerCampus username and password when /account command is called
	public void onAccountCommand(Message message) {
		String username = DatabaseManager.getInstance().getUsername(message.getFrom().getId());
		String password = DatabaseManager.getInstance().getPassword(message.getFrom().getId());
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setText(String.format(Constants.Replies.ACCOUNT_INFORMATION, username, password));
		try {
			sendMessage(sendMessageRequest);
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);
		}
	}

	public void onUsernameReceived(Message message) {
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
		forceReplyKeyboard.setSelective(true);
		sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
		sendMessageRequest.setText(Constants.Replies.SETUP_PASSWORD);

		try {
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
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);
		}
	}

	public void onPasswordReceived(Message message) {
		String username = DatabaseManager.getInstance().getUsername(message.getFrom().getId());
		String password = message.getText();
		SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setChatId(message.getChatId());
		sendMessageRequest.setReplyToMessageId(message.getMessageId());
		sendMessageRequest.setText(String.format(Constants.Replies.SETUP_COMPLETE, username, password));
		try {
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
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);
		}
	}

}
