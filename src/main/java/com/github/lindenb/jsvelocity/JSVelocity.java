package com.github.lindenb.jsvelocity;
import java.util.logging.Logger;
import java.util.*;
import java.util.ArrayList;
import java.io.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;





public class JSVelocity
{
private static final Logger LOG=Logger.getLogger("JSVelocity");
VelocityContext context=new VelocityContext();
private File outDir=null;

public static class Picture
	{
	public String getBase64()
		{
		return "";
		}
	
	public int getWidth()
		{
		return 1;
		}
	public int getHeight()
		{
		return 1;
		}
	}

public static class Tool
	{
	
	
	
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


public class MultiWriter
	extends Writer
	{
	private PrintWriter pw=null;
	public File filename=null;
	
	public File getFile()
		{
		return this.filename;
		}
	
	public void _open(String fName,boolean ignoreIfExists)throws IOException
		{
		close();
		
		this.filename=new File(JSVelocity.this.outDir,fName);
		if(ignoreIfExists && this.filename.exists())
				{
				return;
				}
		LOG.info("opening "+this.filename);
		if(this.filename.getParentFile()!=null)
			{
			this.filename.getParentFile().mkdirs();
			}
		this.pw=new PrintWriter(this.filename);
		}
	
	
	public void open(String fName)throws IOException
		{
		_open(fName,false);
		}
		
	public void openIfMissing(String fName)throws IOException
		{
		_open(fName,true);
		}
	
	@Override
	public void close() throws IOException
		{
		flush();
		if(pw!=null)
			{
			LOG.info("closing "+this.filename);
			pw.close();
			pw=null;	
			}
		filename=null;
		}

	@Override
	public void flush() throws IOException
		{
		if(pw!=null)
			{
			pw.flush();
			}
		else if(this.filename==null)
			{
			System.out.flush();
			}
		}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
		{
		if(pw!=null)
			{
			pw.write(cbuf, off, len);		
			}
		else if(this.filename==null)
			{
			System.out.print(new String(cbuf, off, len));
			}
		}
	@Override
	public String toString()
		{
		return filename==null?"null":filename.toString();
		}
	}


private void usage()
	{
	System.out.println("JS Velocity. Pierre Lindenbaum PhD. 2013.");
	System.out.println("Options:");
	System.err.println(" -C (key) (class.qualified.Name) add this Class into the context.");
	System.err.println(" -c (key) (class.qualified.Name) add an instance of Class into the context.");
	System.err.println(" -s (key) (string) add this string into the context.");
	System.err.println(" -e (key) (json-expr) add this json into the context.");
	System.err.println(" -f (key) (json-file) add this json into the context.");
	System.err.println(" -i (key) and read stdin-json to the context.");
	}
private void put(String key,Object o)
	{
	LOG.info("adding key="+key+" as "+(o==null?"null object":o.getClass().getName()));
	if(context.containsKey(key))
		{
		LOG.warning("Key="+key+" defined twice");
		}
	context.put(key,o);
	}
private void run(String args[]) throws Exception
        {
        
       String readstdin=null;
        int optind=0;
        while(optind< args.length)
                {
                if(args[optind].equals("-h"))
                        {
			usage();
                        return;
                        }
                else if(args[optind].equals("-o") && optind+1< args.length)
			{
			outDir=new File(args[++optind]);
			}

		else if(args[optind].equals("-i") && optind+1< args.length)
			{
			readstdin=args[++optind];
			}
		else if(args[optind].equals("-C") && optind+2< args.length)
			{
			String key=args[++optind];
			String className=args[++optind];
			try
				{
				put(key,Class.forName(className));
				}
			catch(Exception err)
				{
				System.err.println("Cannot load class "+className);
				System.exit(-1);
				}
			}
		else if(args[optind].equals("-c") && optind+2< args.length)
			{
			String key=args[++optind];
			String className=args[++optind];
			try
				{
				put(key,Class.forName(className).newInstance());
				}
			catch(Exception err)
				{
				System.err.println("Cannot create instance of class "+className);
				System.exit(-1);
				}
			}
		else if(args[optind].equals("-s") && optind+2< args.length)
			{
			String key=args[++optind];
			String value=args[++optind];
			put(key,value);
			}
		else if(args[optind].equals("-e") && optind+2< args.length)
			{
			String key=args[++optind];
			Object value=new JSONParser(new StringReader(args[++optind])).parse();
			put(key,value);
			}
		else if(args[optind].equals("-f") && optind+2< args.length)
			{
			String key=args[++optind];
			FileReader r=new FileReader(args[++optind]);
			Object value=new JSONParser(r).parse();
			put(key,value);
			r.close();
			}
                else if(args[optind].equals("--"))
                        {
                        ++optind;
                        break;
                        }
                else if(args[optind].startsWith("-"))
                        {
                        System.err.println("Unknown option "+args[optind]);
                        System.exit(-1);
                        }
                else
                        {
                        break;
                        }
                ++optind;
                }
	if(readstdin!=null)
		{
		LOG.info("Reading from stdin");
		Object o=new JSONParser(new InputStreamReader(System.in)).parse();
		put(readstdin,o);
		}
	if(optind+1!=args.length)
		{
		System.err.println("Illegal number of arguments. Expected one Velocity Template.\n");
		return;
		}
	
	MultiWriter out=new MultiWriter();
	put("out",out);
	put("tool",new Tool());
	put("now",new java.sql.Timestamp(System.currentTimeMillis()));
	File file=new File(args[optind++]);
	LOG.info("Reading VELOCITY template from file "+file);
	VelocityEngine ve = new VelocityEngine();
	ve.setProperty("resource.loader", "file");
	ve.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
	if(file.getParent()!=null) ve.setProperty("file.resource.loader.path",file.getParent());
	ve.init();
	Template template = ve.getTemplate(file.getName());
	template.merge( this.context, out);
	out.close();
        }


public static void main(String args[]) throws Exception
	{
	new JSVelocity().run(args);
	}

}
