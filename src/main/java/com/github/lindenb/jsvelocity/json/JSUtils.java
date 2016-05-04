package com.github.lindenb.jsvelocity.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JSUtils{
private static final Logger LOG=LogManager.getLogger(JSUtils.class);

private static long ID_GENERATOR=0;	
	
static JSNode convert(final JSNode parent,final com.google.gson.JsonElement E) {
	if(E==null || E.isJsonNull()) return new JSNull(parent);
	if(E.isJsonPrimitive()) {
		final JsonPrimitive primitive = E.getAsJsonPrimitive();
		if(primitive.isNumber()) {
			try {
				return new JSInteger(parent, primitive.getAsString(), primitive.getAsBigInteger());
			} catch(NumberFormatException e) {
				return new JSDecimal(parent, primitive.getAsString(), primitive.getAsBigDecimal());
			}
		}
		else if(primitive.isBoolean()){
			return new JSBoolean(parent, primitive.getAsBoolean());
		} else
		{
			return new JSString(parent, primitive.getAsString());
		}
		
		
	}
	if(E.isJsonArray()) return new JSArray(parent,E.getAsJsonArray());
	if(E.isJsonObject()) return new JSMap(parent,E.getAsJsonObject());
	throw new RuntimeException("Cannot get the type of this json element "+E);
}

static long nextId() { return ++ID_GENERATOR;}
public static JSNode parse(InputStream in) {
	return parse(new InputStreamReader(in));
}
public static JSNode parse(final Reader in) {
	return convert(null, new JsonParser().parse(in));
}


}
