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

package net.strongdesign.desij;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.strongdesign.util.CommandLineWrapper;
import net.strongdesign.util.ParsingException;
import net.strongdesign.util.CommandLineWrapper.CommandLineOption;
import net.strongdesign.util.CommandLineWrapper.CommandLineParameter;

public class DesiJCommandLineWrapperTest {


	public static void main(String[] args) {
		
		Map<String, String> longNames = new HashMap<String, String>();
		Map<String, String> shortNames = new HashMap<String, String>();		
		
		try {
			
			CommandLineWrapper clw = new CLW(args);
			
			clw.showHelp();
			
			for (Field field : CLW.class.getDeclaredFields()) {
				Object o = field.get(clw);
				String fieldName = field.getName();
				
				if (o instanceof CommandLineOption) {
					CommandLineOption option = (CommandLineOption)o;
					
					String longName = option.getLongName();
					String old = longNames.put(longName, fieldName);
					if (old != null)
						System.out.println("Duplicate long name (" + longName + ") for command line options " + fieldName + " / " + old );
					
					String shortName = option.getShortName();
					old = shortNames.put(shortName, fieldName);
					if (old != null)
						System.out.println("Duplicate short name (" + shortName + ") for command line options " + fieldName + " / " + old );
				}
				else if (o instanceof CommandLineParameter) {
					CommandLineParameter param = (CommandLineParameter)o;
					
					String name = param.getName();
					String old = longNames.put(name, fieldName);
					if (old != null)
						System.out.println("Duplicate (long) name (" + name + ") for command line options/parameter " + fieldName + " / " + old );
					
				}
			}	
	
			
			
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		
		String abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ#!@1234567890";
		
		for (String c : shortNames.keySet())
			abc=abc.replaceAll(c, "");
		
		System.out.println("Remaining short codes: " + abc);
		
		
	}

	

}
