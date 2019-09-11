package opennlp.ccg.builders;

import opennlp.ccg.lexicon.*;
import java.util.*;

public class LexiconBuilder {
    public static LexiconBuilder builder(){
        return new LexiconBuilder();
    }

    private Lexicon lexicon;
    private List<Family> familyList = new ArrayList<>();
    private List<MorphItem> morphList = new ArrayList<>();
    private List<MacroItem> macroList = new ArrayList<>();
    private LexiconBuilder(){
        this.lexicon = new Lexicon();
    }

    public LexiconBuilder addFamily(Family family){
        this.familyList.add(family);
        return this;
    }

    public LexiconBuilder addMorph(MorphItem item){
        this.morphList.add(item);
        return this;
    }

    public LexiconBuilder addMacro(MacroItem item){
        this.macroList.add(item);
        return this;
    }

    public Lexicon ref(){
        return this.lexicon;
    }

    public Lexicon build(){
        this.lexicon.init(familyList, morphList, macroList);
        return this.lexicon;
    }
}
