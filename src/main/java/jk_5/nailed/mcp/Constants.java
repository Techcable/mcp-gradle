package jk_5.nailed.mcp

import java.io.OutputStream

import groovy.lang.Closure

import scala.util.Properties

/**
 * No description given
 *
 * @author jk-5
 */
public class Constants {
    private Constants() {}

    public static final String MCP_EXTENSION_NAME = "mcpProject";
    public static final String Properties.lineSeparator
    public static final String HASH_FUNC = "MD5"

    public static final String FERNFLOWER_CONFIGURATION = "fernFlower"
    public static final String MINECRAFT_CONFIGURATION = "minecraft"
    public static final String MCJAR_CONFIGURATION = "mcjar"
    public static final String MAPPINGS_CONFIGURATION = "mappings"
    public static final String API_SUBPROJECT = "api"

    public static final String JAR_UNSHADED = "{BUILD_DIR}/tmp/jars/minecraft-unshaded.jar"
    public static final String JAR_SRG = "{BUILD_DIR}/tmp/jars/minecraft-remapped.jar"
    public static final String ZIP_DECOMP = "{BUILD_DIR}/tmp/jars/minecraft-decompiled.zip"
    public static final String PATCHED_DIRTY = "{BUILD_DIR}/tmp/jars/dirty-patched.zip"
    public static final String REMAPPED_CLEAN = "{BUILD_DIR}/tmp/jars/clean-remapped.jar"
    public static final String REMAPPED_DIRTY = "{BUILD_DIR}/tmp/jars/dirty-remapped.jar"
    public static final String RUNTIME_DIR = "{PROJECT_DIR}/runtime"
    public static final String RANGEMAP = "{BUILD_DIR}/tmp/data/rangemap.txt"
    public static final String DIRTY_REMAPPED_SRC = "{BUILD_DIR}/tmp/jars/patch-dirty.zip"
    public static final String REOBFUSCATED = "{BUILD_DIR}/tmp/jars/reobfuscated.jar"
    public static final String BINPATCHES = "{BUILD_DIR}/tmp/jars/binpatches.jar"

  //Mappings
    public static final String JOINED_SRG = "{MAPPINGS_DIR}/joined.srg"
    public static final String JOINED_EXC = "{MAPPINGS_DIR}/joined.exc"
    public static final String EXC_JSON = "{MAPPINGS_DIR}/exceptor.json"
    public static final String MCP_PATCHES = "{MAPPINGS_DIR}/patches"
    public static final String SHADEDLIB_REMOVE_CONFIG = "{MAPPINGS_DIR}/removeClasses.cfg"
    public static final String ASTYLE_CONFIG = "{MAPPINGS_DIR}/astyle.cfg"
    public static final String VERSION_INFO = "{MAPPINGS_DIR}/version.json"

  //Generated files
    public static final String NOTCH_2_SRG_SRG = "{BUILD_DIR}/tmp/mappings/generated/srg/notch2srg.srg"
    public static final String NOTCH_2_MCP_SRG = "{BUILD_DIR}/tmp/mappings/generated/srg/notch2mcp.srg"
    public static final String MCP_2_SRG_SRG = "{BUILD_DIR}/tmp/mappings/generated/srg/mcp2srg.srg"
    public static final String MCP_2_NOTCH_SRG = "{BUILD_DIR}/tmp/mappings/generated/srg/mcp2notch.srg"
    public static final String SRG_EXC = "{BUILD_DIR}/tmp/mappings/generated/exc/srg.exc"
    public static final String MCP_EXC = "{BUILD_DIR}/tmp/mappings/generated/exc/mcp.exc"
    public static final String METHODS_CSV = "{BUILD_DIR}/tmp/mappings/csv/methods.csv"
    public static final String FIELDS_CSV = "{BUILD_DIR}/tmp/mappings/csv/fields.csv"
    public static final String PARAMS_CSV = "{BUILD_DIR}/tmp/mappings/csv/params.csv"
    public static final String CSV_MAPPINGS_DIR = "{BUILD_DIR}/tmp/mappings/csv/"
    public static final String STATICS_LIST = "{BUILD_DIR}/tmp/mappings/generated/statics.txt"

    public static final String NAILED_JAVA_SOURCES = "{PROJECT_DIR}/src/main/java"
    public static final String NAILED_RESOURCES = "{PROJECT_DIR}/src/main/resources"
    public static final String NAILED_JAVA_API_SOURCES = "{PROJECT_DIR}/api/src/main/java"
    public static final String NAILED_API_RESOURCES = "{PROJECT_DIR}/api/src/main/resources"
    public static final String NAILED_JAVA_TEST_SOURCES = "{PROJECT_DIR}/src/test/java"
    public static final String NAILED_TEST_RESOURCES = "{PROJECT_DIR}/src/test/resources"
    public static final String PROJECT_CLEAN = "{PROJECT_DIR}/minecraft/Clean"
    public static final String PROJECT_DIRTY = "{PROJECT_DIR}/minecraft/Modified"
    public static final String MINECRAFT_CLEAN_SOURCES = PROJECT_CLEAN + "/src/main/java"
    public static final String MINECRAFT_CLEAN_RESOURCES = PROJECT_CLEAN + "/src/main/resources"
    public static final String MINECRAFT_DIRTY_SOURCES = PROJECT_DIRTY + "/src/main/java"
    public static final String MINECRAFT_DIRTY_RESOURCES = PROJECT_DIRTY + "/src/main/resources"
    public static final String NAILED_PATCH_DIR = "{PROJECT_DIR}/patches"

    public static final String MINECRAFT_MAVEN_URL = "https://libraries.minecraft.net"

    public static final Closure CALL_FALSE = (Object any -> {
        return false
    });

    public OutputStream getNullStream = new OutputStream() {
       @Override
       public void write(int b) {}
    }
}
