package my.test;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

public class TestNIO {
    public static void convert() {
        System.out.println(Charset.availableCharsets());
        Charset asciiCharset = Charset.forName("US-ASCII");
        CharsetDecoder decoder = asciiCharset.newDecoder();
        byte help[] = {72, 101, 108, 112};
        ByteBuffer asciiBytes = ByteBuffer.wrap(help);
        CharBuffer helpChars = null;
        try {
            helpChars = decoder.decode(asciiBytes);
        } catch (CharacterCodingException e) {
            System.err.println("Error decoding");
            System.exit(-1);
        }
        System.out.println(helpChars);
        Charset utfCharset = Charset.forName("UTF-16LE");
        CharsetEncoder encoder = utfCharset.newEncoder();
        ByteBuffer utfBytes = null;
        try {
            utfBytes = encoder.encode(helpChars);
        } catch (CharacterCodingException e) {
            System.err.println("Error encoding");
            System.exit(-1);
        }
        byte newHelp[] = utfBytes.array();
        for (int i=0, n=newHelp.length; i<n; i++) {
            System.out.println(i + " :" + newHelp[i]);
        }
    }
    
    public static void main(String[] args) throws IOException {
        //System.out.println(Charset.availableCharsets());//可用字符集
        CharBuffer cb=CharBuffer.allocate(10);
        System.out.println("cb.limit()="+cb.limit());
        System.out.println("cb.capacity() ="+cb.capacity());
        System.out.println("cb.position() ="+cb.position());
        System.out.println("cb.length() ="+cb.length());
        cb.put('A');
        cb.flip();
        System.out.println("");
        System.out.println("cb.limit()="+cb.limit());
        System.out.println("cb.capacity() ="+cb.capacity());
        System.out.println("cb.position() ="+cb.position());
        System.out.println("cb.length() ="+cb.length());
        System.out.println("");
        
        char c = cb.get();
        System.out.println("An A: " + c);
        
        System.out.println("cb.limit()="+cb.limit());
        System.out.println("cb.capacity() ="+cb.capacity());
        System.out.println("cb.position() ="+cb.position());
        System.out.println("cb.length() ="+cb.length());
        
        cb = CharBuffer.wrap("123456789");
        for (int i=0, n=cb.length(); i<n; i++) {
            System.out.println("");
            System.out.println(i + " : " + cb.get());
            
            
            System.out.println("cb.limit()="+cb.limit());
            System.out.println("cb.capacity() ="+cb.capacity());
            System.out.println("cb.position() ="+cb.position());
            System.out.println("cb.length() ="+cb.length());
        }
        
        args=new String[]{"123456789"};
        
        
        if (args.length != 0) {
            String arg = args[0];
            int size = arg.length();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size*2);
            CharBuffer buff = byteBuffer.asCharBuffer();
            buff.put(arg);
            buff.rewind();
            for (int i=0, n=buff.length(); i<n; i++) {
                System.out.println(i + " : " + buff.get());
            }
        }
        
        //args=new String[]{"/home/zhh/javac/com"};
        args=new String[]{"/home/zhh/javac/args.txt"};
        //args=new String[]{"/home/zhh/javac/src/my/test/TestNIO.java"};
        
        if (args.length != 0) {
            String filename = args[0];
            FileInputStream fis = new FileInputStream(filename);
            FileChannel channel = fis.getChannel();
            int length = (int)channel.size();
            MappedByteBuffer byteBuffer =
                channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
            //Charset charset = Charset.forName("ISO-8859-1");
            Charset charset = Charset.forName("GBK");
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(byteBuffer);
            for (int i=0, n=charBuffer.length(); i<n; i++) {
                System.out.print(charBuffer.get());
            }
        }
    }
}









