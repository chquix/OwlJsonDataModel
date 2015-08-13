package de.fraunhofer.fit.owljson.owldoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitorEx;

import java.util.*;

/**
 * User: Quix
 * Date: 13.05.2014
 */
public class JSONDataModelGenerator extends OWLOntologyWalkerVisitorEx<Object> {

  /* see also definitions in resources/js/Constants.js
    Definitions here should be in sync with the definitions there.
   */
  public static String CLASSES_KEY="classes";

  public static String OBJECT_PROPERTIES_KEY="objectProperties";

  public static String DATATYPE_PROPERTIES_KEY="datatypeProperties";

  public static String SUBCLASSES_KEY="subClasses";

  public static String SUPERCLASSES_KEY="superClasses";

  public static String EQUIVALENT_CLASSES_KEY="equivalentClasses";

  public static String DISJOINT_CLASSES_KEY="disjointClasses";

  public static String LOCAL_NAME_KEY="localName";

  public static String LABEL_KEY="label";

  public static String INDIVIDUALS_KEY="individuals";

  public static String DOMAINS_KEY="domains";

  public static String RANGES_KEY="ranges";

  public static String SUBPROPERTIES_KEY="subProperties";

  public static String SUPERPROPERTIES_KEY="superProperties";

  public static String DISJOINT_PROPERTIES_KEY="disjointProperties";

  public static String EQUIVALENT_PROPERTIES_KEY="equivalentProperties";

  public static String INVERSE_PROPERTY_KEY="inverseProperty";

  public static String MIN_CARDINALITY_KEY="minCardinality";

  public static String MAX_CARDINALITY_KEY="maxCardinality";

  private OWLOntology mOntology;

  private Set<OWLOntology> mOntologies;

  /**
   * This object describes an ontology in such a way that it
   * can be later mapped easily into a JSON object. The template
   * for the structure looks like this:
   * {
   *   classes : {
   *     className1 : {
   *       relKey1 : [ relItem1, ..., relItem_n ],
   *       relKey2 : [ ... ],
   *       ...
   *     },
   *     className2 : {
   *       ...
   *     }
   *   },
   *   objectProperties : {
   *     objProp1 : {
   *       domains : [ d1, ..., dn],
   *       ranges : [r1, ..., rn],
   *       ...
   *     }
   *   }
   * }
   *
   * Annotation properties of classes or properties will be also added as key-value pairs to the
   * corresponding JSON object, e.g.,
   * className1 : {
   *    annotationName : annotationValue
   *    ....
   * }
   */
  private Map<String, Map<String, Map<String, Collection<String>>>> mOntologyMap;

  public JSONDataModelGenerator(OWLOntologyWalker walker, OWLOntology ontology) {
    super(walker);
    mOntology = ontology;
    mOntologyMap = new TreeMap<String, Map<String, Map<String, Collection<String>>>>();
    mOntologies = new HashSet<OWLOntology>();
    mOntologies.add(mOntology);
  }

  public Map<String, Map<String, Map<String, Collection<String>>>> getOntologyMap() {
    return mOntologyMap;
  }

  @SuppressWarnings("unused")
  public JsonElement toJson() throws Exception {
    return toJson(mOntologyMap);
  }

  private JsonElement toJson(Map<String, ?> map) throws Exception {
    JsonObject res = new JsonObject();
    for(Map.Entry<String, ?> e : map.entrySet()) {
      JsonElement nested = toJson(e.getValue());
      res.add(e.getKey(), nested);
    }
    return res;
  }

  private JsonElement toJson(Collection<String> col) {
    JsonArray res = new JsonArray();
    for(String s : col) {
      res.add(new JsonPrimitive(s));
    }
    return res;
  }

  @SuppressWarnings("unchecked")
  private JsonElement toJson(Object o) throws Exception {
    if(o instanceof Map<?, ?>) {
      return toJson((Map<String, ?>) o);
    } else if(o instanceof Collection<?>) {
      return toJson((Collection<String>) o);
    } else {
      throw new Exception("Json conversion to " + o.getClass().getName() + " not implemented");
    }
  }


  @Override
  public Object visit(OWLClass cls) {
    addClass(cls);
    return null;
  }

  public Object visit(OWLSubClassOfAxiom ax) {
    if(!ax.getSubClass().isAnonymous() && !ax.getSuperClass().isAnonymous()) {
      // no special handling required, should be covered by addClass
      return null;
    }
    Set<OWLClass> subClasses=new HashSet<OWLClass>();
    if(ax.getSubClass().isAnonymous()) {
      if(ax.getSubClass().asDisjunctSet()!=null) {
        Set<OWLClassExpression> unionSet = ax.getSubClass().asDisjunctSet();
        for(OWLClassExpression oce : unionSet) {
          if(!oce.isAnonymous()) {
            subClasses.add(oce.asOWLClass());
          }
        }
      }
    } else {
      // named class
      subClasses.add(ax.getSubClass().asOWLClass());
    }


    Set<OWLClass> superClasses=new HashSet<OWLClass>();
    if(ax.getSuperClass().isAnonymous()) {
      if(ax.getSuperClass().asDisjunctSet()!=null) {
        Set<OWLClassExpression> unionSet = ax.getSuperClass().asDisjunctSet();
        for(OWLClassExpression oce : unionSet) {
          if(!oce.isAnonymous()) {
            superClasses.add(oce.asOWLClass());
          }
        }
      }
    } else {
      // named class
      superClasses.add(ax.getSuperClass().asOWLClass());
    }

    for(OWLClass sub : subClasses) {
      Map<String, Collection<String>> clsPropMap=getClassPropMap(sub);
      putOrExtend(clsPropMap, SUPERCLASSES_KEY, superClasses);
    }
    for(OWLClass sup : superClasses) {
      Map<String, Collection<String>> clsPropMap=getClassPropMap(sup);
      putOrExtend(clsPropMap, SUBCLASSES_KEY, subClasses);
    }
    return null;
  }


  @Override
  public Object visit(OWLObjectProperty p) {
    addProperty(p, OBJECT_PROPERTIES_KEY);
    return null;
  }


  @Override
  public Object visit(OWLObjectPropertyAssertionAxiom ax) {
    addPropertyAnnotation(ax, OBJECT_PROPERTIES_KEY);
    return null;
  }

  @Override
  public Object visit(OWLDataPropertyAssertionAxiom ax) {
    addPropertyAnnotation(ax, DATATYPE_PROPERTIES_KEY);
    return null;
  }


  @Override
  public Object visit(OWLDataMinCardinality c) {
    visitPropertyCardinalityRestriction(c);
    return null;
  }

  @Override
  public Object visit(OWLDataMaxCardinality c) {
    visitPropertyCardinalityRestriction(c);
    return null;
  }

  @Override
  public Object visit(OWLDataExactCardinality c) {
    visitPropertyCardinalityRestriction(c);
    return null;
  }

  @Override
  public Object visit(OWLObjectMinCardinality c) {
    visitPropertyCardinalityRestriction(c);
    return null;
  }

  @Override
  public Object visit(OWLObjectMaxCardinality c) {
    visitPropertyCardinalityRestriction(c);
    return null;
  }

  @Override
  public Object visit(OWLObjectExactCardinality c) {
    visitPropertyCardinalityRestriction(c);
    return null;
  }

  private void visitPropertyCardinalityRestriction(OWLCardinalityRestriction r) {
    if(!(r.getProperty() instanceof OWLNamedObject)) {
      return;
    }
    Map<String, Collection<String>> propMap = null;
    if(r instanceof OWLDataCardinalityRestriction) {
      propMap=getPropertyMap(r.getProperty(), DATATYPE_PROPERTIES_KEY);
    } else if(r instanceof OWLObjectCardinalityRestriction) {
      propMap=getPropertyMap(r.getProperty(), OBJECT_PROPERTIES_KEY);
    }
    if(r instanceof OWLDataMinCardinality) {
      OWLDataMinCardinality c = (OWLDataMinCardinality) r;
      propMap.put(MIN_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
    } else if(r instanceof OWLDataMaxCardinality) {
      OWLDataMaxCardinality c = (OWLDataMaxCardinality) r;
      propMap.put(MAX_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
    } else if(r instanceof OWLDataExactCardinality) {
      OWLDataExactCardinality c = (OWLDataExactCardinality) r;
      propMap.put(MIN_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
      propMap.put(MAX_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
    } else if(r instanceof OWLObjectMinCardinality) {
      OWLObjectMinCardinality c = (OWLObjectMinCardinality) r;
      propMap.put(MIN_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
    } else if(r instanceof OWLObjectMaxCardinality) {
      OWLObjectMaxCardinality c = (OWLObjectMaxCardinality) r;
      propMap.put(MAX_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
    } else if(r instanceof OWLObjectExactCardinality) {
      OWLObjectExactCardinality c = (OWLObjectExactCardinality) r;
      propMap.put(MIN_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
      propMap.put(MAX_CARDINALITY_KEY, getSingleStringList(c.getCardinality()));
    }
  }

  @Override
  public Object visit(OWLDataProperty p) {
    addProperty(p, DATATYPE_PROPERTIES_KEY);
    return null;
  }

  private Collection<String> getSingleStringList(int c) {
    List<String> lValues = new LinkedList<String>();
    lValues.add(Integer.toString(c));
    return lValues;
  }

  private Map<String, Collection<String>> getPropertyMap(OWLPropertyExpression p, String key) {
    Map<String, Map<String, Collection<String>>> dataPropMap = mOntologyMap.get(key);
    if(dataPropMap == null) {
      dataPropMap = new TreeMap<String, Map<String, Collection<String>>>();
      mOntologyMap.put(key, dataPropMap);
    }

    Map<String, Collection<String>> propMap = null;
    if(p instanceof OWLNamedObject) {
      propMap = dataPropMap.get(((OWLNamedObject) p).getIRI().toString());
      if(propMap == null) {
        propMap = new TreeMap<String, Collection<String>>();
        dataPropMap.put(((OWLNamedObject) p).getIRI().toString(), propMap);
      }
    }
    return propMap;
  }


  @SuppressWarnings("unchecked")
  private void addProperty(OWLPropertyExpression p, String key) {
    if(p.isAnonymous()) {
      return;
    }

    if(p instanceof OWLNamedObject) {
      Map<String, Collection<String>> propMap = getPropertyMap(p, key);
      String localName = XMLUtils.getNCNameSuffix(((OWLNamedObject) p).getIRI().toString());

      if(p instanceof OWLDataPropertyExpression) {

        OWLDataProperty dp=((OWLDataPropertyExpression) p).asOWLDataProperty();

        putOrExtend(propMap, DOMAINS_KEY, EntitySearcher.getDomains(dp, mOntologies));
        putOrExtend(propMap, RANGES_KEY, EntitySearcher.getRanges(dp, mOntologies));
        putOrExtend(propMap, SUBPROPERTIES_KEY, EntitySearcher.getSubProperties(dp, mOntologies));
        putOrExtend(propMap, SUPERPROPERTIES_KEY, EntitySearcher.getSuperProperties(dp, mOntologies));
        putOrExtend(propMap, EQUIVALENT_PROPERTIES_KEY, EntitySearcher.getEquivalentProperties(dp, mOntologies));
        putOrExtend(propMap, DISJOINT_PROPERTIES_KEY, EntitySearcher.getDisjointProperties(dp, mOntologies));

      } else if(p instanceof OWLObjectPropertyExpression) {
        OWLObjectPropertyExpression ope=(OWLObjectPropertyExpression) p;
        putOrExtend(propMap, DOMAINS_KEY, EntitySearcher.getDomains(ope, mOntologies));
        putOrExtend(propMap, RANGES_KEY, EntitySearcher.getRanges(ope, mOntologies));
        putOrExtend(propMap, SUBPROPERTIES_KEY, EntitySearcher.getSubProperties(ope, mOntologies));
        putOrExtend(propMap, SUPERPROPERTIES_KEY, EntitySearcher.getSuperProperties(ope, mOntologies));
        putOrExtend(propMap, EQUIVALENT_PROPERTIES_KEY, EntitySearcher.getEquivalentProperties(ope, mOntologies));
        putOrExtend(propMap, DISJOINT_PROPERTIES_KEY, EntitySearcher.getDisjointProperties(ope, mOntologies));
        putOrExtend(propMap, INVERSE_PROPERTY_KEY,EntitySearcher.getInverses(ope,mOntologies));
      }

      Collection<String> lnCol = new LinkedList<String>();
      lnCol.add(localName);
      propMap.put(LOCAL_NAME_KEY, lnCol);


      // Annotation properties for properties
      // are added in addPropertyAnnotations (invoked by another visit method)

      Set<OWLAnnotation> annotations=new HashSet<>();
      if(p instanceof OWLEntity) {
        annotations.addAll(EntitySearcher.getAnnotations((OWLEntity) p, mOntology));
      }
      if(p instanceof OWLAnnotationSubject) {
        annotations.addAll(EntitySearcher.getAnnotations((OWLAnnotationSubject) p, mOntology));
      }
      for(OWLAnnotation anno : annotations) {
        Collection<OWLAnnotationValue> singleList = new LinkedList<>();
        singleList.add(anno.getValue());
        /* String lit=null;
        anno.get
        if(anno.getValue() instanceof OWLLiteral) {
          lit=((OWLLiteral) anno.getValue()).getLiteral();
        } else {
          lit=anno.getValue().toString();
        }
        singleList.add(lit); */
        putOrExtend(propMap,XMLUtils.getNCNameSuffix(anno.getProperty().getIRI().toString()),singleList);
        //propMap.put(, singleList);
      }
    }
  }

  private void addPropertyAnnotation(OWLPropertyAssertionAxiom ax, String key) {
    OWLPropertyExpression p = ax.getProperty();
    if(p.isAnonymous()) {
      return;
    }
    Map<String, Map<String, Collection<String>>> dataPropMap = mOntologyMap.get(key);
    if(dataPropMap == null) {
      dataPropMap = new TreeMap<>();
      mOntologyMap.put(key, dataPropMap);
    }
    if(p instanceof OWLNamedObject) {
      Map<String, Collection<String>> propMap = getPropertyMap(p, key);
      Set<OWLAnnotation> annoSet=ax.getAnnotations();
      for(OWLAnnotation anno : annoSet) {
        Collection<String> singleList = new LinkedList<String>();
        String lit=null;
        if(anno.getValue() instanceof OWLLiteral) {
          lit=((OWLLiteral) anno.getValue()).getLiteral();
        } else {
          lit=anno.getValue().toString();
        }
        singleList.add(lit);
        propMap.put(XMLUtils.getNCNameSuffix(anno.getProperty().getIRI().toString()), singleList);
      }
    }
  }


  private Map<String, Collection<String>> getClassPropMap(OWLClass cls) {
    Map<String, Map<String, Collection<String>>> clsMap = mOntologyMap.get(CLASSES_KEY);
    if(clsMap == null) {
      clsMap = new TreeMap<String, Map<String, Collection<String>>>();
      mOntologyMap.put(CLASSES_KEY, clsMap);
    }
    Map<String, Collection<String>> clsPropMap = clsMap.get(cls.getIRI().toString());
    if(clsPropMap == null) {
      clsPropMap = new TreeMap<String, Collection<String>>();
      clsMap.put(cls.getIRI().toString(), clsPropMap);
    }
    return clsPropMap;
  }

  private void addClass(OWLClass cls) {
    Map<String, Map<String, Collection<String>>> clsMap = mOntologyMap.get(CLASSES_KEY);
    if(clsMap == null) {
      clsMap = new TreeMap<>();
      mOntologyMap.put(CLASSES_KEY, clsMap);
    }
    Map<String, Collection<String>> clsPropMap = clsMap.get(cls.getIRI().toString());
    if(clsPropMap == null) {
      clsPropMap = new TreeMap<String, Collection<String>>();
      clsMap.put(cls.getIRI().toString(), clsPropMap);
    }

    // Properties will be added in the visit(Property) methods
    putOrExtend(clsPropMap, SUBCLASSES_KEY, EntitySearcher.getSubClasses(cls, mOntologies));
    // putOrExtend(clsPropMap, SUBCLASSES_KEY, mReasoner.getSubClasses(cls, true));

    putOrExtend(clsPropMap, SUPERCLASSES_KEY, EntitySearcher.getSuperClasses(cls, mOntologies));

    putOrExtend(clsPropMap, EQUIVALENT_CLASSES_KEY, EntitySearcher.getEquivalentClasses(cls,mOntologies));

    putOrExtend(clsPropMap, DISJOINT_CLASSES_KEY, EntitySearcher.getDisjointClasses(cls,mOntologies));

    String localName = XMLUtils.getNCNameSuffix(cls.getIRI().toString());
    Collection<String> lnCol = new LinkedList<String>();
    lnCol.add(localName);
    clsPropMap.put(LOCAL_NAME_KEY, lnCol);

    putOrExtend(clsPropMap, INDIVIDUALS_KEY, EntitySearcher.getIndividuals(cls,mOntologies));

    // Annotation properties
    Collection<OWLAnnotation> annoSet = EntitySearcher.getAnnotations(cls,mOntology);

    for(OWLAnnotation anno : annoSet) {
      /*
      Collection<String> singleList = new LinkedList<>();
      String lit=null;
      if(anno.getValue() instanceof OWLLiteral) {
        lit=((OWLLiteral) anno.getValue()).getLiteral();
      } else {
        lit=anno.getValue().toString();
      }
      singleList.add(lit);
      clsPropMap.put(XMLUtils.getNCNameSuffix(anno.getProperty().getIRI().toString()), singleList); */
      Collection<OWLAnnotationValue> singleList = new LinkedList<>();
      singleList.add(anno.getValue());
      putOrExtend(clsPropMap,XMLUtils.getNCNameSuffix(anno.getProperty().getIRI().toString()), singleList);
    }
  }

  private Collection<String> convertToStringCollection(NodeSet<OWLClass> clsSet) {
    Collection<String> res = new TreeSet<String>();
    for(OWLClass cls : clsSet.getFlattened()) {
      if(!cls.isAnonymous()) {
        res.add(cls.getIRI().toString());
      }
    }
    return res;
  }

  private Collection<String> convertToStringCollection(Collection<? extends OWLObject> objSet) {
    Collection<String> res = new TreeSet<String>();
    for(OWLObject obj : objSet) {
      if(obj instanceof OWLClassExpression && !((OWLClassExpression) obj).isAnonymous()) {
        res.add(((OWLClassExpression) obj).asOWLClass().getIRI().toString());
      } else if(obj instanceof OWLClassExpression && ((OWLClassExpression) obj).asDisjunctSet()!=null) {
        Set<OWLClassExpression> unionSet=((OWLClassExpression) obj).asDisjunctSet();
        for(OWLClassExpression oce : unionSet) {
          if(!oce.isAnonymous()) {
            res.add(oce.asOWLClass().getIRI().toString());
          }
        }
      } else if(obj instanceof OWLDataRange && ((OWLDataRange) obj).isDatatype()) {
        OWLDatatype odt=((OWLDataRange) obj).asOWLDatatype();

        res.add(odt.getBuiltInDatatype().getShortForm());
      } else if(obj instanceof OWLIndividual && !((OWLIndividual) obj).isAnonymous()) {
        res.add(((OWLIndividual) obj).asOWLNamedIndividual().getIRI().toString());
      } else if(obj instanceof OWLObjectProperty && !((OWLObjectProperty) obj).isAnonymous()) {
        res.add(((OWLObjectProperty) obj).getIRI().toString());
      } else if(obj instanceof OWLDataOneOf) {
        OWLDataOneOf oneOf=(OWLDataOneOf) obj;
        StringBuilder sb=new StringBuilder("{ ");
        boolean isFirst=true;
        for(OWLLiteral lit : oneOf.getValues()) {
          if(!isFirst) {
            sb.append(", ");
          } else {
            isFirst=false;
          }
          sb.append(lit.toString());
        }
        sb.append("}");
        res.add(sb.toString());
      } else if(obj instanceof OWLLiteral) {
        res.add(((OWLLiteral) obj).getLiteral());
      } else {
        res.add(obj.toString());
      }
    }
    return res;
  }

  private void putOrExtend(Map<String, Collection<String>> clsPropMap, String key, Collection<? extends OWLObject> objSet) {
    Collection<String> colStr = convertToStringCollection(objSet);
    Collection<String> exColStr = clsPropMap.get(key);
    if(exColStr != null) {
      exColStr.addAll(colStr);
    } else {
      clsPropMap.put(key, colStr);
    }
  }

  @SuppressWarnings("unused")
  private void putOrExtend(Map<String, Collection<String>> clsPropMap, String key, NodeSet<OWLClass> objSet) {
    Collection<String> colStr = convertToStringCollection(objSet);
    Collection<String> exColStr = clsPropMap.get(key);
    if(exColStr != null) {
      exColStr.addAll(colStr);
    } else {
      clsPropMap.put(key, colStr);
    }
  }


}
