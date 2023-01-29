package a.service.now.util;

import java.util.Map;

public interface Client {
	Response doPost(String url, String referer, Object data, Map<String, String> headers) throws Exception;

	Response doGet(String url, String referer, Map<String, String> headers) throws Exception;

}
