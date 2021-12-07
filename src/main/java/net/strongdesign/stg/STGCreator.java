

package net.strongdesign.stg;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.ParsingException;

public abstract class STGCreator {




	public static STG getPredefinedSTG(String parameter) throws STGException {

		String[] modelParam = parameter.split(":");


		if (modelParam[0].equals("art")) {
			String[] values;
			if (modelParam.length!=2 || (values = modelParam[1].split(",")).length != 2) 
				throw new DesiJException(
						"Model art expects number of pipelines and their length as parameters." +
						" Given was: " + parameter);


			try {
				Integer nroPipelines = Integer.parseInt(values[0]);
				Integer length = Integer.parseInt(values[1]);

				return getArtJorPipeline(nroPipelines, length);
			}
			catch (NumberFormatException e) {
				throw new DesiJException("Could not parse art parameters. Expected two numbers. " + parameter);
			} 
		}

		else if (modelParam[0].equals("seq")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model seq expects identification string." +
						" Given was: " + parameter);

			return getSequencer(modelParam[1], 0);

		}

		else if (modelParam[0].equals("par")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model par expects identification string." +
						" Given was: " + parameter);

			return getParalleliser(modelParam[1], 0);

		}

		else if (modelParam[0].equals("multipar")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model multipar expects identification string." +
						" Given was: " + parameter);

			return getParTree(Integer.parseInt(modelParam[1]));

		}

		else if (modelParam[0].equals("multiseq")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model multiseq expects identification string." +
						" Given was: " + parameter);

			return getSeqTree(Integer.parseInt(modelParam[1]));

		}

		else if (modelParam[0].equals("seqpartree")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model seqpartree expects identification string." +
						" Given was: " + parameter);

			return getParSeqTree(Integer.parseInt(modelParam[1]), true);

		}

		else if (modelParam[0].equals("parseqtree")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model parseqtree expects identification string." +
						" Given was: " + parameter);

			return getParSeqTree(Integer.parseInt(modelParam[1]), false);

		}

		else if (modelParam[0].equals("merge")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model merger expects identification string." +
						" Given was: " + parameter);

			return getMerger(modelParam[1], 0);

		}
		
		else if (modelParam[0].equals("mix")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model mixer expects identification string." +
						" Given was: " + parameter);

			return getMixer(modelParam[1], 0);

		}
		
		else if (modelParam[0].equals("multimix")) {
			if (modelParam.length!=2)
				throw new DesiJException(
						"Model multimixer expects identification string." +
						" Given was: " + parameter);

			return getMixTree(Integer.parseInt(modelParam[1]));

		}


		else 
			throw new DesiJException("Unknown model name: " + modelParam[0] );


	}




	public static STG getArtJorPipeline(int nroPipelines, int length) throws STGException {

		if (nroPipelines < 2 || length < 2)
			throw new IllegalArgumentException("Need at least 2 pipelines with minimum length 2.");


		STG stg = new STG();
		int signal = -1;

		for (int pipeline = 1; pipeline <= nroPipelines; ++pipeline) {
			for (int pos = 1; pos <= length; ++pos) {
				stg.setSignalName(++signal, "x_"+pipeline+"_"+pos);
			}
		}


		for (int pipeline = 2; pipeline <= nroPipelines; ++pipeline) {
			stg.setSignalName(++signal, "sincr_"+pipeline);
		}


		stg.setSignature(stg.getSignals(), Signature.OUTPUT);


		Transition pipeConUp = null;
		Transition pipeConDown = null;



		List<Transition> pipeStarts = new LinkedList<Transition>();

		for (int pipeline = 1; pipeline <= nroPipelines; ++pipeline) {

			Transition pipeStart = stg.addTransition(
					new SignalEdge(
							stg.getSignalNumber("x_"+pipeline+"_1"), 
							EdgeDirection.UP));

			pipeStarts.add(pipeStart);

			if (pipeConDown != null) {
				connect(pipeStart, pipeConDown, stg);
				connect(pipeConUp, pipeStart, stg);				
			}


			if (pipeline < nroPipelines) {
				pipeConUp = stg.addTransition(

						new SignalEdge(
								stg.getSignalNumber("sincr_"+ (pipeline + 1)), 
								EdgeDirection.UP));

				pipeConDown = stg.addTransition(
						new SignalEdge(
								stg.getSignalNumber("sincr_"+ (pipeline + 1)), 
								EdgeDirection.DOWN));

				connect(pipeStart, pipeConUp, stg);
				connect(pipeConDown, pipeStart, stg).setMarking(1);	
			}

		}

		int pipeline = 0;
		for (Transition start : pipeStarts) {
			++pipeline;

			Transition lastTransition = start;
			for (int p = 2; p<=length; ++p) {
				Transition newTrans = stg.addTransition(
						new SignalEdge(
								stg.getSignalNumber("x_" + pipeline + "_" + p), 
								EdgeDirection.UP));

				connect(lastTransition, newTrans, stg);

				lastTransition = newTrans;
			}



			for (int p = 1; p<=length; ++p) {
				Transition newTrans = stg.addTransition(
						new SignalEdge(
								stg.getSignalNumber("x_" + pipeline + "_" + p), 
								EdgeDirection.DOWN));

				connect(lastTransition, newTrans, stg);

				lastTransition = newTrans;
			}

			connect(lastTransition, start, stg).setMarking(1);



		}


		return stg;


	}


	protected static Place connect(Transition source, Transition target, STG stg) {
		Place place = stg.addPlace("p", 0);
		source.setChildValue(place, 1);
		target.setParentValue(place, 1);

		return place;
	}


	/**
	 * Generate a 4-phase handshake sequencer.<br> 
	 * The up channel is (in,out), the down channels are (a_in,a_out) and (b_in,b_out).
	 * The real identifier are prefixed with id.
	 * 
	 * @param id The channel name prefix
	 * @param signal The first signal which is used.
	 * @return
	 * @throws STGException
	 */
	public static STG getSequencer(String id, int signal) throws STGException {

		STG stg = new STG();

		// Define the signal names
		final String _in  =  id + "in";
		final String _out =  id + "out";

		final String _a_in  =  id + "a_in";
		final String _a_out =  id + "a_out";

		final String _b_in  =  id + "b_in";
		final String _b_out =  id + "b_out";

		final String _csc  =  id + "csc";

		
		
		
		// Associate them to signal numbers and define the signatures
		final int in = signal++;
		stg.setSignalName(in, _in);
		stg.setSignature(in, Signature.INPUT);

		final int out = signal++;
		stg.setSignalName(out, _out);
		stg.setSignature(out, Signature.OUTPUT);

		final int a_in = signal++;
		stg.setSignalName(a_in, _a_in);
		stg.setSignature(a_in, Signature.INPUT);

		final int a_out = signal++;
		stg.setSignalName(a_out, _a_out);
		stg.setSignature(a_out, Signature.OUTPUT);

		final int b_in = signal++;
		stg.setSignalName(b_in, _b_in);
		stg.setSignature(b_in, Signature.INPUT);

		final int b_out = signal++;
		stg.setSignalName(b_out, _b_out);
		stg.setSignature(b_out, Signature.OUTPUT);


		final int csc = signal++;
		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			stg.setSignalName(csc, _csc);
			stg.setSignature(csc, Signature.INTERNAL);			
		}
		
		
		
		// Intermediate transitions
		Transition t1 = null, t2 = null;

		// Should be clear from here, essentially seq is a simple cycle and we only have to remember the first transition

		// start sequencer with in+
		Transition first = stg.addTransition(new SignalEdge(in, EdgeDirection.UP));

		// this triggers the a handshake		
		t1 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.UP));
		connect(first, t1, stg);

		t2 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.UP));
		connect(t1, t2, stg);
		t1 = t2;
		
		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			t2 = stg.addTransition(new SignalEdge(csc, EdgeDirection.UP));
			connect(t1, t2, stg);
			t1 = t2;
		}
		
		t2 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		// this triggers the b handshake
		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.UP));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.UP));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.UP));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			t2 = stg.addTransition(new SignalEdge(csc, EdgeDirection.DOWN));
			connect(t1, t2, stg);
			t1 = t2;
		}

		
		// complete up handshake
		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.DOWN));
		connect(t1, t2, stg);

		// close the cycle		
		connect(t2, first, stg).setMarking(1);

		// clean up
		stg.clearUndoStack();

		return stg;
	}





	/**
	 * Generate a 4-phase handshake paralleliser.<br> 
	 * The up channel is (in,out), the down channels are (a_in,a_out) and (b_in,b_out).
	 * The real identifier are prefixed with id.
	 * 
	 * @param id The channel name prefix
	 * @param signal The first signal which is used.
	 * @return
	 * @throws STGException
	 */
	public static STG getParalleliser(String id, int signal) throws STGException {

		STG stg = new STG();


		final String _in  =  id + "in";
		final String _out =  id + "out";

		final String _a_in  =  id + "a_in";
		final String _a_out =  id + "a_out";

		final String _b_in  =  id + "b_in";
		final String _b_out =  id + "b_out";
		
		final String _csc_a  =  id + "csc_a";
		final String _csc_b  =  id + "csc_b";

		
		
		

		final int in = signal++;
		stg.setSignalName(in, _in);
		stg.setSignature(in, Signature.INPUT);

		final int out = signal++;
		stg.setSignalName(out, _out);
		stg.setSignature(out, Signature.OUTPUT);

		final int a_in = signal++;
		stg.setSignalName(a_in, _a_in);
		stg.setSignature(a_in, Signature.INPUT);

		final int a_out = signal++;
		stg.setSignalName(a_out, _a_out);
		stg.setSignature(a_out, Signature.OUTPUT);

		final int b_in = signal++;
		stg.setSignalName(b_in, _b_in);
		stg.setSignature(b_in, Signature.INPUT);

		final int b_out = signal++;
		stg.setSignalName(b_out, _b_out);
		stg.setSignature(b_out, Signature.OUTPUT);

		final int csc_a = signal++;
		final int csc_b = signal++;
		
		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			stg.setSignalName(csc_a, _csc_a);
			stg.setSignature(csc_a, Signature.INTERNAL);
			stg.setSignalName(csc_b, _csc_b);
			stg.setSignature(csc_b, Signature.INTERNAL);
		}
		
		// intermediate transitions
		Transition t1 = null, t2 = null, t3 = null;

		// start of paralleleiser
		Transition first = stg.addTransition(new SignalEdge(in, EdgeDirection.UP));

		// part of up handshake, needed later explicitely for synchronisation
		t3 = stg.addTransition(new SignalEdge(out, EdgeDirection.UP));				


		// in+ triggers the a handshake
		t1 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.UP));
		connect(first, t1, stg);

		t2 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.UP));
		connect(t1, t2, stg);
		t1 = t2;
		
		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			t2 = stg.addTransition(new SignalEdge(csc_a, EdgeDirection.UP));
			connect(t1, t2, stg);
			t1 = t2;
		}

		t2 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		// synchronise
		connect(t2, t3, stg);


		// in+ triggers also the b handshake
		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.UP));
		connect(first, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.UP));
		connect(t1, t2, stg);
		t1 = t2;
	
		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			t2 = stg.addTransition(new SignalEdge(csc_b, EdgeDirection.UP));
			connect(t1, t2, stg);
			t1 = t2;
		}
		
		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.DOWN));
		connect(t1, t2, stg);

		// synchronise
		connect(t2, t3, stg);





		// complete up handshake
		t1 = t3;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.DOWN));
		connect(t1, t2, stg);
		t1 = t2;

		if (CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled()) {
			t2 = stg.addTransition(new SignalEdge(csc_a, EdgeDirection.DOWN));
			connect(t1, t2, stg);
			t1 = t2;
			
			t2 = stg.addTransition(new SignalEdge(csc_b, EdgeDirection.DOWN));
			connect(t1, t2, stg);
			t1 = t2;			
		}
		
		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.DOWN));
		connect(t1, t2, stg);

		// close cycle
		connect(t2, first, stg).setMarking(1);


		// clean up
		stg.clearUndoStack();

		return stg;
	}



	public static STG getMerger(String id, int signal) throws STGException {
		STG stg = new STG();

		final String _in  =  id + "in";
		final String _out =  id + "out";

		final String _a_in  =  id + "a_in";
		final String _a_out =  id + "a_out";

		final String _b_in  =  id + "b_in";
		final String _b_out =  id + "b_out";

		final int in = signal++;
		stg.setSignalName(in, _in);
		stg.setSignature(in, Signature.INPUT);

		final int out = signal++;
		stg.setSignalName(out, _out);
		stg.setSignature(out, Signature.OUTPUT);

		final int a_in = signal++;
		stg.setSignalName(a_in, _a_in);
		stg.setSignature(a_in, Signature.INPUT);

		final int a_out = signal++;
		stg.setSignalName(a_out, _a_out);
		stg.setSignature(a_out, Signature.OUTPUT);

		final int b_in = signal++;
		stg.setSignalName(b_in, _b_in);
		stg.setSignature(b_in, Signature.INPUT);

		final int b_out = signal++;
		stg.setSignalName(b_out, _b_out);
		stg.setSignature(b_out, Signature.OUTPUT);


		// intermediate transitions
		Transition t1 = null, t2 = null;

		Place p = stg.addPlace("p1", 1);

		t1 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.UP));
		p.setChildValue(t1, 1);

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t2.setChildValue(p, 1);




		t1 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.UP));
		p.setChildValue(t1, 1);

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t2.setChildValue(p, 1);



		// clean up
		stg.clearUndoStack();

		return stg;

	}
	
	public static STG getMixer(String id, int signal) throws STGException {
		STG stg = new STG();

		final String _in  =  id + "in";
		final String _out =  id + "out";

		final String _a_in  =  id + "a_in";
		final String _a_out =  id + "a_out";

		final String _b_in  =  id + "b_in";
		final String _b_out =  id + "b_out";

		final int in = signal++;
		stg.setSignalName(in, _in);
		stg.setSignature(in, Signature.INPUT);

		final int out = signal++;
		stg.setSignalName(out, _out);
		stg.setSignature(out, Signature.OUTPUT);

		final int a_in = signal++;
		stg.setSignalName(a_in, _a_in);
		stg.setSignature(a_in, Signature.INPUT);

		final int a_out = signal++;
		stg.setSignalName(a_out, _a_out);
		stg.setSignature(a_out, Signature.OUTPUT);

		final int b_in = signal++;
		stg.setSignalName(b_in, _b_in);
		stg.setSignature(b_in, Signature.INPUT);

		final int b_out = signal++;
		stg.setSignalName(b_out, _b_out);
		stg.setSignature(b_out, Signature.OUTPUT);


		// intermediate transitions
		Transition t1 = null, t2 = null;

		Place p = stg.addPlace("p1", 1);

		t1 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.UP));
		p.setChildValue(t1, 1);

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;
		
		t2 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(a_in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;
		
		t2 = stg.addTransition(new SignalEdge(a_out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t2.setChildValue(p, 1);




		t1 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.UP));
		p.setChildValue(t1, 1);

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1=t2;
		
		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.UP));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(b_in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;

		t2 = stg.addTransition(new SignalEdge(out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1=t2;

		t2 = stg.addTransition(new SignalEdge(in, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t1 = t2;
		
		t2 = stg.addTransition(new SignalEdge(b_out, EdgeDirection.DOWN));
		connect(t1,t2,stg);
		t2.setChildValue(p, 1);

		// clean up
		stg.clearUndoStack();

		return stg;

	}


	public static STG getParTree(int height) throws STGException {
		if (height<1)
			height = 1;

		List<String> hiddenHandshakes = new LinkedList<String>();

		LinkedList<STG> stgs = new LinkedList<STG>();
		stgs.add(getParalleliser("par_0_0_", 0));
		if (height>1) {
			hiddenHandshakes.add("par_0_0_a_out");
			hiddenHandshakes.add("par_0_0_b_out");			
		}

		for (int curLevel=1; curLevel < height; ++curLevel) {
			for (int curPar=0; curPar < Math.pow(2,curLevel); ++curPar) {
				String curPrefix = "par_" + curLevel + "_" + curPar + "_";
				STG curSTG = getParalleliser(curPrefix, 0);			

				int parentLevel=curLevel -1 ;
				int parentNr = curPar / 2;
				String parentPrefix = "par_" + parentLevel + "_" + parentNr + "_" + ((curPar % 2 == 0) ? "a_" : "b_") ;

				Map<String,String> renaming = new HashMap<String, String>();
				renaming.put(curPrefix + "in", parentPrefix + "out");
				renaming.put(curPrefix + "out", parentPrefix + "in");
				curSTG.renameSignals(renaming);	

				stgs.add(curSTG);


				hiddenHandshakes.add(parentPrefix + "in");
				if (curLevel<height-1) {
					hiddenHandshakes.add(curPrefix + "a_out");
					hiddenHandshakes.add(curPrefix + "b_out");					
				}				

			}
		}


		STG parallelComposition = STG.parallelComposition(stgs);


		if (CLW.instance.DUMMIFY_INTERNALHANDSHAKES.isEnabled())
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.DUMMY);
		else
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.INTERNAL);

		return parallelComposition;


	}

	public static STG getSeqTree(int height) throws STGException {
		if (height<1)
			height = 1;

		List<String> hiddenHandshakes = new LinkedList<String>();

		LinkedList<STG> stgs = new LinkedList<STG>();
		stgs.add(getSequencer("seq_0_0_", 0));


		if (height>1) {
			hiddenHandshakes.add("seq_0_0_a_out");
			hiddenHandshakes.add("seq_0_0_b_out");			
		}
		

		for (int curLevel=1; curLevel < height; ++curLevel) {
			for (int curPar=0; curPar < Math.pow(2,curLevel); ++curPar) {
				String curPrefix = "seq_" + curLevel + "_" + curPar + "_";
				STG curSTG = getSequencer(curPrefix, 0);

				int parentLevel=curLevel -1 ;
				int parentNr = curPar / 2;
				String parentPrefix = "seq_" + parentLevel + "_" + parentNr + "_" + ((curPar % 2 == 0) ? "a_" : "b_") ;

				Map<String,String> renaming = new HashMap<String, String>();
				renaming.put(curPrefix + "in", parentPrefix + "out");
				renaming.put(curPrefix + "out", parentPrefix + "in");
				curSTG.renameSignals(renaming);	

				stgs.add(curSTG);
				
				hiddenHandshakes.add(parentPrefix + "in");
				if (curLevel<height-1) {
					hiddenHandshakes.add(curPrefix + "a_out");
					hiddenHandshakes.add(curPrefix + "b_out");					
				}		
			}
		}


		STG parallelComposition = STG.parallelComposition(stgs);
		
		
		if (CLW.instance.DUMMIFY_INTERNALHANDSHAKES.isEnabled())
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.DUMMY);
		else
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.INTERNAL);
		
		return parallelComposition;

	}
	
	
	public static STG getMixTree(int height) throws STGException {
		if (height < 1)
			height = 1;

		List<String> hiddenHandshakes = new LinkedList<String>();

		LinkedList<STG> stgs = new LinkedList<STG>();
		stgs.add(getMixer("mix_0_", 0));


		if (height > 1) {
			hiddenHandshakes.add("mix_0_out");			
		}
		

		for (int curLevel=1; curLevel < height; ++curLevel) {
			
			String curPrefix = "mix_" + curLevel + "_";
			STG curSTG = getMixer(curPrefix, 0);

			int parentLevel = curLevel - 1 ;
			String parentPrefix = "mix_" + parentLevel + "_";

			Map<String,String> renaming = new HashMap<String, String>();
			renaming.put(curPrefix + "a_in", parentPrefix + "out");
			renaming.put(curPrefix + "a_out", parentPrefix + "in");
			curSTG.renameSignals(renaming);	

			stgs.add(curSTG);
			
			hiddenHandshakes.add(parentPrefix + "in");
			if (curLevel < height-1) {
				hiddenHandshakes.add(curPrefix + "out");					
			}		
			
		}


		STG parallelComposition = STG.parallelComposition(stgs);
		
		if (CLW.instance.DUMMIFY_INTERNALHANDSHAKES.isEnabled())
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.DUMMY);
		else
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.INTERNAL);

		return parallelComposition;

	}


	public static STG getParSeqTree(int height, boolean startWithSeq) throws STGException {
		if (height<1)
			height = 1;

		List<String> hiddenHandshakes = new LinkedList<String>();

		LinkedList<STG> stgs = new LinkedList<STG>();
		if (startWithSeq) {
			stgs.add(getSequencer("seq_0_0_", 0));
			if (height>1) {
				hiddenHandshakes.add("seq_0_0_a_out");
				hiddenHandshakes.add("seq_0_0_b_out");			
			}
		}

		else {
			stgs.add(getParalleliser("par_0_0_", 0));
			if (height>1) {
				hiddenHandshakes.add("par_0_0_a_out");
				hiddenHandshakes.add("par_0_0_b_out");			
			}
		}


		startWithSeq = ! startWithSeq;




		for (int curLevel=1; curLevel < height; ++curLevel) {
			for (int curPar=0; curPar < Math.pow(2,curLevel); ++curPar) {
				System.out.println("" + curLevel + " " + curPar);
				String curPrefix = "";
				STG curSTG = null;

				if (startWithSeq) {
					curPrefix = "seq_" + curLevel + "_" + curPar + "_";
					curSTG = getSequencer(curPrefix, 0);
				}
				else {
					curPrefix = "par_" + curLevel + "_" + curPar + "_";
					curSTG = getParalleliser(curPrefix, 0);
				}



				int parentLevel=curLevel -1 ;
				int parentNr = curPar / 2;
				String parentPrefix = "";

				if (startWithSeq) {
					parentPrefix = "par_" + parentLevel + "_" + parentNr + "_" + ((curPar % 2 == 0) ? "a_" : "b_") ;
				}
				else {
					parentPrefix = "seq_" + parentLevel + "_" + parentNr + "_" + ((curPar % 2 == 0) ? "a_" : "b_") ;
				}


				Map<String,String> renaming = new HashMap<String, String>();
				renaming.put(curPrefix + "in", parentPrefix + "out");
				renaming.put(curPrefix + "out", parentPrefix + "in");
				curSTG.renameSignals(renaming);	

				stgs.add(curSTG);
				
				hiddenHandshakes.add(parentPrefix + "in");
				if (curLevel<height-1) {
					hiddenHandshakes.add(curPrefix + "a_out");
					hiddenHandshakes.add(curPrefix + "b_out");					
				}		
			}
			startWithSeq = ! startWithSeq;
		}

		STG parallelComposition = STG.parallelComposition(stgs);
		

		if (CLW.instance.DUMMIFY_INTERNALHANDSHAKES.isEnabled())
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.DUMMY);
		else
			parallelComposition.setSignature(parallelComposition.getSignalNumbers(hiddenHandshakes), Signature.INTERNAL);

		return parallelComposition;

	}



	@SuppressWarnings("unused")
	private static void getParSeqTreePartition(int height, boolean startWithSeq) throws STGException, IOException {
		LinkedList<STG> stgs = new LinkedList<STG>();
		if (startWithSeq)
			stgs.add(getSequencer("seq_0_0_", 0));
		else
			stgs.add(getParalleliser("par_0_0_", 0));

		startWithSeq = ! startWithSeq;

		for (int curLevel=1; curLevel < height; ++curLevel) {
			for (int curPar=0; curPar < Math.pow(2,curLevel); ++curPar) {
				System.out.println("" + curLevel + " " + curPar);
				String curPrefix = "";
				STG curSTG = null;

				if (startWithSeq) {
					curPrefix = "seq_" + curLevel + "_" + curPar + "_";
					curSTG = getSequencer(curPrefix, 0);
				}
				else {
					curPrefix = "par_" + curLevel + "_" + curPar + "_";
					curSTG = getParalleliser(curPrefix, 0);
				}



				int parentLevel=curLevel -1 ;
				int parentNr = curPar / 2;
				String parentPrefix = "";

				if (startWithSeq) {
					parentPrefix = "par_" + parentLevel + "_" + parentNr + "_" + ((curPar % 2 == 0) ? "a_" : "b_") ;
				}
				else {
					parentPrefix = "seq_" + parentLevel + "_" + parentNr + "_" + ((curPar % 2 == 0) ? "a_" : "b_") ;
				}


				Map<String,String> renaming = new HashMap<String, String>();
				renaming.put(curPrefix + "in", parentPrefix + "out");
				renaming.put(curPrefix + "out", parentPrefix + "in");
				curSTG.renameSignals(renaming);	

				stgs.add(curSTG);
			}
			startWithSeq = ! startWithSeq;
		}


		StringBuilder sb = new StringBuilder();
		for (STG stg : stgs) {
			sb.append(stg.getSignalNames((stg.getSignals(Signature.OUTPUT))).toString().replaceAll("[,\\[\\]]", ""));
			sb.append(" ");
			sb.append(stg.getSignalNames((stg.getSignals(Signature.INTERNAL))).toString().replaceAll("[,\\[\\]]", ""));
		
			sb.append("\n");

		}

		FileSupport.saveToDisk(sb.toString(), "partition");

	}


	public static void main(String[] args) throws NumberFormatException, STGException, IOException, ParsingException {
//		String s[] = new String[2];
//		s[0] = "-cH";
//		s[1] = "aaaa";
//		
//		CLW.instance = new
//		CLW(s);
//		getParSeqTreePartition(Integer.parseInt(args[0]), args[1].equals("seq"));
		
		for (int i = 2; i<=12 ; ++i)
			System.out.println("" + i + " : " + getNroStates(i,true));
	}


	
	
	private static BigDecimal getNroStates(int n, boolean seq) {
	
		if (n==1) {
			if (seq)
				return new BigDecimal(12);
			else
				return new BigDecimal(28);
		}
		else {
			BigDecimal h = getNroStates(n-1, !seq);
			if (seq) {
				h = h.multiply(new BigDecimal(2));
				h = h.add(new BigDecimal(4));
			}
			else {
				h = h.multiply(h);
				h = h.add(new BigDecimal(28));				
			}				
			return h;
		}
	}
	
	
	
	
	
	
}
