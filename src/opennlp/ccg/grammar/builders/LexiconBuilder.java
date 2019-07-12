package opennlp.ccg.grammar.builders;

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

    public void addFamily(Family family){
        this.familyList.add(family);
    }

    public void addMorph(MorphItem item){
        this.morphList.add(item);
    }

    public void addMacro(MacroItem item){
        this.macroList.add(item);
    }

    public Lexicon build(){
        this.lexicon.init(familyList, morphList, macroList);
        return this.lexicon;
    }
}
