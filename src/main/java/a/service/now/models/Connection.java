package a.service.now.models;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Connection {
	private java.sql.Connection connection;
	private String database;

	private static final Logger logger = LogManager.getLogger(Connection.class);

	public Connection(String database) {
		this.database = database;
	}

	public void connect() {
		try {
			String url = "jdbc:sqlite:" + database;
			connection = DriverManager.getConnection(url);
			logger.info("Connection to SQLite has been established.");

		} catch (SQLException e) {
			logger.info("Error when trying to open database " + database + " -> " + e.getMessage());
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
}
