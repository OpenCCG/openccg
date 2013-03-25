/*
 * $Id: lexicon.js,v 1.2 2006/12/13 19:25:22 coffeeblack Exp $
 * Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 */
function toggleFeatures(elem) {
	elem.className = (elem.className == "expanded")
		? "" : "expanded";
	
	var anchors = elem.getElementsByTagName("a");
	anchors[0].innerHTML = (elem.className == "expanded")
		? "[-]" : "[+]";
	
	anchors[0].setAttribute("title", 
		((elem.className == "expanded")
			? "collapse" : "expand") + " feature structures");
}