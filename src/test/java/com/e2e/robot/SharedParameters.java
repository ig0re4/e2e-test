package com.e2e.robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.e2e.util.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.e2e.xml.XmlNode;

public class SharedParameters {

	private static final String PARAMETERS = "parameters";
	private Map<String, String> collectMap;
	private Map<String, String> updateMap;
	private ObjectMapper mapper = new ObjectMapper();
	
	public SharedParameters(XmlNode node) {
		collectMap = new ConcurrentHashMap<String, String>();
		updateMap = new ConcurrentHashMap<String, String>();
		node.getSon(PARAMETERS).getSonsExceptDefault().forEach(
				son -> collectMap.put(son.getName(), son.getValue()));
	}

	@FunctionalInterface
	public interface UpdateInterface<T, V> {
	    void accept(final T t, final V v);
	}
	
	/**
	 * 
	 * @param json
	 */
	public void collectParameters(JsonNode json) {
		collectMap.keySet().stream().
			filter(key -> !updateMap.containsKey(collectMap.get(key))).
			forEach(new Consumer<String>() {
			    @Override
				public void accept(String key) {
			    	JsonNode foundNode = JsonUtils.findNodeByName(json, key);
			    	if(foundNode != null){
			    		updateMap.put(collectMap.get(key), foundNode.asText());
			    	}
				}
			});
	}
	
	/**
	 * 
	 * @param jsonList
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public List<JsonNode> updateParameters(List<JsonNode> jsonList) 
			throws JsonProcessingException, IOException {
		for(String key : updateMap.keySet()){
			List<JsonNode> updatedList = new ArrayList<JsonNode>(jsonList);
			for(JsonNode json: jsonList){
				String str = json.toString();
				if(str.indexOf(key) != -1){
					updatedList.add(mapper.readTree(str.replaceAll(key, updateMap.get(key))));
				}else{
					updatedList.add(json);
				}
			}
			jsonList = updatedList;
		}
		return jsonList;
	}
}