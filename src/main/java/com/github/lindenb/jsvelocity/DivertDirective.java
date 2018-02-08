package com.github.lindenb.jsvelocity;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

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
    	LOG.debug("render divert directive");
    	
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
	              	throw new ParseErrorException("filename empty or mising");
	              	}
    	    	final FileOutputStream fos = new FileOutputStream(filename,append);
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
