package opennlp.ccg.builders;

import opennlp.ccg.grammar.RuleGroup;
import opennlp.ccg.grammar.Rule;

public class RulesBuilder {
    public static RulesBuilder builder(){
        return new RulesBuilder();
    }

    private RuleGroup rules;
    private RulesBuilder(){
        this.rules = new RuleGroup();
    }

    public RulesBuilder addRule(Rule rule){
        this.rules.addRule(rule);
        return this;
    }

    public RuleGroup build(){
        return this.rules;
    }
}
