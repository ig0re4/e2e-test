package com.e2e.util;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class JsonUtils {
	private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	private static final String ANY_VALUE = "ANY";

    /**
     * @author igors
     * Check equals of the two JsonNodes, ignore ANY value in other
     * @param o1
     * @param o2
     * @return
     */
    static public boolean equalsIgnoreANY(JsonNode o1, JsonNode o2){
        if(o1 == o2){
            return true;
        }
        if(o1 == null || o2 == null){
            return false;
        }
        if(o1.getClass() != o2.getClass()) {
            return false;
        }
        if(o1.size() != o2.size()) {
            return false;
        }
        JsonNode other = o2;
        if (o1.getFields() != null) {
            Iterator<Map.Entry<String, JsonNode>> iterator = o1.getFields();
            if(iterator.hasNext()){
                while(iterator.hasNext()){
                    Map.Entry<String, JsonNode> entry = iterator.next();
                    String key = entry.getKey();
                    JsonNode value = entry.getValue();
                    JsonNode otherValue = other.get(key);
                    if(otherValue == null ||
                            (ignoreANY(otherValue) && !equalsIgnoreANY(value, otherValue))){
                        logger.error("Jsons are not equals because of value:" + value +
                                " not equals to the other value:" + otherValue);
                        return false;
                    }
                }
            }else{
                return o1.equals(o2);
            }
        }
        return true;
    }

    /**
     *
     * @param json
     * @return
     */
    public static boolean ignoreANY(JsonNode json) {
        if(json.isTextual()){
            String value = json.getTextValue();
            if(!value.equals(ANY_VALUE)){
                return true;
            }else{
                return false;
            }
        }
        return true;
    }

    /**
     * Find JsonNode property by property name in the json,
     * include search in any level.
     * @param node
     * @param name
     * @return
     */
    public static JsonNode findNodeByName(JsonNode node, String name){
        JsonNode tagNode = node.get(name);
        if(tagNode != null){
            return tagNode;
        }
        Iterator<Map.Entry<String, JsonNode>> iterator = node.getFields();
        JsonNode result = null;
        while(iterator.hasNext()){
            Map.Entry<String, JsonNode> son = iterator.next();
            result = findNodeByName(son.getValue(), name);
        }
        return result;
    }
}
