package com.e2e.exceptions;

import java.util.List;

import org.codehaus.jackson.JsonNode;

import com.e2e.robot.Robot;
import com.e2e.robot.RobotListener;

@SuppressWarnings("serial")
public class IncorrectReceivedMessageException extends Exception {

	public IncorrectReceivedMessageException(RobotListener scenario, Robot robot, String message,
			String expectedMessage, String description) {
		super("\nScenario:" + scenario.getName() +
			  "\nRobot:" + robot.getName() +	
			  "\nReceived message:" + message +
			  "\nExpected message:" + expectedMessage + 
			  "\n" + description);
	}
	public IncorrectReceivedMessageException(RobotListener scenario, Robot robot,
			List<JsonNode> receivedMessages, List<JsonNode> expectedMessages, String description) {
		super("\nScenario:" + scenario.getName() +
		      "\nRobot:" + robot.getName() +	
			  "\nReceived message:" + receivedMessages +
			  "\nIncorrect Expected messages:" + expectedMessages + 
			  "\n" + description);
	}
}
