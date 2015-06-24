package com.e2e.robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.websocket.DeploymentException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.exceptions.IncorrectCommandException;
import com.e2e.exceptions.IncorrectReceivedMessageException;
import com.e2e.xml.XmlNode;

public abstract class CommandContainer
	implements Iterable<Command>, Iterator<Command>, Runnable {
	private static final String ROBOT = "robot";

	private static Logger logger = LoggerFactory.getLogger(CommandContainer.class);

	private List<Command> commandList;
	private HashMap<CommandEnum, ThrowingConsumer<XmlNode>> commandMap;

	private int index = 0;

	private String name;
	private RobotListener listener;
	
	abstract protected void disconnect() throws Exception;
	abstract protected void send(XmlNode node) throws JsonParseException, JsonMappingException, JsonGenerationException, IOException;
	abstract protected void onReceive(XmlNode node)throws InterruptedException, JsonProcessingException, IOException,
			IncorrectReceivedMessageException;
	abstract protected void consumerConnect(XmlNode node) throws DeploymentException, IOException;
	abstract protected void agentConnect(XmlNode node) throws DeploymentException, IOException;
	abstract protected void sleep(XmlNode node) throws InterruptedException;
	abstract protected void notImplementedYet(XmlNode node)	throws IncorrectCommandException;
	
	/**
	 * Constructor
	 * 
	 * @param listener
	 * @param robot
	 * @param token
	 * @throws IOException
	 * @throws IncorrectCommandException
	 * @throws DeploymentException 
	 * @throws  
	 */
	public CommandContainer(RobotListener listener, XmlNode robot)
			throws IOException, IncorrectCommandException, DeploymentException {
		this.name = robot.getName();
		this.listener = listener;
		// ********************************************
		commandMap = new HashMap<CommandEnum, ThrowingConsumer<XmlNode>>(7);
		commandMap.put(CommandEnum.consumerconnect, this::consumerConnect);
		commandMap.put(CommandEnum.agentconnect, this::agentConnect);
		commandMap.put(CommandEnum.send, this::send);
		commandMap.put(CommandEnum.receive, this::onReceive);
		commandMap.put(CommandEnum.disconnect, p -> disconnect());
		commandMap.put(CommandEnum.sleep, this::sleep);
		commandMap.put(CommandEnum.not_implemented_yet, this::notImplementedYet);
		// ********************************************
		Collection<XmlNode> robotCommands = robot.getSon(ROBOT).getSonsExceptDefault();
		commandList = new ArrayList<Command>(robotCommands.size());
		for (XmlNode commandNode : robotCommands) {
			commandList.add(new Command(commandNode));
		}
	}
	
	protected String getListenerName() {
		return listener.getName();
	}
	
	protected RobotListener getListener() {
		return listener;
	}
	
	@Override
	public Iterator<Command> iterator() {
		index = 0;
		return this;
	}

	@Override
	public boolean hasNext() {
		return index < commandList.size();
	}

	@Override
	public Command next() {
		return commandList.get(index++);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public void run() {
		boolean result = false;
		try {
			forEach(command -> commandMap.get(command.getCode())
					.accept(command.getNode()));
			result = true;
			logger.info("Scenario:" + listener.getName() + ", robot:"
					+ getName() + " succesfully finished scenario.");
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			listener.robotFinish(getName(), result);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Robot [commandList=").append(commandList)
				.append(", index=").append(index).append(", name=")
				.append(name).append("]");
		return builder.toString();
	}
}