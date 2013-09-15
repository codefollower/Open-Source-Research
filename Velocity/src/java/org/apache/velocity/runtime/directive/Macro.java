package org.apache.velocity.runtime.directive;

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
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *  Macro implements the macro definition directive of VTL.
 *
 *  example :
 *
 *  #macro( isnull $i )
 *     #if( $i )
 *         $i
 *      #end
 *  #end
 *
 *  This object is used at parse time to mainly process and register the
 *  macro.  It is used inline in the parser when processing a directive.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: Macro.java 685685 2008-08-13 21:43:27Z nbubna $
 */

//这个类对应$macro指令，而RuntimeMacro类是对应调用为个宏的指令
public class Macro extends Directive
{
	private static my.Debug DEBUG=new my.Debug(my.Debug.Macro);//我加上的

    private static  boolean debugMode = false;

    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "macro";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    public int getType()
    {
        return BLOCK;
    }

    /**
     *   render() doesn't do anything in the final output rendering.
     *   There is no output from a #macro() directive.
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     */
    public boolean render(InternalContextAdapter context,
                           Writer writer, Node node)
        throws IOException
    {
        /*
         *  do nothing : We never render.  The VelocimacroProxy object does that
         */

        return true;
    }

    /**
     * @see org.apache.velocity.runtime.directive.Directive#init(org.apache.velocity.runtime.RuntimeServices, org.apache.velocity.context.InternalContextAdapter, org.apache.velocity.runtime.parser.node.Node)
     */
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
       throws TemplateInitException
    {
        super.init(rs, context, node);

        /*
         * again, don't do squat.  We want the AST of the macro
         * block to hang off of this but we don't want to
         * init it... it's useless...
         */
    }

    /**
     *  Used by Parser.java to process VMs during the parsing process.
     *
     *  This method does not render the macro to the output stream,
     *  but rather <i>processes the macro body</i> into the internal
     *  representation used by {#link
     *  org.apache.velocity.runtime.directive.VelocimacroProxy}
     *  objects, and if not currently used, adds it to the macro
     *  Factory.
     * @param rs
     * @param t
     * @param node
     * @param sourceTemplate
     * @throws IOException
     * @throws ParseException
     */

	//这个方法是在生成AST的过程中调用的(在Parser类中调用)
    public static void processAndRegister(RuntimeServices rs,  Token t, Node node,
                                          String sourceTemplate)
        throws IOException, ParseException
    {
		try {//我加上的
		DEBUG.P(Macro.class,"processAndRegister(4)");
		DEBUG.P("t="+t);
		DEBUG.P("node="+node);
		DEBUG.P("sourceTemplate="+sourceTemplate);
		//t=#macro
		//node=org.apache.velocity.runtime.parser.node.ASTDirective
		//sourceTemplate=myvm1.vm

        /*
         *  There must be at least one arg to  #macro,
         *  the name of the VM.  Note that 0 following
         *  args is ok for naming blocks of HTML
         */

        int numArgs = node.jjtGetNumChildren();

		/*比如:
		#macro( tablerows $color $somelist )
			#foreach( $something in $somelist )
				<tr><td bgcolor=$color>$something</td></tr>
			#end
		#end
		
		则:
		numArgs=4
		node.jjtGetChild(0)=org.apache.velocity.runtime.parser.node.ASTWord
		node.jjtGetChild(1)=org.apache.velocity.runtime.parser.node.ASTReference
		node.jjtGetChild(2)=org.apache.velocity.runtime.parser.node.ASTReference
		node.jjtGetChild(3)=org.apache.velocity.runtime.parser.node.ASTBlock

		分别对应tablerows $color $somelist，和#foreach，
		第一个是自定义macro的名字，是一个ASTWord类型的结点。
		*/
		DEBUG.P("numArgs="+numArgs);
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
			DEBUG.P("node.jjtGetChild("+i+")="+node.jjtGetChild(i).getClass().getName());

        /*
         *  this number is the # of args + 1.  The + 1
         *  is for the block tree
         */

		//至少要有macro名和macro body
        if (numArgs < 2)
        {

            /*
             *  error - they didn't name the macro or
             *  define a block
             */

            rs.getLog().error("#macro error : Velocimacro must have name as 1st " +
                              "argument to #macro(). #args = " + numArgs);

            throw new MacroParseException("First argument to #macro() must be " +
                    " macro name.", sourceTemplate, t);
        }

        /*
         *  lets make sure that the first arg is an ASTWord
         */

        int firstType = node.jjtGetChild(0).getType();

        if(firstType != ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("First argument to #macro() must be a"
                    + " token without surrounding \' or \", which specifies"
                    + " the macro name.  Currently it is a "
                    + ParserTreeConstants.jjtNodeName[firstType], sourceTemplate, t);
        }

        // get the arguments to the use of the VM - element 0 contains the macro name

		//返回的值不含body，只有macro名和参数名，参数名前的$号被去掉了
        String argArray[] = getArgArray(node, rs);

		DEBUG.PA("argArray",argArray);

        /* 
         * we already have the macro parsed as AST so there is no point to
         * transform it into a String again
         */ 
        rs.addVelocimacro(argArray[0], node.jjtGetChild(numArgs - 1), argArray, sourceTemplate);
        
        /*
         * Even if the add attempt failed, we don't log anything here.
         * Logging must be done at VelocimacroFactory or VelocimacroManager because
         * those classes know the real reason.
         */
		 
		}finally{//我加上的
		DEBUG.P(0,Macro.class,"processAndRegister(4)");
		}
    }


    /**
     * Creates an array containing the literal text from the macro
     * arguement(s) (including the macro's name as the first arg).
     *
     * @param node The parse node from which to grok the argument
     * list.  It's expected to include the block node tree (for the
     * macro body).
     * @param rsvc For debugging purposes only.
     * @return array of arguments
     */
    private static String[] getArgArray(Node node, RuntimeServices rsvc)
    {
        /*
         * Get the number of arguments for the macro, excluding the
         * last child node which is the block tree containing the
         * macro body.
         */
        int numArgs = node.jjtGetNumChildren();
        numArgs--;  // avoid the block tree...

        String argArray[] = new String[numArgs];

        int i = 0;

        /*
         *  eat the args
         */

        while (i < numArgs)
        {
            argArray[i] = node.jjtGetChild(i).getFirstToken().image;

            /*
             *  trim off the leading $ for the args after the macro name.
             *  saves everyone else from having to do it
             */

            if (i > 0)
            {
                if (argArray[i].startsWith("$"))
                {
                    argArray[i] = argArray[i]
                        .substring(1, argArray[i].length());
                }
            }

            i++;
        }

		//我加上的
		 StringBuffer msg2 = new StringBuffer("Macro.getArgArray() : nbrArgs=");
            msg2.append(numArgs).append(" : ");
            macroToString(msg2, argArray);

		DEBUG.P("msg2="+msg2);

        if (debugMode)
        {
            StringBuffer msg = new StringBuffer("Macro.getArgArray() : nbrArgs=");
            msg.append(numArgs).append(" : ");
            macroToString(msg, argArray);
            rsvc.getLog().debug(msg);
        }

        return argArray;
    }

    /**
     * For debugging purposes.  Formats the arguments from
     * <code>argArray</code> and appends them to <code>buf</code>.
     *
     * @param buf A StringBuffer. If null, a new StringBuffer is allocated.
     * @param argArray The Macro arguments to format
     *
     * @return A StringBuffer containing the formatted arguments. If a StringBuffer
     *         has passed in as buf, this method returns it.
     * @since 1.5
     */
    public static final StringBuffer macroToString(final StringBuffer buf,
                                                   final String[] argArray)
    {
        StringBuffer ret = (buf == null) ? new StringBuffer() : buf;

        ret.append('#').append(argArray[0]).append("( ");
        for (int i = 1; i < argArray.length; i++)
        {
            ret.append(' ').append(argArray[i]);
        }
        ret.append(" )");
        return ret;
    }
}
