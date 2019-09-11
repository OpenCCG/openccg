package opennlp.ccg.builders;

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.grammar.RuleGroup;
import opennlp.ccg.grammar.Types;
import opennlp.ccg.lexicon.Lexicon;

public class GrammarBuilder {
    public static GrammarBuilder builder(){
        return new GrammarBuilder();
    }

    private Grammar grammar;

    private GrammarBuilder(){
        this.grammar = new Grammar();
    }

    public boolean isGlobalGrammarInit(){
        return this.grammar.theGrammar != null;
    }

    public GrammarBuilder withTypes(Types types){
        this.grammar.setTypes(types);
        types.setGrammar(this.grammar);
        return this;
    }

    public GrammarBuilder withLexicon(Lexicon lexicon){
        this.grammar.setLexicon(lexicon);
        lexicon.setGrammar(this.grammar);
        return this;
    }

    public GrammarBuilder withRules(RuleGroup rules){
        this.grammar.setRules(rules);
        rules.setGrammar(this.grammar);
        return this;
    }

    public Grammar build(){
        // Check if we added everything
        assert this.grammar.lexicon != null;
        assert this.grammar.rules != null;
        assert this.grammar.types != null;

        return this.grammar;
    }
}
