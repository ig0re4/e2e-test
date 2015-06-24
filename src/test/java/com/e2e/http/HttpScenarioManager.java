package com.e2e.http;

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
public class HttpScenarioManager extends ScenarioManager<HttpRobot> {

	/**
	 * 
	 * @param scenarioNode
	 */
	public HttpScenarioManager(XmlNode scenarioNode) {
		super(scenarioNode);
	}

	@Override
	protected String getToken() {
		return "";
	}

	@Override
	protected HttpRobot createRobot(Scenario<HttpRobot> scenario,
			XmlNode robotNode) throws IOException, IncorrectCommandException,
			DeploymentException {
		return new HttpRobot(scenario, robotNode);
	}

}
