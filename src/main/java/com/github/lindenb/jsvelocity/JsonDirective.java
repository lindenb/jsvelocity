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
import java.io.StringReader;
import java.io.StringWriter;
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


public class JsonDirective extends Directive {
	private static final Logger LOG=LoggerFactory.getLogger(JsonDirective.class);

    public String getName() {
        return "json";
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
    	final JSVelocity instance = (JSVelocity)ctx.get(JSVelocity.PARAM_JSVELOCITY_INSTANCE);
    	if(instance==null) {
    		throw new ResourceNotFoundException("Cannot find jsvelocity instance associated");
    	}
    	String name = null;
    	Object json =null;
    	for(int i=0; i<node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) != null ) {
                if(!(node.jjtGetChild(i) instanceof ASTBlock)) {
                	if(i==0) {
                		name= 	String.valueOf(node.jjtGetChild(i).value(ctx));
                		}
                	} 
                else
                	{
                	final StringWriter blockContent = new StringWriter();
                    node.jjtGetChild(i).render(ctx, blockContent);
                    final String jsonStr = blockContent.toString();
                    json =  instance.convertJson(instance.parseJson(new StringReader(jsonStr) ));
                    break;
                	}
            }
    	}
    	
    	instance.put(name, json);    	
    	return true;
    }
}
