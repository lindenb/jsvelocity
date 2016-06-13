/*
The MIT License (MIT)

Copyright (c) 2016 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
package com.github.lindenb.jsvelocity.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JSUtils{
//private static final org.apache.logging.log4j.Logger LOG=org.apache.logging.log4j.LogManager.getLogger(JSUtils.class);

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
