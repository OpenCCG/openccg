///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Michael White
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

package opennlp.ccg.synsem;

/**
 * Interface for signs scorers with both a base model and a full (reranking) model.
 * The implementation of the score(Sign, boolean) method should vary according to the 
 * full model flag. The base model should be the default. 
 */
public interface ReRankingScorer extends SignScorer {
	/** Sets flag for using full (vs. base) model. The base model should be the default. */
	public void setFullModel(boolean on);
}
