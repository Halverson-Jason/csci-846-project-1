package com.csci846;

public class HttpRequest {

	private String method;
	private String host;
	private String url;
	private String request;

	public HttpRequest(final StringBuilder requestBuilder) {

		// Must add on this to be a valid HTTP1.1 Request
		requestBuilder.append("\r\n");

		this.request = requestBuilder.toString();
		this.method = parseHttpRequest(request);
		this.url = parseUrl(request);
		this.host = parseHost(request);

	}

	private String parseHttpRequest(final String textReceived) {
		String[] requestsLines = textReceived.split("\r\n");
		String[] requestLine = requestsLines[0].split(" ");
		return requestLine[0];
	}

	private String parseUrl(final String textReceived) {
		String[] requestsLines = textReceived.split("\r\n");
		String[] requestLine = requestsLines[0].split(" ");
		return requestLine[1];
	}

	private String parseHost(final String textReceived){
		String[] requestsLines = textReceived.split("\r\n");
		return requestsLines[1].split(" ")[1];
	}

	public String getMethod() {
		return method;
	}

	public String getHost() {
		return host;
	}

	public String getUrl() {
		return url;
	}

	public String getRequest() {
		return request;
	}
}
