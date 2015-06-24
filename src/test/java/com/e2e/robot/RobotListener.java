package com.e2e.robot;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;

public interface RobotListener {

	String getName();

	void robotFinish(String name, boolean result);

	public void collectParameters(JsonNode json);

	public List<JsonNode> updateParameters(List<JsonNode> jsonList)
			throws JsonProcessingException, IOException;

}
