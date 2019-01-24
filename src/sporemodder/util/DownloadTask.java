/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.util;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

public final class DownloadTask extends ResumableTask<File> {

	// Max size of download buffer.
	private static final int MAX_BUFFER_SIZE = 1024;

	public static enum Status {
		DOWNLOADING, PAUSED, COMPLETE, CANCELLED, ERROR
	};

	private URL url; // download URL
	private int size; // size of download in bytes
	private int downloaded; // number of bytes downloaded

	/** The current status of the download. */
	private final ReadOnlyObjectWrapper<Status> status = new ReadOnlyObjectWrapper<Status>();

	// Constructor for Download.
	public DownloadTask(URL url) {
		this.url = url;
		size = -1;
		downloaded = 0;
		status.set(Status.PAUSED);

		// Begin the download.
		download();
	}

	// Get this download's URL.
	public String getUrl() {
		return url.toString();
	}

	// Get this download's size.
	public int getSize() {
		return size;
	}

	public int getDownloadedBytes() {
		return downloaded;
	}

	// Get this download's status.
	public ReadOnlyObjectProperty<Status> statusProperty() {
		return status.getReadOnlyProperty();
	}

	// Pause this download.
	@Override public void pause() {
		status.set(Status.PAUSED);
	}

	// Resume this download.
	@Override public void resume() {
		status.set(Status.DOWNLOADING);
	}

	// Mark this download as having an error.
	private void error() {
		status.set(Status.ERROR);
	}

	// Start or resume downloading.
	private void download() {
		Thread thread = new Thread(this);
		thread.start();
	}

	// Get file name portion of URL.
	private String getFileName(URL url) {
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	public File call() {
		status.set(Status.DOWNLOADING);

		RandomAccessFile file = null;
		InputStream stream = null;
		File tempFile = null;

		try {
			// Open connection to URL.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Specify what portion of file to download.
			connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

			// Connect to server.
			connection.connect();

			// Make sure response code is in the 200 range.
			if (connection.getResponseCode() / 100 != 2) {
				error();
			}

			// Check for valid content length.
			int contentLength = connection.getContentLength();
			if (contentLength < 1) {
				error();
			}

			/*
			 * Set the size for this download if it hasn't been already set.
			 */
			if (size == -1) {
				size = contentLength;
				updateProgress(downloaded, size);
			}


			tempFile = File.createTempFile(getFileName(url), ".tmp");
			file = new RandomAccessFile(tempFile, "rw");
			file.seek(downloaded);

			stream = connection.getInputStream();
			while (status.get() == Status.DOWNLOADING) {
				/*
				 * Size buffer according to how much of the file is left to download.
				 */
				byte buffer[];
				if (size - downloaded > MAX_BUFFER_SIZE) {
					buffer = new byte[MAX_BUFFER_SIZE];
				} else {
					buffer = new byte[size - downloaded];
				}

				// Read from server into buffer.
				int read = stream.read(buffer);
				if (read == -1)
					break;

				// Write buffer to file.
				file.write(buffer, 0, read);
				// output.write(buffer, 0, read);
				downloaded += read;
				updateProgress(downloaded, size);
			}

			file.seek(0);

			/*
			 * Change status to complete if this point was reached because downloading has
			 * finished.
			 */
			if (status.get() == Status.DOWNLOADING) {
				status.set(Status.COMPLETE);

				updateProgress(size, size);
			}
			
		} catch (Exception e) {
			error();
		} finally {
			try {
				if (file != null) file.close();
				if (stream != null) stream.close();
				
			} catch (Exception e) {
				return null;
			}
		}
		
		return tempFile;
	}

}
