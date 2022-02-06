package com.csci846;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

public class RequestHandler extends Thread {

	Socket clientSocket;
	InputStream inFromClient;
	OutputStream outToClient;

	private final ProxyServer server;

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
		try {
			HttpRequest httpRequest = httpRequestBuilder();
			String logRequest = clientSocket.getInetAddress().getHostAddress() + " " + httpRequest.getUrl();

			if(httpRequest.getMethod().equals("GET")){
				if(server.cacheExistsForURL(httpRequest.getUrl())){
					System.out.println("Calling cache");
					sendCachedInfoToClient(server.getCache(httpRequest.getUrl()));
				}
				else{
					proxyServertoClient(httpRequest);
				}
			}

			server.writeLog(logRequest);
			clientSocket.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private HttpRequest httpRequestBuilder() throws IOException {
		BufferedReader proxyToClientBufferedReader = new BufferedReader(new InputStreamReader(inFromClient));
		StringBuilder requestBuilder = new StringBuilder();
		String line;

		while (!(line = proxyToClientBufferedReader.readLine()).isBlank()) {
			requestBuilder.append(line).append("\r\n");
		}

		return new HttpRequest(requestBuilder);
	}

	private void proxyServertoClient(HttpRequest clientRequest) {

		String fileName = "cached/" + generateRandomFileName() + ".dat";

		byte[] serverReply = new byte[4096];

		try (Socket socketToWebServer = new Socket(clientRequest.getHost(), 80)) {
			socketToWebServer.setSoTimeout(2000);
			System.out.println("New Outbound client connected");

			sendRequestFromClientToServer(socketToWebServer, clientRequest);

			InputStream inFromServer = socketToWebServer.getInputStream();
			serverReply = inFromServer.readAllBytes();

			forwardRequestFromServerToClient(serverReply);


		} catch (IOException ex) {
			ex.printStackTrace();
		}

		writeServerToClientResponseToCache(serverReply,clientRequest.getUrl(),fileName);

	}

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

	public String generateRandomFileName() {

		String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
		SecureRandom RANDOM = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 10; ++i) {
			sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}

	private void sendRequestFromClientToServer(final Socket socketToWebServer, final HttpRequest clientRequest) throws IOException {
		OutputStream outputStream = socketToWebServer.getOutputStream();
		BufferedWriter proxyToWebServerBufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

		proxyToWebServerBufferedWriter.write(clientRequest.getRequest());
		proxyToWebServerBufferedWriter.flush();

	}

	private void forwardRequestFromServerToClient(final byte[] serverReply) throws IOException {
		outToClient.write(serverReply, 0, serverReply.length);
		outToClient.flush();
	}

	private void writeServerToClientResponseToCache(byte[] data, String url, String fileName){
		try (FileOutputStream stream = new FileOutputStream(fileName)) {
			stream.write(data);
			stream.flush();
			server.putCache(url,fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




}