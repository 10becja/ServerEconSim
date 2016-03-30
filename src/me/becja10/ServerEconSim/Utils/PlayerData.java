package me.becja10.ServerEconSim.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {
	
	public UUID playerId;
	public List<Request> finishedRequests;
	
	public PlayerData(UUID id){
		playerId = id;
		finishedRequests  = new ArrayList<Request>();
	}

}
