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


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class javascriptDirective extends Directive {
	private static final Logger LOG=LoggerFactory.getLogger(javascriptDirective.class);

    public String getName() {
        return "javascript";
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
    	
    	final List<Object> arguments = new ArrayList<>();
        //loop through all "params"
        for(int i=0; i<node.jjtGetNumChildren(); i++) {
        	 final Node child  = node.jjtGetChild(i);
        	 if (child == null ) continue;
        	 else if(!(child instanceof ASTBlock)) {
        		arguments.add(child.value(ctx));
        	 	}
        	 else
        	 	{        		 
                final ScriptEngineManager manager = new ScriptEngineManager();
                final ScriptEngine engine = manager.getEngineByName("nashorn");
                if(engine==null) {
                	throw new ResourceNotFoundException("Cannot find nashorn engine");
                }
                final String contextVar="__velocity_ctx";
                final String outVar="__velocity_out";
            	final StringWriter outstr = new StringWriter();
                final PrintWriter pw = new PrintWriter(outstr);
                
               
                
                final Bindings bindings = new SimpleBindings();
                bindings.put(contextVar, ctx);
                bindings.put(outVar, pw);
                bindings.put("args", arguments);
                for(final String key: ctx.getKeys())
                	{
                	if(bindings.containsKey(key)) continue;
                	bindings.put(key, ctx.get(key));
                	}
               
                final StringWriter script = new StringWriter();
                script.append(
                		"var ArrayList = Java.type('java.util.ArrayList');"+
                		"var HashMap = Java.type('java.util.HashMap');"+
                		"var HashSet = Java.type('java.util.HashSet');"+
    	                "function getContext(){return "+contextVar+";}"+
    	    			"function print(o){if(arguments.length!=0) "+outVar+".print(o);}"+
    	    			"function println(o){if(arguments.length==0) {"+outVar+".println();} else {"+outVar+".println(o);}}"
    	    			);
       		 	child.render(ctx, script);
       	
       		 	try {
       		 		engine.eval(script.toString(),bindings);
       		 		}
       		 	catch(ScriptException err)
       		 		{
       		 		LOG.error("",err);
       		 		throw new IOException(err);
       		 		}
                
                pw.flush();
                pw.close();
                
    	    	w.write(outstr.toString());
    	    	
    	    	break; 
        	 	}
        }
    	
    return true;
    }
}
