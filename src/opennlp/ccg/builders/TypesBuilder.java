package opennlp.ccg.builders;

import opennlp.ccg.grammar.Types;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class TypesBuilder {
    public static TypesBuilder builder(){
        return new TypesBuilder();
    }

    private List<Element> elementList = new ArrayList<>();
    private Types types;
    private TypesBuilder(){
        this.types = new Types();
    }

    public TypesBuilder addType(String name){
        Element el = new Element("type");
        el.setAttribute("name", name);
        elementList.add(el);
        return this;
    }

    public TypesBuilder addType(String name, String parents){
        Element el = new Element("type");
        el.setAttribute("name", name);
        el.setAttribute("parents", parents);
        elementList.add(el);
        return this;
    }

    public Types build(){
        this.types.readTypes(this.elementList);
        return this.types;
    }
}
