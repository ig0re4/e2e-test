package com.e2e.robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;

import com.e2e.util.JsonUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.exceptions.IncorrectCommandException;
import com.e2e.exceptions.IncorrectReceivedMessageException;
import com.e2e.xml.XmlNode;
import com.e2e.xml.XmlToJsonMapper;


public abstract class Robot extends CommandContainer {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(Robot.class);
	private static final int TIME_TO_WAIT_ON_RECEIVE = 2;

	private String token;
	private ObjectWriter writer;
	private ObjectMapper mapper = new XmlToJsonMapper();
	private SharedParameters parameters;

	private BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(1024);

	abstract protected void connect(String uri) throws DeploymentException,
			IOException;

	abstract protected void disconnect() throws Exception;

	abstract protected void send(List<JsonNode> messages);

	/**
	 * Constructor
	 * 
	 * @param listener
	 * @param robot
	 * @throws IOException
	 * @throws IncorrectCommandException
	 * @throws DeploymentException
	 * @throws
	 */
	public Robot(RobotListener listener, XmlNode robot)
			throws IOException, IncorrectCommandException, DeploymentException {
		super(listener, robot);
		parameters = new SharedParameters(robot);
		writer = mapper.writer().withDefaultPrettyPrinter();
	}

	/**
	 * @param node
	 * @throws InterruptedException
	 */
	@Override
	protected void sleep(XmlNode node) throws InterruptedException {
		Thread.sleep(node.getValueAsInt());
	}

	/**
	 * 
	 * @param node
	 * @throws IncorrectCommandException
	 */
	@Override
	protected void notImplementedYet(XmlNode node)
			throws IncorrectCommandException {
		throw new IncorrectCommandException("Scenario:" + getListenerName()
				+ "\nRobot:" + getName() + "\nThe command" + node.getName()
				+ " not implemented yet.");
	}

	/**
	 * 
	 * @param node
	 * @throws DeploymentException
	 * @throws IOException
	 */
	@Override
	protected void consumerConnect(XmlNode node) throws DeploymentException,
			IOException {
		connect(node.getSon("uri").getValue() + token);
	}

	/**
	 * @param node
	 * @throws DeploymentException
	 * @throws IOException
	 */
	@Override
	protected void agentConnect(XmlNode node) throws DeploymentException,
			IOException {
		connect(node.getSon("uri").getValue() + node.getSon("brand").getValue());
	}

	/**
	 * 
	 * @param node
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	@Override
	protected void send(XmlNode node) throws JsonParseException,
			JsonMappingException, JsonGenerationException, IOException {
		List<JsonNode> jsonList = convertToJson(node);
		send(getListener().updateParameters(
				parameters.updateParameters(jsonList)));
	}

	public void updateToken(String token){
		this.token = token;
	}
	
	/**
	 * 
	 * @param node
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws JsonProcessingException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws IncorrectReceivedMessageException
	 */
	@Override
	protected void onReceive(XmlNode node) throws JsonParseException,
			JsonMappingException, JsonGenerationException,
			JsonProcessingException, InterruptedException, IOException,
			IncorrectReceivedMessageException {
		List<JsonNode> expectedNodes = convertToJson(node);
		List<JsonNode> receivedNodes = new ArrayList<JsonNode>(
				expectedNodes.size());
		do {
			String message = responseQueue.poll(TIME_TO_WAIT_ON_RECEIVE,
					TimeUnit.SECONDS);
			JsonNode receivedNode = null;
			try {
				Assert.assertNotNull(message);
				receivedNode = mapper.readTree(message);
				Assert.assertNotNull(receivedNode);
			} catch (Throwable e) {
				throw new IncorrectReceivedMessageException(getListener(),
						this, receivedNodes, expectedNodes, e.getMessage());
			}
			receivedNodes.add(receivedNode);
			parameters.collectParameters(receivedNode);
			getListener().collectParameters(receivedNode);
			boolean found = false;
			for (JsonNode expectedNode : expectedNodes) {
				if (JsonUtils.equalsIgnoreANY(receivedNode, expectedNode)) {
					expectedNodes.remove(expectedNode);
					receivedNodes.remove(receivedNode);
					found = true;
					break;
				}
			}
			try {
				Assert.assertEquals(true, found);
			} catch (Throwable e) {
				throw new IncorrectReceivedMessageException(getListener(),
						this, receivedNodes, expectedNodes, e.getMessage());
			}
		} while (expectedNodes.size() > 0);
	}

	/**
	 * 
	 * @param node
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	private List<JsonNode> convertToJson(XmlNode node)
			throws JsonParseException, JsonMappingException,
			JsonGenerationException, IOException {
		List<JsonNode> messageList = new ArrayList<JsonNode>(node
				.getSonsExceptDefault().size());
		for (XmlNode son : node.getSonsExceptDefault()) {
			messageList.add(mapper.readValue(writer.writeValueAsString(son),
					JsonNode.class));
		}
		return messageList;
	}

	/**
	 * 
	 * @param response
	 */
	protected void addResponse(String response) {
		responseQueue.add(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Robot [scenario=").append(getListenerName())
				.append(", name=").append(getName()).append("]");
		return builder.toString();
	}
}