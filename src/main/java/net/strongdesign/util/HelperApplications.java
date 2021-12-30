

package net.strongdesign.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.lang.Process;

import org.apache.log4j.Logger;

/// Interface class to support platform dependent external tools
public class HelperApplications {
	
	public static Logger log = Logger.getLogger(HelperApplications.class);

	private static Map<String, String> apps = new HashMap<String, String>();
	
	// Constants for specifying special sections in the command line parameters string
	// in order to PREVent these substring from INTerpretation by the command shell
	public static final String SECTION_START = "<PREV_INT>";
	public static final String SECTION_END = "</PREV_INT>";
	
	// Constants, either for Windows OS or Linux / MAC OS
	public static final String PETRIFY = System.getProperty("os.name").startsWith("Windows") ? "petrify.exe" : "petrify";
	public static final String MPSAT = System.getProperty("os.name").startsWith("Windows") ? "mpsat.exe" : "mpsat";
	public static final String PUNF = System.getProperty("os.name").startsWith("Windows") ? "punf.exe" : "punf";
	public static final String PCOMP = System.getProperty("os.name").startsWith("Windows") ? "pcomp.exe" : "pcomp";
	public static final String DOT = System.getProperty("os.name").startsWith("Windows") ? "dot.exe" : "dot";
	
	// experimental
	public static final String CAT = System.getProperty("os.name").startsWith("Windows") ? "cat.exe" : "cat";
	
	// Special case for MAC-OS could be necessary, because there might be no gv?!
	public static final String GHOSTVIEW = System.getProperty("os.name").startsWith("Windows") ? 
			( System.getProperty("os.arch").endsWith("64") ? "gsview64.exe" : "gsview32.exe" ) // 64-bit or 32-bit?
			: "gv";
	
	
	public static Process startExternalTool(String name, String parameters) throws IOException {
		return startExternalTool(name, parameters, null);
	}
	
	public static Process startExternalTool(String name, String parameters, File directory) throws IOException {
		
		String commandLineParameters = parseCommandLineParameters(parameters, name);
		return Runtime.getRuntime().exec(HelperApplications.getApplicationPath(name) + commandLineParameters, null, directory);
		
			
	}
	
	private static String parseCommandLineParameters(String parameters, String name) {
		String parsedParameters = parameters;
		
		// assure a leading blank in parsedParameters
		if (!parsedParameters.startsWith(" ")) parsedParameters = " " + parsedParameters;
		
		// special parameter that works for the external tool gv but not for gsview
		if (name.equals("gv")) parameters = " --spartan" + parameters; 
		
		// remove "</PREV_INT><PREV_INT>" occurences
		String tmpParameters = null;
		do {
			tmpParameters = new String(parsedParameters); // copy String
			parsedParameters = parsedParameters.replace(SECTION_END+SECTION_START, "");
			
		} while (!parsedParameters.equals(tmpParameters));
		
		// replace <PREV_INT> and </PREV_INT> according to the runtime OS
		if (System.getProperty("os.name").startsWith("Windows")) {
			parsedParameters = parsedParameters.replace(SECTION_START, "\"");
			parsedParameters = parsedParameters.replace(SECTION_END, "\"");
		}
		else {
			parsedParameters = parsedParameters.replace(SECTION_START, "");
			parsedParameters = parsedParameters.replace(SECTION_END, "");
		}
		
		return parsedParameters;
	}
			
	private static String getApplicationPath(String app) throws IOException  {
		String path = apps.get(app);
		if (path==null) {
			path = findPath(app);
			apps.put(app, path);
		}
		if (path==null) log.error(Log.print("Application :0 is not installed", app));
		return path;
	}
	
		
	static private String findPath(String app) throws IOException {
		
		String path = System.getenv("PATH");
		
		File file = null;
		for (String p : path.split(System.getProperty("path.separator")) )  { // old version: path.split("[:;,]")
			file = new File(p, app);
			if (file.exists() && file.isFile())
				return file.getCanonicalPath();
		}
		
		return null;
		
	}

	public static Pair<File, File> doSynthesisViaSsh(String filename, 
			String host, String username, String password) throws Exception {
		
		//SshConnection sshConnection = SshConnection.getSshConnection("192.168.141.129", "user", "ubuntu");
		SshConnection sshConnection = SshConnection.getSshConnection(host, username, password);
		
		sshConnection.putFile(filename);
		sshConnection.complexGateSynthesis(filename);
		
		Pair<File, File> newStgAndEquations = new Pair<File, File>(
				sshConnection.getNewSTGFile(), sshConnection.getEquationsFile());
		
		// tidy up after synthesis: delete input file, newComponentFile and Equations on the server
		sshConnection.deleteFile(filename);
		sshConnection.deleteTempFiles();
		
		// close connection: ssh.disconnect();
		// automatically in the destructor of sshConnection
				
		return newStgAndEquations;		
	}
	
	
	
	
}
