/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;
import org.fife.ui.autocomplete.Util;

import ca.phon.ipa.features.Feature;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PhonexPluginManager;
import ca.phon.phonex.PluginProvider;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Autocomplete provider for rsyntaxtextarea phonex editor.
 *
 */
public class PhonexAutocompleteProvider extends DefaultCompletionProvider {
	
	private final static int FEATURE_RELEVANCE = 1;
	private List<Completion> featureCompletions;
	
	private final static int PHONE_CLASS_RELEVANCE = 200;
	private final static String[] PHONE_CLASSES = {
		".", "Anything",
		"\u03c3", "Syllable (or remainder of)",
		"\\c", "Any consonant",
		"\\v", "Any vowel",
		"\\s", "Stress (primary or secondary)",
		"\\w", "Any consonant or vowel - same as [\\c\\v]",
		"\\W", "Any non-word character - same as [^\\w]",
		"\\b", "Word boundary including beginning of input, end of input, and whitespace.",
		"\\S", "A syllable boundary including implicit boundaries",
		"\\p", "Intra-word pause (i.e., '^')",
		"\\P", "Inter-word pause (i.e., '(.)')"
	};
	private List<Completion> phoneClassCompletions;
	
	private final static int PLUGIN_RELEVANCE = 2;
	private List<Completion> pluginCompletions;
	
	private final static int SCTYPE_RELEVANCE = 150;
	private List<Completion> scTypeCompletions;

	private final static int STRESS_TYPE_RELEVANCE = 125;
	private final static String[] STRESS_TYPES = {
		"!1", "primary stress",
		"!2", "secondary stress",
		"!A", "any stress",
		"!U", "unstressed"
	};
	private List<Completion> stressTypeCompletions;
	
	private final static int QUANTIFIER_RELEVANCE = 175;
	private final static String[] QUANTIFIERS = {
		"*", "zero or more",
		"+", "one or more",
		"?", "zero or one",
		
		"*+", "zero or more (possessive)",
		"++", "one or more (possessive)",
		"?+", "zero or one (possessive)",
		
		"*?", "zero or more (reluctant)",
		"+?", "one or more (reluctant)",
		"??", "zero or one (reluctant)"
	};
	private List<Completion> quantifierCompletions;
	
	private final static int GROUP_TEMPLATE_RELEVANCE = 100;
	private final static String[] GROUP_TEMPLATES = {
		"\u03c3/${start}..${end}/", "\u03c3/", "\u03c3/sctype..sctype/ - syllable range",
		"{${cursor}}", "{", "{} - feature set",
		"(${cursor})", "(", "() - group",
		"(${name}=${cursor})", "(=", "(${name}=) - named group",
		"\\${groupnum}", "\\", "\\${groupnum} - back reference",
		"(?=${cursor})", "(?=", "(?=) - non-capturing group",
		"(?<${cursor})", "(?<", "(?<) - look behind",
		"(?>${cursor})", "(?>", "(?>) - look ahead",
		"[${cursor}]", "[", "[] - phone class",
		"[^${cursor}]", "[^", "[^] - negated phone class"
	};
	private List<Completion> groupCompletions;
	
	public PhonexAutocompleteProvider() {
		super();
		
		addCompletions(getFeatureCompletions());
		addCompletions(getPluginCompletions());
		addCompletions(getScTypeCompletions());
		addCompletions(getStressTypeCompletions());
		addCompletions(getPredefinedPhoneClassCompletions());
		addCompletions(getGroupTemplateCompletions());
		addCompletions(getQuantifierCompletions());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected List<Completion> getCompletionsImpl(JTextComponent comp) {

		List<Completion> retVal = new ArrayList<Completion>();
		String text = getAlreadyEnteredText(comp);

		if (text!=null) {

			int index = Collections.binarySearch(completions, text, comparator);
			if (index<0) { // No exact match
				index = -index - 1;
			}
			else {
				// If there are several overloads for the function being
				// completed, Collections.binarySearch() will return the index
				// of one of those overloads, but we must return all of them,
				// so search backward until we find the first one.
				int pos = index - 1;
				while (pos>0 &&
						comparator.compare(completions.get(pos), text)==0) {
					retVal.add(completions.get(pos));
					pos--;
				}
			}

			while (index<completions.size()) {
				Completion c = completions.get(index);
				if (Util.startsWithIgnoreCase(c.getInputText(), text)) {
					retVal.add(c);
				}
				index++;
			}

		}

		return retVal;
	}

	@Override
	public String getAlreadyEnteredText(JTextComponent comp) {		
		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && 
				(isValidChar(seg.array[start])
						|| seg.array[start] == '\\'
						|| seg.array[start] == ':'
						|| seg.array[start] == '!') ) {
			char ch = seg.array[start];
			start--;
			if(ch == '\\' | ch == ':' || ch == '!') break;
		}
		start++;

		len = segEnd - start;
		
		if(len == 0) {
			// group/feature set/phone class prompts
			start = segEnd - 1;
			while (start>=seg.offset && 
					(seg.array[start] == '('
					|| seg.array[start] == '{'
					|| seg.array[start] == '['
					|| seg.array[start] == '^'
					|| seg.array[start] == '?'
					|| seg.array[start] == '='
					|| seg.array[start] == '<'
					|| seg.array[start] == '>') ) {
				start--;
			}
			start++;
			
			len = segEnd - start;
		}

		if(len == 0) {
			// quantifiers
			start = segEnd - 1;
			while (start>=seg.offset && 
					(seg.array[start] == '*'
							|| seg.array[start] == '?'
							|| seg.array[start] == '+') ) {
				start--;
			}
			start++;
			
			len = segEnd - start;
		}
		
		
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);
	}
	
	

	public List<Completion> getPluginCompletions() {
		if(pluginCompletions == null) {
			pluginCompletions = new ArrayList<>();
			final PhonexPluginManager ppm = PhonexPluginManager.getSharedInstance();
			for(PluginProvider provider:ppm.getPluginProviders()) {
				final PhonexPlugin annotation = provider.getClass().getAnnotation(PhonexPlugin.class);
				if(annotation != null) {
					final StringBuffer buffer = new StringBuffer();
					buffer.append(":").append(annotation.name());
					buffer.append('(');
					for(int i = 0; i < annotation.arguments().length; i++) {
						String arg = annotation.arguments()[i];
						if(i > 0) buffer.append(',');
						buffer.append('"').append("${").append(arg).append("}\"");
					}
					buffer.append(')');
					
					final TemplateCompletion tc = new TemplateCompletion(this, ":" + annotation.name(),
							buffer.toString() + " - " + annotation.description(), buffer.toString());
					tc.setRelevance(PLUGIN_RELEVANCE);
					pluginCompletions.add(tc);
				}
			}
		}
		return pluginCompletions;
	}
	
	/**
	 * Add group template completions
	 */
	public List<Completion> getGroupTemplateCompletions() {
		if(groupCompletions == null) {
			groupCompletions = new ArrayList<>();
			for(int i = 0; i < GROUP_TEMPLATES.length; i += 3) {
				final String template = GROUP_TEMPLATES[i];
				final String inputText = GROUP_TEMPLATES[i+1];
				final String def = GROUP_TEMPLATES[i+2];
				
				TemplateCompletion tc = new TemplateCompletion(this, inputText, def, template);
				tc.setRelevance(GROUP_TEMPLATE_RELEVANCE + GROUP_TEMPLATES.length - i);
				groupCompletions.add(tc);
			}
		}
		return groupCompletions;
	}
	
	/**
	 * Add predefined phone class completions.
	 */
	public List<Completion> getPredefinedPhoneClassCompletions() {
		if(phoneClassCompletions == null) {
			phoneClassCompletions = new ArrayList<>();
			for(int i = 0; i < PHONE_CLASSES.length; i += 2) {
				final String completion = PHONE_CLASSES[i];
				final String shortDesc = PHONE_CLASSES[i+1];
				
				BasicCompletion pcCompletion = new BasicCompletion(this, completion);
				pcCompletion.setShortDescription(shortDesc);
				pcCompletion.setRelevance(PHONE_CLASS_RELEVANCE + PHONE_CLASSES.length - i);
				phoneClassCompletions.add(pcCompletion);
			}
		}
		return phoneClassCompletions;
	}
	
	/**
	 * Add feature name and synonym completions.
	 */
	public List<Completion> getFeatureCompletions() {
		if(featureCompletions == null) {
			featureCompletions = new ArrayList<>();
			final FeatureMatrix fm = FeatureMatrix.getInstance();
			
			for(Feature feature:fm.getFeatureData()) {
				BasicCompletion featureCompletion = new BasicCompletion(this, feature.getName());
				featureCompletion.setShortDescription("feature name");
				featureCompletion.setRelevance(FEATURE_RELEVANCE);
				featureCompletions.add(featureCompletion);
				for(String syn:feature.getSynonyms()) {
					BasicCompletion synCompletion = new BasicCompletion(this, syn);
					synCompletion.setShortDescription("synonym for " + feature.getName());
					synCompletion.setRelevance(FEATURE_RELEVANCE);
					featureCompletions.add(synCompletion);
				}
			}
		}
		return featureCompletions;
	}
	
	public List<Completion> getScTypeCompletions() {
		if(scTypeCompletions == null) {
			scTypeCompletions = new ArrayList<>();
			
			for(SyllableConstituentType scType:SyllableConstituentType.values()) {
				if(scType == SyllableConstituentType.UNKNOWN
						|| scType == SyllableConstituentType.WORDBOUNDARYMARKER
						|| scType == SyllableConstituentType.SYLLABLEBOUNDARYMARKER
						|| scType == SyllableConstituentType.SYLLABLESTRESSMARKER)
					continue;
				final BasicCompletion scCompletion = new BasicCompletion(this, ":" + scType.getIdChar());
				scCompletion.setRelevance(SCTYPE_RELEVANCE + SyllableConstituentType.values().length - scType.ordinal());
				scCompletion.setShortDescription(scType.name().toLowerCase());
				scTypeCompletions.add(scCompletion);
			}
			// add diphthong
			final BasicCompletion scCompletion = new BasicCompletion(this, ":D");
			scCompletion.setRelevance(SCTYPE_RELEVANCE + SyllableConstituentType.values().length - SyllableConstituentType.NUCLEUS.ordinal());
			scCompletion.setShortDescription("diphthong");
			scTypeCompletions.add(scCompletion);
		}
		return scTypeCompletions;
	}
	
	public List<Completion> getStressTypeCompletions() {
		if(stressTypeCompletions == null) {
			stressTypeCompletions = new ArrayList<>();
			for(int i = 0; i < STRESS_TYPES.length; i += 2) {
				final String text = STRESS_TYPES[i];
				final String shortDesc = STRESS_TYPES[i+1];
				
				BasicCompletion completion = new BasicCompletion(this, text);
				completion.setShortDescription(shortDesc);
				completion.setRelevance(STRESS_TYPE_RELEVANCE + STRESS_TYPES.length - i);
				stressTypeCompletions.add(completion);
			}
			
		}
		return stressTypeCompletions;
	}
	
	public List<Completion> getQuantifierCompletions() {
		if(quantifierCompletions == null) {
			quantifierCompletions = new ArrayList<>();
			for(int i = 0; i < QUANTIFIERS.length; i += 2) {
				final String text = QUANTIFIERS[i];
				final String shortDesc = QUANTIFIERS[i+1];
				
				BasicCompletion completion = new BasicCompletion(this, text);
				completion.setShortDescription(shortDesc);
				completion.setRelevance(QUANTIFIER_RELEVANCE + QUANTIFIERS.length - i);
				quantifierCompletions.add(completion);
			}
		}
		
		final String template = "<${min},${max}>";
		final String desc = template + " - bounded quantifier";
		// bounded quantifier template
		final TemplateCompletion completion = new TemplateCompletion(this, "<", desc, template);
		completion.setRelevance(QUANTIFIER_RELEVANCE);
		quantifierCompletions.add(completion);
		
		return quantifierCompletions;
	}

}
