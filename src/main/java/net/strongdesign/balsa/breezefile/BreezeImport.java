package net.strongdesign.balsa.breezefile;

/**
 * Copyright 2012-2014 Stanislavs Golubcovs
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
