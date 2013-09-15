/*
System.err.println();

System.out.println();

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

//import com.sun.tools.javac.util.JavacMessages;

import java.util.*;
public class Debug {
    //private static JavacMessages m = new JavacMessages("my.debug");

	private static ResourceBundle rb;

    static {
		try {
			rb = ResourceBundle.getBundle("my.debug", Locale.getDefault());
		} catch (MissingResourceException e) {
			throw new Error("找不到my.debug.properties文件",e);
		}
	}

    public static boolean K(String key) {
		try {
			return "1".equals(rb.getString(key));
		} catch (MissingResourceException e) {
			//System.err.println("找不到key:"+key+" "+e);
			//e.printStackTrace();
			return false;
		}
        //return "1".equals(m.getLocalizedString(key));
    }

	//private static my.Debug DEBUG=new my.Debug(my.Debug.flag);//我加上的
    private static boolean globalFlag = K("globalFlag");//调试总开关
    //对应的类文件是否输出调试信息
    public static boolean Driver = K("Driver"),
		CharsetMapping = K("CharsetMapping"),
		ConnectionImpl = K("ConnectionImpl"),
		StatementImpl = K("StatementImpl"),
		ConnectionPropertiesImpl = K("ConnectionPropertiesImpl"),
		PreparedStatement = K("PreparedStatement"),
		ServerPreparedStatement = K("ServerPreparedStatement"),
		
		ReplicationConnection = K("ReplicationConnection"),
		MysqlIO = K("MysqlIO"),
		Buffer = K("Buffer"),
		ResultSetMetaData = K("ResultSetMetaData"),
		ResultSetImpl = K("ResultSetImpl"),


		MysqlDataSource = K("MysqlDataSource"),

		RowData = K("RowData");


		
	
    
	private int count = 0;
    private boolean flag = true;
    //private boolean privateGlobalFlag=false;

    public Debug() {
    }

    public Debug(boolean flag) {
        this.flag = flag;

		//this.flag = true;
    }

    public Debug(boolean flag, String className) {
        this.flag = flag;

		//this.flag = true;
    }

	public void STACK() {
		throw new Error();
	}

	public void STACK(Object o) {
		throw new Error(o+"");
	}
	//同STACK，是STACK的首字母
	public void S() {
		throw new Error();
	}

	public void S(Object o) {
		throw new Error(o+"");
	}

	public void E(Object o) {
		try {
			throw new Error(o+"");
		} catch (Error e) {
			e.printStackTrace();
		}
		System.out.println(o+" 退出:"+this.getClass().getName());
		System.exit(1);
	}
	public void E() {
		try {
			throw new Error();
		} catch (Error e) {
			e.printStackTrace();
		}
		System.out.println("退出:"+this.getClass().getName());
		System.exit(1);
	}

	public void e(Object o) {
		try {
			throw new Error(o+"");
		} catch (Error e) {
			e.printStackTrace();
		}
		System.out.println(o+" 退出(调试用途)");
		System.exit(1);
	}
	public void e() {
		try {
			throw new Error();
		} catch (Error e) {
			e.printStackTrace();
		}
		System.out.println("退出(调试用途)");
		System.exit(1);
	}

    public void on() {
        flag = true;
    }

    public void off() {
        flag = false;
    }

	//打印数组(print array)
	public void PA(String arrayName,Object[] array) {
		if (!(flag && globalFlag))
			return;

		if(array!=null) {
			System.out.println(arrayName+".length = "+array.length);
			for(int i=0;i<array.length;i++) {
				System.out.println(arrayName+"["+i+"] = "+array[i]);
			}
        } else {
			System.out.println(arrayName+" = null");
		}
    }

	public void PA(String arrayName,long[] array) {
		if (!(flag && globalFlag))
			return;

		if(array!=null) {
			System.out.println(arrayName+".length = "+array.length);
			for(int i=0;i<array.length;i++) {
				System.out.println(arrayName+"["+i+"] = "+array[i]);
			}
        } else {
			System.out.println(arrayName+" = null");
		}
    }

	public void PA(String arrayName,int[] array) {
		if (!(flag && globalFlag))
			return;

		if(array!=null) {
			System.out.println(arrayName+".length = "+array.length);
			for(int i=0;i<array.length;i++) {
				System.out.println(arrayName+"["+i+"] = "+array[i]);
			}
        } else {
			System.out.println(arrayName+" = null");
		}
    }

	//打印数组(print array)
	public void PA(String arrayName,char[] array) {
		if (!(flag && globalFlag))
			return;

		if(array!=null) {
			for(int i=0;i<array.length;i++) {
				System.out.println(arrayName+"["+i+"] = "+array[i]+"  ("+(int)array[i]+")");
			}
        } else {
			System.out.println(arrayName+" = null");
		}
    }

	public void P() {
        count++;
        //if(flag && globalFlag) System.out.println(count+":"+s);
        if (flag && globalFlag) {
            System.out.println();
        }
    }

    public void P(String s) {
        count++;
        //if(flag && globalFlag) System.out.println(count+":"+s);
        if (flag && globalFlag) {
            System.out.println(s);
        }
    }

    public void P(String s1, String s2) {
        P(s1, s2, false);
    }

    public void P(String s1, String s2, boolean b) {
        if (flag && globalFlag) {
            System.out.println(s1 + STR1 + s2);
        }
        if (b) {
            System.exit(1);
        }
    }

    public void P(String s, boolean b) {
        count++;
        //if(flag && globalFlag) System.out.println(count+":"+s);
        if (flag && globalFlag) {
            System.out.println(s);
        }
        if (b) {
            System.exit(1);
        }
    }

    public void P(Object o, boolean b) {
        count++;
        //if(flag && globalFlag) System.out.println(count+":"+s);
        if (flag && globalFlag) {
            System.out.println(o);
        }
        if (b) {
            System.exit(1);
        }
    }

    public void P(Object s1, Object s2) {
        P(s1, s2, false);
    }

    public void P(int n, Object s1, Object s2) {
        P(n, s1, s2, false);
    }

    public void P(int n, Object s1, Object s2, boolean b) {
        if (flag && globalFlag) {
            //String s=s1.toString();
            //if(s.indexOf("@")!=-1) s=s.substring(0,s.indexOf("@"));
            if (s1.getClass().getName().equals("java.lang.Class"))//static方法的情况
            {
                System.out.print(s1 + STR1 + s2);
            } else {
                System.out.print(s1.getClass().getName() + STR1 + s2);
            }
            System.out.println("  END");
            System.out.println(STR2);

            if (n == 0) {
                n = 1;
            }
            for (int i = 0; i < n; i++) {
                System.out.println("");
            }
        }
        if (b) {
            System.exit(1);
        }
    }

    public void P(Object s1, Object s2, boolean b) {
        if (flag && globalFlag) {

            //String s=s1.toString();
            //if(s.indexOf("@")!=-1) s=s.substring(0,s.indexOf("@"));
            if (s1.getClass().getName().equals("java.lang.Class"))//static方法的情况
            {
                System.out.println(s1 + STR1 + s2);
            } else {
                System.out.println(s1.getClass().getName() + STR1 + s2);
            }
            System.out.println(STR2);
        }
        if (b) {
            System.exit(1);
        }
    }

    public void P(int n) {
        if (flag && globalFlag) {
            for (int i = 0; i < n; i++) {
                System.out.println("");
            }
        }
    }

    public static void ON() {
		globalFlag = true;
		//privateGlobalFlag=true;
    }

    public static void OFF() {
        globalFlag = false;
		//privateGlobalFlag=false;
    }
    private String STR1 = "===>";
    private String STR2 = "-----------------------------------------------------------";
}