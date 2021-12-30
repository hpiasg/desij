

package net.strongdesign.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public final class FileSupport {
	
	public static File createTempDirectory(String prefix) 
	{
		File tempDir;
		try {
			tempDir = File.createTempFile(prefix, "");
		} catch (IOException e) {
			throw new RuntimeException("can't create a temp file");
		}
		tempDir.delete();
		if(!tempDir.mkdir())
			throw new RuntimeException("can't create a temp directory");
		return tempDir;
	}
	
	public static void convertDotToPS(String dot, String fileName) throws IOException, InterruptedException {
		String file = File.createTempFile("desij-stg", ".dot").getCanonicalPath();
		
		FileSupport.saveToDisk(dot, file);
		
		HelperApplications.startExternalTool(HelperApplications.DOT, 
				" -Tps " +
				HelperApplications.SECTION_START+file+HelperApplications.SECTION_END +
				" -o " +
				HelperApplications.SECTION_START+fileName+HelperApplications.SECTION_END).waitFor();
	}
	
	
	
	private FileSupport(){}
	
	public static String loadFileFromDisk(String fileName) 
	throws FileNotFoundException, IOException{
		
		File file = new File(fileName);
		return loadFileFromDisk(file);
	}
	
	public static String loadFileFromDisk(File file) throws FileNotFoundException, IOException {
		FileReader fileReader = new FileReader(file);
		char[] content = new char[(int) file.length()];
		fileReader.read(content);
		
		return new String(content);
	}
	
		
	public static void saveToDisk(String content, String fileName) 
	throws IOException{
		File file=new File(fileName);
		
		FileWriter fw = new FileWriter(file);
		fw.write(content);
		fw.close();		
	}
	
}
