import java.io.*;
class MyFileFilter implements FileFilter {

public static String[] name={".java",".c",".erl",".php"};
	public boolean accept(File pathname) {
	 if(pathname.isDirectory()) return true;
		else
		  for(int i=0;i<name.length;i++) 
			    if (pathname.getName().endsWith(name[i])) return true;
		return false;
		
	/*
		if(pathname.isDirectory()) return true;
		else if(pathname.getName().endsWith(".java") 
			|| pathname.getName().endsWith(".c")
			|| pathname.getName().endsWith(".erl")
			|| pathname.getName().endsWith(".php")) return true;
		else return false;
		*/
	}
}
public class FileLines {
	public static int count=0;
	public static int countFile=0;
	public static void main(String[] args) throws IOException {

	int index=0;

		for(;index<args.length;index++) {
		//System.out.println(args[index]);
		if(args[index].equals("@")) {
		//System.out.println("@@@@@@@");
		//System.out.println(index+ "  "+args.length);
		  MyFileFilter.name=new String[args.length-index-1];
		  System.arraycopy(args,index+1,MyFileFilter.name,0,args.length-index-1);
		     break;
		}

		}


   //System.out.println("index="+index);
		for(int i=0;i<index;i++) {

			File f = new File(args[i]);
			lines(f);
		}
		
		System.out.println("files:"+countFile);
		System.out.println("lines:"+count);
	}
	public static void lines(File file) throws IOException {
		File[] files = file.listFiles(new MyFileFilter());
		if (files == null) return;
        else {
            for (File f: files) {
            	//System.out.println("fname:"+f);
            	if(f.isFile()) {
            	System.out.println("fname:"+f);
            		countFile++;
            		BufferedReader br = new BufferedReader(new FileReader(f));
            		while (br.readLine()!=null) count++;
            		br.close();
            		br=null;
            	} else {
            		if (f.isDirectory()) lines(f);
            	}
            }
        }
	}
}
