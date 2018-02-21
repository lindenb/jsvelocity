package com.github.lindenb.jsvelocity;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.apache.commons.lang3.StringUtils;

public class FileLoader
	{
	private static final Logger LOG=LoggerFactory.getLogger(FileLoader.class);
	private boolean lenient_json_parser = false;
	
	
	private boolean isURL(final String uri) {
		if(uri==null) return false;
		return (uri.startsWith("http://") ||
				uri.startsWith("https://") || 
				uri.startsWith("ftp://"));
	}
	
	public InputStream openInputStream(final String uri) throws IOException{
		if(uri==null) throw new NullPointerException("uri is null");
		if(StringUtils.isBlank(uri)) throw new IllegalArgumentException("openInputStream uri is empty");
		
		if(isURL(uri)) {
			final URL url = new URL(uri);
			InputStream in = url.openStream();
			int q= uri.indexOf('?');
			if(q==-1) q=uri.length();
			if(uri.substring(0, q).endsWith(".gz"))
				{
				in = new GZIPInputStream(in);
				}
			return in;
			}
		else
			{
			InputStream in= new FileInputStream(uri);
			if(uri.endsWith(".gz")) {
				in = new GZIPInputStream(in);
			}
			return in;
			}
		}
	
	
	public Reader openReader(final String uri) throws IOException{
		return new InputStreamReader(openInputStream(uri));
		}

	
	public Properties readProperties(final String uri) throws IOException {
		InputStream in = null;
		try {
			in=openInputStream(uri);
			final Properties prop = new Properties();
			prop.load(in);
			in.close();
			return prop;
			}
		finally
			{
			close(in);
			}
		}
	
	
	public List<List<String>> readTable(final String uri,final Pattern pat) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(openReader(uri));
			int ncols=0;
			final List<List<String>> table = new ArrayList<>();
			String line;
			while((line=in.readLine())!=null)
				{
				final List<String> row = new ArrayList<String>(Arrays.asList(pat.split(line)));
				ncols=Math.max(ncols, row.size());
				table.add(row);
				}
			for(final List<String> row:table)
				{
				while(row.size()<ncols) row.add("");
				}
			return table;
			}
		finally
			{
			close(in);
			}
		}
	
	public List<Map<String,String>> readHashTable(final String uri,final Pattern pat) throws IOException {
	BufferedReader in = null;
	try {
		in = new BufferedReader(openReader(uri));
		final List<Map<String,String>> table = new ArrayList<>();
		String line = in.readLine();
		if(line==null ) {
			throw new IOException("Cannot find first line of "+uri);
			}
		final String header[]=pat.split(line);
		while((line=in.readLine())!=null)
			{
			final Map<String,String> row = new LinkedHashMap<>(header.length);
			final String tokens[]=pat.split(line);
			for(int x=0;x<header.length && x< tokens.length;++x)
				{
				row.put(header[x],tokens[x]);
				}
			for(int x=tokens.length;x<header.length;++x)
				{
				row.put(header[x],"");
				}
			table.add(row);
			}
		return table;
		}
	finally
		{
		close(in);
		}
	}
	
	public JsonElement parseJson(final Reader r) {
		final JsonParser parser= new JsonParser();
		JsonReader jr = new JsonReader(r);
		if(this.lenient_json_parser) jr.setLenient(true);
		final JsonElement E= parser.parse(jr);
		return E;
		}
	
	public Object readJSon(final String uri) throws IOException{
		Reader r=null;
		try {
			r = openReader(uri);
			return parseJson(r);
			}
		finally
			{
			close(r);
			}
		}


	private void close(final Closeable c)
		{
		if(c==null) return;
		try { c.close();} catch(final IOException err) {}
		}	
	}
