package jk_5.nailed.mcp;

import java.util.*;

import lombok.*;

@RequiredArgsConstructor
@Getter
@Setter
public class NailedMCPExtension {
    private final Project project;
    
    private String minecraftVersion;
    private String mainClass;
    @Setter(AccessLevel.NONE)
    private List<String> extraSrg = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private boolean mappingsAreSet;
    private String mappingsChannel;
    private String mappingsVersion;
    
    public boolean mappingsSet() {
        return mappingsAreSet;
    }
    
    public String getMappings() {
        return mappingsChannel + "_" + mappingsVersion;
    }
    
    public void setMappings(String mappings) {
        if (mappings == null || mappings.isEmpty()) {
            mappingsVersion = null;
            mappingsChannel = null;
            mappingsAreSet = false;
            return;
        }
        
        if (!mappings.contains("_")) {
            throw new IllegalArgumentException("Mappings must be in format 'channel_version'. eg: snapshot_20141109");
        }
        
        int index = mappings.lastIndexOf('_')
        mappingsChannel = mappings.substring(0, index)
        mappingsVersion = mappings.substring(index + 1)
        mappingsAreSet = true
    }
    
}