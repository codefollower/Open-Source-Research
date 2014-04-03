com.sun.tools.javac.jvm.Gen===>genClass(2) 
璋冪敤implementInterfaceMethods(c)鐨勭洰鐨勶細
c鏄竴涓狢lassSymbol锛屽鏋渃鏄疉BSTRACT鐨勶紝涓旀病鏈夊疄鐜版帴鍙ｄ腑鐨勬涓�柟娉曪紝
鍒欒嚜瀹氫箟涓�釜MethodSymbol浠ｈ〃鎺ュ彛涓殑鐩稿簲鏂规硶锛屽姞鍏.members()锛�
濡傛灉c瀹炵幇浜嗘帴鍙ｄ腑鐨勬涓�PROXY鏂规硶锛屽垯淇敼c涓浉搴旀柟娉曠殑寮傚父鎶涘嚭绫诲瀷

瑕佹弧瓒充互涓嬫潯浠朵负true
if (generateIproxies && //jdk1.1涓巎dk1.0鎵嶉渶瑕�		(c.flags() & (INTERFACE|ABSTRACT)) == ABSTRACT
		&& !allowGenerics // no Miranda methods available with generics
		)
		implementInterfaceMethods(c);

褰撳姞浜�XDmiranda鎴栬�鎸囧畾(-target 1.1 涓�source 涓�1.2 鍒�.4鏃讹級鏃秅enerateIproxies=true

褰�c.flags() & (INTERFACE|ABSTRACT)) == ABSTRACT鏃讹紝c鍙兘鏄竴涓猘bstract绫伙紝

涓嬮潰鐨勪緥瀛愬彲浠ヤ娇娴佺▼鎵цimplementInterfaceMethods(c);

鍦╦avac鍚庡姞涓嬮潰涓や釜閫夐」:
-target 1.1 -source 1.2

缂栬瘧婧愭枃浠讹細
public abstract class GenTest {锝�//public interface GenTest {锝濊繖绉嶆柟寮忎笉琛�


涓嬮潰鐨勪緥瀛愬姞-target 1.1 -source 1.2鏃舵湁閿�
src/my/test/GenTest.java:6: my.test.GenTest 涓殑 interfaceMethodC() 鏃犳硶瀹炵幇 my.test.GenTestInterfaceB 涓殑 interfaceMethodC()锛涙鍦ㄥ皾璇曚娇鐢ㄤ笉鍏煎鐨勮繑鍥炵被鍨�鎵惧埌锛�java.lang.Integer
闇�锛�java.lang.Number
    public abstract Integer interfaceMethodC();
                            ^
1 閿欒


package my.test;


public abstract class GenTest implements GenTestInterfaceB {
    public abstract Integer interfaceMethodC();
}

interface GenTestInterfaceA {
    void interfaceMethodA();
}

//鎺ヤ腑涓嶈兘浣跨敤implements锛屼絾鍙互浣跨敤extends锛屼笖extends鍚庡彲鎺ュ涓悕绉�//interface GenTestInterfaceB implements GenTestInterfaceA {
interface GenTestInterfaceB extends GenTestInterfaceA {
    void interfaceMethodB();
    Number interfaceMethodC();
    
    //static void interfaceMethodStatic();
}













