/*
my.L.o
private static my.Debug DEBUG=new my.Debug(my.Debug.flag);//我加上的
DEBUG.P
DEBUG.ON();
DEBUG.OFF();
//类全限定名称:
try {//我加上的
DEBUG.P(this,"loadClass()");
DEBUG.P("env="+env);

}finally{//我加上的
DEBUG.P(0,this,"loadClass");
}
DEBUG.P("kind="+Kinds.toString(kind));

DEBUG.P("flag="+Flags.toString(flag));

DEBUG.P("type.getKind()="+type.getKind());

DEBUG.P("tag="+TypeTags.toString(tag));

DEBUG.P("typeTag.tag="+TypeTags.toString(typeTag.tag));

DEBUG.P("tree.kind="+tree.getKind());


F:\Javac>javap -classpath bin\classes -verbose -p com.sun.tools.javac.main.Main
>out.txt
*/
package my;

import com.sun.tools.javac.util.Messages;
public class Debug {
    // <editor-fold defaultstate="collapsed">
        /*
	private static boolean globalFlag=true;//调试总开关
	
	//对应的类文件是否输出调试信息
	public static boolean Main=K(""),RecognizedOptions=K(""),Context=K(""),
	
        JavaCompiler=K(""),Parser=K(""),Scanner=K(""),Convert=K(""),
        DocCommentScanner=K(""),
	ClassReader=K(""),
        
	JavacFileManager=K(""),Keywords=K(""),Name=K(""),Log=K(""),
        TreeInfo=K(""),TreeMaker=K(""),JCTree=K(""),
	Paths=K(""),
        
        JavacProcessingEnvironment=K(""),PrintingProcessor=K(""),Service=K(""),
        CreateSymbols=K(""),ToolProvider=K(""),JavacTool=K(""),
	
	
	
	Source=K(""),Lint=K(""),Options=K(""),
	
	Symtab=K(""),Scope=K(""),
	
	Symbol=K(""),Types=K(""),Type=K(""),
	
	Annotate=K(""),
	
	//Enter=K(""),MemberEnter=K(""),
	Enter=K(""),MemberEnter=K(""),
	
	
	Attr=K(""),Check=K(""),Resolve=K(""),
	
	
	
	
	
	Flow=K(""),Bits=K(""),TreeScanner=K(""),
	
	
	TransTypes=K(""),TreeTranslator=K(""),Lower=K(""),
	
	//Gen=K(""),Code=K(""),Items=K(""),CRTable=K(""),ClassWriter=false;
	Gen=K(""),Code=K(""),Items=K(""),CRTable=K(""),ClassWriter=false;*/
        // </editor-fold>
    
        private static Messages m=new Messages("my.debug");
        public static boolean K(String key) {
            return "1".equals(m.getLocalizedString(key));
        }
        
        private static boolean globalFlag=K("globalFlag");//调试总开关
	
	//对应的类文件是否输出调试信息
	public static boolean Main=K("Main"),
                RecognizedOptions=K("RecognizedOptions"),Context=K("Context"),
	
        JavaCompiler=K("JavaCompiler"),Parser=K("Parser"),Scanner=K("Scanner"),Convert=K("Convert"),
        DocCommentScanner=K("DocCommentScanner"),
	ClassReader=K("ClassReader"),
        
	JavacFileManager=K("JavacFileManager"),Keywords=K("Keywords"),Name=K("Name"),Log=K("Log"),
        TreeInfo=K("TreeInfo"),TreeMaker=K("TreeMaker"),JCTree=K("JCTree"),
	Paths=K("Paths"),
        
        JavacProcessingEnvironment=K("JavacProcessingEnvironment"),PrintingProcessor=K("PrintingProcessor"),Service=K("Service"),
        CreateSymbols=K("CreateSymbols"),ToolProvider=K("ToolProvider"),JavacTool=K("JavacTool"),
	
	
	
	Source=K("Source"),Lint=K("Lint"),Options=K("Options"),
	
	Symtab=K("Symtab"),Scope=K("Scope"),
	
	Symbol=K("Symbol"),Types=K("Types"),Type=K("Type"),
	
	Annotate=K("Annotate"),
	
	Enter=K("Enter"),MemberEnter=K("MemberEnter"),
	
	
	Attr=K("Attr"),Check=K("Check"),Resolve=K("Resolve"),
	
	
	
	
	
	Flow=K("Flow"),Bits=K("Bits"),TreeScanner=K("TreeScanner"),
	
	
	TransTypes=K("TransTypes"),TreeTranslator=K("TreeTranslator"),Lower=K("Lower"),

	Gen=K("Gen"),Code=K("Code"),Items=K("Items"),CRTable=K("CRTable"),ClassWriter=K("ClassWriter");
	
	
	private int count=0;
	private boolean flag=true;
	//private boolean privateGlobalFlag=false;
	private String className="";
        
        
	
	public Debug() {}
	
	public Debug(boolean flag) {
		this.flag=flag;
	}
	public Debug(boolean flag,String className) {
		this.flag=flag;
		this.className=className;
	}
        
        public void on() {
            flag=true;
        }
        public void off() {
            flag=false;
        }
	
	public void P(String s) {
		count++;
		//if(flag && globalFlag) System.out.println(count+":"+s);
		if(flag && globalFlag) System.out.println(s);
	}
	
	public void P(String s1,String s2) {
		P(s1,s2,false);
	}
	
	public void P(String s1,String s2,boolean b) {
		if(flag && globalFlag) System.out.println(s1+STR1+s2);
		if(b) System.exit(1);
	}
	
	public void P(String s,boolean b) {
		count++;
		//if(flag && globalFlag) System.out.println(count+":"+s);
		if(flag && globalFlag) System.out.println(s);
		if(b) System.exit(1);
	}
	
	public void P(Object o,boolean b) {
		count++;
		//if(flag && globalFlag) System.out.println(count+":"+s);
		if(flag && globalFlag) System.out.println(o);
		if(b) System.exit(1);
	}
	
	public void P(Object s1,Object s2) {
		P(s1,s2,false);
	}
	
	public void P(int n,Object s1,Object s2) {
		P(n,s1,s2,false);
	}
	
	public void P(int n,Object s1,Object s2,boolean b) {
		if(flag && globalFlag) {
			//String s=s1.toString();
			//if(s.indexOf("@")!=-1) s=s.substring(0,s.indexOf("@"));
			if(s1.getClass().getName().equals("java.lang.Class"))//static方法的情况
				System.out.print(s1+STR1+s2);
			else
				System.out.print(s1.getClass().getName()+STR1+s2);
			System.out.println("  END");
			System.out.println(STR2);
			
			if(n==0) n=1;	
			for(int i=0;i<n;i++) System.out.println("");
		}
		if(b) System.exit(1);
	}
	
	public void P(Object s1,Object s2,boolean b) {
		if(flag && globalFlag) {
			
			//String s=s1.toString();
			//if(s.indexOf("@")!=-1) s=s.substring(0,s.indexOf("@"));
			if(s1.getClass().getName().equals("java.lang.Class"))//static方法的情况
				System.out.println(s1+STR1+s2);
			else
				System.out.println(s1.getClass().getName()+STR1+s2);
			System.out.println(STR2);
		}
		if(b) System.exit(1);
	}
	public void P(int n) {
		if(flag && globalFlag) {	
			for(int i=0;i<n;i++) System.out.println("");
		}
	}
	
	public static void ON() {
		globalFlag=true;
		//privateGlobalFlag=true;
	}
	public static void OFF() {
		globalFlag=false;
		//privateGlobalFlag=false;
	}
	
	private String STR1="===>";
	private String STR2="-------------------------------------------------------------------------";
	
}
	
	