package a.service.now.database;

public class MySQLDataSource extends DataSource {

	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String MYSQL_BRIDGE = "jdbc:mysql:";

	public MySQLDataSource() {

	}

	public MySQLDataSource(String host, String source, String username, String password) {
		super(MYSQL_DRIVER, MYSQL_BRIDGE, host, source, username, password);
		setDelimiters("``");
	}

	public MySQLDataSource(String source, String username, String password) {
		this("localhost", source, username, password);
	}

	public MySQLDataSource(String source, String username) {
		this("localhost", source, username, "toor");
	}

	public MySQLDataSource(String source) {
		this("localhost", source, "root", "toor");
	}

	public String getUrl() {
		return getBridge() + "//" + getHost() + "/" + getSource();
	}

}
