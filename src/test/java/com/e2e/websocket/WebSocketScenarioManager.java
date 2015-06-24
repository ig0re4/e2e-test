package com.e2e.websocket;

import java.io.IOException;

import javax.websocket.DeploymentException;

import com.e2e.exceptions.IncorrectCommandException;
import com.e2e.robot.Scenario;
import com.e2e.robot.ScenarioManager;
import com.e2e.xml.XmlNode;

/**
 * 
 * @author igors
 *
 */
public class WebSocketScenarioManager extends ScenarioManager<WebSocketRobot> {

	/**
	 * 
	 * @param scenarioNode
	 */
	public WebSocketScenarioManager(XmlNode scenarioNode) {
		super(scenarioNode);
	}

	@Override
	protected String getToken() {
		return "";
	}

	@Override
	protected WebSocketRobot createRobot(Scenario<WebSocketRobot> scenario,
			XmlNode robotNode) throws IOException, IncorrectCommandException,
			DeploymentException {
		return new WebSocketRobot(scenario, robotNode);
	}

}
