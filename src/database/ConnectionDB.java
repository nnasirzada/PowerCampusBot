package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.telegram.telegrambots.logging.BotLogger;

import main.Constants;

public class ConnectionDB {

	private static final String LOGTAG = "CONNECTIONDB";
    private Connection currentConnection;

    public ConnectionDB() {
        this.currentConnection = openConnection();
    }
	
	private Connection openConnection() {
		Connection connection = null;
        try {
            Class.forName(Constants.DB.controllerDB).newInstance();
            connection = DriverManager.getConnection(Constants.DB.linkDB, Constants.DB.userDB, Constants.DB.password);
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            BotLogger.error(LOGTAG, e);
        }

        return connection;
	}

	public void closeConnection() {
        try {
            this.currentConnection.close();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
	
	/**
     * Initialize a transaction in database
     * @throws SQLException If initialization fails
     */
    public void initTransaction() throws SQLException {
        this.currentConnection.setAutoCommit(false);
    }

    /**
     * Finish a transaction in database and commit changes
     * @throws SQLException If a rollback fails
     */
    public void commitTransaction() throws SQLException {
        try {
            this.currentConnection.commit();
        } catch (SQLException e) {
            if (this.currentConnection != null) {
                this.currentConnection.rollback();
            }
        } finally {
            this.currentConnection.setAutoCommit(false);
        }
    }

	public boolean executeQuery(String query) throws SQLException {
        final Statement statement = this.currentConnection.createStatement();
        return statement.execute(query);
	}

	public PreparedStatement getPreparedStatement(String query) throws SQLException {
		return this.currentConnection.prepareStatement(query);
	}
	
	public ResultSet runSqlQuery(String query) throws SQLException {
        final Statement statement;
        statement = this.currentConnection.createStatement();
        return statement.executeQuery(query);
    }
}
