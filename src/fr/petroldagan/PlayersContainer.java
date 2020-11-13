package fr.petroldagan;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

public class PlayersContainer {
	
	public static int RUN_PAY = 2000;
	private static BotInstance inst;
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Collator col = Collator.getInstance();
	
	static {
		col.setDecomposition(Collator.NO_DECOMPOSITION);
	}
	
	private File saveFile = new File("." + File.separator + "save.json");
	private ArrayList<PlayerData> list = null;
	
	public PlayersContainer(BotInstance botinst) {
		inst = botinst;
		if(!saveFile.exists()) {
			new File(".").mkdirs();
			try {
				saveFile.createNewFile();
				list = new ArrayList<PlayersContainer.PlayerData>();
				save();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		load();
	}
	
	public void save() {
		try {
			FileWriter writer = new FileWriter(saveFile);
			
			String json = gson.toJson(list);
			writer.write(json);
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		try {
			list = gson.fromJson(new FileReader(saveFile), new TypeToken<ArrayList<PlayersContainer.PlayerData>>(){}.getType());
			
			for(PlayerData i : list) {
				if(i.runs == null) {
					i.runs = new HashMap<Long, Boolean>();
				}
				if(i.payDates == null) {
					i.payDates = new HashMap<Long, Integer>();//In run number
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			list = null;
			return;
		}
	}
	
	public List<PlayerData> searchPlayers(String anything){
		System.out.println("Launching a search for :" + anything);
		ArrayList<PlayerData> listSearched = new ArrayList<PlayersContainer.PlayerData>();
		
		for(PlayerData data : list) {
			try {
				long id = Long.parseLong(anything);
				if(data.id == id) {
					listSearched.add(data);
				}
				
				if((data.id + "").contains(id + "")) {
					listSearched.add(data);
				}
			}catch(NumberFormatException e) {}
			
			if(data.displayName.equalsIgnoreCase(anything)) {
				listSearched.add(data);
			}
			
			if(data.username.equalsIgnoreCase(anything)) {
				listSearched.add(data);
			}
			
			if(col.equals(anything.toLowerCase(), data.displayName.toLowerCase())) {
				listSearched.add(data);
			}
			
			if(data.displayName.toLowerCase().contains(anything.toLowerCase())) {
				listSearched.add(data);
			}
			
			if(data.username.toLowerCase().contains(anything.toLowerCase())) {
				listSearched.add(data);
			}
		}
		
		return listSearched.stream().distinct().collect(Collectors.toList());
	}
	
	public PlayerData searchPlayer(String anything) {
		List<PlayerData> list = searchPlayers(anything);
		return list.size() == 1 ? list.get(0) : null;
	}
	
	public PlayerData getPlayer(long id) {
		for(PlayerData data : list) {
			if(data.id == id) {
				return data;
			}
		}
		
		return null;
	}
	
	public PlayerData createPlayer(String username, String displayName, long id) {
		if(getPlayer(id) != null) return getPlayer(id);
		PlayerData data = new PlayerData(username, displayName, id);
		list.add(data);
		save();
		return data;
	}
	
	public PlayerData getFromMessageId(long messageId) {
		for(PlayerData data : list) {
			if(data.messageId == messageId) {
				return data;
			}
		}
		
		return null;
	}
	
	public List<PlayerData> getAllData(){
		return list;
	}
	
	//
	
	public class PlayerData {
		public final String username;
		public final String displayName;
		public final long id;
		private long lastEdit = System.currentTimeMillis();
		private HashMap<Long, Boolean> runs = new HashMap<Long, Boolean>();
		private HashMap<Long, Integer> payDates = new HashMap<Long, Integer>();//In run number
		private long messageId = -1;
		
		public PlayerData(String username, String displayName, long id) {
			this.username = username;
			this.displayName = displayName;
			this.id = id;
		}
		
		public void addPassiveRun(long i) {
			while(i > 0) {
				i--;
				runs.put((new Random().nextInt(100000) + i), true);
			}
		}
		
		public void addRun(int i) {
			while(i > 0) {
				i--;
				runs.put(System.currentTimeMillis() + i, false);
			}
			lastEdit = System.currentTimeMillis();
		}
		
		public void pay(int payRun) {
			if(payRun > toPayRuns()) throw new IllegalArgumentException("Impossible de payer plus que travailler...");
			if(payRun + paidRunsThisWeek() > 35) throw new IllegalArgumentException("Impossible de dépasser le quota de 35 runs par semaine");

			payDates.put(System.currentTimeMillis(), payRun);
			for(Entry<Long, Boolean> entry : runs.entrySet()) {
				if(entry.getValue()) continue;
				runs.replace(entry.getKey(), true);
				payRun--;
				if(payRun <= 0) break;
			}
			lastEdit = System.currentTimeMillis();
		}
		
		public boolean stillActive() {
			return (System.currentTimeMillis() - lastEdit) <= 1000 * 60 * 60 * 24 * 7;
		}

		public int toPayRuns() {
			int total = 0;
			for(Entry<Long, Boolean> entry : this.runs.entrySet()) {
				if(!entry.getValue()) total++;
			}
			return total;
		}
		
		public int toPay() {
			return toPayRuns() * RUN_PAY;
		}
		
		public int alreadyPaidRuns() {
			return this.runs.size() - toPayRuns();
		}
		public int alreadyPaid() {
			return alreadyPaidRuns() * RUN_PAY;
		}
		
		public int paidRunsThisWeek() {
			int total = 0;
			for(Entry<Long, Integer> entry : this.payDates.entrySet()) {
				if(entry.getKey() > System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) {
					total += entry.getValue();
				}
			}
			return total;
		}
		
		public int paidThisWeek() {
			return this.paidRunsThisWeek() * RUN_PAY;
		}
		
		public int totalRuns() {
			return runs.size();
		}
		
		public int runsThisWeek() {
			int total = 0;
			for(Entry<Long, Integer> entry : this.payDates.entrySet()) {
				if(entry.getKey() > System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) {
					total++;
				}
			}
			return total;
		}
		
		//
		
		public void postPay() {
			if(messageId > 0) {
				updatePostPay();
				return;
			}

			inst.client.getChannelById(inst.salaireId).cast(TextChannel.class).doOnSuccess(channel -> {
				Message msg = channel.createMessage(spec -> {
					spec.setEmbed(embed -> {
						embed.setTitle("**" + this.displayName + "**  (" + this.id + ")");
						embed.setColor(Color.CYAN);
						embed.setDescription(
									"\n**Run** : " + this.runs.size()
								+ 	"\n**Reste à payer** : " + this.toPay() + "$"
								+	"\n**Déjà payé cette semaine** : " + this.paidThisWeek() + "$"
								+	"\n**Déjà payé** : " + this.alreadyPaid() + "$");
						embed.setFooter("Dernière Run : " + new Date(this.lastEdit).toString(), "");
					});
				}).block();
				messageId = msg.getId().asLong();
			}).block();
		}

		private void updatePostPay() {
			try {
				inst.client.getChannelById(inst.salaireId)
					.cast(TextChannel.class)
					.flatMap(channel -> channel.getMessageById(Snowflake.of(messageId)))
					.flatMap(Message::delete)
					.block();
			}catch(Throwable e) {
				System.out.println("Unable to delete previous msg...");
			}
			messageId = -1;
			postPay();
		}
	}
	
}
