

package net.strongdesign.desij.gui;

import java.io.File;

class STGFileFilter extends javax.swing.filechooser.FileFilter {
	protected String pattern;
	protected String descr;

	public static final STGFileFilter STANDARD_OPEN = new STGFileFilter(".*((\\.g)|(\\.breeze))", "STG Files and Breeze Files");
	public static final STGFileFilter STANDARD = new STGFileFilter(".*\\.g", "STG File and Breeze Files");
	
	public static final STGFileFilter SVGFILTER = new STGFileFilter(".*\\.svg", "SVG File");
	
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