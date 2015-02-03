package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * A lexicon builder that reads in a series of families among other objects. The format comes from
 * previous versions of OpenCCG.
 *
 * @author Daniel Couto-Vale
 */
public class LexiconBuilderStd implements LexiconBuilder {

	private final LexiconObject lexicon = new LexiconObject();

	@Override
	public final void makeItem(Element element) {
		String name = element.getName();
		if (name.equals("family")) {
			makeFamilyItem(element);
		} else if (name.equals("distributive-features")) {
			makeDistributiveFeatures(element);
		} else if (name.equals("licensing-features")) {
			makeLicensingFeatures(element);
		} else if (name.equals("relation-sorting")) {
			makeRelationSorting(element);
		}
	}

	@Override
	public final void makeRelationSorting(Element element) {
		String orderAttr = element.getAttributeValue("order");
		String[] relSortOrder = orderAttr.split("\\s+");
		for (int i = 0; i < relSortOrder.length; i++) {
			lexicon.relationIndexMap.put(relSortOrder[i], new Integer(i));
		}
	}

	@Override
	public final void makeLicensingFeatures(Element element) {
		List<LicensingFeature> licensingFeats = new ArrayList<LicensingFeature>();
		boolean containsLexFeat = false;
		if (element != null) {
			for (@SuppressWarnings("unchecked")
			Iterator<Element> it = element.getChildren("feat").iterator(); it.hasNext();) {
				Element featElt = it.next();
				String attr = featElt.getAttributeValue("attr");
				if (attr.equals("lex"))
					containsLexFeat = true;
				String val = featElt.getAttributeValue("val");
				List<String> alsoLicensedBy = null;
				String alsoVals = featElt.getAttributeValue("also-licensed-by");
				if (alsoVals != null) {
					alsoLicensedBy = Arrays.asList(alsoVals.split("\\s+"));
				}
				boolean licenseEmptyCats = true;
				boolean licenseMarkedCats = false;
				boolean instantiate = true;
				byte loc = LicensingFeature.BOTH;
				String lmc = featElt.getAttributeValue("license-marked-cats");
				if (lmc != null) {
					licenseMarkedCats = Boolean.valueOf(lmc).booleanValue();
					// change defaults
					licenseEmptyCats = false;
					loc = LicensingFeature.TARGET_ONLY;
					instantiate = false;
				}
				String lec = featElt.getAttributeValue("license-empty-cats");
				if (lec != null) {
					licenseEmptyCats = Boolean.valueOf(lec).booleanValue();
				}
				String inst = featElt.getAttributeValue("instantiate");
				if (inst != null) {
					instantiate = Boolean.valueOf(inst).booleanValue();
				}
				String locStr = featElt.getAttributeValue("location");
				if (locStr != null) {
					if (locStr.equals("target-only"))
						loc = LicensingFeature.TARGET_ONLY;
					if (locStr.equals("args-only"))
						loc = LicensingFeature.ARGS_ONLY;
					if (locStr.equals("both"))
						loc = LicensingFeature.BOTH;
				}
				licensingFeats.add(new LicensingFeature(attr, val, alsoLicensedBy,
						licenseEmptyCats, licenseMarkedCats, instantiate, loc));
			}
		}
		if (!containsLexFeat) {
			licensingFeats.add(LicensingFeature.defaultLexFeature);
		}
		lexicon.licensingFeatures = new LicensingFeature[licensingFeats.size()];
		licensingFeats.toArray(lexicon.licensingFeatures);
	}

	@Override
	public final void makeDistributiveFeatures(Element element) {
		String distributiveFeaturesAttrs = element.getAttributeValue("attrs");
		lexicon.distributiveFeatures = distributiveFeaturesAttrs.split("\\s+");
	}

	@Override
	public final void makeFamilyItem(Element element) {
		try {
			lexicon.families.add(new Family(element));
		} catch (RuntimeException exc) {
			System.err.println("Skipping family: " + element.getAttributeValue("name"));
			System.err.println(exc.toString());
		}
	}

	@Override
	public final LexiconObject buildLexicon() {
		return lexicon;
	}

}
