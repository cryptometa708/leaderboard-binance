package a.service.now.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Database {

	private DataSource dataSource;
	private Connection db;

	public Database() {

	}

	public Database(DataSource dataSource) {
		this();
		setDataSource(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		db = dataSource.getConnection();
	}

	public Connection getDb() {
		return db;
	}

	public void setDb(Connection db) {
		this.db = db;
	}

	private Map<String, String> initMap(ResultSetMetaData rsm, int nc) throws SQLException {
		Map<String, String> data = new ConcurrentHashMap<>();
		for (int i = 0; i < nc; i++) {
			data.put(rsm.getColumnName(i + 1), "");
		}
		return data;
	}

	public synchronized List<Map<String, String>> executeQuery(String tableName, String query) {
		try {
			Statement sql = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = sql.executeQuery(query);
			ResultSetMetaData rsm = rs.getMetaData();
			int nc = rsm.getColumnCount();
			List<Map<String, String>> allData = new CopyOnWriteArrayList<Map<String, String>>();
			while (rs.next()) {
				Map<String, String> data = initMap(rsm, nc);
				data.keySet().stream().forEach(element -> {
					try {
						data.replace(element, rs.getString(element));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});
				allData.add(data);
			}
			return allData;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error : " + e.getMessage());
			return new CopyOnWriteArrayList<Map<String, String>>();
		}
	}

	public long saveAll(List<? extends Object> objects, String key) {
		if (objects == null || objects.size() == 0)
			return 0;
		AtomicInteger counter = new AtomicInteger(0);
		objects.parallelStream().forEach(object -> counter.addAndGet(save(object, key)));
		return counter.longValue();
	}

	public int save(Object element, String key) {
		Class<? extends Object> object = element.getClass();
		String tableName = object.getSimpleName();
		String value = "";
		try {
			value = setAccessible(element, object.getDeclaredField(key));
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}

		List<Map<String, String>> select = select(tableName, key, value);
		if (select.size() != 0) {
			Map<String, String> map = Arrays.asList(object.getDeclaredFields()).stream()
					.collect(Collectors.toMap(k -> k.getName(), field -> setAccessible(element, field)));
			String req = "UPDATE " + tableName + " SET ";
			req += map.keySet().stream().map(k -> k + "='" + map.get(k) + "'").collect(Collectors.joining(","));
			req += " WHERE " + key + "=" + "'" + value + "';";
			return executeUpdate(req);

		}
		return insert(element);

	}

	public List<Map<String, String>> select(String tableName) {
		char d1 = dataSource.getDelimiters().charAt(0);
		char d2 = dataSource.getDelimiters().charAt(1);
		return executeQuery(tableName, "SELECT * FROM " + d1 + tableName + d2);
	}

	public List<Map<String, String>> select(String tableName, String key, Object value) {
		char d1 = dataSource.getDelimiters().charAt(0);
		char d2 = dataSource.getDelimiters().charAt(1);
		return executeQuery(tableName, "SELECT * FROM " + d1 + tableName + d2 + " WHERE " + key + " ='" + value + "'");
	}

	public List<Map<String, String>> selectLike(String tableName, String key, Object value) {
		char d1 = dataSource.getDelimiters().charAt(0);
		char d2 = dataSource.getDelimiters().charAt(1);
		return executeQuery(tableName,
				"SELECT * FROM " + d1 + tableName + d2 + " WHERE " + key + " LIKE '%" + value + "%'");
	}

	public synchronized int executeUpdate(String query) {
		try {
			Statement sql = db.createStatement();
			return sql.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erreur : " + e.getMessage());
			return 0;
		}
	}

	public int insert(String tableName, Object... row) {
		if (row.length == 0)
			return 0;
		StringBuffer req = new StringBuffer("INSERT INTO " + tableName + " VALUES ('" + quotes(row[0]) + "'");
		for (int i = 1; i < row.length; i++) {
			req.append(", '" + quotes(row[i]) + "'");
		}
		req.append(")");
		System.out.println(req);
		return executeUpdate(req.toString());
	}

	private static String quotes(Object data) {
		if (data == null)
			return "";
		return data.toString().replace("\"", "\\\"");
	}

	public int insert(Object element) {
		Class<? extends Object> object = element.getClass();
		String tableName = object.getSimpleName();
		Map<String, String> map = Arrays.asList(object.getDeclaredFields()).stream()
				.collect(Collectors.toMap(key -> key.getName(), field -> setAccessible(element, field)));
		String req = map.keySet().stream().collect(Collectors.joining("','", "INSERT INTO " + tableName + "('", "')"));
		req += map.values().stream().collect(Collectors.joining("','", "VALUES ('", "')"));
		return executeUpdate(req);
	}

	private String setAccessible(Object element, Field field) {
		try {
			field.setAccessible(true);
			return quotes(field.get(element));
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			throw new RuntimeException(e1);
		}
	}

	public int insert(String tableName, Object[][] data) {
		int counter = 0;
		for (int i = 1; i < data.length; i++) {
			counter += insert(tableName, data[i]);
		}
		return counter;
	}

	public List<Map<String, String>> select(Object element, String key) {
		Class<? extends Object> object = element.getClass();
		String tableName = object.getSimpleName();
		String value = "";
		try {
			value = setAccessible(element, object.getDeclaredField(key));
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
		return select(tableName, key, value);
	}

	public Vector<String> getTables() {
		Vector<String> tables = new Vector<>();
		try {
			DatabaseMetaData dbm = db.getMetaData();
			ResultSet rs = dbm.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				tables.add(rs.getString(3));
			}
		} catch (Exception e) {
			System.out.println("Erreur : " + e.getMessage());
		}
		return tables;
	}

}
