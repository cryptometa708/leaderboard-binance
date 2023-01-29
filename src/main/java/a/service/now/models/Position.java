package a.service.now.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Position implements Comparable<Position> {

	private int id;
	private String symbol;
	private double entryPrice;
	private double amount;
	private double markPrice;
	private double pnl;
	private double roe;
	private String updateTimeStamp;
	private String yellow;
	private String leader;
	private int status;

	public Position() {
		this.status = 1;
	}

	public Position(int id, String symbol, double entryPrice, double amount, double markPrice, double pnl, double roe,
			String updateTimeStamp, String yellow, int status, String leader) {
		this.id = id;
		this.symbol = symbol;
		this.amount = amount;
		this.entryPrice = entryPrice;
		this.markPrice = markPrice;
		this.pnl = pnl;
		this.roe = roe;
		this.updateTimeStamp = updateTimeStamp;
		this.yellow = yellow;
		this.status = status;
		this.leader = leader;
	}

	public Position(Map<String, String> values) {
		this(Integer.parseInt(values.get("id")), values.get("symbol"), Double.parseDouble(values.get("entryPrice")),
				Double.parseDouble(values.get("amount")), Double.parseDouble(values.get("markPrice")),
				Double.parseDouble(values.get("pnl")), Double.parseDouble(values.get("roe")),
				values.get("updateTimeStamp"), values.get("yellow"), Integer.parseInt(values.get("status")),
				values.get("leader"));
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public String getLeader() {
		return leader;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getEntryPrice() {
		return entryPrice;
	}

	public void setEntryPrice(double entryPrice) {
		this.entryPrice = entryPrice;
	}

	public double getMarkPrice() {
		return markPrice;
	}

	public void setMarkPrice(double markPrice) {
		this.markPrice = markPrice;
	}

	public double getPnl() {
		return pnl;
	}

	public void setPnl(double pnl) {
		this.pnl = pnl;
	}

	public double getRoe() {
		return roe;
	}

	public void setRoe(double roe) {
		this.roe = roe;
	}

	public String getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(String updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
	}

	public String getYellow() {
		return yellow;
	}

	public void setYellow(String yellow) {
		this.yellow = yellow;
	}

	@Override
	public String toString() {
		String shortOrLong = amount > 0 ? "Long" : "Short";
		String closeOrOpen = status == 0 ? "Close" : "Open";
		return "🔥 " + closeOrOpen + " position :" + symbol + " " + shortOrLong + " 🔥\nentry price : " + entryPrice
				+ "\ncurrent price : " + markPrice + "\namount : " + Math.abs((long) (amount * entryPrice)) + "$";
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof Position))
			return false;
		Position position = (Position) object;
		double percent = (position.entryPrice / this.entryPrice) * 100;
		return position.symbol.equalsIgnoreCase(this.symbol) && leader.equals(position.leader)
				&& (position.entryPrice == this.entryPrice || Math.abs(percent - 100) <= 5);
	}

	@Override
	public int hashCode() {
		return this.symbol.hashCode() + Double.valueOf(this.entryPrice).hashCode() + leader.hashCode();
	}

	@Override
	public int compareTo(Position o) {
		return symbol.compareTo(o.getSymbol());
	}
}
