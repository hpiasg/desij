

package net.strongdesign.util;

public final class Log {
	private Log() {};
	
	public static String print(String mes, Object... params) {
		if (params == null)
			return mes;
		
		
		int n=0;
		for (Object param : params) {
			mes = mes.replaceAll(":"+(n++), "" + param);
		}
		
		return mes;
	}
	
	
}
