package opennlp.ccg.grammar.builders;

import opennlp.ccg.lexicon.Lexicon;

public class LexiconBuilder {
    public static LexiconBuilder builder(){
        return new LexiconBuilder();
    }

    private Lexicon lexicon;
    private LexiconBuilder(){
        this.lexicon = new Lexicon();
    }

    public Lexicon build(){
        return this.lexicon;
    }
}
