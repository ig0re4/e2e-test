package com.e2e.robot;

import java.util.HashMap;
import java.util.Map;


public enum CommandEnum {
	send(1, "send"), 
	receive(2, "receive"), 
	sleep(3, "sleep"), 
	consumerconnect(4, "consumerconnect"), 
	agentconnect(5, "agentconnect"), 
	disconnect(6, "disconnect"), 
	token(7, "token"),
	not_implemented_yet(8, "not_implemented_yet");
	
	private static Map<String, CommandEnum> map = new HashMap<String, CommandEnum>();
	static{
		for(CommandEnum command: CommandEnum.values()){
			map.put(command.getName(), command);
		}
	}
	
	private String name;
	private int id;
	
	private CommandEnum(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the name
	 */
	public int getId() {
		return id;
	}
	
	public static CommandEnum getCommand(String myName){
		int index = myName.indexOf("-");
		if(index != -1){
			return map.get(myName.substring(0, index));
		}else{
			return not_implemented_yet; 
		}
	}
}