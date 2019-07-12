package opennlp.ccg.grammar.builders;

import opennlp.ccg.grammar.RuleGroup;

public class RulesBuilder {
    public static RulesBuilder builder(){
        return new RulesBuilder();
    }

    private RuleGroup rules;
    private RulesBuilder(){
        this.rules = new RuleGroup();
    }

    public RuleGroup build(){
        return this.rules;
    }
}
