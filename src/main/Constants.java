package main;
import services.Emoji;

public class Constants {
	public static final String USER_AGENT_FIREFOX = "Firefox";
	
	class BotConfig {
		public static final String POWERCAMPUS_TOKEN = "<bot-token>";
		public static final String POWERCAMPUS_USER = "PowerCampusBot";
		public static final long ID_NAZAR = 177192578L;
		public static final long ID_FARHAD = 129967564L;
		public static final long ID_FARID = 43893105L;
	}
	
	public static class Replies {
		public static final String START_COMMAND = "Welcome, %s. %s me to begin.";
		public static final String SETUP_USERNAME = "Let's get started. Please reply to this message with your PowerCampus username.";
		public static final String SETUP_PASSWORD = "Last step, please reply to this message with your PowerCampus password.";
		public static final String ACCOUNT_INFORMATION = "Your username: %s\nYour password: %s";
		public static final String SETUP_COMPLETE = "Setup is complete.\n\n" + ACCOUNT_INFORMATION;
		public static final String CHECK_ACCOUNT = "Check " + Commands.accountCommand + " whether your username and password is correct";
		public static final String SELECT_DAY = "Please select <b>day of week</b>";
		public static final String HELP_COMMAND = "These are the available commands:\n\n" + Commands.commandList;
		public static final String SELECT_COURSE = "Please select a <b>course</b>";
		public static final Object NO_ACTIVITY = "There are no activity grades currently available for this course.";
	}
	
	public static class Commands {
		public static final String commandInitChar = "/";
		public static final String startCommand = commandInitChar + "start";
		public static final String setupCommand = commandInitChar + "setup";
		public static final String accountCommand = commandInitChar + "account";
		public static final String cgpaCommand = commandInitChar + "cgpa";
		public static final String scheduleCommand = commandInitChar + "schedule";
		public static final String helpCommand = commandInitChar + "help";
		public static final String gradesCommand = commandInitChar + "grades";
		
		public static final String commandList = Emoji.GEAR + setupCommand + " - set your PowerCampus username and password\n"
				+ Emoji.INFORMATION_SOURCE + accountCommand + " - information about your username and password\n"
				+ Emoji.GRADUATION_CUP + cgpaCommand + " - your cumulative grade point average\n"
				+ Emoji.TEAR_OFF_CALENDAR + scheduleCommand + " - information about your class schedule\n"
				+ Emoji.HUNDRED_POINTS_SYMBOL + gradesCommand + " - information about your grades\n"
				+ Emoji.SQUARED_SOS + helpCommand + " - information about all commands\n";

	}
	
	public class Url {
		public static final String PC_LOGIN = "http://selfservice.ada.edu.az/Web/Login.aspx";
	    public static final String PC_TRANSCRIPT = "http://selfservice.ada.edu.az/Web/Records/Transcripts.aspx";
	    public static final String PC_CLASS_SCHEDULE = "http://selfservice.ada.edu.az/Web/Records/ClassSchedule.aspx";
	    public static final String PC_GRADE_REPORT = "http://selfservice.ada.edu.az/Web/Records/GradeReport.aspx";
	    public static final String PREFIX_COURSE_LINK = "http://selfservice.ada.edu.az/Web/Records/";
	}
	
	public class Selector {
		public static final String CGPA = "#content div .text75em tbody tr:nth-last-child(3) td:last-child";
		public static final String CGPA_REGEX = "(?:\\d*\\.)?\\d+";
	    public static final String CLASS_NAMES = "a[title]:contains(-)";
	    public static final String CLASS_TIME = "tr[valign]>td[align='left'] + td";
		public static final String FORMAT_SCHEDULE = "<b>Class:</b> %s\n<b>Time & Room:</b> %s\n\n";
		public static final String CLASS_LINKS = "#content>div:last-child>div[class='defaultTable']>div>ul>li>a";
		public static final String CLASS_NAMES_GRADE_REPORT = "#content>div:last-child>table[width='100%']>tbody>tr[id]>td:nth-child(3)>a";
		public static final String CURRENT_TERM = "#content>table>tbody>tr:nth-child(2)>td>a";
		public static final String CUMULATIVE_GRADE = ".total-credits>span>b";
		public static final String ACTIVITIES = "#narrowResultsFinal>div>div[id]>table>tbody>tr:not(tr[class='trTableHeader'])";
	}
	
	public class Status {
		public static final int WAITING_USERNAME = 1;
		public static final int WAITING_PASSWORD = 2;
		public static final int SETUP_OK = 3;
	}
	
	public static class State {
		public static final int DEFAULT = 0;
		public static final int WAITING_SCHEDULE = 1;
		public static final int WAITING_CLASS_NAME = 2;
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
													+ "status INTEGER NOT NULL DEFAULT 0, " 
													+ "messageId INTEGER NOT NULL DEFAULT 0, "
													+ "telegramName VARCHAR(100), "
													+ "username VARCHAR(100), "
													+ "password VARCHAR(100)"
													+ ");";
	   public static final String addUserSQL = "INSERT INTO users (userId, status, messageId, telegramName, username, password) "
													+ "VALUES(?, ?, ?, ?, ?, ?) "
													+ "ON DUPLICATE KEY UPDATE status = ?, messageId = ?";
	   public static final String createUserStateTable = "CREATE TABLE IF NOT EXISTS UserState (userId INTEGER PRIMARY KEY, state INTEGER DEFAULT 0, courses VARCHAR(1200));";
	}
}
