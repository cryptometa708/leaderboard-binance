package a.service.now;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import a.service.now.database.Database;
import a.service.now.database.SQLITEDataSource;
import a.service.now.models.Leader;
import a.service.now.models.PeriodType;
import a.service.now.models.Position;
import a.service.now.models.SatisticsType;
import a.service.now.util.Client;
import a.service.now.util.DefaultClient;
import a.service.now.util.Util;

public class Worker {

	private Client client;
	private Database database;

	private static final Logger logger = LogManager.getLogger(Worker.class);

	public Worker() {
		this.client = new DefaultClient();
		database = new Database(new SQLITEDataSource("db/leaderboard.db"));
	}

	public Worker(Client client, Database database) {
		this.client = client;
		this.database = database;
	}

	public List<Leader> leaderBoardPolling(PeriodType period, SatisticsType type, int limit) {
		Map<String, String> headers = new Hashtable<String, String>();
		headers.put("Accept", "*/*");
		headers.put("Accept-Language", "en-GB,en;q=0.9,en-US;q=0.8,fr;q=0.7,ar;q=0.6,es;q=0.5,de;q=0.4");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Connection", "keep-alive");
		headers.put("DNT", "1");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Sec-Fetch-Dest", "empty");
		headers.put("Sec-Fetch-Mode", "cors");
		headers.put("Sec-Fetch-Site", "same-site");
		headers.put("Content-Type", "application/json; charset=utf-8");
		headers.put("User-Agent", Util.USER_AGENT);

		String requestUrl = "https://www.binance.com/bapi/futures/v2/public/future/leaderboard/getLeaderboardRank";
		logger.info("request Url  - " + requestUrl);
		String postString = "{\"isShared\":true,\"periodType\":\"" + period + "\",\"statisticsType\":\"" + type
				+ "\",\"tradeType\":\"PERPETUAL\"}";
		try {
			String data = client.doPost(requestUrl, "https://www.binance.com", postString, headers).getResponse();
			data = data.substring(data.indexOf("[{"));
			data = data.substring(0, data.indexOf("}],\"") + 2);

			ObjectMapper mapper = new ObjectMapper();
			List<Leader> leaders = mapper.readValue(data,
					mapper.getTypeFactory().constructCollectionType(CopyOnWriteArrayList.class, Leader.class));
			return leaders.subList(0, limit);
		} catch (Exception e) {
			logger.error("Error when trying get leaders ->" + e.getMessage());
			return new ArrayList<Leader>();
		}

	}

	public List<Position> leaderPositions(String leaderId, AtomicInteger index) {
		if (index.get() > 100)
			index.set(0);
		int urlIndex = index.getAndIncrement();

		Map<String, String> headers = new Hashtable<String, String>();
		String url = "https://app-server-binance-" + urlIndex
				+ ".herokuapp.com/?command=leader-board-positions&leaderId=" + leaderId;
		headers.put("Accept", "*/*");
		headers.put("Accept-Language", "en-GB,en;q=0.9,en-US;q=0.8,fr;q=0.7,ar;q=0.6,es;q=0.5,de;q=0.4");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Connection", "keep-alive");
		headers.put("DNT", "1");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Sec-Fetch-Dest", "empty");
		headers.put("Sec-Fetch-Mode", "cors");
		headers.put("Sec-Fetch-Site", "same-site");
		headers.put("Content-Type", "application/json; charset=utf-8");
		headers.put("User-Agent", Util.USER_AGENT);
		try {
			String data = client.doGet(url, "https://www.binance.com", headers).getResponse();
			if (data.contains("otherPositionRetList")) {
				ObjectMapper mapper = new ObjectMapper();
				List<Position> positions = mapper.readValue(
						mapper.readTree(data).get("data").get("otherPositionRetList").toString(),
						mapper.getTypeFactory().constructCollectionType(CopyOnWriteArrayList.class, Position.class));
				return positions == null ? new ArrayList<Position>()
						: positions.parallelStream().peek(position -> position.setLeader(leaderId))
								.peek(position -> position.setId(position.hashCode())).collect(Collectors.toList());
			} else {
				String message = "Error in url " + url + " with response \n" + data;
				logger.error(message);
				return leaderPositions(leaderId, index);
			}
		} catch (Exception e) {
			logger.error("Error in leaderPositions method caused by the http get method ->" + e.getMessage());
		}
		return new ArrayList<Position>();

	}

	public void init(PeriodType period, SatisticsType type, int limit) throws Exception {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(() -> {
			List<Leader> leaders = leaderBoardPolling(period, type, limit);
			this.database.saveAll(leaders, "encryptedUid");
			this.polling(leaders);
		}, 0, 4, TimeUnit.HOURS);

	}

	private void notification(Leader leader, Set<Position> positions) {
		String body = leader.getNickName() + "\n";
		body += positions.stream().map(Position::toString).collect(Collectors.joining("\n\n"));
		body += "\nhttps://www.binance.com/en/futures-activity/leaderboard?type=myProfile&tradeType=PERPETUAL&encryptedUid="
				+ leader.getEncryptedUid();
		try {
			Util.telegram(client, "1930919537:AAHh8DGAIHUUu7Uc9_Rb-TD4R1__t3ovBWU", "@ui9018xvb380019bsurqlmzxc7hka612",
					body);
		} catch (Exception e) {
			logger.error("Error when try to send telegram message");
		}
		logger.info(body);
	}

	public void polling(List<Leader> leaders) {
		AtomicInteger index = new AtomicInteger(1);
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleWithFixedDelay(() -> {
			pollingForPositions(leaders, index);
		}, 0, 1, TimeUnit.MINUTES);
	}

	private void pollingForPositions(List<Leader> leaders, AtomicInteger index) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		leaders.forEach(leader -> {
			executorService.execute(() -> job(index, leader));
		});
		executorService.shutdown();
	}

	private void job(AtomicInteger index, Leader leader) {
		String leaderId = leader.getEncryptedUid();
		List<Position> onlinePositions = leaderPositions(leaderId, index);
		List<Position> databasePositions = this.database.select("Position", "leader", leaderId).stream()
				.map(position -> new Position(position)).filter(position -> position.getStatus() == 1)
				.collect(Collectors.toCollection(CopyOnWriteArrayList::new));

		Set<Position> onlinePositionsSet = new HashSet<Position>(onlinePositions);
		Set<Position> databasePositionsSet = new HashSet<Position>(databasePositions);

		if (!onlinePositionsSet.equals(databasePositionsSet)) {
			databasePositionsSet.stream().forEach(position -> position.setStatus(0));
			onlinePositionsSet.addAll(databasePositionsSet);

			Set<Position> difference = onlinePositionsSet.stream()
					.filter(p -> !databasePositions.contains(p) || p.getStatus() == 0).collect(Collectors.toSet());
			notification(leader, difference);
			this.database.saveAll(new ArrayList<Position>(difference), "id");
			// notification not work properly
//			List<Position> difference = onlinePositionsSet.stream().filter(position -> !onlinePositions.contains(position))
//					.collect(Collectors.toList());

//			databasePositionsSet.stream().forEach(p -> System.out.println("databasePositionsSet : " + p.getSymbol()
//					+ "--" + p.getEntryPrice() + "--" + p.getStatus() + " -- " + leader.getNickName()));
//			databasePositions.stream().forEach(p -> System.out.println("databasePositions : " + p.getSymbol() + "--"
//					+ p.getEntryPrice() + "--" + p.getStatus() + " -- " + leader.getNickName()));
		}
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Client getClient() {
		return client;
	}

	public static void main(String[] args) {
		Worker worker = new Worker();
		try {
			switch (args.length) {
			case 1:
				worker.init(PeriodType.ALL, SatisticsType.valueOf(args[0]), 50);
				break;
			case 2:
				worker.init(PeriodType.valueOf(args[0]), SatisticsType.valueOf(args[0]), 50);
				break;
			case 3:
				worker.init(PeriodType.valueOf(args[0]), SatisticsType.valueOf(args[1]), Integer.parseInt(args[2]));
				break;

			default:
				worker.init(PeriodType.ALL, SatisticsType.PNL, 50);
				break;
			}
		} catch (Exception e) {
			System.err.println("java -jar leaderboard.jar [ALL|DAILY|WEEKLY|MONTHLY] [PNL|ROI] [SIZE_OF_LEADERS]");
			logger.error("Error in main method" + e.getMessage());
		}

	}
}
