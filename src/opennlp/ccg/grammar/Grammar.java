///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 University of Edinburgh (Michael White) and Gunes Erkan
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.grammar;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.util.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.hylo.*;
import opennlp.ccg.parse.Parser;
import opennlp.ccg.parse.ParseException;
import opennlp.ccg.realize.Realizer;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.sax.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * A CCG grammar is essentially a lexicon plus a rule group.
 * A grammar may also have sequences of transformations to use in 
 * loading/saving LFs from/to XML.
 *
 * @author  Michael White
 * @author  Gunes Erkan
 * @version $Revision: 1.45 $, $Date: 2010/12/06 02:39:35 $ 
 */
public class Grammar {

    /** The lexicon. */
    public Lexicon lexicon;
    
    /** The rule group. */
    public RuleGroup rules;

    /** The type hierarchy. */
    public Types types;
    
    /** The features to include in supertags. */
    public final Set<String> supertagFeatures = new HashSet<String>();
    
    /** The sequence of transformations to use when loading LFs from XML. */
    public final URL[] fromXmlTransforms;
    
    /** The sequence of transformations to use when saving LFs to XML. */
    public final URL[] toXmlTransforms;

    /** Preferences for displaying elements in this grammar. */
    public DisplayPrefs prefs = new DisplayPrefs();
   
    /** For access to the current grammar; should be generalized eventually. */
    public static Grammar theGrammar;
	
    // name of the grammar
    private String grammarName = null;
	
    // parser, for getting parsed words
    private Parser parser = null; 

    // XML factories
    private SAXParserFactory spf = null; 
    private static SAXTransformerFactory stf = null; 
    
    // transformer for loading/saving LFs from/to XML
    private Transformer transformer = null;
    
    // transformations for loading/saving LFs from/to XML
    private Templates[] fromXmlTemplates = null;
    private Templates[] toXmlTemplates = null;
    
    // transformer for saving strings to APML
    private Transformer apmlTransformer = null;
    
    /** The pitch accents recognized as underscored suffixes for translation to APML. */
    public static final String[] pitchAccents = { 
        "H*", "L*", "L+H*", "L*+H", "H*+L", "H+L*"
    };

    // set of pitch accents
    private static Set<String> pitchAccentsSet = null;    
    
    /** The boundary tones recognized as separate tokens for translation to APML. */
    public static final String[] boundaryTones = { 
        "L", "H", "LL%", "HH%", "LH%", "HL%"
    };
    
    // set of boundary tones
    private static Set<String> boundaryTonesSet = null;

    public Grammar(){
        theGrammar = this;
        this.fromXmlTransforms = new URL[0];
        this.toXmlTransforms = new URL[0];
    }

    public void setTypes(Types types){
        this.types = types;
    }

    public void setLexicon(Lexicon lexicon){
        this.lexicon = lexicon;
    }

    public void setRules(RuleGroup rules){
        this.rules = rules;
    }

    
    /** Loads a grammar from the given filename. */
    public Grammar(String filename) throws IOException {
        this(new File(filename).toURI().toURL());
    }
    
    /** Loads a grammar from the given URL. */
	public Grammar(URL url) throws IOException {
    	this(url, false);
    }
    
    /** Loads a grammar from the given URL, with the given flag for whether to ignore rule combos. */
    @SuppressWarnings("unchecked")
	public Grammar(URL url, boolean ignoreCombos) throws IOException {
        theGrammar = this;
        // read XML
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(url);
        } catch (JDOMException jde) {
            throw (IOException) new IOException().initCause(jde);
        }
        Element root = doc.getRootElement();	// root corresponds to <grammar>
		    grammarName = root.getAttributeValue("name");
		
        Element supertagsElt = root.getChild("supertags");
        if (supertagsElt != null) {
            String feats = supertagsElt.getAttributeValue("feats");
            if (feats != null) {
                String[] names = feats.split("\\s+");
                for (int i = 0; i < names.length; i++) {
                    supertagFeatures.add(names[i]);
                }
            }
        }
        if (supertagFeatures.isEmpty()) {
            // default is "form" and "lex"
            supertagFeatures.add("form"); supertagFeatures.add("lex"); 
        }
        
        Tokenizer tokenizer = null;
        Element tokenizerElt = root.getChild("tokenizer");
        if (tokenizerElt != null) {
            String tokenizerClass = tokenizerElt.getAttributeValue("classname");
            if (tokenizerClass != null) {
                try {
                    tokenizer = (Tokenizer) Class.forName(tokenizerClass).newInstance();
                } catch (Exception exc) {
                    throw (IOException) new IOException().initCause(exc);
                }
            }
            else tokenizer = new DefaultTokenizer();
            String replacementSemClasses = tokenizerElt.getAttributeValue("replacement-sem-classes");
            if (replacementSemClasses != null) {
                String[] semClasses = replacementSemClasses.split("\\s+");
                for (int i = 0; i < semClasses.length; i++) {
                    tokenizer.addReplacementSemClass(semClasses[i]);
                }
            }
        }
        
        Element typesElt = root.getChild("types");
        URL typesUrl;
        if (typesElt != null) {
            typesUrl = new URL(url, typesElt.getAttributeValue("file"));
        }
        else typesUrl = null;
        Element lexiconElt = root.getChild("lexicon");
        boolean openlex = "true".equals(lexiconElt.getAttributeValue("openlex"));
        URL lexiconUrl = new URL(url, lexiconElt.getAttributeValue("file")); 
        Element morphElt = root.getChild("morphology");
        URL morphUrl = new URL(url, morphElt.getAttributeValue("file"));
        Element rulesElt = root.getChild("rules");
        URL rulesUrl = new URL(url, rulesElt.getAttributeValue("file"));
        Element fromXmlElt = root.getChild("LF-from-XML");
        if (fromXmlElt != null) {
            List<Element> children = fromXmlElt.getChildren();
            fromXmlTransforms = new URL[children.size()];
            for (int i = 0; i < children.size(); i++) {
                Element transformElt = (Element) children.get(i);
                fromXmlTransforms[i] = new URL(url, transformElt.getAttributeValue("file"));
            }
        } else {
            fromXmlTransforms = new URL[0];
        }
        Element toXmlElt = root.getChild("LF-to-XML");
        if (toXmlElt != null) {
            List<Element> children = toXmlElt.getChildren();
            toXmlTransforms = new URL[children.size()];
            for (int i = 0; i < children.size(); i++) {
                Element transformElt = (Element) children.get(i);
                toXmlTransforms[i] = new URL(url, transformElt.getAttributeValue("file"));
            }
        } else {
            toXmlTransforms = new URL[0];
        }
        
        // load type hierarchy, lexicon and rules
        if (typesUrl != null) types = new Types(typesUrl, this);
        else types = new Types(this);
        if (tokenizer != null) lexicon = new Lexicon(this, tokenizer);
        else lexicon = new Lexicon(this);
        lexicon.openlex = openlex;
        lexicon.init(lexiconUrl, morphUrl); 
        rules = new RuleGroup(rulesUrl, this);
        
        // add observed supertag-rule combos for filtering, if any, unless ignoring combos
        if (!ignoreCombos) {
	        String combosfile = rulesElt.getAttributeValue("combosfile");
	        if (combosfile != null) {
	        	URL combosUrl = new URL(url, combosfile);
	        	rules.loadSupercatRuleCombos(combosUrl);
	        }
	        // set dynamic combos: defaults to true with a combosfile, otherwise defaults to false
	        boolean dynamic = (combosfile != null);
	        String dynamicCombos = rulesElt.getAttributeValue("dynamic-combos");
	        if (dynamicCombos != null) dynamic = Boolean.parseBoolean(dynamicCombos);
	        rules.setDynamicCombos(dynamic);
        }
    }

    
    /**
     * Returns a file url string relative to the user's current directory 
     * for the given filename.
     */
    public static String convertToFileUrl(String filename) {
        try {
            return new File(filename).toURI().toURL().toString();
        }
        catch (java.net.MalformedURLException exc) {
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        // return "file:"+System.getProperty("user.dir")+"/"+filename;
    }
    
    
    // initializes factories and transformers
    private void initializeTransformers() throws TransformerConfigurationException {
        // init factories
        if (spf == null) {
            spf = SAXParserFactory.newInstance(); 
            spf.setNamespaceAware(true);
        }
        if (stf == null) {
            stf = (SAXTransformerFactory) TransformerFactory.newInstance();
            try { // try setting indent at factory level
                stf.setAttribute("indent-number", new Integer(2));
            } catch (IllegalArgumentException exc) {} // ignore
        }
        // set up transformer with indenting
        // nb: with some JVMs (eg JDK 1.4.1 on Windows), 
        //     the transformer needs to be reinitialized each time, in order to 
        //     run multiple :r FN commands in tccg 
        if (transformer == null) {
            transformer = stf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            try { // also try setting indent as a xalan property 
                transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            } catch (IllegalArgumentException exc) {} // ignore
        }
        // set up apml transformer 
        if (apmlTransformer == null) {
            InputStream toApmlStr = ClassLoader.getSystemResourceAsStream("opennlp/ccg/grammar/to-apml.xsl");
            apmlTransformer = stf.newTransformer(new StreamSource(toApmlStr));
            // nb: DOCTYPE SYSTEM also specified in to-apml.xsl; including  
            //     redundant specification here to workaround omission of DOCTYPE with Linux 1.5 JVM
            apmlTransformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "apml.dtd");
        }
    }
    
    
    // does setup for LF from XML transformation, and returns a SAXSource for the given input stream
    // nb: need a new filter chain one for each use (perhaps due to an underyling bug)
    private SAXSource fromXmlSetup(InputStream istream) throws IOException {
        try {
            // initialize transformer
            initializeTransformers();
            // load transformations
            if (fromXmlTemplates == null) {
                fromXmlTemplates = new Templates[fromXmlTransforms.length];
                for (int i = 0; i < fromXmlTemplates.length; i++) {
                    String url = fromXmlTransforms[i].toString();
                    fromXmlTemplates[i] = stf.newTemplates(new StreamSource(url));
                }
            }
            // set up initial reader
            SAXParser parser = spf.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            // set up chain of filters
            XMLFilter[] filters = new XMLFilter[fromXmlTransforms.length];
            for (int i = 0; i < filters.length; i++) {
                // create filter
                filters[i] = stf.newXMLFilter(fromXmlTemplates[i]);
                // set parent
                if (i == 0) { filters[0].setParent(reader); }
                else { filters[i].setParent(filters[i-1]); }
            }
            // set final reader/filter
            XMLReader finalReader = (filters.length == 0) ? reader : filters[filters.length-1];
            // set up and return LF from XML SAX source with final reader/filter
            return new SAXSource(finalReader, new InputSource(istream));
        } catch (ParserConfigurationException pce) {
            throw (IOException) new IOException().initCause(pce);
        } catch (SAXException se) {
            throw (IOException) new IOException().initCause(se);
        } catch (TransformerConfigurationException tce) {
            throw (IOException) new IOException().initCause(tce);
        }
    }
    
    /**
     * Loads a document from the XML in the given input stream, 
     * applying the configured from-XML transformations.
     */
    public synchronized Document loadFromXml(InputStream istream) throws IOException {
        try {
            // do setup and get source
            Source source = fromXmlSetup(istream);
            // do transformation
            JDOMResult result = new JDOMResult();
            transformer.transform(source, result);
            // return result doc
            return result.getDocument();
        } catch (TransformerException exc) { 
            throw (IOException) new IOException().initCause(exc);
        }
    }
    
    /**
     * Loads a document from the XML file with the given filename, 
     * applying the configured from-XML transformations.
     */
    public synchronized Document loadFromXml(String filename) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
        Document retval = loadFromXml(bis);
        bis.close();
        return retval;
    }
    

    // does setup for LF to XML transformation, and returns a SAXSource for the given source
    // nb: need a new filter chain one for each use (perhaps due to an underyling bug)
    private SAXSource toXmlSetup(Source source) throws IOException {
        try {
            // initialize transformer
            initializeTransformers();
            // load transformations
            if (toXmlTemplates == null) {
                toXmlTemplates = new Templates[toXmlTransforms.length];
                for (int i = 0; i < toXmlTemplates.length; i++) {
                    // File file = new File(toXmlTransforms[i]);
                    // toXmlTemplates[i] = stf.newTemplates(new StreamSource(file));
                    String url = toXmlTransforms[i].toString();
                    toXmlTemplates[i] = stf.newTemplates(new StreamSource(url));
                }
            }
            // set up initial reader
            SAXParser parser = spf.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            // set up chain of filters
            XMLFilter[] filters = new XMLFilter[toXmlTransforms.length];
            for (int i = 0; i < filters.length; i++) {
                // create filter
                filters[i] = stf.newXMLFilter(toXmlTemplates[i]);
                // set parent
                if (i == 0) { filters[0].setParent(reader); }
                else { filters[i].setParent(filters[i-1]); }
            }
            // set final reader/filter
            XMLReader finalReader = (filters.length == 0) ? reader : filters[filters.length-1];
            // set up and return LF to XML SAX source with final reader/filter
            return new SAXSource(finalReader, SAXSource.sourceToInputSource(source));
        } catch (ParserConfigurationException pce) {
            throw (IOException) new IOException().initCause(pce);
        } catch (SAXException se) {
            throw (IOException) new IOException().initCause(se);
        } catch (TransformerConfigurationException tce) {
            throw (IOException) new IOException().initCause(tce);
        }
    }

    /**
     * Saves the given LF with the given target string to an XML file 
     * with the given filename, applying the configured to-XML
     * transformations.
     */
    public synchronized void saveToXml(LF lf, String target, String filename) throws IOException { 
        // ensure dirs exist for filename
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) { parent.mkdirs(); }
        FileOutputStream out = new FileOutputStream(file); 
        saveToXml(lf, target, out);
        out.close();
    }

    /**
     * Saves the given LF with the given target string as XML to the 
     * given output stream, applying the configured to-XML
     * transformations.
     */
    public synchronized void saveToXml(LF lf, String target, OutputStream out) throws IOException { 
        // make doc with XML for LF and target
        Document doc = new Document();
        Element root = new Element("xml");
        doc.setRootElement(root);
        root.addContent(HyloHelper.toXml(lf));
        Element targetElt = new Element("target");
        targetElt.addContent(target);
        root.addContent(targetElt);

        // write transformed doc to file
        try {
            // do setup and get source
            Source source = toXmlSetup(new JDOMSource(doc));
            // do transformation
            transformer.transform(source, new StreamResult(new OutputStreamWriter(out)));
        } catch (TransformerException exc) { 
            throw (IOException) new IOException().initCause(exc);
        }
    }

    
    /**
     * Transforms an LF by applying the configured to-XML and from-XML transformations, 
     * then loading the LF from the resulting doc.
     */
    public synchronized LF transformLF(LF lf) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saveToXml(lf, "", out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Document doc = loadFromXml(in);
        return Realizer.getLfFromDoc(doc);
    }
    
    /**
     * Loads an LF by applying the configured from-XML transformations, 
     * then loading the LF from the resulting doc.
     */
    public synchronized LF loadLF(Document doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializeXml(doc, out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Document doc2 = loadFromXml(in);
        return Realizer.getLfFromDoc(doc2);
    }
    
    
    /**
     * Convenience method to serialize XML.
     */
    public synchronized void serializeXml(Document doc, OutputStream out) throws IOException {
        try {
            initializeTransformers();
            JDOMResult result = new JDOMResult(); // as suggested by Amy Isard, for better java/xml version compatibility
            transformer.transform(new JDOMSource(doc), result);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(result.getDocument(), new OutputStreamWriter(out)); // end of A.I. suggestion
        } catch (TransformerException exc) { 
            throw (IOException) new IOException().initCause(exc);
        }
    }

    
    /** 
     * Makes an element for the given LF, applying the configured to-XML transformations.
     */
    public synchronized Element makeLfElt(LF lf) throws IOException { 
        // make doc with LF in it
        Document lfDoc = new Document();
        lfDoc.setRootElement(HyloHelper.toXml(lf));
        // apply to-XML transformations
        try {
            // do setup and get source
            Source source = toXmlSetup(new JDOMSource(lfDoc));
            // do transformation and get resulting doc
            JDOMResult result = new JDOMResult();
            transformer.transform(source, result);
            lfDoc = result.getDocument();
        } catch (TransformerException exc) { 
            throw (IOException) new IOException().initCause(exc);
        }
        return lfDoc.detachRootElement();
    }

    
    /** 
     * Returns whether the given string is a recognized pitch accent.
     */
    public static boolean isPitchAccent(String s) {
        if (pitchAccentsSet == null) {
            pitchAccentsSet = new HashSet<String>();
            for (int i = 0; i < pitchAccents.length; i++) {
                pitchAccentsSet.add(pitchAccents[i]);
            }
        }
        return pitchAccentsSet.contains(s);
    }
    
    /** 
     * Returns whether the given string is a recognized boundary tone. 
     */
    public static boolean isBoundaryTone(String s) {
        if (boundaryTonesSet == null) {
            boundaryTonesSet = new HashSet<String>();
            for (int i = 0; i < boundaryTones.length; i++) {
                boundaryTonesSet.add(boundaryTones[i]);
            }
        }
        return boundaryTonesSet.contains(s);
    }
    
    
    /**
     * Saves the given sign's words, pitch accents and boundary tones 
     * to an APML file with the given filename.
     */
    public synchronized void saveToApml(Sign sign, String filename) throws IOException {
        // ensure dirs exist for filename
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) { parent.mkdirs(); }
        // do transformation
        FileWriter fw = new FileWriter(file);
        saveToApml(sign, fw);
        fw.close();
    }
    
    /**
     * Saves the given sign's words, pitch accents and boundary tones 
     * as APML to the given writer.
     * The orthography is first converted to XML using Sign.getWordsInXml, 
     * and then converted to APML using opennlp/ccg/grammar/to-apml.xsl.
     * The string is assumed to be a single performative.
     */
    public synchronized void saveToApml(Sign sign, Writer writer) throws IOException { 
        // convert words
        Document doc = sign.getWordsInXml();
        // write transformed doc to file
        try {
            // do setup and get source
            initializeTransformers();
            Source source = new JDOMSource(doc);
            // do transformation
            apmlTransformer.transform(source, new StreamResult(writer));
        } catch (TransformerException exc) { 
            throw (IOException) new IOException().initCause(exc);
        }
    }
    
    
    /** 
     * Returns the words for the given string, as determined by its 
     * first parse, or an empty list, if it cannot be parsed.
     */
    // NB: Could try to extend this to find the parse with the intended LF.
    public List<Word> getParsedWords(String s) {
        // ensure parser instantiated
        if (parser == null) parser = new Parser(this);
        // get parses
        try {
            parser.parse(s);
        }
        catch (ParseException pe) {
            return new ArrayList<Word>(0);
        }
        List<Sign> parses = parser.getResult();
        // return words of first parse
        Sign sign = parses.get(0);
        return sign.getWords();
    }


	/**
	* Returns the name of the loaded grammar. Null if no name given.
	*/
	public final String getName() {
		return grammarName;
	}
	
	
    /**
     * Writes the list of words to a basic morph file.
     * @throws IOException 
     */
    public void toMorphXml(List<Word> words, String filename) throws IOException {
    	Collections.sort(words);
    	XMLOutputter xout = new XMLOutputter();
    	xout.setFormat(Format.getPrettyFormat());
    	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	out.println("<morph name=\"" + grammarName + "\">");
    	for (Word w : words) {
    		Element e = new Element("entry");
    		e.setAttribute("word", w.getForm());
    		if (w.getForm() != w.getStem() && w.getStem() != null) e.setAttribute("stem", w.getStem());
    		if (w.getPOS() != null) e.setAttribute("pos", w.getPOS());
    		if (w.getSemClass() != null) e.setAttribute("class", w.getSemClass());
    		xout.output(e, out); out.println();
    	}
    	out.println("</morph>");
    	out.flush(); out.close();
    }
	
    /**
     * Writes the list of categories and associated POS tags to a basic lexicon file.
     * Note that the LFs are expected to have a [*DEFAULT*] proposition in the 
     * desired location for predicate insertion.
     * @throws IOException 
     */
    public void toLexiconXml(List<Category> cats, List<String> POSs, String filename) throws IOException {
    	// create map from supertags with unique suffixes to cat/pos pairs
    	Map<String,Pair<Category,String>> stagMap = new HashMap<String,Pair<Category,String>>();
    	for (int i=0; i < cats.size(); i++) {
    		Category cat = cats.get(i); String pos = POSs.get(i);
    		String stag = cat.getSupertag();
    		if (stagMap.containsKey(stag)) {
        		int j = 1;
    			while (stagMap.containsKey(stag+"-"+j)) j++;
    			stag += "-"+j;
    		}
    		stagMap.put(stag, new Pair<Category,String>(cat, pos));
    	}
    	List<String> keys = new ArrayList<String>(stagMap.keySet());
    	Collections.sort(keys);
    	XMLOutputter xout = new XMLOutputter();
    	xout.setFormat(Format.getPrettyFormat());
    	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	out.println("<lexicon name=\"" + grammarName + "\">");
    	for (String key : keys) {
    		Pair<Category,String> p = stagMap.get(key);
    		Category cat = p.a; String pos = p.b;
    		Element fam = new Element("family");
    		fam.setAttribute("name", key);
    		fam.setAttribute("pos", pos);
    		Element ent = new Element("entry");
    		ent.setAttribute("name", "1");
    		fam.addContent(ent);
    		ent.addContent(cat.toXml());
    		xout.output(fam, out); out.println();
    	}
    	out.println("</lexicon>");
    	out.flush(); out.close();
    }
}

