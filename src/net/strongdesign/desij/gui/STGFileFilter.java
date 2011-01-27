/**
 * 
 */
package net.strongdesign.desij.gui;

import java.io.File;

class STGFileFilter extends javax.swing.filechooser.FileFilter {
	protected String pattern;
	protected String descr;

	public static final STGFileFilter STANDARD = new STGFileFilter(".*\\.g", "STG File");
	
	public STGFileFilter(String pattern, String descr) {
		this.pattern = pattern;
		this.descr = descr;
	}

	public boolean accept(File file) {
		return file.isDirectory() || file.getName().matches(pattern);
	}

	public String getDescription() {
		return descr;
	}

}