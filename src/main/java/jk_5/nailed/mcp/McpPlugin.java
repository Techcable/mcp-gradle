package jk_5.nailed.mcp;

import java.io.FileReader;
import java.util.*;

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import jk_5.nailed.mcp.delayed.*;
import jk_5.nailed.mcp.tasks.*;

import java.util.function.*;

import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugins.ide.idea.model.IdeaModel;

public class McpPlugin extends Plugin<Project> {
    private Project project;
    
    @Override
    public void apply(Project project) {
        this.project = project;
        
        project.afterEvaluate(Project project -> afterEvaluate(project));
        
        project.allProjects(Project project -> {
            addMavenRepo(project, (ArtifactRepository r) -> {
               r.setName("minecraft");
               r.setUrl(Constants.MINECRAFT_MAVEN_URL);
            });
            project.getRepositories().mavenCentral();
            addMavenRepo(project, (ArtifactRepository r) -> {
               r.setName("techcable-repo");
               r.setUrl("http://repo.techcable.net/content/groups/public/");
            });
            addMavenRepo(project, (ArtifactRepository r) -> {
                r.setName("forge");
                r.setUrl("http://files.minecraftforge.net/maven")
            });
            addIvyRepo(project, (ArtifactRepository r) -> {
                r.setName("Minecraft amazon bucket")
                r.artifactPattern("http://s3.amazonaws.com/Minecraft.Download/versions/[revision]/[module].[revision].jar")
                r.ivyPattern("http://s3.amazonaws.com/file.xml")
            });
        });
        
        project.getExtensions().create(Constants.MCP_EXTENSION_NAME, NailedMCPExtension.class, project);
        
        project.getConfigurations().create(Constants.FERNFLOWER_CONFIGURATION);
        project.getConfigurations().create(Constants.MCJAR_CONFIGURATION);
        project.getConfigurations().create(Constants.MAPPINGS_CONFIGURATION);
        Configuration mcCfg = project.getConfigurations().create(Constants.MINECRAFT_CONFIGURATION);
        
        project.getConfigurations().getByName("compile").extendsFrom(mcCfg);
        
        project.getDependencies.add(Constants.FERNFLOWER_CONFIGURATION, "de.fernflower:fernflower:1.0");
    
        Project apiProject = null;
        for (Project subproject : project.getSubprojects()) {
            if (subproject.getName().equals(Constants.API_SUBPROJECT)) {
                apiProject = project;
            }
        }
        
        JavaPluginConvention javaConv = (JavaPluginConvention) project.getConvention().getPlugins().get("java")
        
        ExtractTask extractMappingsTask = makeTask("extractMappings", ExtractTask.class);
        ExtractTask.into(Constants.CSV_MAPPINGS_DIR);
        t.from(new DelayedFile("dummy", project) {
           @Override
           public File resolve() {
               return project.getConfigurations().getByName(Constants.MAPPINGS_CONFIGURATION).getSingleFile();
           }
        });
        t.setDoesCache(true);
        
        GenerateMappingsTask generateMappingsTask = makeTask("extractMappings", GenerateMappingsTask.class);
        generateMappingsTask.setInSrg(Constants.JOINED_SRG);
        generateMappingsTask.setInExc(Constants.JOINED_EXC);
        generateMappingsTask.setMethodCsv(Constants.METHODS_CSV);
        generateMappingsTask.setFieldCsv(Constants.FIELDS_CSV);
        generateMappingsTask.setNotchToSrg(Constants.NOTCH_2_SRG_SRG);
        generateMappingsTask.setNotchToMcp(Constants.NOTCH_2_MCP_SRG);
        generateMappingsTask.setMcpToSrg(Constants.MCP_2_SRG_SRG);
        generateMappingsTask.setMcpToNotch(Constants.MCP_2_NOTCH_SRG);
        generateMappingsTask.setSrgExc(Constants.SRG_EXC);
        generateMappingsTask.setMcpExc(Constants.MCP_EXC);
        generateMappingsTask.setDoesCache(false);
    
        for (File f : project.fileTree(toDelayedFile(Constants.NAILED_RESOURCES)).call()).getFiles) {
            if (f.getPath().endsWith(".exc")) {
                project.getLogger.lifecycle("Added extra exc file " + f.getName());
                generateMappingsTask.addExtraExc(f);
            } else if (f.getPath().endsWith(".srg")) {
                project.getLogger.lifecycle("Added extra srg file " + f.getName());
                generateMappingsTask.addExtraSrg(f);
            }
        }
        
        generateMappingsTask.dependsOn("extractMappings");
        generateMappingsTask.setDescription("Generates remapped .srg and .exc files from the joined srg and exc files combined with the mcp mappings");
        
        DeobfuscateTask deobfuscateTask = makeTask("deobfuscate", DeobfuscateTask.class);
        deobfuscateTask.setInJar(Constants.JAR_UNSHADED);
        deobfuscateTask.setOutJar(Constants.JAR_SRG);
        deobfuscateTask.setSrg(Constants.NOTCH_2_SRG_SRG);
        deobfuscateTask.setExceptorConfig(Constants.JOINED_EXC);
        deobfuscateTask.setExceptorJson(Constants.EXC_JSON);
        deobfuscateTask.setApplyMarkers(applyMarkers = true);
        deobfuscateTask.setFieldCsv(Constants.FIELDS_CSV);
        deobfuscateTask.setMethodCsv(Constants.METHODS_CSV);
        deobfuscateTask.dependsOn("removeShadedLibs", "generateMappings");
        
        for (File f : project.fileTree(toDelayedFile(Constants.NAILED_RESOURCES).call()).getFiles()) {
            if(f.getPath.endsWith("_at.cfg")){
                project.getLogger.lifecycle("Added AccessTransformer file " + f.getName());
                deobfuscateTask.addAccessTransformer(f);
            }
        }
        deobfuscateTask.setDescription("Remaps the obfuscated jar to srgnames and applies the exc files");
        
        DecompileTask decompileTask = makeTask("decompile", DecompileTask.class);
        decompileTask.setInJar(Constants.JAR_SRG);
        decompileTask.setPatch(Constants.MCP_PATCHES);
        decompileTask.setOutJar(Constants.ZIP_DECOMP);
        decompileTask.setAStyleConfig(Constants.ASTYLE_CONFIG);
        decompileTask.dependsOn("deobfuscate", "generateMappings");
        decompileTask.setDescription("Decompiles the jar");
        
        RemapSourceTask remapCleanSourceTask = makeTask("remapCleanSource", RemapSourceTask.class);
        remapCleanSourceTask.setInJar(Constants.ZIP_DECOMP);
        remapCleanSourceTask.setOutJar(Constants.REMAPPED_CLEAN);
        remapCleanSourceTask.setMethodCsv(Constants.METHODS_CSV);
        remapCleanSourceTask.setFieldCsv(Constants.FIELDS_CSV);
        remapCleanSourceTask.setParamCsv(Constants.PARAMS_CSV);
        remapCleanSourceTask.setDoesCache(false);
        remapCleanSourceTask.noJavadocs();
        remapCleanSourceTask.dependsOn("decompile");
        remapCleanSourceTask.setDescription("Remaps the clean srg source jar to mapped mcp names");
    
        PatchSourceJarTask patchSourceJarTask = makeTask("patchDirtySource", PatchSourceJarTask.class);
        patchSourceJarTask.setInJar(Constants.ZIP_DECOMP);
        patchSourceJarTask.setOutJar(Constants.PATCHED_DIRTY);
        patchSourceJarTask.addStage("Patch", Constants.NAILED_PATCH_DIR);
        patchSourceJarTask.setDoesCache(false);
        patchSourceJarTask.setMaxFuzz(2);
        patchSourceJarTask.dependsOn("decompile");
        patchSourceJarTask.setDescription("Applies the patches to the source jar");
        
        RemapSourceTask remapDirtySourceTask = makeTask("remapDirtySource", RemapSourceTask.class);
        remapDirtySourceTask.setInJar(Constants.PATCHED_DIRTY);
        remapDirtySourceTask.setOutJar(Constants.REMAPPED_DIRTY);
        remapDirtySourceTask.setMethodCsv(Constants.METHODS_CSV);
        remapDirtySourceTask.setFieldCsv(Constants.FIELDS_CSV);
        remapDirtySourceTask.setParamCsv(Constants.PARAMS_CSV);
        remapDirtySourceTask.setDoesCache(false);
        remapDirtySourceTask.noJavadocs();
        remapDirtySourceTask.dependsOn("patchDirtySource");
        remapDirtySourceTask.setDescription("Remaps the dirty srg source jar to mapped mcp names");
    
        ExtractTask extractMinecraftResourcesTask = makeTask("extractMinecraftResources", ExtractTask.class);
        extractMinecraftResourcesTask.exclude(this.javaFiles);
        extractMinecraftResourcesTask.setIncludeEmptyDirs(includeEmptyDirs = false);
        extractMinecraftResourcesTask.from(Constants.REMAPPED_CLEAN);
        extractMinecraftResourcesTask.into(Constants.MINECRAFT_CLEAN_RESOURCES);
        extractMinecraftResourcesTask.dependsOn("remapCleanSource");
        extractMinecraftResourcesTask.setDescription("Extracts the minecraft resources from the jar into the clean src/main/resources");
    
        ExtractTask extractMinecraftSourcesTask = makeTask("extractMinecraftSources", ExtractTask.class);
        t.include(this.javaFiles);
        t.from(Constants.REMAPPED_CLEAN);
        t.into(Constants.MINECRAFT_CLEAN_SOURCES);
        t.dependsOn("extractMinecraftResources");
        t.setDescription("Extracts the remapped and decompiled minecraft sources into the clean src/main/java");
    
        ExtractTask extractNailedResourcesTask = makeTask("extractProjectResources", ExtractTask.class);
        extractNailedResourcesTask.exclude(this.javaFiles);
        extractNailedResourcesTask.setIncludeEmptyDirs(includeEmptyDirs = false);
        extractNailedResourcesTask.from(Constants.REMAPPED_DIRTY);
        extractNailedResourcesTask.into(Constants.MINECRAFT_DIRTY_RESOURCES);
        extractNailedResourcesTask.doFirst(Task p1 -> {
            List<File> files = toDelayedFile(Constants.MINECRAFT_DIRTY_RESOURCES).resolve().listFiles()
            if(files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        });
        extractNailedResourcesTask.dependsOn("remapDirtySource")
        extractNailedResourcesTask.setDescription("Extracts the minecraft resources into the dirty src/main/resources")
    
        ExtractTask extractProjectSourcesTask = makeTask("extractProjectSources", ExtractTask.class)
        extractProjectSourcesTask.include(this.javaFiles: _*)
        extractProjectSourcesTask.from(Constants.REMAPPED_DIRTY)
        extractProjectSourcesTask.into(Constants.MINECRAFT_DIRTY_SOURCES)
        extractProjectSourcesTask.doFirst(Task p1 -> {
            List<File> files = toDelayedFile(Constants.MINECRAFT_DIRTY_SOURCES).resolve().listFiles()
            if(files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        });
        extractProjectSourcesTask.dependsOn("extractProjectResources")
        extractProjectSourcesTask.setDescription("Extracts the remapped and decompiled minecraft sources into the dirty src/main/java")
    
        ExtractRangeMapTask generateRangeMapTask = makeTask("generateRangeMap", ExtractRangeMapTask.class);
        generateRangeMapTask.addConfiguration(mcCfg)
        generateRangeMapTask.addInput(Constants.MINECRAFT_DIRTY_SOURCES)
        generateRangeMapTask.setRangeMap(Constants.RANGEMAP)
        generateRangeMapTask.setStaticsList(Constants.STATICS_LIST)
        generateRangeMapTask.setCleanCompiled(Constants.JAR_SRG)
        generateRangeMapTask.dependsOn("deobfuscate")
        generateRangeMapTask.setDescription("Generates the rangemaps used for remapping the sourcecode from mapped names back to srg")
    
        ApplySrg2SourceTask retroMapSourcesTask = makeTask("retroMapSources", ApplySrg2SourceTask.class);
        retroMapSourcesTask.addInput(Constants.MINECRAFT_DIRTY_SOURCES);
        retroMapSourcesTask.setOutput(Constants.DIRTY_REMAPPED_SRC);
        retroMapSourcesTask.addSrg(toDelayedFile(Constants.MCP_2_SRG_SRG));
        retroMapSourcesTask.addExc(toDelayedFile(Constants.MCP_EXC));
        retroMapSourcesTask.addExc(toDelayedFile(Constants.SRG_EXC));
        retroMapSourcesTask.setRangeMap(Constants.RANGEMAP);
        retroMapSourcesTask.setStaticsList(Constants.STATICS_LIST);
        retroMapSourcesTask.dependsOn("generateMappings", "generateRangeMap");
        retroMapSourcesTask.setDescription("Uses the generated rangemaps to remap the mapped sourcecode back to srg");
        
        GeneratePatchesTask generatePatchesTask = makeTask("generatePatches", GeneratePatchesTask.class);
        generatePatchesTask.setPatchDir(Constants.NAILED_PATCH_DIR)
        generatePatchesTask.setOriginal(Constants.ZIP_DECOMP)
        generatePatchesTask.setChanged(Constants.DIRTY_REMAPPED_SRC)
        generatePatchesTask.setOriginalPrefix("../src-base/minecraft")
        generatePatchesTask.setChangedPrefix("../src-work/minecraft")
        generatePatchesTask.setGroup("Project-MCP")
        generatePatchesTask.dependsOn("retroMapSources")
        generatePatchesTask.setDescription("Generates patches from the difference between the dirty source and the clean source")
        
        Jar deobfJarTask = makeTask("deobfJar", Jar.class);
        deobfJarTask.from(javaConv.getSourceSets.getByName("main").getOutput)
        deobfJarTask.setClassifier("deobf")
        deobfJarTask.setDestinationDir(t.getTemporaryDir)
        deobfJarTask.dependsOn("classes")
        
        ReobfuscateTask reobfuscateTask = makeTask("reobfuscate", ReobfuscateTask.class);
        reobfuscateTask.setSrg(Constants.MCP_2_NOTCH_SRG)
        reobfuscateTask.setExc(Constants.SRG_EXC)
        reobfuscateTask.setReverse(reverse = false)
        reobfuscateTask.setPreFFJar(Constants.JAR_SRG)
        reobfuscateTask.setOutJar(Constants.REOBFUSCATED)
        reobfuscateTask.setMethodCsv(Constants.METHODS_CSV)
        reobfuscateTask.setFieldCsv(Constants.FIELDS_CSV)
        reobfuscateTask.dependsOn("deobfJar", "extractProjectSources", "generateMappings")
        reobfuscateTask.setDescription("Reobfuscates the nailed code to use obfuscated names")
    
        GenerateBinaryPatchesTask generateBinaryPatchesTask = makeTask("generateBinaryPatches", GenerateBinaryPatchesTask.class);
        generateBinaryPatchesTask.setDirtyJar(Constants.REOBFUSCATED)
        generateBinaryPatchesTask.setOutJar(Constants.BINPATCHES)
        generateBinaryPatchesTask.setSrg(Constants.NOTCH_2_SRG_SRG)
        generateBinaryPatchesTask.addPatchList(toDelayedFileTree(Constants.NAILED_PATCH_DIR))
        generateBinaryPatchesTask.dependsOn("reobfuscate", "generateMappings")
        generateBinaryPatchesTask.setDescription("Checks the binary difference between the compiled dirty source and the clean source, and writes it to the patch file")
        
        Jar packageJavadocJar = makeTask("packageJavadoc", Jar.class);
        packageJavadocJar.getOutputs.upToDateWhen(Constants.CALL_FALSE);
        packageJavadocJar.setClassifier("javadoc");
        packageJavadocJar.from("build/docs/javadoc");
        packageJavadocJar.dependsOn("javadoc");
        project.getArtifacts.add("archives", packageJavadocJar);
        packageJavadocJar.setDescription("Packages the javadoc");
        
        Jar packageSource = makeTask("packageSource", Jar.class);
        packageSource.getOutputs.upToDateWhen(Constants.CALL_FALSE);
        packageSource.setClassifier("sources");
        packageSource.from(toDelayedFileTree(Constants.NAILED_JAVA_SOURCES));
        packageSource.from(toDelayedFileTree(Constants.NAILED_RESOURCES));
        packageSource.from(toDelayedFileTree(Constants.NAILED_JAVA_API_SOURCES));
        packageSource.from(toDelayedFileTree(Constants.NAILED_API_RESOURCES));
        project.getArtifacts.add("archives", packageSource);
        packageSource.setDescription("Packages all sourcecode");
        
        DefaultTask setupProjectTask = metaTask("setupProject");
        setupProjectTask.dependsOn("extractNailedSources", "extractMinecraftSources");
        setupProjectTask.setGroup("Project-MCP");
        setupProjectTask.setDescription("Decompiles minecraft and sets up the development environment");
    
        DefaultTask buildPackagesTask = metaTask("buildPackages");
        buildPackagesTask.dependsOn("packageJavadoc", "packageSource").setGroup("Project-MCP");
        buildPackagesTask.setGroup("Project-MCP");
        buildPackagesTask.setDescription("Builds all packages");
        
        project.getTasks.getByName("uploadArchives").dependsOn("buildPackages");
        
        IdeaModel ideaConv = (IdeaModel) project.getExtensions.getByName("idea");
        ideaConv.getModule().getExcludeDirs().addAll(project.files(".gradle", "build", ".idea").getFiles());
        ideaConv.getModule().setDownloadJavadoc(true);
        ideaConv.getModule().setDownloadSources(true);
    
        SourceSet main = javaConv().getSourceSets().getByName("main");
        main.getJava().srcDir(toDelayedFile(Constants.MINECRAFT_DIRTY_SOURCES));
        main.getResources().srcDir(toDelayedFile(Constants.MINECRAFT_DIRTY_RESOURCES));
    }
    
    public void afterEvaluate() {
        FileReader reader = new FileReader(new DelayedFile(Constants.VERSION_INFO, project).call());
        JsonParser json = new JsonParser().parse(reader).getAsJsonObject();
        reader.close();
        
        DependencyHandler deps = project.getDependencies();
        for (JsonElement element : json.getAsJsonArray("dependencies")) {
            deps.add(Constants.MINECRAFT_CONFIGURATION, element.getAsString() )
        }
        
        NailedMCPExtension ext = (NailedMCPExtension) project.getExtentions().getByName(Constants.MCP_EXTENSION_NAME);
    
        if (ext.mappingsSet()) {
            project.getDependencies().add(Constants.MAPPINGS_CONFIGURATION, ImmutableMap.of(
                "group", "de.oceanlabs.mcp",
                "name", "mcp_" + ext.getMappingsChannel(),
                "version", ext.getMappingsVersion() + "-" + ext.getMinecraftVersion(),
                "ext", "zip"
            ));
        }
        
        ReobfuscateTask task = (ReobfuscateTask) project.getTasks.getByName("reobfuscate");
        task.setExtraSrg(ext.getExtraSrg());
        
        project.getDependencies().add(Constants.MCJAR_CONFIGURATION, s"net.minecraft:minecraft_server:${ext.getMinecraftVersion}@jar");
    }
    
    public void addMavenRepo(Project project, Consumer<MavenArtifactRepository> configure){
        project.getRepositories().maven(MavenArtifactRepository repo -> configure.accept(repo));
    }
    
    public void addIvyRepo(Project project, Consumer<IvyArtifactRepository> configure) {
        project.getRepositories().ivy(IvyArtifactRepository repo -> configure.accept(repo));
    }
    
    public DefaultTask metaTask(String name) {
        return makeTask(this.project, name);
    }
    public <T> T makeTask(String name, Class<T> type) {
        Map<String, Object> = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        Task task = project.task(map, name);
        return (T) task;
    }
    
    public DelayedString toDelayedString(String s) {
        return new DelayedString(s, this.project);
    }
    public DelayedFile toDelayedFile(String s) {
        return new DelayedFile(s, this.project);
    }
    public DelayedFileTree toDelayedFileTree(String s) {
        return new DelayedFileTree(s, this.project);
    }
    public DelayedFileTree toDelayedZipFileTree(String s) {
        return new DelayedFileTree(s, this.project, true);
    }
}