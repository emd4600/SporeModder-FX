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

public class VersionInfo {
	public static final String ALPHA = "alpha";
	public static final String BETA = "beta";
	
	private int majorVersion;
	private int minorVersion;
	private int buildVersion;
	private String buildTag;
	
	public VersionInfo(int majorVersion, int minorVersion,
			int buildVersion, String buildTag) {
		this.minorVersion = minorVersion;
		this.majorVersion = majorVersion;
		this.buildVersion = buildVersion;
		this.buildTag = buildTag;
	}
	
	@Override
	public String toString() {
		return "v" + majorVersion + "." + minorVersion + "." + buildVersion + (buildTag == null ? "" : ("-" + buildTag));
	}
	
	/** 
	 * Returns true if this version is greater than the given version info.
	 * @param lastRelease
	 * @return
	 */
	public boolean isGreaterThan(VersionInfo other) {
		if (majorVersion < other.majorVersion) {
			return false;
		}
		else if (majorVersion > other.majorVersion) {
			return true;
		}
		else {
			if (minorVersion < other.minorVersion) {
				return false;
			}
			else if (minorVersion > other.minorVersion) {
				return true;
			}
			else {
				return buildVersion > other.buildVersion;
			}
		}
	}
	
	public static VersionInfo fromString(String str) {
		int minorVersion = 0;
		int majorVersion = 0;
		int buildVersion = 0;
		String buildTag = null;
		
		if (str.startsWith("v")) {
			str = str.substring(1);
		}
		
		String[] splits = str.split("-");
		if (splits.length > 1) {
			buildTag = splits[1];
		}
		String[] versions = splits[0].split("\\.");
		
		majorVersion = Integer.parseInt(versions[0]);
		
		if (versions.length > 1) {
			minorVersion = Integer.parseInt(versions[1]);
			
			if (versions.length > 2) {
				buildVersion = Integer.parseInt(versions[2]);
			}
		}
		
		return new VersionInfo(majorVersion, minorVersion, buildVersion, buildTag);
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public int getBuildVersion() {
		return buildVersion;
	}

	public String getBuildTag() {
		return buildTag;
	}
	
}
