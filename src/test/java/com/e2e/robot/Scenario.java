package com.e2e.robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;

import com.e2e.xml.XmlNode;

/**
 * Holding cross scenario variables
 * 
 * @author igors
 *
 */
public class Scenario<T extends Robot> implements RobotListener {

	private String name;
	private CountDownLatch latch;
	private Map<String, Boolean> resultMap;
	private List<T> robots;
	private SharedParameters parameters;

	public Scenario(XmlNode scenario, CountDownLatch latch) {
		resultMap = new ConcurrentHashMap<String, Boolean>();
		name = scenario.getName();
		this.latch = latch;
		robots = new ArrayList<T>();
		parameters = new SharedParameters(scenario);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 * @param result
	 */
	public void robotFinish(String name, boolean result) {
		resultMap.put(name, result);
		latch.countDown();
	}

	/**
	 * @return the latch
	 */
	public CountDownLatch getLatch() {
		return latch;
	}

	/**
	 * 
	 * @return
	 */
	public Collection<String> getRobotResults() {
		return resultMap.keySet();
	}

	/**
	 * 
	 * @param robotName
	 * @return
	 */
	public Boolean getResult(String robotName) {
		return resultMap.get(robotName);
	}

	/**
	 * 
	 * @param robot
	 */
	public void addRobot(T robot) {
		robots.add(robot);
		resultMap.put(robot.getName(), false);
	}

	/**
	 * 
	 * @param json
	 */
	@Override
	public void collectParameters(JsonNode json) {
		parameters.collectParameters(json);
	}

	/**
	 * 
	 * @param jsonList
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	@Override
	public List<JsonNode> updateParameters(List<JsonNode> jsonList)
			throws JsonProcessingException, IOException {
		return parameters.updateParameters(jsonList);
	}

	/**
	 * 
	 * @return
	 */
	public Collection<T> getRobots() {
		return robots;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Scenario [name=").append(name).append("]");
		return builder.toString();
	}
}
