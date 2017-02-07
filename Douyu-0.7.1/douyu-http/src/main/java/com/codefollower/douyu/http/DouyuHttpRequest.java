package com.codefollower.douyu.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;

import douyu.http.UploadedFile;

public class DouyuHttpRequest extends DefaultHttpRequest implements douyu.http.HttpRequest {
	private Map<String, List<String>> params;
	private Map<String, List<UploadedFile>> uploadedFiles;

	protected HashMap<String, Object> attributes = new HashMap<String, Object>();

	private String queryString;
	private int serverPort = -1;
	private String serverName;

	String protocol;

	String requestURI;
	String remoteAddr;
	String remoteHost;
	String localName;
	int localPort;
	int remotePort;

	String remoteUser;
	String authType;
	String scheme;

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	boolean isSSL;

	int contentLength = -1;
	String contentType;
	String instanceId;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public boolean isSSL() {
		return isSSL;
	}

	public void setSSL(boolean isSSL) {
		this.isSSL = isSSL;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public DouyuHttpRequest() {
		super(HttpVersion.HTTP_1_1, com.codefollower.douyu.http.HttpMethod.GET, "/");
	}

	public DouyuHttpRequest(HttpVersion httpVersion, com.codefollower.douyu.http.HttpMethod method, String uri) throws Exception {
		super(httpVersion, method, uri);
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
		Map<String, List<String>> params = queryStringDecoder.getParameters();
		this.params = new HashMap<String, List<String>>();
		this.params.putAll(params);
	}

	@Override
	public douyu.http.HttpMethod getHttpMethod() {
		return douyu.http.HttpMethod.valueOf(getMethod().getName());
	}

	@Override
	public String getProtocol() {
		return getProtocolVersion().getText();
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public String getParameter(String name) {
		List<String> values = params.get(name);
		if (values == null || values.isEmpty())
			return null;
		else
			return values.get(0);
	}

	@Override
	public String[] getParameterValues(String name) {
		List<String> values = params.get(name);
		if (values == null || values.isEmpty())
			return null;
		else
			return values.toArray(new String[values.size()]);
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	@Override
	public String getRequestURI() {
		if (requestURI == null)
			return getUri();
		return requestURI;
	}

	@Override
	public UploadedFile getUploadedFile(String name) {
		List<UploadedFile> values = uploadedFiles.get(name);
		if (values == null || values.isEmpty())
			return null;
		else
			return values.get(0);
	}

	@Override
	public UploadedFile[] getUploadedFiles() {
		if (uploadedFiles == null || uploadedFiles.isEmpty())
			return null;
		else {
			List<UploadedFile> values = new ArrayList<UploadedFile>(uploadedFiles.size());
			for (List<UploadedFile> list : uploadedFiles.values()) {
				if (list != null) {
					values.addAll(list);
				}
			}
			return values.toArray(new UploadedFile[values.size()]);
			// return uploadedFiles.values().toArray(new
			// UploadedFile[uploadedFiles.size()]);
		}

	}

	public void addParameter(String name, String value) {
		List<String> values = params.get(name);
		if (values == null)
			values = new ArrayList<String>(1);

		values.add(value);
		params.put(name, values);
	}

	public void addUploadedFile(String name, UploadedFile file) {
		if (uploadedFiles == null)
			uploadedFiles = new HashMap<String, List<UploadedFile>>(1);
		List<UploadedFile> values = uploadedFiles.get(name);
		if (values == null)
			values = new ArrayList<UploadedFile>(1);

		values.add(file);
		uploadedFiles.put(name, values);
	}

	// 覆盖父类的content，父类的setContent不能放chunk，但是解析ajp协议时必须把所有chunk组装成一个整体
	private ChannelBuffer content = ChannelBuffers.EMPTY_BUFFER;

	@Override
	public void setContent(ChannelBuffer content) {
		this.content = content;
	}

	@Override
	public ChannelBuffer getContent() {
		return content;
	}
}
