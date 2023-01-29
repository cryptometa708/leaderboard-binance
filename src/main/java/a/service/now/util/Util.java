package a.service.now.util;

import static a.service.now.util.RandomUserAgent.getRandomUserAgent;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import a.service.now.models.Position;

public class Util {

	private static final Logger logger = LogManager.getLogger(Util.class);

	public static final String USER_AGENT = getRandomUserAgent();

	public static String rand(int len) {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();

	}

	public static boolean isValidEmail(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

	public static boolean isValidPhone(String phone) {
		final String regex = "^" + "    (?:(?:\\+|00)33|0)    #indicatif"
				+ "    \\s*[1-9]             #first number (from 1 to 9)"
				+ "    (?:[\\s.-]*\\d{2}){4}  #End of the phone number" + "$";

		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
		final Matcher matcher = pattern.matcher(phone);
		return matcher.find();
	}

	public static void log(Object aMsg) {
		System.out.println(String.valueOf(aMsg));
	}

	public static void putInFile(String fileName, String message) {
		Path path = Paths.get(fileName);
		try (PrintStream out = new PrintStream(new FileOutputStream(path.toFile(), true))) {
			if (!Files.exists(path))
				Files.createFile(path);
			out.println(message);
			out.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static String generateToken() {

		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";

		int stringLength = 11;

		String randomString = "";

		for (int i = 0; i < stringLength; i++) {

			int rnum = (int) Math.floor(Math.random() * chars.length());

			randomString += chars.substring(rnum, rnum + 1);

		}
		String token = "";
		try {
			token = URLEncoder.encode((randomString), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return token;

	}

	public static String encode(String string) {
		return Base64.getEncoder().encodeToString(string.getBytes());
	}

	public static String decode(String string) {
		return new String(Base64.getDecoder().decode(string.getBytes()));
	}

	public static String telegram(Client client, String token, String userId, String message) throws Exception {

		Map<String, String> data = new HashMap<>();
		data.put("chat_id", userId);
		data.put("text", message);
		String url = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + userId + "&text="
				+ URLEncoder.encode(message, "UTF-8");
		return client.doGet(url, "https://api.telegram.org", null).getResponse();
	}
	public static void main(String[] args) {
		Position position = new Position(12112, "ADA/USDT", 3, 1900, 3.19, 10, 10, "2109201921", "true", 0, "212121212");
		Position _position = new Position(12112, "ADA/USDT", 2.8, 1900, 3.19, 10, 10, "2109201921", "true", 0, "212121212");
		System.out.println(position.equals(_position));
	}

}
