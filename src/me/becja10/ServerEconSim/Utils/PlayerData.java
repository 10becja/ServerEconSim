package me.becja10.ServerEconSim.Utils;

import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
	
	public UUID playerId;
	public HashMap<Integer, Integer> finishedRequests;
	
	public PlayerData(UUID id){
		playerId = id;
		finishedRequests  = new HashMap<Integer, Integer>();
	}

}
