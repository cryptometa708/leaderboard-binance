package a.service.now.database;

import java.sql.Connection;
import java.sql.DriverManager;

public abstract class DataSource {
	private String driver;
	private String bridge;
	private String host;
	private String source;
	private String username;
	private String password;
	
	private String delimiters = "[]";
	
	public DataSource() {
		
	}

	public DataSource(String driver, String bridge, String host, String source, String username, String password) {
		super();
		this.driver = driver;
		this.bridge = bridge;
		this.host = host;
		this.source = source;
		this.username = username;
		this.password = password;
	}

	
	public String getDelimiters() {
		return delimiters;
	}

	public void setDelimiters(String delimiters) {
		this.delimiters = delimiters;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setBridge(String bridge) {
		this.bridge = bridge;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	public String getDriver() {
		return driver;
	}

	public String getBridge() {
		return bridge;
	}

	public String getHost() {
		return host;
	}

	public String getSource() {
		return source;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Connection getConnection() {
		try {
			Class.forName(driver);
			Connection db = DriverManager.getConnection(getUrl(), username, password);
			System.out.println("Connexion bien etablie...");
			return db;
		} catch (Exception e) {
			System.out.println("Erreur : " + e.getMessage());
			return null;
		}
	}
	
 
	
	abstract public String getUrl();
	
	
}
