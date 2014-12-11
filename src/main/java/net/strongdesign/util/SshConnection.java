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

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.ConsoleKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

public class SshConnection {
	
	private static SshConnection instance = null;
	
	private SshClient ssh = null;
	private SshConnectionProperties properties = null;
	private PasswordAuthenticationClient pwd = null;
	
	private SessionChannelClient session = null;
	private ScpClient scp = null;
	
	private static final String GENERATED_STG = "newComponentSTG.g";
	private static final String EQUATIONS = "componentEquations.eq";
	
	private static File generatedSTGLocalFile = null;
	private static File equationsLocalFile = null;
	
	private SshConnection(String host, String user, String pass) throws Exception {
		ssh = new SshClient();
		properties = new SshConnectionProperties();
		properties.setHost(host);
								
		pwd = new PasswordAuthenticationClient();
		pwd.setUsername(user); 
		pwd.setPassword(pass);
		
		assureAuthenticatedConnection();
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 * Destructor
	 * disconnect from ssh
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		ssh.disconnect();
	}
	
	private void connect() throws InvalidHostFileException, UnknownHostException, IOException {
		ssh.connect(properties, new ConsoleKnownHostsKeyVerification() {
			public void onUnknownHost(String paramString, SshPublicKey paramSshPublicKey) { 
				try {
					allowHost(paramString, paramSshPublicKey, true);
				} catch (InvalidHostFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			} 
			});
	}
	
	private void assureAuthenticatedConnection() throws Exception {
		
		boolean reEstablished = false;
		
		if (!ssh.isConnected()) { 
			connect();
			reEstablished = true;
		}
		
		if (reEstablished || !ssh.isAuthenticated())  {
			if (ssh.authenticate(pwd)!=AuthenticationProtocolState.COMPLETE) {
				throw new RuntimeException("SSH authentication error");
			}
			reEstablished = true;
		}
		
		if (reEstablished) {
			session = null;
			scp = null;
		}
					
	}
	
	

	// Implemented as Singleton
	public static SshConnection getSshConnection(String host, String user, String pass) throws Exception {
		if (instance == null) {
			instance = new SshConnection(host, user, pass);
		}
		return instance;
	}
	
	public void putFile(String filename) throws Exception {
		assureAuthenticatedConnection();
		
		if (scp == null)
			scp = ssh.openScpClient(new File("." + File.separator)); // working directory "./"
		
		scp.put(filename, filename, false);
	}
	
	public File getNewSTGFile() throws Exception {
		assureAuthenticatedConnection();
		
		if (scp == null)
			scp = ssh.openScpClient(new File("." + File.separator)); // working directory "./"
		
		if (generatedSTGLocalFile == null)
			generatedSTGLocalFile = File.createTempFile("desij", ".g");
			
		scp.get(generatedSTGLocalFile.getCanonicalPath(), GENERATED_STG, false);
		
		return generatedSTGLocalFile;
	}
	
	public File getEquationsFile() throws Exception {
		assureAuthenticatedConnection();
		
		if (scp == null)
			scp = ssh.openScpClient(new File("." + File.separator)); // working directory "./"
		
		if (equationsLocalFile == null)
			equationsLocalFile = File.createTempFile("desij", ".eq");
		
		scp.get(equationsLocalFile.getCanonicalPath(), EQUATIONS, false);
		
		return equationsLocalFile;
	}
	
	public void complexGateSynthesis(String filename) throws Exception {
		assureAuthenticatedConnection();
		
		session = ssh.openSessionChannel();
				
		if (session.executeCommand("petrify -cg -eqn " + EQUATIONS + " -o " + GENERATED_STG + " " + filename))
			session.getState().waitForState(ChannelState.CHANNEL_CLOSED);	
	}
	
	public void deleteFile(String filename) throws Exception {
		assureAuthenticatedConnection();
		
		session = ssh.openSessionChannel();
			
		if (session.executeCommand("rm " + filename)) 
			session.getState().waitForState(ChannelState.CHANNEL_CLOSED);
	}
	
	public void deleteTempFiles() throws Exception {
		assureAuthenticatedConnection();
		
		session = ssh.openSessionChannel();
		if (session.executeCommand("rm " + GENERATED_STG)) 
			session.getState().waitForState(ChannelState.CHANNEL_CLOSED);
		
		session = ssh.openSessionChannel();
		if (session.executeCommand("rm " + EQUATIONS))
			session.getState().waitForState(ChannelState.CHANNEL_CLOSED);
	}

}
