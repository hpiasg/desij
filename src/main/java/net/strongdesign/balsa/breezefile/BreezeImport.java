package net.strongdesign.balsa.breezefile;



public class BreezeImport extends AbstractBreezeElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -489866964489709268L;
	private String importString;

	public BreezeImport(String importString) {
		this.importString = importString;
	}

	@Override
	public void output() {
		System.out.printf("(import "+importString+")\n");
	}
	
	

}
