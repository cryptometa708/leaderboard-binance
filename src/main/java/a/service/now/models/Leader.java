package a.service.now.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Leader {

	private String encryptedUid;
	private String nickName;
	private long rank;
	private double value;
	private String twitterUrl;

	public Leader() {
	}

	public Leader(String encryptedUid, String nickName, long rank, double value, String twitterUrl) {
		super();
		this.encryptedUid = encryptedUid;
		this.nickName = nickName;
		this.rank = rank;
		this.value = value;
		this.twitterUrl = twitterUrl;
	}

	public Leader(Map<String, String> values) {
		this(values.get("encryptedUid"), values.get("nickName"), Long.parseLong(values.get("rank")),
				Double.parseDouble(values.get("value")), values.get("twitterUrl"));
	}

	public String getEncryptedUid() {
		return encryptedUid;
	}

	public void setEncryptedUid(String encryptedUid) {
		this.encryptedUid = encryptedUid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getTwitterUrl() {
		return twitterUrl;
	}

	public void setTwitterUrl(String twitterUrl) {
		this.twitterUrl = twitterUrl;
	}

	@Override
	public String toString() {
		return "Leader [encryptedUid=" + encryptedUid + ", nickName=" + nickName + ", rank=" + rank + ", value=" + value
				+ ", twitterUrl=" + twitterUrl + "]";
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof Position))
			return false;
		Leader leader = (Leader) object;
		return leader.encryptedUid.equals(this.encryptedUid);
	}

	@Override
	public int hashCode() {
		return this.encryptedUid.hashCode();
	}

}
