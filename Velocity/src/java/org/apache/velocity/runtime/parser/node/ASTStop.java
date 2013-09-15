package org.apache.velocity.runtime.parser.node;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.Parser;

/**
 * This class is responsible for handling the #stop directive
 *
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id: ASTStop.java 685685 2008-08-13 21:43:27Z nbubna $
 * @since 1.5
 */
public class ASTStop extends SimpleNode
{
    /**
     * @param id
     */
    public ASTStop(int id)
    {
        super(id);
    }

    /**
     * @param p
     * @param id
     */
    public ASTStop(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#jjtAccept(org.apache.velocity.runtime.parser.node.ParserVisitor, java.lang.Object)
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     * Do not output anything, just shut off the rendering.
     * @param context
     * @param writer
     * @return Always true.
     * @throws IOException
     * @throws MethodInvocationException
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public boolean render( InternalContextAdapter context, Writer writer)
        throws IOException, MethodInvocationException, ParseErrorException, ResourceNotFoundException
    {
		//如果有#stop指令，那么setAllowRendering设为false，不输出之后的内容
		//#break指令是直接抛异常
        context.setAllowRendering(false);

        return true;
    }
}

