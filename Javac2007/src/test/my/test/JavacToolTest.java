package my.test;

import javax.tools.*;
import com.sun.tools.javac.api.*;

public class JavacToolTest {
    
    public static void main(String[] args) {
        //JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        
        JavacTool tool = (JavacTool)ToolProvider.getSystemJavaCompiler();
        System.out.println(tool.getSourceVersions());
        tool.setOption("-d","ddd");
        //tool=null;
        //tool.getClass();//在javac源码中常调用getClass()进行是否为空的测试(null check)
    }
}

