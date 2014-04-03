package test.memberEnter;

import static test.memberEnter.*; //用来测试importStaticAll(3)，当tsym.kind=ERR
//下面两条语句测试importStaticAll(3)与staticImportAccessible(2)
//import static test.memberEnter.ImportStaticTest.*;
//import static test.memberEnter.ImportStaticTest.*;

//import static test.memberEnter.ClassC.*;

/*
enum TestEnum {
    BAR,
    QUX,
    BAZ;
    static String X = "X";
    TestEnum() {
        String y = X;
    }
}
*/

enum TestEnum {
    BAR,
    QUX,
    BAZ {
        private final String x = X;
		{
			String y = X;
		}
		static {
			String z = X;
		}
    };
    static String X = "X";

	String x = X;

	{
			String y = X;
		}
		static {
			String z = X;
		}
}
/*
test\memberEnter\ImportTest.java:9: 软件包 test2.memberEnter 不存在
import static test2.memberEnter.ClassE.*;
                               ^
1 错误
*/ //在Resolve.ResolveError.report(...)中的log.error(pos, "doesnt.exist", site.tsym);
//当visitSelect(test2.memberEnter.ClassE)时，因为pink=TYP
//接着调用selectSym(5)无法在test2.memberEnter中找到ClassE。
//import static test2.memberEnter.ClassE.*;

/*
test\memberEnter\ImportTest.java:19: 找不到符号
符号： 类 ClassE2
位置： 软件包 test.memberEnter
import static test.memberEnter.ClassE2.*;
                              ^
1 错误
*///同上，但是，是在log.error(pos, "cant.resolve.location",....
//import static test.memberEnter.ClassE2.*;

//import static test2.memberEnter.ClassE.*;
//import test2.test3.*;

//import static test.memberEnter.ClassF.*;
//import static test.memberEnter.ClassG.Class1;//错误:找不到符号
//import test.memberEnter.ClassG.Class1;


//import test.memberEnter.subdir.UniqueImport;
//import test.memberEnter.subdir.UniqueImport.MemberClassA;
//import static test.memberEnter.subdir.UniqueImport.MemberClassB;

//import static test.memberEnter.subdir.UniqueImport.staticFieldA;
//import static test.memberEnter.UniqueImport.staticFieldA;

//import test.memberEnter.UniqueImport;
//import test.memberEnter.UniqueImport;

//class UniqueImport {} 

public class ImportTest {}

//starImportScope 已处理
//class MyTheSamePackageClass {}



