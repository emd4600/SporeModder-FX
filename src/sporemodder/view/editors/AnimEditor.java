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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import sporemodder.GameManager;
import sporemodder.HashManager;
import sporemodder.UIManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.anim.AnimConverter;
import sporemodder.file.anim.SPAnimation;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.dbpf.DebugInformation;
import sporemodder.file.tlsa.TLSAAnimation;
import sporemodder.file.tlsa.TLSAAnimationChoice;
import sporemodder.file.tlsa.TLSAAnimationGroup;
import sporemodder.file.tlsa.TLSAConverter;
import sporemodder.file.tlsa.TLSAUnit;
import sporemodder.util.ProjectItem;
import sporemodder.util.SporeGame;
import sporemodder.view.dialogs.ProgramSettingsUI;

public class AnimEditor extends AnimTextEditor {
	
	private static final String PACKAGE_NAME = "_SporeModderFX_AnimEditor";

	@Override protected File getFile(ProjectItem item) {
		// We want to return _anim_editor.anim.anim_t (create it if it does not exist).
		File folder = item.getFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		File mainFile = new File(folder, "_SporeModder_AnimEditor.animation.anim_t");
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
	
	private File getAnimFile(ProjectItem item) {
		// We want to return _anim_editor.animation (create it if it does not exist).
		File folder = item.getFile();
		
		return new File(folder, "_SporeModder_AnimEditor.animation");
	}
	
	@Override protected void saveData() throws Exception {
		// We want to save the file but also the .animation
		super.saveData();
		
		SPAnimation anim = new SPAnimation();

		ArgScriptStream<SPAnimation> stream = anim.generateStream();
		
		stream.process(getFile(getItem()));
		
		if (stream.getErrors().isEmpty()) {
			try (FileStream out = new FileStream(getAnimFile(getItem()), "rw")) {
				anim.write(out, "SporeModder FX\\Anim Editor\\_SporeModder_AnimEditor.animation", HashManager.get().getFileHash("_SporeModder_AnimEditor"));
			}
		}
	}
	
	@Override public void loadFile(ProjectItem item) throws IOException {
		super.loadFile(item);
		
		if (item != null) {
			SporeGame game = GameManager.get().getGalacticAdventures();
			
			if (game == null) {
				Alert alert = new Alert(AlertType.WARNING, "For the Anim Editor to work, you must set the Galatic Adventures path in the program settings.", ButtonType.OK);
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
						DebugInformation debugInfo = new DebugInformation(PACKAGE_NAME, getFile().getParentFile().getAbsolutePath());
						
						ResourceKey key = new ResourceKey();
						key.setGroupID(0);
						key.setInstanceID("_SporeModder_AnimEditor");
						key.setTypeID(AnimConverter.TYPE_ID);
						
						debugInfo.addFile("_SporeModder_AnimEditor.animation", key);
						
						// write an empty animation, just so that it exists
						SPAnimation anim = new SPAnimation();
						packer.writeFile(key, 
								stream -> anim.write(stream, "SporeModder FX\\Anim Editor\\_SporeModder_AnimEditor.animation", 
										HashManager.get().getFileHash("_SporeModder_AnimEditor")));
						
						debugInfo.saveInformation(packer);
						
						// We need to write a TLSA that points to our animation as well
						writeTLSA(packer);
					}
					catch (Exception e) {
						e.printStackTrace();
						UIManager.get().showErrorDialog(e, "Could not set up the anim editor.", true);
						
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
	
	private void writeTLSA(DBPFPacker packer) throws IOException {
		TLSAUnit tlsa = new TLSAUnit();
		
		TLSAAnimationGroup group = new TLSAAnimationGroup();
		group.name = "_anim_editor";
		group.id = HashManager.get().getFileHash("_anim_editor");
		group.endMode = 0;
		tlsa.getGroups().add(group);
		
		TLSAAnimation anim = new TLSAAnimation();
		anim.description = "SporeModder FX\\Anim Editor\\_SporeModder_AnimEditor.animation";
		anim.id = HashManager.get().getFileHash("_SporeModder_AnimEditor");
		
		TLSAAnimationChoice choice = new TLSAAnimationChoice();
		choice.animations.add(anim);
		group.choices.add(choice);
		
		ResourceKey key = new ResourceKey();
		key.setGroupID(0x00006F14);
		key.setInstanceID("_SporeModder_AnimEditor");
		key.setTypeID(TLSAConverter.TYPE_ID);
		
		packer.writeFile(key, stream -> tlsa.write(stream));
	}
}
