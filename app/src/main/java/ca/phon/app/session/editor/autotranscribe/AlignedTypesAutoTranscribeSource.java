package ca.phon.app.session.editor.autotranscribe;

import ca.phon.alignedTypesDatabase.AlignedTypesDatabase;
import ca.phon.alignedTypesDatabase.AlignedTypesDatabaseFactory;
import ca.phon.autotranscribe.AutoTranscribeSource;
import ca.phon.session.SystemTierType;

import java.util.*;

public class AlignedTypesAutoTranscribeSource implements AutoTranscribeSource {

    private final AlignedTypesDatabase alignedTypesDatabase;

    private final List<String> tierNames = new ArrayList<>();

    public AlignedTypesAutoTranscribeSource() {
        this(AlignedTypesDatabaseFactory.newDatabase());
    }

    public AlignedTypesAutoTranscribeSource(AlignedTypesDatabase alignedTypesDatabase) {
        this.alignedTypesDatabase = alignedTypesDatabase;
        tierNames.add(SystemTierType.IPATarget.getName());
        tierNames.add(SystemTierType.IPAActual.getName());
    }

    public AlignedTypesDatabase getAlignedTypesDatabase() {
        return this.alignedTypesDatabase;
    }

    public void addTier(String tierName) {
        tierNames.add(tierName);
    }

    public void removeTier(String tierName) {
        tierNames.remove(tierName);
    }

    public void clearTiers() {
        tierNames.clear();
    }

    public void setTierNames(List<String> tierNames) {
        this.tierNames.clear();
        this.tierNames.addAll(tierNames);
    }

    public List<String> getTierNames() {
        return Collections.unmodifiableList(tierNames);
    }

    @Override
    public String[] lookup(String text) {
        final Map<String, String[]> alignedTypes =
                alignedTypesDatabase.alignedTypesForTier(SystemTierType.Orthography.getName(), text);
        if(alignedTypes.size() > 0) {
            final Set<String> ipas = new LinkedHashSet<>();
            for(String tierName:alignedTypes.keySet()) {
                if(!tierNames.contains(tierName)) continue;
                final String[] types = alignedTypes.get(tierName);
                for(String ipa:types) {
                    ipas.add(ipa);
                }
            }
            return ipas.toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

}
