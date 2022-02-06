package com.csci846;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyServer {
	Map<String, String> cache;

	static String logFileName = "log.txt";

	public static void main(String[] args) {

		verifyRequiredArgs(args);
		createLogFile();
		createCacheDir();

		new ProxyServer().startServer(Integer.parseInt(args[0]));
	}

	void startServer(int proxyPort) {

		cache = new ConcurrentHashMap<>();

		try (ServerSocket proxySocket = new ServerSocket(proxyPort)) {

			System.out.println("Server is listening on port " + proxyPort);

			while (true) {
				Socket socket = proxySocket.accept();
				System.out.println("New client connected");

				new RequestHandler(socket, this).start();
			}

		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public String getCache(String hashcode) {
		return cache.get(hashcode);
	}

	public void putCache(String hashcode, String fileName) {
		cache.put(hashcode, fileName);
	}

	public synchronized void writeLog(String info) {

		Path path = Paths.get(logFileName);

		String timeStamp = new SimpleDateFormat("MMM d yyyy HH:mm:ss").format(new Date());
		try{
			Files.write(path, (timeStamp + " " + info + "\n").getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private static void verifyRequiredArgs(final String[] args) {
		if(args.length < 1){
			System.out.println("Please input a port number (0-65353)");
			System.exit(1);
		}
	}

	private static void createLogFile() {
		try{
			File file = new File(logFileName);
			file.createNewFile();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private static void createCacheDir() {
		File cacheDir = new File("cached");
		if (!cacheDir.exists() || (cacheDir.exists() && !cacheDir.isDirectory())) {
			cacheDir.mkdirs();
		}
	}
}