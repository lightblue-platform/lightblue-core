package com.redhat.lightblue;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.crud.UpdateExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.util.JsonObject;

public class JsonNodeBuilder {

	private ObjectNode root = JsonObject.getFactory().objectNode();
	
	private boolean includeNulls = false;
	
	public boolean includeNulls() {
		return includeNulls;
	}
	
	public void includeNulls(boolean include) {
		includeNulls = include;
	}
	
	public JsonNodeBuilder add(String key, JsonNode value) {	
		if(include(value)) {
			root.put(key, value);	
		}
		return this;
		
	}
	
	public JsonNodeBuilder add(String key, QueryExpression value) {	
		if(include(value)) {
			root.put(key, value.toString().toLowerCase());	
		}
		return this;
		
	}
	public JsonNodeBuilder add(String key, Projection value) {
		if(include(value)) {
			root.put(key, value.toString());
		}	
		return this;
	}
	
	public JsonNodeBuilder add(String key, Sort value) {
		if(include(value)) {
			root.put(key, value.toString());
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, Long value) {
		if(include(value)) {
			root.put(key, value);
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, Boolean value) {
		if(include(value)) {
			root.put(key, value);
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, OperationStatus value) {
		if(include(value)) {
			root.put(key, value.name().toString());
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, EntityVersion value) {
		if(include(value)) {
			root.put(key, value.toString());
		}
		return this;
	}

	public JsonNodeBuilder add(String key, ClientIdentification value) {
		if(include(value)) {
			root.put(key, value.toString());
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, ExecutionOptions value) {
		if(include(value)) {
			root.put(key, value.toString());
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, SessionInfo value) {
		if(include(value)) {
			root.put(key, value.toString());
		}
		return this;
	}
	
	public JsonNodeBuilder add(String key, UpdateExpression value) {
		if(include(value)) {
			root.put(key, value.toString());
		}
		return this;
	}
	
	public <T> JsonNodeBuilder add(String key, List<T> values) {
		if(includes(values)) {
			ArrayNode arr = JsonObject.getFactory().arrayNode();
            root.set(key, arr);
            for (Object err : values) {
                arr.add(err.toString());
            }
		}
		return this;
	}
	
	private boolean include(Object object) {
		return object != null || includeNulls == true;
	}

	private <T> boolean includes(Collection<T> collection) {
		if(includeNulls == true) {
			return true;
		} else {
			return collection != null && !collection.isEmpty();	
		}
	}
	
	public JsonNodeBuilder add(String key, String value) {
		if(value != null) {
			root.put(key, value);	
		}
		return this;
	}
	
	public JsonNode build() {
		return root;
	}
	
}
