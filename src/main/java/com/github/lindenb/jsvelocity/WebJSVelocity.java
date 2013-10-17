package com.github.lindenb.jsvelocity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.github.lindenb.jsvelocity.json.JSNode;


public class WebJSVelocity extends AbstractHandler
	{
	private File templateFile=null;
	private Map<String,Object> context=new HashMap<String, Object>();
	
	private static class DynamicJsonLoading
		{
		private File f;
		DynamicJsonLoading(File f)
			{
			this.f=f;
			}
		public JSNode get() throws IOException
			{
			FileReader in=null;
			try {
				in=new FileReader(f);
				JSONParser p=new JSONParser(in);
				return p.parse();
			} catch (Exception e) {
				throw new IOException(e);
				}
			finally
				{
				if(in!=null) in.close();
				}
			}
		}
	
	@Override
    public void handle(String target,
    			Request baseRequest,
    			HttpServletRequest request,
    			HttpServletResponse response
    			) throws IOException, ServletException
    	{
    	PrintWriter w=response.getWriter();
    	VelocityContext ctx=new VelocityContext();

    	for(String k:context.keySet())
    		{
    		Object v= context.get(k);
    		if(v instanceof DynamicJsonLoading)
    			{
    			v=((DynamicJsonLoading)v).get();
    			}
    		ctx.put(k, v);
    		}
    	
		ctx.put("now",new java.sql.Timestamp(System.currentTimeMillis()));
		ctx.put("tool",new Tools());
		ctx.put("request",request);
		ctx.put("response",response);
		ctx.put("baseRequest",baseRequest);
        
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loader", "file");
		ve.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		if(this.templateFile.getParent()!=null)
			{
			ve.setProperty("file.resource.loader.path",templateFile.getParent());
			}
		ve.init();
		Template template = ve.getTemplate(templateFile.getName());
		
    	response.setContentType("text/plain");
    	//text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
		template.merge(ctx, w);
		w.flush();
		w.close();
		baseRequest.setHandled(true);
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
		System.err.println(" -F (key) (json-file) add this json into the context. Dynamic Loading: the file is reloaded for each request.");
		System.err.println(" -i (key) and read stdin-json to the context.");
		System.err.println(" -P (port) listen port.");
		}
	
	private void run(String args[]) throws Exception
	    {
	    int port=8080;
	   String readstdin=null;
	    int optind=0;
	    while(optind< args.length)
	            {
	            if(args[optind].equals("-h"))
	                    {
	                    usage();
	                    return;
	                    }
	            else if(args[optind].equals("-P") && optind+1< args.length)
					{
					port=Integer.parseInt(args[++optind]);
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
				this.context.put(key,Class.forName(className));
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
				this.context.put(key,Class.forName(className).newInstance());
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
			this.context.put(key,value);
			}
		else if(args[optind].equals("-e") && optind+2< args.length)
			{
			String key=args[++optind];
			Object value=new JSONParser(new StringReader(args[++optind])).parse();
			this.context.put(key,value);
			}
		else if(args[optind].equals("-f") && optind+2< args.length)
			{
			String key=args[++optind];
			FileReader r=new FileReader(args[++optind]);
			Object value=new JSONParser(r).parse();
			this.context.put(key,value);
			r.close();
			}
		else if(args[optind].equals("-F") && optind+2< args.length)
			{
			String key=args[++optind];
			File r=new File(args[++optind]);
			this.context.put(key,new DynamicJsonLoading(r));
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
		Object o=new JSONParser(new InputStreamReader(System.in)).parse();
		this.context.put(readstdin,o);
		}
	if(optind+1!=args.length)
		{
		System.err.println("Illegal number of arguments. Expected one Velocity Template.\n");
		return;
		}
	
	this.templateFile=new File(args[optind++]);
	
	Server server = new Server(port);
    server.setHandler(this);
    server.start();
    server.join();
	}


public static void main(String args[]) throws Exception
	{
	new WebJSVelocity().run(args);
	}

	
	}
