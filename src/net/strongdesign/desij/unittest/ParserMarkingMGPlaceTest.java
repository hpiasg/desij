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

package net.strongdesign.desij.unittest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Assert;

import net.strongdesign.desij.CLW;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.parser.GParser;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.ParsingException;

import org.junit.Test;

public class ParserMarkingMGPlaceTest {

	@Test
	public void test() throws ParseException, STGException, ParsingException, FileNotFoundException, IOException
	{
		CLW.instance = new CLW(new String[0]);
		Reader gFile = new StringReader(FileSupport.loadFileFromDisk("testfiles/ParserTestMGPlaceMarking.g"));
		if(gFile == null) throw new RuntimeException();
		STG stg = new GParser(gFile).STG(false);
		Assert.assertNotNull(stg);
	}
}
