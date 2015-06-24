package com.e2e.robot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.websocket.DeploymentException;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.exceptions.IncorrectCommandException;
import com.e2e.xml.XmlLoader;
import com.e2e.xml.XmlNode;

@RunWith(Parameterized.class)
/**
 * 
 * @author igors
 */
public abstract class ScenarioManager<T extends Robot> {
	private static final Logger logger = LoggerFactory.getLogger(ScenarioManager.class);
	private static String SCENARIOS_LOCATION = "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + "scenarios";
	private static final String SCENARIO = "scenario";
	private static XmlLoader loader = XmlLoader.getInstance();
	private XmlNode scenarioNode;

	protected abstract String getToken();

	protected abstract T createRobot(Scenario<T> scenario, XmlNode robotNode)
			throws IOException, IncorrectCommandException, DeploymentException;

	public ScenarioManager(XmlNode scenarioNode) {
		this.scenarioNode = scenarioNode;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@Parameters(name = "{0}")
	public static Collection<Object[]> loadScenarios() throws Exception {
		loader.init(SCENARIOS_LOCATION);
		Collection<XmlNode> scenarios = loader.getEntry("fullcycle/scenarios")
				.getSonsExceptDefault();
		Object[][] array = new Object[scenarios.size()][1];
		int index = 0;
		for (XmlNode scenario : scenarios) {
			array[index++][0] = scenario;
		}

		return Arrays.asList(array);
	}

	/**
	 * 
	 * @param scenarioNode
	 * @return
	 * @throws IncorrectCommandException
	 */
	private Scenario<T> createScenario(XmlNode scenarioNode)
			throws IncorrectCommandException {
		Collection<XmlNode> robotNodes = scenarioNode.getSon(SCENARIO)
				.getSonsExceptDefault();
		Scenario<T> scenario = new Scenario<T>(scenarioNode,
				new CountDownLatch(robotNodes.size()));
		for (XmlNode robotNode : robotNodes) {
			try {
				scenario.addRobot(createRobot(scenario, robotNode));
			} catch (Throwable t) {
				throw new IncorrectCommandException("Scenario:" + scenario
						+ ", Robot:" + robotNode.getName() + ", Exception:"
						+ t.getMessage());
			}
		}
		return scenario;
	}

	//@Test
	public void runScenario() throws IncorrectCommandException {
		Scenario<T> scenario = createScenario(scenarioNode);
		if (scenario != null) {
			logger.info("Start scenario:" + scenario.getName());
			ExecutorService threadPool = Executors.newFixedThreadPool(scenario
					.getRobots().size(), Executors.defaultThreadFactory());
			scenario.getRobots().forEach(new Consumer<T>() {
				@Override
				public void accept(T robot) {
					robot.updateToken(getToken());
					threadPool.execute(new Thread(robot));
				}
			});
			try {
				scenario.getLatch().await();
			} catch (Exception e) {
				logger.info("Error --->>> exception:", e);
			}
			scenario.getRobotResults().forEach(new Consumer<String>() {
				@Override
				public void accept(String robotName) {
					logger.info("Scenario:" + scenario.getName() + ", robot:"
							+ robotName + ", result:"
							+ scenario.getResult(robotName));
					Assert.assertEquals(true, scenario.getResult(robotName));
				}
			});
			logger.info("Finished scenario:" + scenario.getName());
		} else {
			logger.info("Error --->>> scenario is null!!!");
		}
	}
}