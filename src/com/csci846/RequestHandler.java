package com.csci846;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;


// RequestHandler is thread that process requests of one client connection
public class RequestHandler extends Thread {


	Socket clientSocket;

	InputStream inFromClient;

	OutputStream outToClient;

	byte[] request = new byte[1024];

	BufferedReader proxyToClientBufferedReader;

	BufferedWriter proxyToClientBufferedWriter;


	private ProxyServer server;


	public RequestHandler(Socket clientSocket, ProxyServer proxyServer) {


		this.clientSocket = clientSocket;


		this.server = proxyServer;

		try {
			clientSocket.setSoTimeout(2000);
			inFromClient = clientSocket.getInputStream();
			outToClient = clientSocket.getOutputStream();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	@Override

	public void run() {

		/**
		 * To do
		 * Process the requests from a client. In particular,
		 * (1) Check the request type, only process GET request and ignore others
		 * (2) If the url of GET request has been cached, respond with cached content
		 * (3) Otherwise, call method proxyServertoClient to process the GET request
		 *
		 */
		try {
			proxyToClientBufferedReader = new BufferedReader(new InputStreamReader(inFromClient));

			// Build the request byte array
			StringBuilder textReceived = new StringBuilder();
			for(String line = proxyToClientBufferedReader.readLine(); line != null; line = proxyToClientBufferedReader.readLine()){
				textReceived.append(line);
			}

			// TODO: Is this needed to be set here????
			request = textReceived.toString().getBytes();

			String httpRequest = parseHttpRequest(textReceived);
			String url = parseUrl(textReceived);

			if(httpRequest.equals("GET")){
				// If cache exists forward it on
				if(server.getCache(url) != null){
					sendCachedInfoToClient(server.getCache(url));
				}
				// else proceed to get info
				proxyServertoClient(textReceived.toString().getBytes());
			}

			clientSocket.close();
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}

	}

	private String parseHttpRequest(final StringBuilder textReceived) {
		int firstSpacePosition = textReceived.indexOf(" ");
		return textReceived.substring(0, firstSpacePosition);
	}

	private String parseUrl(final StringBuilder textReceived) {
		int firstSpacePosition = textReceived.indexOf(" ");
		int secondWordStartPosition = textReceived.indexOf(" ", firstSpacePosition + 1);
		return textReceived.substring(firstSpacePosition + 1, secondWordStartPosition);
	}

	private boolean proxyServertoClient(byte[] clientRequest) {

		FileOutputStream fileWriter = null;
		Socket serverSocket = null;
		InputStream inFromServer;
		OutputStream outToServer;

		// Create Buffered output stream to write to cached copy of file
		String fileName = "cached/" + generateRandomFileName() + ".dat";

		// to handle binary content, byte is used
		byte[] serverReply = new byte[4096];


		/**
		 * To do
		 * (1) Create a socket to connect to the web server (default port 80)
		 * (2) Send client's request (clientRequest) to the web server, you may want to use flush() after writing.
		 * (3) Use a while loop to read all responses from web server and send back to client
		 * (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
		 * (5) close file, and sockets.
		 */
		return true;
	}



	// Sends the cached content stored in the cache file to the client
	private void sendCachedInfoToClient(String fileName) {

		try {

			byte[] bytes = Files.readAllBytes(Paths.get(fileName));

			outToClient.write(bytes);
			outToClient.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			if (clientSocket != null) {
				clientSocket.close();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}


	// Generates a random file name
	public String generateRandomFileName() {

		String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
		SecureRandom RANDOM = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 10; ++i) {
			sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}

}