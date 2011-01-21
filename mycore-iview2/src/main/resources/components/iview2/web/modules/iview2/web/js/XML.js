/**
 * @public
 * @function
 * @name	namespaceCheck
 * @memberOf	iview.XML
 * @description	checks if the browser supports Namespaces within getElementsByTagName, if not the namespace will be parsed out
 * @param	{string} name the Namespace name which needs to be checked
 * @return	{string} name the Namespace name special convert for the browser
 */
function namespaceCheck(name) {
	if (isBrowser(["Opera", "Firefox/2", "Safari", "Mozilla/5"]) && name.indexOf(":") != -1) {
		return name.substring(name.indexOf(":")+1,name.length);
	}
	return name;
}

//TODO checken, ob das so richtig
/**
 * @public
 * @function
 * @name	attributeCheck
 * @memberOf	iview.XML
 * @description	checks if the browser supports attributes within getElementsByTagName, if not the attributes will be parsed out
 * @param	{string} name the attribute name which needs to be checked
 * @return	{string} name the attribute name special convert for the browser
 */
function attributeCheck(name) {
	if (isBrowser(["Opera", "Firefox/2"]) && name.indexOf(":") != -1) {
		return name.substring(name.indexOf(":")+1,name.length);
	}
	return name;
}

//TODO Doku ergänzen
/**
 * @public
 * @function
 * @name	nodeAttributes
 * @memberOf	iview.XML
 * @description	collects into an array all attributes of a supplied Node, where the name of the object attribute is the attributes name,
 cleaned from any namespace things like regex: .*:
 * @param	{object} node the node to collect all properties from
 * @return	{} an array with all Informations the node contained as attributes
 */
function nodeAttributes(node) {
	var attributes = new Object();
	for (var i = 0; i < node.attributes.length; i++) {
		//Remove the Namespace, as this is hindering access laterly
		attributes[node.attributes.item(i).nodeName.replace(/^.*:/,"")] = node.attributes.item(i).value;
	}
	return attributes;
}

//TODO Doku ergänzen	
/**
 * @public
 * @function
 * @name		getNodes
 * @memberOf	iview.XML
 * @description	Gains the requested information from a given XML File
 * @param	{object} xmlfile XML file from which the data will be extruded
 * @param	{string} nodeName the Node of the XML File which shall be gained, can be a group of nodes
 * @return	{} List of all Nodes with the given NodeName
 */
function getNodes(xmlfile, nodeName) {
	var nodes;
	if (typeof arguments[2] === "undefined") {
		nodes = xmlfile.getElementsByTagName(namespaceCheck(nodeName));
	} else {
		if (isBrowser("IE")) {
			nodes = arguments[2].getElementsByTagName(namespaceCheck(nodeName));
		} else {
			nodes = xmlfile.getElementsByTagName(namespaceCheck(nodeName), arguments[2]);
		}
	}
	var nodeList = new Array();
	for ( i = 0; i < nodes.length; i++) {
		try {
			nodeList[i] = document.importNode(nodes.item(i), true);
		} catch (e) {
			nodeList[i] = nodes.item(i);
		}
	}
	try {
		return nodeList;
	} finally {
		nodeList = null;
	}
}