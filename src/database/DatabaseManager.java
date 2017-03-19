package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.telegram.telegrambots.logging.BotLogger;
import main.Constants;

public class DatabaseManager {
    private static final String LOGTAG = "DATABASEMANAGER";
	private static volatile DatabaseManager instance;
    private static volatile ConnectionDB connection;
    
    private DatabaseManager() {
        connection = new ConnectionDB();
        recreateTable(Constants.DB.version);
    }
    
    public static DatabaseManager getInstance() {
        final DatabaseManager currentInstance;
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }
    
    private void recreateTable(int currentVersion) {
        try {
            connection.initTransaction();
            if (currentVersion == 0) {
                currentVersion = createNewTables();
            }
            connection.commitTransaction();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
    
    private int createNewTables() throws SQLException {
        connection.executeQuery(Constants.DB.createUserTable);
        return Constants.DB.version;
    }
    
    public boolean addUser(Integer userId, int status, int messageId, String telegramName) {
    	int updatedRows = 0;
    	try {
//    		connection.initTransaction();

    		final PreparedStatement preparedStatement = connection.getPreparedStatement(Constants.DB.addUserSQL);
    		preparedStatement.setInt(1, userId);
	        preparedStatement.setInt(2, status);
	        preparedStatement.setInt(3, messageId);
	        if (telegramName == null || telegramName.isEmpty()) {
                preparedStatement.setNull(4, Types.VARCHAR);
            } else {
                preparedStatement.setString(4, telegramName);
            }
	        preparedStatement.setNull(5, Types.VARCHAR);
	        preparedStatement.setNull(6, Types.VARCHAR);
	        preparedStatement.setInt(7, Constants.Status.WAITING_USERNAME);
	        preparedStatement.setInt(8, messageId);
	        updatedRows = preparedStatement.executeUpdate();
//	        connection.commitTransaction();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return updatedRows > 0;
    }
    
    public boolean setUsername(Integer userId, int status, int messageId, String username) {
    	int updatedRows = 0;
    	try {
//    		connection.initTransaction();
	    	final PreparedStatement preparedStatement = connection.getPreparedStatement("UPDATE Users SET status = ?, messageId = ?, username = ? WHERE userId = ?");
	        preparedStatement.setInt(1, status);
	        preparedStatement.setInt(2, messageId);
	        if (username == null || username.isEmpty()) {
                preparedStatement.setNull(3, Types.VARCHAR);
            } else {
                preparedStatement.setString(3, username);
            }
	        preparedStatement.setInt(4, userId);
	        updatedRows = preparedStatement.executeUpdate();
//	        connection.commitTransaction();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return updatedRows > 0;
    }
    
    public boolean setPassword(Integer userId, int status, int messageId, String password) {
    	int updatedRows = 0;
    	try {
    		connection.initTransaction();
	    	final PreparedStatement preparedStatement = connection.getPreparedStatement("UPDATE Users SET status = ?, messageId = ?, password = ? WHERE userId = ?");
	        preparedStatement.setInt(1, status);
	        preparedStatement.setInt(2, messageId);
	        if (password == null || password.isEmpty()) {
                preparedStatement.setNull(3, Types.VARCHAR);
            } else {
                preparedStatement.setString(3, password);
            }
	        preparedStatement.setInt(4, userId);
	        updatedRows = preparedStatement.executeUpdate();
	        connection.commitTransaction();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        return updatedRows > 0;
    }
    
    public String getUsername(Integer userId) {
        String username = "";
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT username FROM users WHERE userId = ?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                username = result.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }
    
    public String getPassword(Integer userId) {
        String password = "";
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT password FROM users WHERE userId = ?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                password = result.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return password;
    }
    
    public int getUserStatus(Integer userId) {
        int status = -1;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT status FROM users WHERE userId = ?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                status = result.getInt("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }

    public int getUserMessageId(Integer userId) {
        int messageId = 0;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT messageId FROM users WHERE userId = ?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                messageId = result.getInt("messageId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageId;
    }
}
