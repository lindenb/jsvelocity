/*
The MIT License (MIT)

Copyright (c) 2018 Pierre Lindenbaum

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
package com.github.lindenb.jsvelocity;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.StringConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;



/**
 * JSVelocity
 * @author Pierre Lindenbaum
 *
 */
public class JSVelocity
	{
	private static final Logger LOG=LoggerFactory.getLogger(JSVelocity.class);
	private VelocityContext context=new VelocityContext();
	static final String PARAM_JSVELOCITY_INSTANCE="\0"+JSVelocity.class.getName()+".instance";
	
	public static interface HierachicalContainer
		{
		public Object parent();
		}
	
	@SuppressWarnings("serial")
	public static class ArrayWithParent extends ArrayList<Object>
		implements HierachicalContainer
		{
		final Object owner;
		ArrayWithParent(final Object owner,final int reserve) {
			super(reserve);
			this.owner = owner;
			}
		@Override
		public Object parent() { return this.owner;}
		}
	@SuppressWarnings("serial")
	public static class MapWithParent extends LinkedHashMap<String,Object>
		implements HierachicalContainer
		{
		final Object owner;
		MapWithParent(final Object owner) {
			this.owner = owner;
			}
		@Override
		public Object parent() { return this.owner;}
		}

	
	
	void put(final String key,final Object o)
		{
		LOG.debug("adding key="+key+" as "+(o==null?"null object":o.getClass().getName()));
		if(this.context.containsKey(key))
			{
			LOG.warn("Key="+key+" defined twice");
			}
		this.context.put(key,o);
		}
	@Parameter(names= {"-h","--help"},description="Show Help",help=true)
	private boolean showHelp=false;
	@Parameter(description = "Files")
	private List<String> files=new ArrayList<>();
	@Parameter(names= {"-o","--output"},description = "Output File. Default: standout")
	private File outputFile=null;
	@Parameter(names= {"-D","--dir","--directory"},description = "Output directory")
	private File outDir=null;

	@Parameter(names= {"-C","--class"},arity=2,description = "Add this java Class into the context..")
	private List<String> inputJavaClasses = new ArrayList<>();
	@Parameter(names= {"-c","--instance"},arity=2,description = "Add this java instance into the context..")
	private List<String> inputJavaInstances = new ArrayList<>();
	@Parameter(names= {"-s","--string"},arity=2,description = "Add this String into the context..")
	private List<String> inputStrings= new ArrayList<>();
	@Parameter(names= {"-e","--expr"},arity=2,description = "Add this JSON-Expression into the context..",converter=StringConverter.class,listConverter=StringConverter.class)
	private List<String> inputJsonExpr= new ArrayList<>();
	@Parameter(names= {"-f","--json"},arity=2,description = "Add this JSON-File into the context..")
	private List<String> inputJsonFiles= new ArrayList<>();
	@Parameter(names= {"-y","--yaml"},arity=2,description = "Add this YAML-File into the context..")
	private List<String> inputYamlFiles= new ArrayList<>();
	@Parameter(names= {"-gson","--gson"},description = "Do not convert json object to java. Keep the com.google.gson.* objects")
	private boolean keep_gson = false;
	@Parameter(names= {"-tsv","--tsv"},arity=2,description = "Read tab delimited table in file.")
	private List<String> inputTsvFiles= new ArrayList<>();
	@Parameter(names= {"-lenient","--lenient"},description = "Use a lenient json parser")
	private boolean lenient_json_parser = false;
	
	Object convertJson(final JsonElement  E)
		{	
		if(this.keep_gson) return E;
		return json2java(null,E);
		}
	
	private Object json2java(final Object owner,final JsonElement  E){
		if(E.isJsonNull()) {
			return null;
			}
		else if(E.isJsonArray())
			{
			final JsonArray o = E.getAsJsonArray();
			final List<Object> L = new ArrayWithParent(owner,o.size());
			for(int i=0;i< o.size();i++)
				{
				L.add(json2java(L,o.get(i)));
				}
			return L;
			}
		else if(E.isJsonObject())
			{
			final JsonObject o = E.getAsJsonObject();
			final Map<String,Object> M = new MapWithParent(owner);
			o.entrySet().stream().forEach(KV->M.put(KV.getKey(), json2java(M,KV.getValue())));
			return M;
			}
		else if(E.isJsonPrimitive())
			{
			final JsonPrimitive prim = E.getAsJsonPrimitive();
			if(prim.isNumber()) {
				try {return new Integer(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new Long(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new BigInteger(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new Double(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new BigDecimal(prim.getAsString());} catch(NumberFormatException e) {}
				}
			if(prim.isBoolean()) return prim.getAsBoolean();
			if(prim.isString()) return prim.getAsString();
			
			
			}
		throw new IllegalStateException("Cannot convert "+E);
		};
	
	private <T>  List<AbstractMap.SimpleEntry<String,T>> mapKeyValues(
		final List<String> array,
		final Function<String,T> mapper)
		{
		final List<AbstractMap.SimpleEntry<String,T>> L = new ArrayList<>(array.size()/2);
		for(int i=0;i+1<array.size();i+=2)
			{
			L.add(new AbstractMap.SimpleEntry<String,T>(
					array.get(i),
					mapper.apply(array.get(i+1))
					));
			}
		return L;
		}
	
	JsonElement parseJson(final Reader r) {
		JsonParser parser= new JsonParser();
		JsonReader jr = new JsonReader(r);
		if(this.lenient_json_parser) jr.setLenient(true);
		final JsonElement E= parser.parse(jr);
		return E;
		}
	
	private Object _parseYaml(final Object owner,Object root)
		{
		if(root==null) return null;
		if(root instanceof List)
			{
			final List<?> o = (List<?>)root;
			final List<Object> L = new ArrayWithParent(owner,o.size());
			for(int i=0;i< o.size();i++)
				{
				L.add(_parseYaml(L,o.get(i)));
				}
			return L;
			}
		else if(root instanceof Map)
			{
			final Map<String,?> o = (Map)root;
			final Map<String,Object> M = new MapWithParent(owner);
			o.entrySet().stream().forEach(KV->M.put((String)KV.getKey(), _parseYaml(M,KV.getValue())));
			return M;
			}
		else
			{
			return root;	
			}
		}
	
	private Object parseYaml(final Reader r) {
		final Yaml yaml = new Yaml();
		final Object o=_parseYaml(null,yaml.load(r));
		return o;
	}
	
	private int run(final String args[]) throws Exception
		{ 
		final JCommander jcommander= new JCommander(this);
		jcommander.setProgramName("jsvelocity");
		jcommander.parse(args);
		if(this.showHelp)
			{
			jcommander.usage();
			return 0;
			}
		
		   
		
		if(this.files.size()!=1)
			{
			LOG.error("Illegal number of arguments. Expected one Velocity Template.\n");
			System.exit(-1);
			}
		
		
		this.mapKeyValues(this.inputJavaClasses,className->{
			try
				{
				return Class.forName(className);
				}
			catch(final Exception err)
				{
				LOG.error("Cannot load class "+className);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));
		
		this.mapKeyValues(this.inputJavaInstances,className->{
			try
				{
				return Class.forName(className).newInstance();
				}
			catch(final Exception err)
				{
				LOG.error("Cannot load new instance of class "+className);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));
		
		this.mapKeyValues(this.inputStrings,value->value).
			forEach(KV->put(KV.getKey(),KV.getValue()));

		this.mapKeyValues(this.inputJsonExpr,value->{
			try 
				{
				return  convertJson(parseJson(new StringReader(value)));
				}
			catch(final Exception err)
				{
				LOG.error("Cannot parse expression "+value);
				System.exit(-1);
				return null;
				}
			
			}).
			forEach(KV->put(KV.getKey(),KV.getValue()));

		this.mapKeyValues(this.inputJsonFiles,value->{
			try
			{
			final FileReader r=new FileReader(value);
			final Object o= convertJson(parseJson(r));
			r.close();
			return o;
			} catch(final IOException err)
				{
				LOG.error("Cannot read file "+value);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));

		this.mapKeyValues(this.inputYamlFiles,value->{
			try
			{
			final FileReader r=new FileReader(value);
			final Object o= parseYaml(r);
			r.close();
			return o;
			} catch(final IOException err)
				{
				LOG.error("Cannot read file "+value);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));

		
		final Writer out;
		if(this.outputFile==null)
			{
			LOG.debug("writing to stdout");
			out = new PrintWriter(System.out);
			}
		else
			{
			LOG.debug("writing to "+this.outputFile);
			out = new FileWriter(this.outputFile);
			}
		put("out",out);
		put("tool",new Tools());
		put("now",new java.sql.Timestamp(System.currentTimeMillis()));
		put(PARAM_JSVELOCITY_INSTANCE, this);
		final File file = new File(this.files.get(0));
		LOG.info("Reading VELOCITY template from file "+file);
		final VelocityEngine ve = new VelocityEngine();
		
		
		
		ve.setProperty("userdirective",
				Arrays.asList(
						JsonDirective.class,
						DivertDirective.class,
						javascriptDirective.class,
						ReadFileDirective.class
						).
				stream().
				map(C->C.getName()).
				collect(Collectors.joining(",")
				));
		ve.setProperty("resource.loader", "file");
		ve.setProperty("file.resource.loader.description", "Velocity File Resource Loader");
		ve.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		if(file.getParent()!=null)
			{	
			ve.setProperty("file.resource.loader.path",file.getParent());		
			}
		ve.init();
		final Template template = ve.getTemplate(file.getName());
		template.merge( this.context, out);
		out.flush();
		out.close();
		return 0;
	    }
	
	public int execute(final String args[]) {
		try 
			{
			int i = this.run(args);
			return i;
			}
		catch(final Throwable err)
			{
			LOG.error("FAILURE", err);
			return -1;
			}
		}
	
	public static void main(final String args[])
		{
		final int err= new JSVelocity().execute(args);
		System.exit(err);
		}
	
}
