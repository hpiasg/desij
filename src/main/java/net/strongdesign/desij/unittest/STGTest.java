

package net.strongdesign.desij.unittest;

import static org.junit.Assert.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.FileSupport;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * @author mark
 *
 */
public class STGTest {
	
	
	STG stgSQT_04;
	STG stgSQT_05;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String args[] = new String[2];
		args[0] = "-U"; // use cloning instaed of undo stack
		args[1] = "testfile/seqpartree-04.g";
		
		CLW.instance = new CLW(args);
	}
	

	
	
	@Before
	public void setUp() throws Exception {
		stgSQT_04 = STGFile.convertToSTG(
				FileSupport.loadFileFromDisk("testfiles/seqpartree-04.g"), 
				false);
		
		stgSQT_05 = STGFile.convertToSTG(
				FileSupport.loadFileFromDisk("testfiles/seqpartree-05.g"), 
				false);
		

	}

	public void tearDown() throws Exception {
	}



	/**
	 * Test method for {@link net.strongdesign.stg.STG#equals(java.lang.Object)}.
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 * @throws STGException 
	 */
	@Test
	public final void testEqualsObject() throws FileNotFoundException, ParseException, IOException, STGException {
		STG stg = STGFile.convertToSTG(
				FileSupport.loadFileFromDisk("testfiles/seqpartree-05.g"), 
				false);
		
		assertEquals(stgSQT_05, stg);					
		assertFalse(stgSQT_04.equals(stgSQT_05));
		
		modifySTG(stgSQT_05);
		assertFalse(stgSQT_05.equals(stg));

	}

	@Test
	public final void testCloneAndRestore() throws FileNotFoundException, ParseException, IOException, STGException {
		STG copy = stgSQT_05.clone();
		assertEquals(stgSQT_05, copy);
	
		modifySTG(stgSQT_05);	
		stgSQT_05.restore(copy);
		assertEquals(copy, stgSQT_05);
				
		STG stg = STGFile.convertToSTG(
				FileSupport.loadFileFromDisk("testfiles/seqpartree-05.g"), 
				false);
		assertEquals(stg, stgSQT_05);		
	}	
	
	@Test
	public final void testUndo() throws FileNotFoundException, ParseException, IOException, STGException {
		
		Integer m1 = 1;
		STG stg1 = stgSQT_05.clone();
		stgSQT_05.addUndoMarker(m1);
		modifySTG(stgSQT_05);
		assertFalse(stgSQT_05.equals(stg1));
		
		Integer m2 = 1;
		STG stg2 = stgSQT_05.clone();
		stgSQT_05.addUndoMarker(m2);
		modifySTG(stgSQT_05);
		assertFalse(stgSQT_05.equals(stg2));
		
		Integer m3 = 1;
		STG stg3 = stgSQT_05.clone();
		stgSQT_05.addUndoMarker(m3);
		modifySTG(stgSQT_05);
		assertFalse(stgSQT_05.equals(stg3));
			
		
		stgSQT_05.undoToMarker(m3);		
		assertEquals(stgSQT_05, stg3);
		
		stgSQT_05.undoToMarker(m2);		
		assertEquals(stgSQT_05, stg2);
		
		stgSQT_05.undoToMarker(m1);		
		assertEquals(stgSQT_05, stg1);
				
		
	}
	
	enum OP {	ADD_PLACE, ADD_TRANSITION, 
				CHANGE_SIGNATURE, CONTRACT, 
				REMOVE_PLACE, REMOVE_TRANSITION};
	
	private void modifySTG(STG stg)  {
		
		
		int mod = (int) (Math.random()*10) + 1;
		
		for (int n=0; n<mod; ++n) {
		OP op = OP.values()[(int)(Math.random()*OP.values().length)];
		System.out.println(op);		
		switch (op) {	
		
		case ADD_PLACE: 
			stg.addPlace("__"+(int)Math.random()*1000, 666);
			break;
			
		case ADD_TRANSITION: 
			stg.addTransition(new SignalEdge((int)(Math.random()*1000), EdgeDirection.UP));
			stg.addTransition(new SignalEdge((int)(Math.random()*1000), EdgeDirection.DOWN));			
			break;
			
		case REMOVE_PLACE: 
			Place p = stg.getPlaces().iterator().next();
			stg.removePlace(p);
			break;
			
		case REMOVE_TRANSITION: 
			Transition t = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS).iterator().next();
			stg.removeTransition(t);
			break;
			
		case CHANGE_SIGNATURE: 
			List<Integer> sigs = new LinkedList<Integer>(stg.getSignals());
			Collections.shuffle(sigs);
			Integer sig = sigs.get(0);
			stg.setSignature(sig, Signature.INPUT);
			sig = sigs.get(1);
			stg.setSignature(sig, Signature.OUTPUT);
			break;
		case CONTRACT: 
			t = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS).iterator().next();
			sig = t.getLabel().getSignal();
			stg.setSignature(sig, Signature.DUMMY);
			try {
					stg.contract(t);
			} catch (STGException e) { 
				System.out.println("Contraction not possible");
			}

			break;
		}
			
			
		}
	}
	
}






///**
// * @throws java.lang.Exception
// */
//@AfterClass
//public static void tearDownAfterClass() throws Exception {
//}
