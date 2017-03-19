package main;

public class Constants {
	public static final String USER_AGENT_FIREFOX = "Firefox";
	
	class BotConfig {
		public static final String POWERCAMPUS_TOKEN = "<bot-token>";
		public static final String POWERCAMPUS_USER = "PowerCampusBot";
		public static final long ID_NAZAR = 177192578L;
		public static final long ID_FARHAD = 129967564L;
		public static final long ID_FARID = 43893105L;
	}
	
	public class Replies {
		public static final String START_COMMAND = "Welcome, %s. %s me to begin.";
		public static final String SETUP_USERNAME = "Let's get started. Please reply to this message with your PowerCampus username.";
		public static final String SETUP_PASSWORD = "Last step, please reply to this message with your PowerCampus password.";
		public static final String ACCOUNT_INFORMATION = "Your username: %s\nYour password: %s";
		public static final String SETUP_COMPLETE = "Setup is complete.\n\n" + ACCOUNT_INFORMATION;
		public static final String CHECK_ACCOUNT = "Check " + Constants.Commands.accountCommand + " whether your username and password is correct";
	}
	
	public class Commands {
		public static final String commandInitChar = "/";
		public static final String startCommand = commandInitChar + "start";
		public static final String setupCommand = commandInitChar + "setup";
		public static final String accountCommand = commandInitChar + "account";
		public static final String cgpaCommand = commandInitChar + "cgpa";
		
		private static final String commandList = "setup - set your PowerCampus username and password"
				+ "account - information about your username and password"
				+ "cgpa - your cumulative grade point average";
	}
	
	public class Url {
		public static final String PC_LOGIN = "http://selfservice.ada.edu.az/Web/Login.aspx";
	    public static final String PC_TRANSCRIPT = "http://selfservice.ada.edu.az/Web/Records/Transcripts.aspx";
	    public static final String PC_CLASS_SCHEDULE = "http://selfservice.ada.edu.az/Web/Records/ClassSchedule.aspx";
	}
	
	public class Selector {
		public static final String CGPA = "#content div .text75em tbody tr:nth-last-child(3) td:last-child";
		public static final String CGPA_REGEX = "(?:\\d*\\.)?\\d+";
	    public static final String CLASS_NAMES = "a[title]:contains(-)";
	    public static final String CLASS_TIME = "tr[valign]>td[align='left'] + td";
	}
	
	public class Status {
		public static final int WAITING_USERNAME = 0;
		public static final int WAITING_PASSWORD = 1;
		public static final int SETUP_OK = 2;
	}
	
	public class DB {
		public static final int version = 0;
		public static final String DB_NAME = "powercampusbot";
		public static final String linkDB = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useUnicode=true&characterEncoding=UTF-8";
	    public static final String controllerDB = "com.mysql.jdbc.Driver";
	    public static final String userDB = "root";
	    public static final String password = "";
		public static final String createUserTable = "CREATE TABLE IF NOT EXISTS Users "
													+ "("
													+ "userId INTEGER PRIMARY KEY, " 
													+ "status INTEGER NOT NULL, " 
													+ "messageId INTEGER NOT NULL DEFAULT 0, "
													+ "telegramName VARCHAR(100), "
													+ "username VARCHAR(100), "
													+ "password VARCHAR(100)"
													+ ");";
	   public static final String addUserSQL = "INSERT INTO users (userId, status, messageId, telegramName, username, password) "
													+ "VALUES(?, ?, ?, ?, ?, ?) "
													+ "ON DUPLICATE KEY UPDATE status = ?, messageId = ?";
	}
}
