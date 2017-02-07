package com.codefollower.douyu.http;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


import douyu.http.UploadedFile;

public class DouyuUploadedFile implements UploadedFile {
	private FileUpload fileUpload;

	public DouyuUploadedFile(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}

	@Override
	public byte[] getBytes() {
		try {
			return fileUpload.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getContent() {
		try {
			return fileUpload.getString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getContent(String encoding) {
		try {
			return fileUpload.getString(Charset.forName(encoding));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getContentType() {
		return fileUpload.getContentType();
	}

	@Override
	public String getFullName() {
		return fileUpload.getFilename();
	}

	@Override
	public String getPathName() {
		return fileUpload.getFilename();
	}

	@Override
	public String getSimpleName() {
		return fileUpload.getFilename();
	}

	@Override
	public long getSize() {
		try {
			return fileUpload.get().length;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void saveTo(String file) throws Exception {
		fileUpload.renameTo(new File(file));

	}

	@Override
	public void saveTo(File file) throws Exception {
		fileUpload.renameTo(file);
	}

}
