///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.supertagger.ml;

import opennlp.ccg.parse.tagger.TaggedWord;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.1 $, $Date: 2010/09/21 04:12:41 $
 */
public interface FeatureExtractor {
	
    /** 
     * @param sentence A {@code Map<Integer,Word>} giving the (string-indexed) sentence of
     *  {@code Word}s to be tagged.
     * @param wordIndex An {@code Integer}, giving the string index of the current word.
     * @return A {@code Collection<Pair<String,Double>><String>} representing the 
     * real-valued activations of features (predicates) in the context of a word to be labelled.
     */
    public Collection<Pair<String,Double>> getFeatures(Map<Integer,TaggedWord> sentence, Integer wordIndex);
    
     /** 
     * Same as getFeatures, but for the whole sentence, returning a List of contextual features, in order, one
      * per <code>Word</code> in <tt>sentence</tt>
     */
    public List<Collection<Pair<String,Double>>> getSentenceFeatures(Map<Integer,TaggedWord> sentence);
    
    /** 
     * @param sentence A <code>Map<Integer,Word></code> giving the (string-indexed) sentence of
     *  <tt>Word</tt>s to be tagged.
     * @param wordIndex An <code>Integer</code>, giving the string index of the current word.
     * @param training A boolean indicating whether we are extracting features for training (in which case
     * we need the label too).
     * @return A <code>Collection<Pair<String,Double>><String></code> representing the 
     * real-valued activations of features (predicates) in the context of a word to be labelled.
     */
    public Collection<Pair<String,Double>> getFeatures(Map<Integer,TaggedWord> sentence, Integer wordIndex, boolean training);
    
     /** 
     * Same as getFeatures, but for the whole sentence, returning a List of contextual features, in order, one
      * per <code>Word</code> in <tt>sentence</tt>
     */
    public List<Collection<Pair<String,Double>>> getSentenceFeatures(Map<Integer,TaggedWord> sentence, boolean training);
}
