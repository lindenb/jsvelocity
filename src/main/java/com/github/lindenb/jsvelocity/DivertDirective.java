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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DivertDirective extends Directive {
	private static final Logger LOG=LoggerFactory.getLogger(DivertDirective.class);

    public String getName() {
        return "divert";
    }
    
    @Override
    public int getType() {
    	return Directive.BLOCK;
    	}
    @Override
    public boolean render(
    		final InternalContextAdapter ctx,final Writer w,final Node node) 
    		throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
    	LOG.debug("render "+getName()+" directive");
    	
    	String filename=null;
    	boolean append=false;
    	
        //loop through all "params"
        for(int i=0; i<node.jjtGetNumChildren(); i++) {
        	 final Node child  = node.jjtGetChild(i);
        	 if (child == null ) continue;
        	 else if(!(child instanceof ASTBlock)) {
        		switch(i)
        			{
        			case 0: filename = String.valueOf(child.value(ctx)); break;
        			case 1: append = (Boolean)child.value(ctx);break;
        			default:
        				{
        				LOG.warn("Too many arguments for "+this.getName());
        				break;
        				}
        			}
        	 	}
        	 else
        	 	{
        		if(filename==null || filename.trim().isEmpty())
	              	{
	              	throw new ParseErrorException("filename empty or mising in "+getName());
	              	}
        		final File outputFile = new File(filename);
        		if(outputFile.getParentFile()!=null)
        			{
        			outputFile.getParentFile().mkdirs();
        			}
        		LOG.info("diverting to "+outputFile);
    	    	final FileOutputStream fos = new FileOutputStream(outputFile,append);
    	    	final PrintWriter pw = new PrintWriter(fos);
    	    	child.render(ctx, pw);
    	    	pw.flush();
    	    	pw.close();
    	    	break; 
        	 	}
        }
    	
    return true;
    }
}
