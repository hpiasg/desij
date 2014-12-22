package net.strongdesign.balsa;

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

public class HSChannel {
	public enum ChannelSense {SYNC, PUSH, PULL}; 
	
	public ChannelSense sense=ChannelSense.SYNC;
	public int width=0;
	public HSPort Source=null;
	public HSPort Destination=null;
	public HSChannel() {
	
	}
	
	public HSChannel(ChannelSense sense, int width) {
		this.sense = sense;
		this.width = width;
	}
}
