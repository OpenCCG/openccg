package opennlp.ccg.grammar.builders;

import opennlp.ccg.grammar.Types;

public class TypesBuilder {
    public static TypesBuilder builder(){
        return new TypesBuilder();
    }

    private TypesBuilder(){

    }

    public Types build(){
        return new Types();
    }
}
