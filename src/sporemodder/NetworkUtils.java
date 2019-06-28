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
package sporemodder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONObject;
import org.json.JSONTokener;

public class NetworkUtils {
	
	/** The URL to the GitHub API services. */
	public static final String GITHUB_API_URL = "https://api.github.com";
	
	public static long downloadFile(URL url, File output) throws IOException {
		try (ReadableByteChannel input = Channels.newChannel(url.openStream());
				FileOutputStream outStream = new FileOutputStream(output)) {
			return outStream.getChannel().transferFrom(input, 0, Integer.MAX_VALUE);
		}
	}

	/**
	 * Executes a GET request into the GitHub API
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String getGitHubAPI(String request) throws IOException {
		
		HttpsURLConnection connection = null;
		
		try
		{
			URL url = new URL(GITHUB_API_URL + request);
			
			connection = (HttpsURLConnection) url.openConnection();
			// github api uses HTTPS
			connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
			
			int responseCode = connection.getResponseCode();
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			// Make sure response code is in the 200 range.
	        if (responseCode / 100 != 2) {
	            throw new IOException(response.toString());
	        }

			//print result
			return response.toString();
		}
		finally 
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}
	
	public static JSONObject getJSON(String contents) {
		return new JSONObject(new JSONTokener(contents));
	}
	
	// https://stackoverflow.com/a/52225761/3779214
	public static String getUrl(String aUrl) throws MalformedURLException, IOException {
	    String urlData = "";
	    URL urlObj = new URL(aUrl);
	    URLConnection conn = urlObj.openConnection();
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) 
	    {
	        urlData = reader.lines().collect(Collectors.joining("\n"));
	    }
	    return urlData;
	}
}
