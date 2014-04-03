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
public class Debug {
	private static boolean globalFlag=true;//调试总开关
	
	//对应的类文件是否输出调试信息
	public static boolean Main=false,RecognizedOptions=false,Context=false,
	JavacProcessingEnvironment=true,DocCommentScanner=false,
	JavacFileManager=false,Keywords=false,Name=false,
	
	JavaCompiler=true,Parser=false,Scanner=false,Convert=false,
	ClassReader=false,
	
	Source=false,Lint=false,Options=false,
	
	Symtab=false,Scope=false,
	
	Symbol=false,Types=false,Type=false,
	
	Annotate=false,
	
	//Enter=true,MemberEnter=true,
	Enter=false,MemberEnter=false,
	
	
	Attr=false,Check=false,Resolve=false,
	
	
	TreeInfo=false,TreeMaker=false,
	Paths=false,
	
	
	Flow=false,Bits=false,TreeScanner=false,
	
	
	TransTypes=false,TreeTranslator=false,Lower=false,
	
	//Gen=true,Code=true,Items=true,CRTable=true,ClassWriter=false;
	Gen=false,Code=false,Items=false,CRTable=false,ClassWriter=false;
	
	
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
	
	