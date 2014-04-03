  
// line comment begin
/* block comment */
/**
 * test only
 * 
 @deprecated \u0009
 * @author zhh
 * @since 1.7
 */
package my.test;

public class ParserTest01 {
    /*
    public static void isHighSurrogate() {
        char ch='\uD800';//ch >= '\uD800' && ch <= '\uDBFF'
        while(ch <= '\uDBFF') {
            //System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") "+Character.isJavaIdentifierStart(ch));
            
            if(Character.isJavaIdentifierStart(ch))
                System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierStart");
            
            if(Character.isJavaIdentifierPart(ch))
                System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierPart");
            ch++;
        }
    }
    
    public static void isLowSurrogate() {
        char ch='\uDC00';//ch >= '\uDC00' && ch <= '\uDFFF'
        while(ch <= '\uDFFF') {
            if(Character.isJavaIdentifierStart(ch))
                System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierStart");
            
            if(Character.isJavaIdentifierPart(ch))
                System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierPart");
            ch++;
        }
    }
    
    public static void main(String[] args) {
        int i=0;
        switch (i) {
            case 0: System.out.println("0");
            case 1: System.out.println("1");//break;
            case 3: System.out.println("3");//break;
            default:System.out.println("null");
        }
        
        System.out.println(((int)'1')+" "+((int)'a')+" "+((int)'z')+" "+((int)'A')+" "+((int)'Z'));
        //Character.MIN_RADIX <= radix <= MAX_RADIX
        System.out.println("Character.MIN_RADIX="+Character.MIN_RADIX);
        System.out.println("Character.MAX_RADIX="+Character.MAX_RADIX);
        digit('\u0012', 36);
        //System.out.println("0123456789abcdef".charAt(35));
        //isHighSurrogate();
        //isLowSurrogate();
    }
    
    private static void digit(char ch,int base) {
        char c = ch;
        int result = Character.digit(c, base);
        if (result >= 0 && c > 0x7f) {
            //lexError(pos+1, "illegal.nonascii.digit");
            ch = "0123456789abcdef".charAt(result);
        }
        System.out.println("ch="+ch+" base="+base+" result="+result);
    }*/
    
    //int \u0081;
    //int \uD800\uDC00; int 中国ren;
    //int \uD800中;
    //int \u001A中;//EOI(结束符)不能作为Java语言标识符的开头
    //int 中\u001A;//EOI(结束符)可以放在Java语言标识符中间或结尾
    //int \\uD80\uDC00;
    //输出:processWhitespace(1952,1958)=| \uD800|
    
      static final int A[] = new int[256];
  static final String A_DATA =
    "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u5800\u400F\u5000\u400F\u5800\u400F\u6000\u400F"+
    "\u5000\u400F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F"+
    "\u4800\u100F\u4800\u100F\u5000\u400F\u5000\u400F\u5000\u400F\u5800\u400F\u6000"+
    "\u400C\u6800\030\u6800\030\u2800\030\u2800\u601A\u2800\030\u6800\030\u6800"+
    "\030\uE800\025\uE800\026\u6800\030\u2800\031\u3800\030\u2800\024\u3800\030"+
    "\u2000\030\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800"+
    "\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u3800\030\u6800\030"+
    "\uE800\031\u6800\031\uE800\031\u6800\030\u6800\030\202\u7FE1\202\u7FE1\202"+
    "\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1"+
    "\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202"+
    "\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1\202\u7FE1"+
    "\202\u7FE1\uE800\025\u6800\030\uE800\026\u6800\033\u6800\u5017\u6800\033\201"+
    "\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2"+
    "\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201"+
    "\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2\201\u7FE2"+
    "\201\u7FE2\201\u7FE2\201\u7FE2\uE800\025\u6800\031\uE800\026\u6800\031\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u5000\u100F"+
    "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F"+
    "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800"+
    "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F"+
    "\u3800\014\u6800\030\u2800\u601A\u2800\u601A\u2800\u601A\u2800\u601A\u6800"+
    "\034\u6800\034\u6800\033\u6800\034\000\u7002\uE800\035\u6800\031\u6800\u1010"+
    "\u6800\034\u6800\033\u2800\034\u2800\031\u1800\u060B\u1800\u060B\u6800\033"+
    "\u07FD\u7002\u6800\034\u6800\030\u6800\033\u1800\u050B\000\u7002\uE800\036"+
    "\u6800\u080B\u6800\u080B\u6800\u080B\u6800\030\202\u7001\202\u7001\202\u7001"+
    "\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202"+
    "\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001"+
    "\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\u6800\031\202\u7001\202"+
    "\u7001\202\u7001\202\u7001\202\u7001\202\u7001\202\u7001\u07FD\u7002\201\u7002"+
    "\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201"+
    "\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002"+
    "\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\u6800"+
    "\031\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002\201\u7002"+
    "\u061D\u7002";

  // In all, the character property tables require 1024 bytes.

    static {
                { // THIS CODE WAS AUTOMATICALLY CREATED BY GenerateCharacter:
            char[] data = A_DATA.toCharArray();
            assert (data.length == (256 * 2));
            int i = 0, j = 0;
            while (i < (256 * 2)) {
                int entry = data[i++] << 16;
                A[j++] = entry | data[i++];
                
                //System.out.println("A["+(j-1)+"]="+A[j-1]);
            }
        }

    }        
}
// line comment end