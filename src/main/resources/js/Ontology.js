/* see also definitions in de.fraunhofer.fit.owljson.owldoc.JSONDataModelGenerator.java,
 definitions there should be in sync with definitions here */
var CLASSES_KEY="classes";

var OBJECT_PROPERTIES_KEY="objectProperties";

var DATATYPE_PROPERTIES_KEY="datatypeProperties";

var SUBCLASSES_KEY="subClasses";

var SUPERCLASSES_KEY="superClasses";

var EQUIVALENT_CLASSES_KEY="equivalentClasses";

var DISJOINT_CLASSES_KEY="disjointClasses";

var LOCAL_NAME_KEY="localName";

var LABEL_KEY="label";

var INDIVIDUALS_KEY="individuals";

var DOMAINS_KEY="domains";

var RANGES_KEY="ranges";

var SUBPROPERTIES_KEY="subProperties";

var SUPERPROPERTIES_KEY="superProperties";

var DISJOINT_PROPERTIES_KEY="disjointProperties";

var EQUIVALENT_PROPERTIES_KEY="equivalentProperties";

var INVERSE_PROPERTY_KEY="inverseProperty";

var MIN_CARDINALITY_KEY="minCardinality";

var MAX_CARDINALITY_KEY="maxCardinality";

/* ----------------------------------------------------------- */
/*                      Ontology                               */
/* ----------------------------------------------------------- */

function Ontology(data) {
  this.data = data;
}

Ontology.prototype.getClasses = function() {
  var res=new Array();
  var i=0;
  for(var cls in this.data[CLASSES_KEY]) {
    res[i]=new OntClass(this,this.data[CLASSES_KEY][cls],cls);
    i++;
  }
  return res;
}

Ontology.prototype.getClass = function(uri) {
  var clsdata=this.data[CLASSES_KEY][uri];
  if(clsdata!=null) {
    var clsobj=new OntClass(this,clsdata,uri);
    return clsobj;
  }
  return null;
}

Ontology.prototype.getObjectProperty = function(uri) {
  var prpdata=this.data[OBJECT_PROPERTIES_KEY][uri];
  if(prpdata!=null) {
    var prpobj=new OntObjectProperty(this,prpdata,uri);
    return prpobj;
  }
  return null;
}

Ontology.prototype.getDatatypeProperty = function(uri) {
  var prpdata=this.data[DATATYPE_PROPERTIES_KEY][uri];
  if(prpdata!=null) {
    var prpobj=new OntDatatypeProperty(this,prpdata,uri);
    return prpobj;
  }
  return null;
}

Ontology.prototype.getDatatypeProperties = function() {
  var res=new Array();
  for(var prop in this.data[DATATYPE_PROPERTIES_KEY]) {
    res.push(new OntDatatypeProperty(this,this.data[DATATYPE_PROPERTIES_KEY][prop],prop));
  }
  return res;
}

Ontology.prototype.getObjectProperties = function() {
  var res=new Array();
  for(var prop in this.data[OBJECT_PROPERTIES_KEY]) {
    res.push(new OntObjectProperty(this,this.data[OBJECT_PROPERTIES_KEY][prop],prop));
  }
  return res;
}


/* ----------------------------------------------------------- */
/*                      OntClass                               */
/* ----------------------------------------------------------- */

function OntClass(ontology, data, id) {
  this.id=id;
  this.data=data;
  this.ontology=ontology;
}

OntClass.prototype.getSubClasses = function() {
  if(this.subClasses==null && this.data[SUBCLASSES_KEY]!=null) {
    this.subClasses=getClassArray(this.ontology,SUBCLASSES_KEY,this.data);
  }
  return this.subClasses;
}

OntClass.prototype.getSuperClasses = function() {
  if(this.superClasses==null && this.data[SUPERCLASSES_KEY]!=null) {
    this.superClasses=getClassArray(this.ontology,SUPERCLASSES_KEY,this.data);
  }
  return this.superClasses;
}

OntClass.prototype.getEquivalentClasses = function() {
  if(this.equivClasses==null && this.data[EQUIVALENT_CLASSES_KEY]!=null) {
    this.equivClasses=getClassArray(this.ontology,EQUIVALENT_CLASSES_KEY,this.data);
  }
  return this.equivClasses;
}

OntClass.prototype.getDisjointClasses = function() {
  if(this.disjClasses==null && this.data[DISJOINT_CLASSES_KEY]!=null) {
    this.disjClasses=getClassArray(this.ontology,DISJOINT_CLASSES_KEY,this.data);
  }
  return this.disjClasses;
}

OntClass.prototype.getDatatypeProperties = function() {
  if(this.datatypeProperties==null) {
    this.datatypeProperties=new Array();
    var props=this.ontology.getDatatypeProperties();
    for(var i=0;i<props.length;i++) {
      if(idInObjectArray(this.id, props[i].getDomains())>=0) {
        this.datatypeProperties.push(props[i]);
      }
    }
  }
  return this.datatypeProperties;
}

OntClass.prototype.getObjectProperties = function() {
  if(this.objectProperties==null) {
    this.objectProperties=new Array();
    var props=this.ontology.getObjectProperties();
    for(var i=0;i<props.length;i++) {
      if(idInObjectArray(this.id, props[i].getDomains())>=0) {
        this.objectProperties.push(props[i]);
      }
    }
  }
  return this.objectProperties;
}

OntClass.prototype.getName = function() {
  return this.data[LOCAL_NAME_KEY][0];
}

OntClass.prototype.getLabel = function() {
  if(this.data[LABEL_KEY]!=null && this.data[LABEL_KEY].length==1) {
    return this.data[LABEL_KEY][0];
  } else {
    return this.data[LOCAL_NAME_KEY][0];
  }
}


OntClass.prototype.getLabels = function() {
  return this.data[LABEL_KEY];
}

OntClass.prototype.getId = function() {
  return this.id;
}

/* ----------------------------------------------------------- */
/*                      OntObjectProperty                      */
/* ----------------------------------------------------------- */

function OntObjectProperty(ontology, data, id) {
  this.id=id;
  this.data=data;
  this.ontology=ontology;
}


OntObjectProperty.prototype.getSubProperties = function() {
  if(this.subProperties==null && this.data[SUBPROPERTIES_KEY]!=null) {
    this.subProperties=getObjectPropertyArray(this.ontology,SUBPROPERTIES_KEY,this.data);
  }
  return this.subProperties;
}

OntObjectProperty.prototype.getSuperProperties = function() {
  if(this.superProperties==null && this.data[SUPERPROPERTIES_KEY]!=null) {
    this.superProperties=getObjectPropertyArray(this.ontology,SUPERPROPERTIES_KEY,this.data);
  }
  return this.superProperties;
}

OntObjectProperty.prototype.getEquivalentProperties = function() {
  if(this.equivProperties==null && this.data[EQUIVALENT_PROPERTIES_KEY]!=null) {
    this.equivProperties=getObjectPropertyArray(this.ontology,EQUIVALENT_PROPERTIES_KEY,this.data);
  }
  return this.equivProperties;
}

OntObjectProperty.prototype.getDisjointProperties = function() {
  if(this.disjProperties==null && this.data[DISJOINT_PROPERTIES_KEY]!=null) {
    this.disjProperties=getObjectPropertyArray(this.ontology,DISJOINT_PROPERTIES_KEY,this.data);
  }
  return this.disjProperties;
}

OntObjectProperty.prototype.getName = function() {
  return this.data[LOCAL_NAME_KEY];
}

OntObjectProperty.prototype.getLabel = function() {
  if(this.data[LABEL_KEY]!=null && this.data[LABEL_KEY].length==1) {
    return this.data[LABEL_KEY][0];
  } else {
    return this.data[LOCAL_NAME_KEY][0];
  }
}


OntObjectProperty.prototype.getLabels = function() {
  return this.data[LABEL_KEY];
}


OntObjectProperty.prototype.getDomains = function() {
  if(this.domains==null && this.data[DOMAINS_KEY]!=null) {
    this.domains=getClassArray(this.ontology,DOMAINS_KEY,this.data);
  }
  return this.domains;
}

OntObjectProperty.prototype.getRanges = function() {
  if(this.ranges==null && this.data[RANGES_KEY]!=null) {
    this.ranges=getClassArray(this.ontology,RANGES_KEY,this.data);
  }
  return this.ranges;
}

OntObjectProperty.prototype.getInverseProperties = function() {
  if(this.invProperties==null && this.data[INVERSE_PROPERTY_KEY]!=null) {
    this.invProperties=getObjectPropertyArray(this.ontology,INVERSE_PROPERTY_KEY,this.data);
  }
  return this.invProperties;
}

OntObjectProperty.prototype.getMinCardinality = function() {
  if(this.minCardinality==null && this.data[MIN_CARDINALITY_KEY]!=null) {
    this.minCardinality=getSingleInteger(this.data[MIN_CARDINALITY_KEY]);
  }
  return this.minCardinality;
}

OntObjectProperty.prototype.getMaxCardinality = function() {
  if(this.maxCardinality==null && this.data[MAX_CARDINALITY_KEY]!=null) {
    this.maxCardinality=getSingleInteger(this.data[MAX_CARDINALITY_KEY]);
  }
  return this.maxCardinality;
}

OntObjectProperty.prototype.getId = function() {
  return this.id;
}


/* ----------------------------------------------------------- */
/*                      OntDatatypeProperty                    */
/* ----------------------------------------------------------- */

function OntDatatypeProperty(ontology, data,id) {
  this.id=id;
  this.data=data;
  this.ontology=ontology;
}

OntDatatypeProperty.prototype.getSubProperties = function() {
  if(this.subProperties==null) {
    this.subProperties=getDatatypePropertyArray(this.ontology,SUBPROPERTIES_KEY,this.data);
  }
  return this.subProperties;
}

OntDatatypeProperty.prototype.getSuperProperties = function() {
  if(this.superProperties==null) {
    this.superProperties=getDatatypePropertyArray(this.ontology,SUPERPROPERTIES_KEY,this.data);
  }
  return this.superProperties;
}

OntDatatypeProperty.prototype.getEquivalentProperties = function() {
  if(this.equivProperties==null) {
    this.equivProperties=getDatatypePropertyArray(this.ontology,EQUIVALENT_PROPERTIES_KEY,this.data);
  }
  return this.equivProperties;
}

OntDatatypeProperty.prototype.getDisjointProperties = function() {
  if(this.disjProperties==null) {
    this.disjProperties=getDatatypePropertyArray(this.ontology,DISJOINT_PROPERTIES_KEY,this.data);
  }
  return this.disjProperties;
}

OntDatatypeProperty.prototype.getName = function() {
  return this.data[LOCAL_NAME_KEY];
}

OntDatatypeProperty.prototype.getLabel = function() {
  if(this.data[LABEL_KEY]!=null && this.data[LABEL_KEY].length==1) {
    return this.data[LABEL_KEY][0];
  } else {
    return this.data[LOCAL_NAME_KEY][0];
  }
}


OntDatatypeProperty.prototype.getLabels = function() {
  return this.data[LABEL_KEY];
}

OntDatatypeProperty.prototype.getDomains = function() {
  if(this.domains==null && this.data[DOMAINS_KEY]!=null) {
    this.domains=getClassArray(this.ontology,DOMAINS_KEY,this.data);
  }
  return this.domains;
}

OntDatatypeProperty.prototype.getRanges = function() {
  if(this.ranges==null) {
    if(this.data[RANGES_KEY]!=null) {
      this.ranges=new Array(this.data[RANGES_KEY].length);
      for(var i=0;i<this.data[RANGES_KEY].length;i++) {
        this.ranges[i]=this.data[RANGES_KEY][i];
      }
    } else {
      this.ranges=new Array();
    }
  }
  return this.ranges;
}

OntDatatypeProperty.prototype.getMinCardinality = function() {
  if(this.minCardinality==null && this.data[MIN_CARDINALITY_KEY]!=null) {
    this.minCardinality=getSingleInteger(this.data[MIN_CARDINALITY_KEY]);
  }
  return this.minCardinality;
}

OntDatatypeProperty.prototype.getMaxCardinality = function() {
  if(this.maxCardinality==null && this.data[MAX_CARDINALITY_KEY]!=null) {
    this.maxCardinality=getSingleInteger(this.data[MAX_CARDINALITY_KEY]);
  }
  return this.maxCardinality;
}

OntDatatypeProperty.prototype.getId = function() {
  return this.id;
}


/* ----------------------------------------------------- */
/*                 Utility methods                       */
/* ----------------------------------------------------- */

/* utility method for transforming array of class ids to array of OntClasses */
function getClassArray(ont,key,data) {
  var res;
  if(data[key]!=null) {
    res=new Array(data[key].length);
    for(var i=0;i<data[key].length;i++) {
      res[i]=ont.getClass(data[key][i]);
    }
  } else {
    res=new Array();
  }
  return res;
}

/* utility method for transforming array of objprop ids to array of OntObjectProperties */
function getObjectPropertyArray(ont,key,data) {
  var res;
  if(data[key]!=null) {
    res=new Array(data[key].length);
    for(var i=0;i<data[key].length;i++) {
      res[i]=ont.getObjectProperty(data[key][i]);
    }
  } else {
    res=new Array();
  }
  return res;
}


/* utility method for transforming array of dataprop ids to array of OntDatatypeProperties */
function getDatatypePropertyArray(ont,key,data) {
  var res;
  if(data[key]!=null) {
    res=new Array(data[key].length);
    for(var i=0;i<data[key].length;i++) {
      res[i]=ont.getDatatypeProperty(data[key][i]);
    }
  } else {
    res=new Array();
  }
  return res;
}


/* get a single integer from a string array (for cardinalities) */
function getSingleInteger(list) {
  if(list instanceof Array && list.length>0) {
    return parseInt(list[0]);
  }
  return -1;
}

/* Return true if idstr is an id of the objects in ar */
function idInObjectArray(idstr,ar) {
  for(var i=0;i<ar.length;i++) {
    if(ar[i].getId()==idstr) {
      return i;
    }
  }
  return -1;
}

