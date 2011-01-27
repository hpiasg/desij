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
