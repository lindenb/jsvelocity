package com.github.lindenb.jsvelocity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonParser;

public class Tools
	{
	private static final Logger LOG=LogManager.getLogger(Tools.class);

	private InputStream openStream(Object o) throws Exception
	{
	LOG.info("open stream "+o);
	if(o==null) return null;
	String uri=o.toString().trim();
	if(uri.isEmpty()) return null;
	InputStream in;
	if( uri.startsWith("https://") ||
		uri.startsWith("http://") ||
		uri.startsWith("ftp://"))
		{
		URL url=new URL(uri);
		in=url.openStream();
		}
	else
		{
		if(uri.startsWith("file://")) uri=uri.substring(7);
		in=new FileInputStream(uri);
		}
	
	return in;
	}


public String getContent(Object o) throws Exception
	{
	InputStream in=openStream(o);
	if(in==null) return "";
	InputStreamReader r=new InputStreamReader(in);
	StringBuilder b=new StringBuilder();
	int c;
	while((c=r.read())!=-1) b.append((char)c);
	r.close();
	return b.toString();
	}
public Object getJSon(final Object o) throws Exception
	{
	final JsonParser parser = new JsonParser();
	InputStream in=openStream(o);
	if(in==null) return null;
	Object n=parser.parse(new InputStreamReader(in));
	in.close();
	return n;
	}

public String escapeTex(final Object o)
	{
	return escapeLaTex(o);
	}

public String escapeLaTex(final Object o)
	{
	if(o==null) return "";
	String s=String.valueOf(o);
	StringBuilder b=new StringBuilder(s.length());
			
	for(int i=0;i< s.length();++i)
		{
		char c=s.charAt(i);
		switch(c)
			{
			case '&': case '%' :
			case '$': case '#':
			case '_': case '{':
			case '}':
				b.append("\\"+c);break;
			case '~': b.append("$\textasciitilde$");
			case '^': b.append("$\textasciicircum$");
			case '\\': b.append("$\textbackslash$");
			
			default: b.append(c);break;
			}
		}
	return b.toString();
	}

public String escapeC(final Object o)
	{
	if(o==null) return "";
	String s=String.valueOf(o);
	StringBuilder b=new StringBuilder(s.length());
	for(int i=0;i< s.length();++i)
		{
		char c=s.charAt(i);
		switch(c)
			{
			case '\n': b.append("\\n");break;
			case '\r': b.append("\\r");break;
			case '\t': b.append("\\t");break;
			case '\\': b.append("\\\\");break;
			case '\'': b.append("\\\'");break;
			case '\"': b.append("\\\"");break;
			default: b.append(c);break;
			}
		}
	return b.toString();
	}

public String escapeXml(final Object o)
	{
	if(o==null) return "";
	final String s=String.valueOf(o);
	final StringBuilder b=new StringBuilder(s.length());
	for(int i=0;i< s.length();++i)
		{
		char c=s.charAt(i);
		switch(c)
			{
			case '<': b.append("&lt;");break;
			case '>': b.append("&gt;");break;
			case '&': b.append("&amp;");break;
			case '\'': b.append("&apos;");break;
			case '\"': b.append("&quot;");break;
			default: b.append(c);break;
			}
		}
	return b.toString();
	}

public String escapeHttp(final Object o) throws Exception
	{
	if(o==null) return "";
	return java.net.URLEncoder.encode(o.toString(),"UTF-8");
	}

@Override
public String toString()
	{
	return "jsvelocity.tool";
	}
}
