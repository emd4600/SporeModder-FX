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
package sporemodder.view.editors;

import java.io.File;
import java.io.IOException;

import emord.filestructures.FileStream;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import sporemodder.GameManager;
import sporemodder.UIManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.dbpf.DebugInformation;
import sporemodder.file.effects.EffectDirectory;
import sporemodder.file.effects.EffectUnit;
import sporemodder.file.effects.EffectsConverter;
import sporemodder.util.ProjectItem;
import sporemodder.util.SporeGame;
import sporemodder.view.dialogs.ProgramSettingsUI;
import sporemodder.view.editors.PfxEditor;

public class EffectEditor extends PfxEditor {
	
	private static final String PACKAGE_NAME = "_SporeModderFX_EffectEditor";

	@Override protected File getFile(ProjectItem item) {
		// We want to return main.pfx (create it if it does not exist).
		File folder = item.getFile();
		if (folder.exists()) {
			folder.mkdirs();
		}
		
		File mainFile = new File(folder, "main.pfx");
		if (!mainFile.exists()) {
			try {
				mainFile.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return mainFile;
	}
	
	private File getEffdirFile(ProjectItem item) {
		// We want to return main.effdir (create it if it does not exist).
		File folder = item.getFile();
		
		return new File(folder, "main.effdir");
	}
	
	@Override protected void saveData() throws Exception {
		// We want to save the file but also the .effdir
		super.saveData();
		
		EffectDirectory directory = new EffectDirectory();
		EffectUnit unit = new EffectUnit(directory);

		ArgScriptStream<EffectUnit> stream = unit.generateStream();
		
		stream.process(getFile(getItem()));
		
		if (stream.getErrors().isEmpty()) {
			
			try (FileStream out = new FileStream(getEffdirFile(getItem()), "rw")) {
				
				directory.addEffectUnit(unit);
				directory.write(out);
			}
		}
	}
	
	@Override public void loadFile(ProjectItem item) throws IOException {
		super.loadFile(item);
		
		if (item != null) {
			SporeGame game = GameManager.get().getGalacticAdventures();
			
			if (game == null) {
				Alert alert = new Alert(AlertType.WARNING, "For the Effect Editor to work, you must set the Galatic Adventures path in the program settings.", ButtonType.OK);
				UIManager.get().setOverlay(true);
				UIManager.get().showDialog(alert);
				ProgramSettingsUI.show();
				UIManager.get().setOverlay(false);
			}
			
			game = GameManager.get().getGame();
			if (game != null) {
				File packageFile = new File(game.getDataFolder(), PACKAGE_NAME + ".package");
				
				if (!packageFile.exists()) {
					try (DBPFPacker packer = new DBPFPacker(packageFile)) {
						ResourceKey key = new ResourceKey();
						
						key.setGroupID("_SporeModder_EffectEditor");
						key.setInstanceID("main");
						key.setTypeID(EffectsConverter.TYPE_ID);
						
						DebugInformation debugInfo = new DebugInformation(PACKAGE_NAME, getFile().getParentFile().getAbsolutePath());
						debugInfo.addFile("main.effdir", key);
						debugInfo.saveInformation(packer);
						
						EffectDirectory effdir = new EffectDirectory();
						packer.writeFile(key, stream -> effdir.write(stream));
					}
					catch (Exception e) {
						e.printStackTrace();
						UIManager.get().showErrorDialog(e, "Could not set up the effect editor.", true);
						
						// Remove leftover file so we can try again in the future.
						try {
							packageFile.delete();
						}
						catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			}
		}
	}
}
