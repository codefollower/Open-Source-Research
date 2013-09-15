package org.apache.velocity.runtime;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.MapFactory;

/**
 * Manages VMs in namespaces.  Currently, two namespace modes are
 * supported:
 *
 * <ul>
 * <li>flat - all allowable VMs are in the global namespace</li>
 * <li>local - inline VMs are added to it's own template namespace</li>
 * </ul>
 *
 * Thanks to <a href="mailto:JFernandez@viquity.com">Jose Alberto Fernandez</a>
 * for some ideas incorporated here.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:JFernandez@viquity.com">Jose Alberto Fernandez</a>
 * @version $Id: VelocimacroManager.java 698376 2008-09-23 22:15:49Z nbubna $
 */
public class VelocimacroManager
{
	private static my.Debug DEBUG=new my.Debug(my.Debug.Macro);//我加上的

    private static String GLOBAL_NAMESPACE = "";

	//当定义宏的文件是从velocimacro.library指定时，
	//在解析这个文件时会把registerFromLib设为true，解析完后得到一个Template，
	//然后再设为false,见VelocimacroFactory.initVelocimacro()
    private boolean registerFromLib = false;

    /** Hash of namespace hashes. */
	//Map<定义宏的文件名，Map<宏名，MacroEntry>>
    private final Map namespaceHash = MapFactory.create(17, 0.5f, 20, false);

	//Map<宏名，MacroEntry>
    /** reference to global namespace hash */
    private final Map globalNamespace;

    /** set of names of library tempates/namespaces */

	//Set<定义宏的文件名>，用来保存velocimacro.library属性指定的文件名
    private final Set libraries = Collections.synchronizedSet(new HashSet());

    /*
     * big switch for namespaces.  If true, then properties control
     * usage. If false, no.
     */
    private boolean namespacesOn = true;
    private boolean inlineLocalMode = false; //对应velocimacro.permissions.allow.inline.local.scope
    private boolean inlineReplacesGlobal = false; //对应velocimacro.permissions.allow.inline.to.replace.global

    /**
     * Adds the global namespace to the hash.
     */
    VelocimacroManager(RuntimeServices rsvc)
    {
		try {//我加上的
		DEBUG.P(this,"VelocimacroManager(1)");

        /*
         *  add the global namespace to the namespace hash. We always have that.
         */
		
		//全局的命名空间用""空串来代表
        globalNamespace = addNamespace(GLOBAL_NAMESPACE);

		DEBUG.P("globalNamespace="+globalNamespace);

		}finally{//我加上的
		DEBUG.P(0,this,"VelocimacroManager(1)");
		}
    }

    /**
     * Adds a VM definition to the cache.
     * 
     * Called by VelocimacroFactory.addVelociMacro (after parsing and discovery in Macro directive)
     * 
     * @param vmName Name of the new VelociMacro.
     * @param macroBody String representation of the macro body.
     * @param argArray Array of macro parameters, first parameter is the macro name.
     * @param namespace The namespace/template from which this macro has been loaded.
     * @return Whether everything went okay.
     */
    public boolean addVM(final String vmName, final Node macroBody, final String argArray[],
                         final String namespace, boolean canReplaceGlobalMacro)
    {
		try {//我加上的
		DEBUG.P(this,"addVM(5)");
		DEBUG.P("vmName="+vmName);
		DEBUG.P("macroBody="+macroBody);
		DEBUG.P("namespace="+namespace);
		DEBUG.P("canReplaceGlobalMacro="+canReplaceGlobalMacro);
		DEBUG.PA("argArray",argArray);

        if (macroBody == null)
        {
            // happens only if someone uses this class without the Macro directive
            // and provides a null value as an argument
            throw new RuntimeException("Null AST for "+vmName+" in "+namespace);
        }

        MacroEntry me = new MacroEntry(vmName, macroBody, argArray, namespace);

		DEBUG.P("registerFromLib="+registerFromLib);
        me.setFromLibrary(registerFromLib);
        
        /*
         *  the client (VMFactory) will signal to us via
         *  registerFromLib that we are in startup mode registering
         *  new VMs from libraries.  Therefore, we want to
         *  addto the library map for subsequent auto reloads
         */

        boolean isLib = true;

        MacroEntry exist = (MacroEntry) globalNamespace.get(vmName);
        
        if (registerFromLib)
        {
           libraries.add(namespace);
        }
        else
        {
            /*
             *  now, we first want to check to see if this namespace (template)
             *  is actually a library - if so, we need to use the global namespace
             *  we don't have to do this when registering, as namespaces should
             *  be shut off. If not, the default value is true, so we still go
             *  global
             */

            isLib = libraries.contains(namespace);
        }

		DEBUG.P("isLib="+isLib);

        if ( !isLib && usingNamespaces(namespace) )
        {
            /*
             *  first, do we have a namespace hash already for this namespace?
             *  if not, add it to the namespaces, and add the VM
             */

            Map local = getNamespace(namespace, true);

			DEBUG.P("local="+local);

            local.put(vmName, me);

			DEBUG.P("local="+local);
            
            return true;
        }
        else
        {
			//如果在velocimacro.library中指定的宏定义文件定义了两个相同的宏名，
			//则只保留最后一个文件中定义的，
			//比如velocimacro.library = myvm1.vm,myvm2.vm，
			//myvm1.vm,myvm2.vm都定义了tablerows，
			//则globalNamespace={tablerows=MacroEntry[vmName=tablerows, sourceTemplate=myvm2.vm]}
            
			/*
             *  otherwise, add to global template.  First, check if we
             *  already have it to preserve some of the autoload information
             */

			DEBUG.P("exist="+exist);
            if (exist != null)
            {
                me.setFromLibrary(exist.getFromLibrary());
            }

            /*
             *  now add it
             */

            globalNamespace.put(vmName, me);

			DEBUG.P("globalNamespace="+globalNamespace);

            return true;
        }

		}finally{//我加上的
		DEBUG.P(0,this,"addVM(5)");
		}
    }
    
    /**
     * Gets a VelocimacroProxy object by the name / source template duple.
     * 
     * @param vmName Name of the VelocityMacro to look up.
     * @param namespace Namespace in which to look up the macro.
     * @return A proxy representing the Macro.
     */
     public VelocimacroProxy get(final String vmName, final String namespace)
     {
        return(get(vmName, namespace, null));
     }

     /**
      * Gets a VelocimacroProxy object by the name / source template duple.
      * 
      * @param vmName Name of the VelocityMacro to look up.
      * @param namespace Namespace in which to look up the macro.
      * @param renderingTemplate Name of the template we are currently rendering.
      * @return A proxy representing the Macro.
      * @since 1.6
      */
	 //namespace是指定义vmName这个宏的文件，
	 //renderingTemplate是被渲染的模板文件。
     public VelocimacroProxy get(final String vmName, final String namespace, final String renderingTemplate)
     {
		try {//我加上的
		DEBUG.P(this,"get(3)");
		DEBUG.P("vmName="+vmName);
		DEBUG.P("namespace="+namespace);
		DEBUG.P("renderingTemplate="+renderingTemplate);
		DEBUG.P("inlineReplacesGlobal="+inlineReplacesGlobal);
		DEBUG.P(1);

		//如果velocimacro.permissions.allow.inline.to.replace.global为true，
		//那么先看看被渲染的模板文件中是否定义了相同名称的宏，
		//这样就可以覆盖全局的宏
        if( inlineReplacesGlobal && renderingTemplate != null )
        {
            /*
             * if VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL is true (local macros can
             * override global macros) and we know which template we are rendering at the
             * moment, check if local namespace contains a macro we are looking for
             * if so, return it instead of the global one
             */
            Map local = getNamespace(renderingTemplate, false);
            
			DEBUG.P("local0="+local);
			if (local != null)
            {
                MacroEntry me = (MacroEntry) local.get(vmName);

				DEBUG.P("me0="+me);
                if (me != null)
                {
                    return me.getProxy(namespace);
                }
            }
        }

		DEBUG.P("namespacesOn="+namespacesOn);
		DEBUG.P("inlineLocalMode="+inlineLocalMode);
        
        if (usingNamespaces(namespace))
        {
            Map local = getNamespace(namespace, false);

			DEBUG.P("local="+local);

            /*
             *  if we have macros defined for this template
             */

            if (local != null)
            {
                MacroEntry me = (MacroEntry) local.get(vmName);
                
				DEBUG.P("me="+me);
                if (me != null)
                {
                    return me.getProxy(namespace);
                }
            }
        }

        /*
         * if we didn't return from there, we need to simply see
         * if it's in the global namespace
         */

        MacroEntry me = (MacroEntry) globalNamespace.get(vmName);

		DEBUG.P("me2="+me);
        if (me != null)
        {
            return me.getProxy(namespace);
        }

        return null;

		}finally{//我加上的
		DEBUG.P(0,this,"get(3)");
		}
    }

    /**
     * Removes the VMs and the namespace from the manager.
     * Used when a template is reloaded to avoid
     * losing memory.
     *
     * @param namespace namespace to dump
     * @return boolean representing success
     */
    public boolean dumpNamespace(final String namespace)
    {
        synchronized(this)
        {
            if (usingNamespaces(namespace))
            {
                Map h = (Map) namespaceHash.remove(namespace);

                if (h == null)
                {
                    return false;
                }

                h.clear();

                return true;
            }

            return false;
        }
    }

    /**
     *  public switch to let external user of manager to control namespace
     *  usage indep of properties.  That way, for example, at startup the
     *  library files are loaded into global namespace
     *
     * @param namespaceOn True if namespaces should be used.
     */
    public void setNamespaceUsage(final boolean namespaceOn)
    {
        this.namespacesOn = namespaceOn;
    }

    /**
     * Should macros registered from Libraries be marked special?
     * @param registerFromLib True if macros from Libs should be marked.
     */
    public void setRegisterFromLib(final boolean registerFromLib)
    {
        this.registerFromLib = registerFromLib;
    }

    /**
     * Should macros from the same template be inlined?
     *
     * @param inlineLocalMode True if macros should be inlined on the same template.
     */
    public void setTemplateLocalInlineVM(final boolean inlineLocalMode)
    {
        this.inlineLocalMode = inlineLocalMode;
    }

    /**
     *  returns the hash for the specified namespace, and if it doesn't exist
     *  will create a new one and add it to the namespaces
     *
     *  @param namespace  name of the namespace :)
     *  @param addIfNew  flag to add a new namespace if it doesn't exist
     *  @return namespace Map of VMs or null if doesn't exist
     */
	//获得一个名称空间中定义的所有宏的一个Map,
	//名称空间其实就是定义宏的文件
    private Map getNamespace(final String namespace, final boolean addIfNew)
    {
        Map h = (Map) namespaceHash.get(namespace);

        if (h == null && addIfNew)
        {
            h = addNamespace(namespace);
        }

        return h;
    }

    /**
     *   adds a namespace to the namespaces
     *
     *  @param namespace name of namespace to add
     *  @return Hash added to namespaces, ready for use
     */
	
	//这个方法不会覆盖原有的值，
	//如查namespace这个key也存在，那么返回null，
	//否则返回一个新的map
    private Map addNamespace(final String namespace)
    {
        Map h = MapFactory.create(17, 0.5f, 20, false);
        Object oh;

		//如果namespaceHash中已存在namespace这个key对应的值，
		//那么put方法会返回一个旧的对象放到oh中
        if ((oh = namespaceHash.put(namespace, h)) != null)
        {
          /*
           * There was already an entry on the table, restore it!
           * This condition should never occur, given the code
           * and the fact that this method is private.
           * But just in case, this way of testing for it is much
           * more efficient than testing before hand using get().
           */
          namespaceHash.put(namespace, oh); //比用get查找更高效
          /*
           * Should't we be returning the old entry (oh)?
           * The previous code was just returning null in this case.
           */
          return null;
        }

        return h;
    }

    /**
     *  determines if currently using namespaces.
     *
     *  @param namespace currently ignored
     *  @return true if using namespaces, false if not
     */

	//是否使用名称空间，如果当前名称空间的启用标志是false，那么返回false，
	//否则看看是否处在inlineLocalMode(内联本地模式)
    private boolean usingNamespaces(final String namespace)
    {
        /*
         *  if the big switch turns of namespaces, then ignore the rules
         */

        if (!namespacesOn)
        {
            return false;
        }

        /*
         *  currently, we only support the local template namespace idea
         */

        if (inlineLocalMode)
        {
            return true;
        }

        return false;
    }

    /**
     * Return the library name for a given macro.
     * @param vmName Name of the Macro to look up.
     * @param namespace Namespace to look the macro up.
     * @return The name of the library which registered this macro in a namespace.
     */
    public String getLibraryName(final String vmName, final String namespace)
    {
        if (usingNamespaces(namespace))
        {
			//先从本地取，名称空间不存在时也不会构造一个新Map
            Map local = getNamespace(namespace, false);

            /*
             *  if we have this macro defined in this namespace, then
             *  it is masking the global, library-based one, so
             *  just return null
             */

            if ( local != null)
            {
                MacroEntry me = (MacroEntry) local.get(vmName);

				//如果能在本地名称空间中找到vmName这个宏名，
				//说明定义这个宏的文件并不是在velocimacro.library中，
				//所以要返回一个库名时，只能返回null
                if (me != null)
                {
                    return null;
                }
            }
        }

        /*
         * if we didn't return from there, we need to simply see
         * if it's in the global namespace
         */

        MacroEntry me = (MacroEntry) globalNamespace.get(vmName);

        if (me != null)
        {
            return me.getSourceTemplate();
        }

        return null;
    }
    
    /**
     * @since 1.6
     */
    public void setInlineReplacesGlobal(boolean is)
    {
        inlineReplacesGlobal = is;
    }


    /**
     *  wrapper class for holding VM information
     */
    private static class MacroEntry
    {
        private final String vmName;
        private final String[] argArray;
        private final String sourceTemplate;
        private SimpleNode nodeTree = null;
        private boolean fromLibrary = false;
        private VelocimacroProxy vp;

        private MacroEntry(final String vmName, final Node macro,
                   final String argArray[], final String sourceTemplate)
        {
			try {//我加上的
			DEBUG.P(this,"MacroEntry(4)");

            this.vmName = vmName;
            this.argArray = argArray;
            this.nodeTree = (SimpleNode)macro;
            this.sourceTemplate = sourceTemplate;

            vp = new VelocimacroProxy();
            vp.setName(this.vmName);
            vp.setArgArray(this.argArray);
            vp.setNodeTree(this.nodeTree);

			}finally{//我加上的
			DEBUG.P(0,this,"MacroEntry(4)");
			}
        }

		//我加上的
		public String toString() {
			return "MacroEntry[vmName="+vmName+", sourceTemplate="+sourceTemplate+"]";
		}
        
        /**
         * Has the macro been registered from a library.
         * @param fromLibrary True if the macro was registered from a Library.
         */
        public void setFromLibrary(final boolean fromLibrary)
        {
            this.fromLibrary = fromLibrary;
        }

        /**
         * Returns true if the macro was registered from a library.
         * @return True if the macro was registered from a library.
         */
        public boolean getFromLibrary()
        {
            return fromLibrary;
        }

        /**
         * Returns the node tree for this macro.
         * @return The node tree for this macro.
         */
        public SimpleNode getNodeTree()
        {
            return nodeTree;
        }

        /**
         * Returns the source template name for this macro.
         * @return The source template name for this macro.
         */
        public String getSourceTemplate()
        {
            return sourceTemplate;
        }

        VelocimacroProxy getProxy(final String namespace)
        {
            /*
             * FIXME: namespace data is omitted, this probably 
             * breaks some error reporting?
             */ 
            return vp;
        }
    }
}


