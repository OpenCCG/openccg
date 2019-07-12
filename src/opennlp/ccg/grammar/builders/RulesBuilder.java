package opennlp.ccg.grammar.builders;

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

    public void addRule(Rule rule){
        this.rules.addRule(rule);
    }

    public RuleGroup build(){
        return this.rules;
    }
}
