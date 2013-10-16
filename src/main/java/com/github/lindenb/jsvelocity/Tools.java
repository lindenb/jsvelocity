package com.github.lindenb.jsvelocity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.github.lindenb.jsvelocity.json.JSNode;

public class Tools
	{
	private InputStream openStream(Object o) throws Exception
	{
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
public JSNode getJSon(Object o) throws Exception
	{
	InputStream in=openStream(o);
	if(in==null) return null;
	JSNode n=new JSONParser(in).parse();
	in.close();
	return n;
	}

public String escapeTex(Object o)
	{
	return escapeLaTex(o);
	}

public String escapeLaTex(Object o)
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

public String escapeC(Object o)
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

public String escapeXml(Object o)
	{
	if(o==null) return "";
	String s=String.valueOf(o);
	StringBuilder b=new StringBuilder(s.length());
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

public String escapeHttp(Object o) throws Exception
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
