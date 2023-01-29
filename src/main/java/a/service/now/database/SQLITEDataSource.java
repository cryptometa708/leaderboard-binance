package a.service.now.database;

public class SQLITEDataSource extends DataSource {
	
	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	public static final String SQLITE_BRIDGE = "jdbc:sqlite:";

	public SQLITEDataSource() {
		
	}
	
	public SQLITEDataSource(String host, String source, String username, String password) {
		super(SQLITE_DRIVER, SQLITE_BRIDGE, host, source, username, password);
		setDelimiters("``");
	}

	public SQLITEDataSource(String source, String username, String password) {
		this("", source, username, password);
	}
	
	public SQLITEDataSource(String source, String username) {
		this("", source, username, "");
	}
	
	public SQLITEDataSource(String source) {
		this("", source, "", "");
	}
	
	public String getUrl() {
		return getBridge() + getSource();
	}
	
	
}
