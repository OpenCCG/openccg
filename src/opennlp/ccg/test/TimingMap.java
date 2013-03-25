///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 University of Edinburgh (Michael White)
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
package opennlp.ccg.test;

import java.util.*;

import org.jdom.*;

/**
 * Utility class for managing average times per number of nodes.
 *
 * @author  Michael White
 * @version $Revision: 1.5 $, $Date: 2009/12/21 04:18:31 $
 */
public class TimingMap {
    
    private String label;
    private List<Integer> times = new ArrayList<Integer>();
    private HashMap<Integer,List<Integer>> map = new HashMap<Integer,List<Integer>>();
    
    /** Constructor, with label. */
    public TimingMap(String label) {
        this.label = label;
    }
    
    /** Adds a number, time pair. */
    public void add(int num, int time) {
        Integer timeInt = new Integer(time);
        times.add(timeInt); 
        Integer key = new Integer(num);
        List<Integer> timesPerNum = map.get(key);
        if (timesPerNum == null) {
            timesPerNum = new ArrayList<Integer>();
            map.put(key, timesPerNum);
        }
        timesPerNum.add(timeInt);
    }
    
    /** Returns the mean time. */
    public double mean() {
        int total = 0;
        for (int i = 0; i < times.size(); i++) {
            Integer time = times.get(i);
            total += time.intValue();
        }
        return (1.0 * total) / times.size(); 
    }
    
    /** Returns the standard deviation. */
    public double sigma() {
        if (times.size() < 2) return -1; // NA
        double mean = mean();
        double numerator = 0;
        for (int i = 0; i < times.size(); i++) {
            Integer time = times.get(i);
            numerator += Math.pow(time.intValue() - mean, 2);
        }
        int denominator = times.size() - 1;
        return Math.sqrt(numerator / denominator);
    }
    
    /** Saves the times and times per number (with average) as XML elements under the given one. */
    public void saveTimes(Element root) {
        Element timesElt = new Element("times");
        root.addContent(timesElt);
        timesElt.setAttribute("label", label);
        Element listElt = new Element("list");
        timesElt.addContent(listElt);
        listElt.setAttribute("mean", "" + mean());
        listElt.setAttribute("sigma", "" + sigma());
        for (int i = 0; i < times.size(); i++) {
            Element timeElt = new Element("time");
            listElt.addContent(timeElt);
            timeElt.setAttribute("val", times.get(i).toString());
        }
        Element perNumsElt = new Element("per-nums");
        timesElt.addContent(perNumsElt);
        Set<Integer> keys = map.keySet();
        List<Integer> nums = new ArrayList<Integer>(keys.size());
        nums.addAll(keys);
        Collections.sort(nums);
        int min = nums.get(0).intValue();
        int max = nums.get(nums.size()-1).intValue();
        for (int num = min; num <= max; num++) {
            Element perNumElt = new Element("per");
            perNumsElt.addContent(perNumElt);
            perNumElt.setAttribute("num", "" + num);
            Integer numKey = new Integer(num);
            List<Integer> timesPer = map.get(numKey);
            if (timesPer == null) { 
                perNumElt.setAttribute("count", "0");
                continue; 
            }
            int sum = 0;
            int count = timesPer.size();
            perNumElt.setAttribute("count", "" + count);
            for (int i = 0; i < timesPer.size(); i++) {
                Integer time = (Integer) timesPer.get(i);
                sum += time.intValue();
                Element timeElt = new Element("time");
                perNumElt.addContent(timeElt);
                timeElt.setAttribute("val", time.toString());
            }
            double mean = (sum * 1.0) / count;
            perNumElt.setAttribute("mean", "" + mean);
        }
    }
}


