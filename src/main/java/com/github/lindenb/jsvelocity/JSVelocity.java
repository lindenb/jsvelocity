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
package com.github.lindenb.jsvelocity;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import com.github.lindenb.jsvelocity.json.JSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * JSVelocity
 * @author Pierre Lindenbaum
 *
 */
public class JSVelocity
	{
	private static final Logger LOG=LogManager.getLogger(JSVelocity.class);
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
		
		
		public void open(final String fName)throws IOException
			{
			_open(fName,false);
			}
			
		public void openIfMissing(final String fName)throws IOException
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
		System.out.println("JS Velocity. Pierre Lindenbaum PhD. 2016.");
		System.out.println("Options:");
		System.err.println(" -C (key) (class.qualified.Name) add this Class into the context.");
		System.err.println(" -c (key) (class.qualified.Name) add an instance of Class into the context.");
		System.err.println(" -s (key) (string) add this string into the context.");
		System.err.println(" -e (key) (json-expr) add this json expression into the context.");
		System.err.println(" -f (key) (json-file) add this json file into the context.");
		System.err.println(" -i (key) and read stdin-json to the context.");
		}
	private void put(final String key,final Object o)
		{
		LOG.info("adding key="+key+" as "+(o==null?"null object":o.getClass().getName()));
		if(context.containsKey(key))
			{
			LOG.warn("Key="+key+" defined twice");
			}
		context.put(key,o);
		}
	private void run(String args[]) throws Exception
		   { 
		    String readstdin=null;
		    int optind=0;
		    while(optind< args.length)
		        {
		        if(args[optind].equals("-h") || args[optind].equals("--help"))
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
			else if((args[optind].equals("-C") || args[optind].equals("--class")) && optind+2< args.length)
				{
				final String key=args[++optind];
				final String className=args[++optind];
				try
					{
					put(key,Class.forName(className));
					}
				catch(Exception err)
					{
					LOG.error("Cannot load class "+className);
					System.exit(-1);
					}
				}
			else if((args[optind].equals("-c") || args[optind].equals("--instance")) && optind+2< args.length)
				{
				final String key=args[++optind];
				final String className=args[++optind];
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
			else if((args[optind].equals("-s") || args[optind].equals("--string")) && optind+2< args.length)
				{
				final String key=args[++optind];
				final String value=args[++optind];
				put(key,value);
				}
			else if((args[optind].equals("-e") || args[optind].equals("--jsonstring")) && optind+2< args.length)
				{
				final String key=args[++optind];
				final Object value = JSUtils.parse(new StringReader(args[++optind]));
				put(key,value);
				}
			else if((args[optind].equals("-f") || args[optind].equals("--json")) && optind+2< args.length)
				{
				final String key=args[++optind];
				final FileReader r=new FileReader(args[++optind]);
				final Object value= JSUtils.parse(r);
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
                LOG.error("Unknown option "+args[optind]);
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
			Object o= JSUtils.parse(System.in);
			put(readstdin,o);
			}
		if(optind+1!=args.length)
			{
			LOG.error("Illegal number of arguments. Expected one Velocity Template.\n");
			System.exit(-1);
			}
		
		final MultiWriter out=new MultiWriter();
		put("out",out);
		put("tool",new Tools());
		put("now",new java.sql.Timestamp(System.currentTimeMillis()));
		final File file =new File(args[optind++]);
		LOG.info("Reading VELOCITY template from file "+file);
		final VelocityEngine ve = new VelocityEngine();
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
		out.close();
	    }
	
	
	public static void main(String args[])
		{
		try {
			new JSVelocity().run(args);
			}
		catch(Throwable err)
			{
			LOG.error(err);
			err.printStackTrace();
			System.exit(-1);
			}
		}
	
}
