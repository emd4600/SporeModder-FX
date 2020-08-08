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

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import sporemodder.util.DownloadTask;
import sporemodder.util.VersionInfo;
import sporemodder.view.dialogs.ProgressDialogUI;

public class UpdateManager {

	/**
     * Pattern to present day in ISO-8601.
     */
    public static final String FORMAT_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public static final SimpleDateFormat FORMAT_ISO = new SimpleDateFormat(FORMAT_ISO_PATTERN);
    
    /**
     * The time zone we're in.
     */
    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
    
    public final VersionInfo versionInfo = new VersionInfo(2, 1, 18, null);
    
    public static UpdateManager get() {
    	return MainApp.get().getUpdateManager();
    }
    
    public VersionInfo getVersionInfo() {
    	return versionInfo;
    }
    
    /**
     * Checks whether there is any update available, and if it is, it asks the user whether to download it.
     * Returns true if he program can continue, false if the program should abort and execute the update.
     * @return
     */
    public boolean checkUpdate() {
    	try {
			JSONObject githubRelease = getLastRelease("Emd4600", "SporeModder-FX");
			
			VersionInfo newVersion = VersionInfo.fromString(githubRelease.getString("tag_name"));
			if (newVersion.isGreaterThan(versionInfo)) {
				return showUpdateDialog(githubRelease);
			}
			
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
    	return true;
    }
    
    private boolean showUpdateDialog(JSONObject release) {
    	Dialog<ButtonType> dialog = new Dialog<ButtonType>();
    	dialog.setTitle("Update available");
    	
    	DialogPane dialogPane = new DialogPane() {
    		@Override
    		protected Node createButtonBar() {
    			ButtonBar buttonBar = (ButtonBar) super.createButtonBar();
    			//buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
    			
    			Button button = new Button("View changelog");
    			button.setOnAction(event -> {
    				try {
						Desktop.getDesktop().browse(new URI((String) release.get("html_url")));
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
    			});
    			
    			buttonBar.getButtons().add(button);

    			return buttonBar;
    		}
    	};
    	dialog.setDialogPane(dialogPane);
    	
    	Hyperlink hyperlink = new Hyperlink(release.getString("html_url"));
    	hyperlink.setOnAction(event -> {
    		try {
				Desktop.getDesktop().browse(new URI(release.getString("html_url")));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
    	});
    			
    	VBox pane = new VBox();
    	pane.getChildren().add(new Label("A new update (\"" + release.get("tag_name") + "\") has been released. Do you want to download it?"));
    	pane.getChildren().add(new Label("You can view the changelog in the following link:"));
    	pane.getChildren().add(hyperlink);
    	
    	dialog.getDialogPane().setContent(pane);
    	dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
    	
    	AtomicBoolean closeProgram = new AtomicBoolean(false);
    	
    	if (UIManager.get().showDialog(dialog, false).orElse(ButtonType.NO) == ButtonType.YES) {
    		JSONArray assets = release.getJSONArray("assets");
    		JSONObject asset = null;
    		int count = assets.length();
    		for (int i = 0; i < count; ++i) {
    			JSONObject object = assets.getJSONObject(i);
    			if ("SporeModderFX.Updater.jar".equals(object.getString("name"))) {
    				asset = object;
    				break;
    			}
    		}
    		
    		try {
				DownloadTask task = new DownloadTask(new URL(asset.getString("browser_download_url")));
				
				ProgressDialogUI progressUI = UIManager.get().loadUI("dialogs/ProgressDialogUI");
				progressUI.setText("");
				progressUI.getProgressBar().progressProperty().bind(task.progressProperty());
	    		Dialog<ButtonType> progressDialog = progressUI.createDialog(task);
	    		progressDialog.setTitle("Downloading updater");
	    		
	    		progressUI.setOnSucceeded(() -> {
	    			if (!task.isCancelled()) UIManager.get().tryAction(() -> {
						Runtime.getRuntime().exec("java -jar \"" + task.get().getAbsolutePath() + "\" \"" + PathManager.get().getProgramFolder().getAbsolutePath() + '"');
		    			
		    			closeProgram.set(true);
	    			}, "Updater could not be executed.");
	    		});
	    		
	    		UIManager.get().showDialog(progressDialog, false);
			} 
    		catch (MalformedURLException | JSONException e) {
				e.printStackTrace();
			}
    	}
    	
    	UIManager.get().setOverlay(false);
    	
    	return !closeProgram.get();
    }

	private static String executeGet(String request) throws IOException {
		
		HttpsURLConnection connection = null;
		
		try
		{
			URL url = new URL(NetworkUtils.GITHUB_API_URL + request);
			
			connection = (HttpsURLConnection) url.openConnection();
			// github api uses HTTPS
			connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
			
			int responseCode = connection.getResponseCode();
//			System.out.println("\nSending 'GET' request to URL : " + url);
//			System.out.println("Response Code : " + responseCode);
			
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
	
	public JSONObject getLastRelease(String owner, String repo) throws IOException, ParseException {
		return new JSONObject(new JSONTokener(executeGet("/repos/" + owner + "/" + repo + "/releases/latest")));
	}
}
