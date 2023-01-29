package a.service.now.database;

import java.util.Hashtable;
import java.util.Map;

public class RowMapper {

	private String[] source = null;
	private String[] target = null;
	private Map<Integer, Integer> map;

	public RowMapper() {
	}

	public RowMapper(String[] source, String[] target) {
		setSource(source);
		setTarget(target);
	}

	public RowMapper(String... source) {
		this.source = source;
	}

	public String[] getSource() {
		return source;
	}

	public void setSource(String... source) {
		this.source = source;
	}

	public String[] getTarget() {
		return target;
	}

	public void setTarget(String... target) {
		this.target = target;
		createMap();
	}

	public String[] mapRow(String... row) {
		String[] out = new String[target.length];
		for (int i = 0; i < out.length; i++) {
			int j = map.get(i);
			if (j == -1) {
				out[i] = "";
			} else {
				out[i] = row[j];
			}

		}
		return out;
	}

	public void createMap() {
		map = new Hashtable<>();
		if (target != null && source != null) {
			for (int i = 0; i < target.length; i++) {
				int j = indexOf(target[i], source);
				if (j == -1) {
					System.out.println("Erreur de mapping : champs introuvable [" + target[i] + "]");
				}
				map.put(i, j);
			}
		}

	}

	private int indexOf(String index, String[] data) {
		for (int i = 0; i < data.length; i++) {
			if (index.toLowerCase().equals(data[i].toLowerCase()))
				return i;
		}
		return -1;
	}

}
