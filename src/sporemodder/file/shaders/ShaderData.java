package sporemodder.file.shaders;

import java.util.HashMap;
import java.util.Map;

import sporemodder.HashManager;

public class ShaderData {

	private static final Map<Integer, String> indexToName = new HashMap<>();
	private static final Map<String, Integer> nameToIndex = new HashMap<>();
	
	private static final Map<Integer, Integer> flags = new HashMap<>();

	private static final int FLAG_MODEL_SHADER_DATA = 8;
	private static final int FLAG_MODEL_TRANSFORM = 1;
	private static final int FLAG_WORLD_TRANSFORM = 0x4000;
	private static final int FLAG_MATERIAL_COLOR = 0x10;
	private static final int FLAG_AMBIENT_COLOR = 0x20;
	private static final int FLAG_LIGHT_DATA = 0x3FC0;
	
	static
	{
		flags.put(0x3, FLAG_MODEL_SHADER_DATA);
		flags.put(0x4, FLAG_MODEL_SHADER_DATA);
		flags.put(0x202, FLAG_MODEL_SHADER_DATA);
		flags.put(0x203, FLAG_MODEL_SHADER_DATA);
		flags.put(0x204, FLAG_MODEL_SHADER_DATA);
		flags.put(0x205, FLAG_MODEL_SHADER_DATA);
		flags.put(0x206, FLAG_MODEL_SHADER_DATA);
		flags.put(0x20A, FLAG_MODEL_SHADER_DATA);
		flags.put(0x20B, FLAG_MODEL_SHADER_DATA);
		flags.put(0x20C, FLAG_MODEL_SHADER_DATA);
		flags.put(0x20F, FLAG_MODEL_SHADER_DATA);
		flags.put(0x210, FLAG_MODEL_SHADER_DATA);
		flags.put(0x211, FLAG_MODEL_SHADER_DATA);
		flags.put(0x212, FLAG_MODEL_SHADER_DATA);
		flags.put(0x213, FLAG_MODEL_SHADER_DATA);
		flags.put(0x219, FLAG_MODEL_SHADER_DATA);
		flags.put(0x21A, FLAG_MODEL_SHADER_DATA);
		flags.put(0x21E, FLAG_MODEL_SHADER_DATA);
		flags.put(0x220, FLAG_MODEL_SHADER_DATA);
		flags.put(0x223, FLAG_MODEL_SHADER_DATA);
		flags.put(0x22E, FLAG_MODEL_SHADER_DATA);
		flags.put(0x22F, FLAG_MODEL_SHADER_DATA);
		flags.put(0x230, FLAG_MODEL_SHADER_DATA);
		flags.put(0x231, FLAG_MODEL_SHADER_DATA);
		flags.put(0x233, FLAG_MODEL_SHADER_DATA);
		flags.put(0x234, FLAG_MODEL_SHADER_DATA);
		flags.put(0x235, FLAG_MODEL_SHADER_DATA);
		flags.put(0x236, FLAG_MODEL_SHADER_DATA);
		flags.put(0x23C, FLAG_MODEL_SHADER_DATA);
		flags.put(0x23E, FLAG_MODEL_SHADER_DATA);
		flags.put(0x23D, FLAG_MODEL_SHADER_DATA);
		flags.put(0x241, FLAG_MODEL_SHADER_DATA);
		flags.put(0x242, FLAG_MODEL_SHADER_DATA);
		flags.put(0x248, FLAG_MODEL_SHADER_DATA);
		flags.put(0x24A, FLAG_MODEL_SHADER_DATA);
		flags.put(0x24B, FLAG_MODEL_SHADER_DATA);
		flags.put(0x24C, FLAG_MODEL_SHADER_DATA);
		flags.put(0x24D, FLAG_MODEL_SHADER_DATA);
		flags.put(0x250, FLAG_MODEL_SHADER_DATA);
		flags.put(0x251, FLAG_MODEL_SHADER_DATA);
		flags.put(0x252, FLAG_MODEL_SHADER_DATA);
		flags.put(0x255, FLAG_MODEL_SHADER_DATA);
		flags.put(0x256, FLAG_MODEL_SHADER_DATA);
		flags.put(0x301, FLAG_MODEL_SHADER_DATA);
		flags.put(0x304, FLAG_MODEL_SHADER_DATA);
		flags.put(0x305, FLAG_MODEL_SHADER_DATA);
		
		flags.put(0x22, FLAG_MATERIAL_COLOR);
		flags.put(0x23, FLAG_AMBIENT_COLOR);
		
		flags.put(0x21b, FLAG_MODEL_TRANSFORM | FLAG_MODEL_SHADER_DATA);
		flags.put(0x254, FLAG_MODEL_TRANSFORM | FLAG_MODEL_SHADER_DATA);
		flags.put(0x225, FLAG_WORLD_TRANSFORM | FLAG_MODEL_TRANSFORM | FLAG_MODEL_SHADER_DATA);
		flags.put(0x006, FLAG_WORLD_TRANSFORM | FLAG_MODEL_TRANSFORM);
		flags.put(0x007, FLAG_WORLD_TRANSFORM | FLAG_MODEL_TRANSFORM);
		flags.put(0x228, FLAG_WORLD_TRANSFORM | FLAG_MODEL_TRANSFORM);
		flags.put(0x229, FLAG_WORLD_TRANSFORM | FLAG_MODEL_TRANSFORM);
		flags.put(0x008, FLAG_MODEL_TRANSFORM);
		flags.put(0x02A, FLAG_MODEL_TRANSFORM);
		flags.put(0x00C, FLAG_WORLD_TRANSFORM);
		flags.put(0x00D, FLAG_WORLD_TRANSFORM);
		flags.put(0x00E, FLAG_WORLD_TRANSFORM);
		flags.put(0x00F, FLAG_WORLD_TRANSFORM);
		flags.put(0x010, FLAG_WORLD_TRANSFORM);
		flags.put(0x011, FLAG_WORLD_TRANSFORM);
		flags.put(0x012, FLAG_WORLD_TRANSFORM);
		flags.put(0x01F, FLAG_WORLD_TRANSFORM);
		flags.put(0x021, FLAG_WORLD_TRANSFORM);
		flags.put(0x222, FLAG_WORLD_TRANSFORM);
		
		flags.put(0x014, FLAG_LIGHT_DATA);
		flags.put(0x019, FLAG_LIGHT_DATA);
		flags.put(0x214, FLAG_LIGHT_DATA);
		flags.put(0x215, FLAG_LIGHT_DATA);
		
		flags.put(0x27, 0xffffd);
		flags.put(0x28, 0xffffd);
		flags.put(0x21f, 0xffffd);
		flags.put(0x246, 0xffffd);
		
		flags.put(0x25, 0x10000 | 0x40000);
		flags.put(0x24, 0x20000);
	}
	
	static
	{
		nameToIndex.put("skinWeights", 0x003);
		nameToIndex.put("skinBones", 0x004);
		
		nameToIndex.put("modelToClip", 0x006);
		nameToIndex.put("modelToCamera", 0x007);
		nameToIndex.put("modelToWorld", 0x008);
		
		nameToIndex.put("worldToClip", 0x00C);
		nameToIndex.put("cameraToWorld", 0x00D);
		nameToIndex.put("worldToCamera", 0x00E);
		nameToIndex.put("worldToClipTranspose", 0x00F);
		nameToIndex.put("cameraToWorldTranspose", 0x010);
		nameToIndex.put("worldToCameraTranspose", 0x011);
		nameToIndex.put("cameraToClip", 0x012);
		
		nameToIndex.put("lightPosModel", 0x014);
		
		nameToIndex.put("lightDirCamera", 0x019);  // float3
		
		nameToIndex.put("worldCameraPosition", 0x01F);
		
		nameToIndex.put("worldCameraDirection", 0x021);
		nameToIndex.put("materialColor", 0x022);
		nameToIndex.put("ambient", 0x023);
		
		nameToIndex.put("time", 0x027);
		nameToIndex.put("pulse", 0x028);
		
		nameToIndex.put("worldToModel", 0x02A);

		// 0x201 shaderRenderType
		nameToIndex.put("objectTypeColor", 0x202);
		nameToIndex.put("frameInfo", 0x203);
		nameToIndex.put("screenInfo", 0x204);
		nameToIndex.put("mNoiseScale", 0x205);  // struct { float4 mFrequencyMag; float mTime; } mNoiseScale;
		nameToIndex.put("customParams", 0x206);
		
		nameToIndex.put("geomToRTT", 0x20A);
		nameToIndex.put("geomToRTTViewTrans", 0x20B);
		nameToIndex.put("tintParams", 0x20C);  // float4
		
		nameToIndex.put("region", 0x20F);  // float4
		nameToIndex.put("materialParams", 0x210);  // if 5, applyMaterialAlpha
		nameToIndex.put("uvTweak", 0x211);
		nameToIndex.put("editorColors[]", 0x212);  // float4 editorColors[2];
		nameToIndex.put("editorColors", 0x213);  // struct { float mColor1; float mColor2; float mParams } editorColors;
		nameToIndex.put("dirLightsWorld", 0x214);
		nameToIndex.put("dirLightsModel", 0x215);
		
		nameToIndex.put("sunDirAndCelStrength", 0x219);  // 0x219 to display buildings and creatures?
		nameToIndex.put("shCoeffs", 0x21A);
		nameToIndex.put("cameraDistance", 0x21B);   // float
		
		
		nameToIndex.put("uvSubRect", 0x21E);
		nameToIndex.put("mousePosition", 0x21F);
		nameToIndex.put("expandAmount", 0x220);
		
		nameToIndex.put("cameraParams", 0x222);  // cameraParams[1]
		nameToIndex.put("shadowMapInfo", 0x223);
		// 224 related to per-vertex fogging
		nameToIndex.put("foggingCPU", 0x225);
		nameToIndex.put("patchLocation", 0x226);
		
		nameToIndex.put("clipToWorld", 0x228);  // float4x4
		nameToIndex.put("clipToCamera", 0x229);  // float4x4
		
		nameToIndex.put("identityColor", 0x22e);
		nameToIndex.put("pcaTexture", 0x22f);
		nameToIndex.put("rolloverRegion", 0x230);
		nameToIndex.put("renderDepth", 0x231);

		nameToIndex.put("terrainTint", 0x233);
		nameToIndex.put("utfWin", 0x234);  //   struct {   float4 mColorAlpha; row_major float4x4 mTransform; float4 mClipRect; } utfWin[16];
		nameToIndex.put("deadTerrainTint", 0x235);
		nameToIndex.put("cellStage", 0x236);
		// 238 also related with cells
		
		nameToIndex.put("terrainBrushFilterKernel", 0x23C);  // float4 terrainBrushFilterKernel[3];
		nameToIndex.put("terraformValues", 0x23D);
		nameToIndex.put("worldToPatch", 0x23E);
		
		nameToIndex.put("terrainBrushCubeMatRot", 0x241);
		nameToIndex.put("terrainSynthParams", 0x242);
		
		nameToIndex.put("debugPSColor", 0x246);
		
		nameToIndex.put("gameInfo", 0x248);
	
		nameToIndex.put("ramp", 0x24A);  //   struct {  float4 envelope; float4 values2;  float4 values3;  }
		nameToIndex.put("sunDir", 0x24B);
		nameToIndex.put("tramp", 0x24C);  // tramp[8]
		// struct { float4 vSunDir; float4 nightLightTint; float4 nightLightColor; float4 duskLightColor; 
		// float4 dayLightColor; float4 nightShadowColor; float4 duskShadowColor; float4 dayShadowColor; float4 duskDawnStartEnd;  }
		nameToIndex.put("terrainLighting", 0x24D);
		
		nameToIndex.put("beachColor", 0x250);
		nameToIndex.put("cliffColor", 0x251);
		nameToIndex.put("viewTransform", 0x252);
		
		// 0x254  float4 shCoeffs[4] ?
		nameToIndex.put("minWater", 0x255);  // minWater[2]
		nameToIndex.put("worldCameraNormal", 0x256);
		
		nameToIndex.put("terrainTransform", 0x301);
		
		nameToIndex.put("decalState", 0x304);
		nameToIndex.put("terrainState", 0x305);

		nameToIndex.put("ModAPIShader", 0x3FF);
		
		for (Map.Entry<String, Integer> entry : nameToIndex.entrySet()) {
			indexToName.put(entry.getValue(), entry.getKey());
		}
	}
	
	public static boolean hasName(int index) {
		return indexToName.containsKey(index);
	}
	
	public static boolean hasIndex(String name) {
		return nameToIndex.containsKey(name);
	}
	
	public static String getName(int index) {
		String name = indexToName.get(index);
		if (name == null) name = "0x" + Integer.toHexString(index);
		return name;
	}
	
	public static int getIndex(String name) {
		Integer result = nameToIndex.get(name);
		if (result == null) return HashManager.get().int32(name);
		else return result;
	}

	public static int getFlags(int dataIndex) {
		Integer value = flags.get(dataIndex);
		return value == null ? 0 : value;
	}
}
