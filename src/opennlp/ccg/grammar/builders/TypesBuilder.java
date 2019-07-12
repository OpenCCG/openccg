package opennlp.ccg.grammar.builders;

import opennlp.ccg.grammar.Types;

public class TypesBuilder {
    public static TypesBuilder builder(){
        return new TypesBuilder();
    }

    private Types types;
    private TypesBuilder(){
        this.types = new Types();
    }

    public Types build(){
        return this.types;
    }
}
