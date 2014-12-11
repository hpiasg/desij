/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.strongdesign.util;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;





/**
 * Primitive command line parser. Allowed options and parameters are provided to
 * the constructor along with the command line arguments. Some basic validity
 * checks are perfomed. <br>
 * Options is a 2-dim String array of the form: <br>{<br>{ <short
 * description>, <long description>}, <br>
 * ... <br>}<br>
 * <br>
 * 
 * Parameter is a 2-dim String array of the form: <br>{<br>{ <parameter
 * name>, <allowed values>, <default value>}, <br>
 * ... <br>}<br>
 * <br>
 * 
 * 
 * 
 * After construction the member methods may be used to check_ the value of
 * parameters or wether options are checked or not. Arguments which are neither
 * an option nor a parameter are supposed to be free parameters, e.g. filenames,
 * they are collected as others. All types may occurr in any place of the
 * command line. Options may be specified multiple times in short and/or long
 * form. If an unknown option or paramter occurs, a ParsingException will be
 * thrown. The same, if a parameter has not an allowed value. If allowed values
 * is an empty string all values are possible. <br>
 * <br>
 * 
 * 
 * <b>Example: </b> <br>
 * <br>
 * Consider this argument line: -opkw --make-something filename1
 * --make-something-else filename2 <br>
 * <br>
 * After parsing this, the short options o,p,k and w will be set, as well as the
 * lomg options make-something and make-something-else, other will contain
 * filename1 and filename2
 * 
 * @author Mark Schaefer
 */
public abstract class CommandLineWrapper {
	
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Help {String value();}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface RelatedTo {String[] value();}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface NotEnabled {}
	
	
	protected static final Integer MARKER = new Integer(0); 
	
	protected static final String parameterRegex = "[\\w-]*=[%!\\?\\*\\w-\\./:_,;]*";
	
	protected Set notAllowedOptions;
	
	public static class CommandLineOption {
		private String 		shortName;
		private String 		longName;
		private boolean 	enabled;
		private boolean 	externalySet;
			
		public String getLongName() {
			return longName;
		}

		public String getShortName() {
			return shortName;
		}

		public CommandLineOption(Character shortName, String longName, boolean defaultEnabled) {
			this.shortName = String.valueOf(shortName);
			this.longName = longName;
			this.enabled = defaultEnabled;
			this.externalySet = false;
		}
		
		public void parseCommandLine(String args[]) {
			if (externalySet)
				return;
			
			for (String arg : args) {
				if (arg.startsWith("--")) {
					if (arg.replaceFirst("--", "").equals(longName)) {
						enabled = ! enabled;
						return;
					}
				}
				else if (arg.startsWith("-")) {
					if (arg.replaceFirst("-", "").contains(shortName)) {
						enabled = ! enabled;
						return;
					}
				}
			}
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		
		public void setEnabled(boolean b) {
			externalySet = true;
			enabled = b;
		}
		
	}
	
	

	
	public static class CommandLineParameter {
		protected String name;
		private Set<String> possibleValues;
		private String value = "";		
		protected boolean mandantory;
		
		protected boolean  externalySet;
		
		private boolean error = false;
		private String cl;
		
		protected String getCl() {
			return cl;
		}
		
		public CommandLineParameter(String name, String possibleValues, String defaultValue, boolean mandantory) {
			this.name = name; 
			this.mandantory = mandantory;
			this.externalySet = false;
			
			this.value = defaultValue;
			
			if (possibleValues.equals("")) {
				this.possibleValues = null;
			}
			else {
				this.possibleValues = new HashSet<String>();
				Collections.addAll(this.possibleValues, possibleValues.split(","));
				
				if (! defaultValue.equals("")) {
					if (this.possibleValues.contains(defaultValue)) {
						this.mandantory = false;
					}
					else {
						error = true;
					}
				}
			}
		}
		
		
		public void setValue(String v) {
			externalySet = true;
			value = v;
		}
		
		
		public void parseCommandLine(String[] args) throws ParsingException {
			if (externalySet)
				return;
			
			if (error) 
				throw new ParsingException("Invalid defaultvalue ("+value+") for parameter "+name+".\nAllowed" +
						" are: " + this.possibleValues);

			
			for (String arg : args) {
				if (arg.matches(parameterRegex)) {
					String[] p = arg.split("=");
					if (p[0].equals(name)) {
						cl = arg;
						if (possibleValues!=null)
							if (possibleValues.contains(p[1]))
								value = p[1];
							else
								throw new ParsingException("Invalid value ("+p[1]+") for parameter "+name+".\nAllowed" +
										" are: " + this.possibleValues);
						else
							value=p[1];
						
					}
				}
			}
			
			if ((value == null || value.length() == 0)  && mandantory)
				throw new ParsingException("Parameter "+name+" is mandantory. Allowed values are: "+possibleValues);
		}
		
		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}
		
	}
	
	public static class CommandLineDouble extends CommandLineParameter {
		private double value;
		private double min;
		private double max;
	
		
		
		public CommandLineDouble(String name, double min, double max, double defaultValue, boolean mandantory) {
			super(name, "", ""+defaultValue, mandantory);
			this.min = min;
			this.max = max;
			
		}
		
		public void parseCommandLine(String[] args) throws ParsingException {
			super.parseCommandLine(args);
			
			try {
				value = Double.parseDouble(getValue());	
			} 
			catch (NumberFormatException e) {
				throw new ParsingException("Invalid number format: "+getCl());
			}
			if (value<min || value>max)
				throw new ParsingException("Number out of range ["+min+","+max+"]: "+value);
		}
		
		public double getDoubleValue() {
			return value;
		}
		
		public void setValue(double v) {
			externalySet = true;
			value = v;
		}
		
		
	}
	
	public static class CommandLineInteger extends CommandLineParameter {
		private int value;
		private int min;
		private int max;
		
		public CommandLineInteger(String name, int min, int max, int defaultValue, boolean mandantory) {
			super(name, "", ""+defaultValue, mandantory);
			this.value = defaultValue;
			this.min = min;
			this.max = max;
			
		}
		
		public void parseCommandLine(String[] args) throws ParsingException {
			super.parseCommandLine(args);
			
			try {
				value = Integer.parseInt(getValue());	
			} 
			catch (NumberFormatException e) {
				throw new ParsingException("Invalid number format: "+getCl());
			}
			if (value<min || value>max)
				throw new ParsingException("Number out of range ["+min+","+max+"]: "+value);
		}
		
		public int getIntValue() {
			return value;
		}
		
		public void setValue(int i) {
			externalySet = true;
			value = i;
		}
	}
	
	
	protected List<CommandLineOption> options = new LinkedList<CommandLineOption>();
	protected List<CommandLineParameter> parameters = new LinkedList<CommandLineParameter>();
	protected List<String> files = new LinkedList<String>();
	private String[] args;
	
	public CommandLineWrapper(String[] args) {
		this.args = args;
		
		for (String value : args) {
			if (! (value.matches(parameterRegex) || value.startsWith("-") ) ) {
				files.add(value);			
			}
		}
		
	}

	
	protected void registerAll() throws ParsingException {
		for (Field field : getClass().getFields()) {
			try {
				if (field.isAnnotationPresent(NotEnabled.class))
					continue;
				
				Object o =  field.get(this);
				
				if (o instanceof CommandLineParameter)
					registerParameter( (CommandLineParameter) o);
				else if (o instanceof CommandLineOption)
					registerOptions( (CommandLineOption) o);
				
			} 
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	protected void registerOptions(CommandLineOption... options) {
		for (CommandLineOption option : options) {
			this.options.add(option);
			option.parseCommandLine(args);
		}
	}
	
	protected void registerParameter(CommandLineParameter... parameters) throws ParsingException {
		for (CommandLineParameter parameter : parameters) {
			this.parameters.add(parameter);
			parameter.parseCommandLine(args);
		}
	}
	
	
	protected abstract void checkForValidity();
	
	public List<String> getOtherParameters() {
		return files;
	}
	
	
	

	public void showHelp() {
		System.out.println("");		
		
		for (Field field : getClass().getFields()) {
			
			try {
				if (field.isAnnotationPresent(NotEnabled.class))
					continue;
				
				Class classType = field.getType();
				
				if (classType.equals(CommandLineOption.class)) {
					CommandLineOption option = (CommandLineOption) field.get(this);
					System.out.println("--"+option.longName + "\t-"+option.shortName + "\n\tDefault: " + (option.isEnabled()));	
				}
				else if (classType.equals(CommandLineParameter.class)) {
					CommandLineParameter parameter = (CommandLineParameter) field.get(this);
					Set<String> posv = parameter.possibleValues;
					System.out.println(parameter.name + "=" +
							(parameter.mandantory?"(mandantory)":"") + 
							"\n\tPossible values: " +	(posv==null?"arbitrary":posv) +
							"\n\tDefault: " + (parameter.value.equals("")?"not defined":parameter.value)	);
				}
				else if (classType.equals(CommandLineInteger.class)) {
					CommandLineInteger cli = (CommandLineInteger) field.get(this);
					System.out.println(cli.name + "=" + (cli.mandantory?"(mandantory)":"") + "\n\tRange: " + cli.min + " - " + cli.max 
							+ "\n\tDefault: " + cli.value);
				}
				else if (classType.equals(CommandLineDouble.class)) {
					CommandLineDouble cld = (CommandLineDouble) field.get(this);
					System.out.println(cld.name  + "=" +  (cld.mandantory?"(mandantory)":"") + "\n\tRange: " + cld.min + " - " + cld.max
							+ "\n\tDefault: " + cld.value);
				}
				else if (field.get(this) == MARKER);
				else
					continue;
				
				Help annotation = field.getAnnotation(Help.class);
				System.out.println("\t" + annotation.value().replaceAll("\n", "\n\t"));
				System.out.println("");
				
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
	
			
	}

	

	public String showValues() {
		StringBuilder result = new StringBuilder();		
		
		for (Field field : getClass().getFields()) {
			
			try {
				if (field.isAnnotationPresent(NotEnabled.class))
					continue;
				
				Class classType = field.getType();
				
				if (classType.equals(CommandLineOption.class)) {
					CommandLineOption option = (CommandLineOption) field.get(this);
					result.append(option.longName + "=" + (option.isEnabled()?"true":"false") + " - ");						
				}
				else if (classType.equals(CommandLineParameter.class)) {
					CommandLineParameter parameter = (CommandLineParameter) field.get(this);
					result.append(parameter.name + "=" + parameter.value + " - ");
				}
				else if (classType.equals(CommandLineInteger.class)) {
					CommandLineInteger cli = (CommandLineInteger) field.get(this);
					result.append(cli.name + "=" + cli.value + " - ");
				}
				else if (classType.equals(CommandLineDouble.class)) {
					CommandLineDouble cld = (CommandLineDouble) field.get(this);
					result.append(cld.name + "=" + cld.value + " - ");
				}
				else if (field.get(this) == MARKER);
				
				
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
	return result.toString();
			
	}

	

}
