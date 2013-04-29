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

private void usage()
	{
	System.out.println("JS Velocity. Pierre Lindenbaum PhD. 2013.");
	System.out.println("Options:");
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
		else if(args[optind].equals("-i") && optind+1< args.length)
			{
			readstdin=args[++optind];
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
	
	File file=new File(args[optind++]);
	LOG.info("Reading VELOCITY template from file "+file);
	VelocityEngine ve = new VelocityEngine();
	ve.setProperty("resource.loader", "file");
	ve.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
	if(file.getParent()!=null) ve.setProperty("file.resource.loader.path",file.getParent());
	ve.init();
	Template template = ve.getTemplate(file.getName());
	PrintWriter w=new PrintWriter( System.out);
	template.merge( this.context, w);
	w.flush();
	w.close();
        }


public static void main(String args[]) throws Exception
	{
	new JSVelocity().run(args);
	}

}
