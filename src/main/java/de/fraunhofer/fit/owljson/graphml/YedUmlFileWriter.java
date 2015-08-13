package de.fraunhofer.fit.owljson.graphml;

import de.fraunhofer.fit.owljson.owldoc.JSONDataModelGenerator;

import java.io.*;
import java.util.Collection;
import java.util.Map;

/**
 * Class to create graphml representation of ontology to be used in yEd.
 * It uses UML shapes of yEd.
 * <p/>
 * The code is based on http://www.bennyn.de/programmierung/java/create-graphml-xml-files-that-can-be-used-in-the-yed-graph-editor.html
 *
 * @author Benny Neugebauer (http://www.bennyn.de)
 * @author Christoph Quix (christoph.quix@fit.fraunhofer.de)
 */
public class YedUmlFileWriter {
    private String xml = null;

    private Map<String, Map<String, Map<String, Collection<String>>>> mOntology;
    private String mFilename;

    private int isaCounter=0;


    public YedUmlFileWriter(Map<String, Map<String, Map<String, Collection<String>>>> ontology, String outFilename) {
        this.mOntology = ontology;
        this.mFilename = outFilename;
    }

    public void execute() throws IOException {
        StringBuilder sbNodes=new StringBuilder();
        StringBuilder sbEdges=new StringBuilder();

        Map<String, Map<String, Collection<String>>> classes=mOntology.get(JSONDataModelGenerator.CLASSES_KEY);
        if(classes!=null) {
            for(String clsId : classes.keySet()) {
                sbNodes.append(getClassNode(clsId,classes.get(clsId)));
                sbEdges.append(getIsaEdges(clsId,classes.get(clsId)));
            }
        }

        Map<String, Map<String, Collection<String>>> objProps=mOntology.get(JSONDataModelGenerator.OBJECT_PROPERTIES_KEY);
        if(objProps!=null) {
            for(String objPropId : objProps.keySet()) {
                sbEdges.append(getAssociationEdges(objPropId, objProps.get(objPropId)));
            }
        }


        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("js" + File.separator + mFilename + ".graphml"), "UTF-8"));
        writer.write(getGraphMLHeader());
        writer.newLine();
        writer.write(sbNodes.toString());
        writer.newLine();
        writer.write(sbEdges.toString());
        writer.newLine();
        writer.write(getGraphMLFooter());
        writer.flush();
        writer.close();
    }


    private String getGraphMLHeader() {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        header += "\n<graphml\n  xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n  xmlns:y=\"http://www.yworks.com/xml/graphml\"\n  xmlns:yed=\"http://www.yworks.com/xml/yed/3\"\n  xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n  http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\"\n>";
        header += "\n  <key for=\"node\" id=\"d5\" attr.name=\"description\" attr.type=\"string\" />";
        header += "\n  <key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>";
        header +="  <key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>\n" +
                "  <key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/>\n" +
                "  <key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/>";
        header += "\n  <graph id=\"G\" edgedefault=\"directed\">";
        return header;
    }

    private String getGraphMLFooter() {
        String footer = "\n  </graph>\n</graphml>";
        return footer;
    }

    private String getClassNode(String id, Map<String, Collection<String>> cls) {
        StringBuilder sbNode = new StringBuilder();
        sbNode.append("\n    <node id=\"" + id + "\">");
        sbNode.append("\n      <data key=\"d5\"/>");
        sbNode.append("\n      <data key=\"d6\">");
        sbNode.append("\n        <y:UMLClassNode>");
        sbNode.append("\n          <y:NodeLabel>");
        sbNode.append(getLabel(JSONDataModelGenerator.CLASSES_KEY,id));
        sbNode.append("</y:NodeLabel>");
        sbNode.append("\n          <y:UML clipContent=\"true\" constraint=\"\" omitDetails=\"false\" stereotype=\"\" use3DEffect=\"true\">\n");
        sbNode.append("            <y:AttributeLabel>\n");
        sbNode.append(getAttributes(cls));
        sbNode.append("            </y:AttributeLabel>\n");
        sbNode.append("            <y:MethodLabel/>\n");
        sbNode.append("          </y:UML>\n");
        sbNode.append("        </y:UMLClassNode>");
        sbNode.append("\n      </data>");
        sbNode.append("\n    </node>");
        return sbNode.toString();
    }
    
    private String getAttributes(Map<String, Collection<String>> cls) {
        StringBuilder res=new StringBuilder();
        Collection<String> dtProps=cls.get(JSONDataModelGenerator.DATATYPE_PROPERTIES_KEY);
        if(dtProps!=null) {
            for(String dtPropId : dtProps) {
                res.append(getLabel(JSONDataModelGenerator.DATATYPE_PROPERTIES_KEY,dtPropId));
                res.append("\n");
            }
        }
        return res.toString();
    }

    private String getLabel(String key, String id) {
        Map<String, Map<String, Collection<String>>> ontObjs=mOntology.get(key);
        if(ontObjs!=null) {
            Map<String, Collection<String>> ontObj=ontObjs.get(id);
            if(ontObj!=null) {
                Collection<String> labels=ontObj.get(JSONDataModelGenerator.LABEL_KEY);
                if(labels!=null && labels.size()>0) {
                    return labels.iterator().next();
                } else {
                    Collection<String> lns=ontObj.get(JSONDataModelGenerator.LOCAL_NAME_KEY);
                    if(lns!=null && lns.size()>0) {
                        return lns.iterator().next();
                    }
                }
            }
        }
        return id;
    }


    private String getAssociationEdges(String id, Map<String, Collection<String>> objProp) {
        StringBuilder sbEdges=new StringBuilder();
        Collection<String> ranges=objProp.get(JSONDataModelGenerator.RANGES_KEY);
        Collection<String> domains=objProp.get(JSONDataModelGenerator.DOMAINS_KEY);
        if(ranges!=null && domains!=null) {
            for(String range : ranges) {
                for(String domain : domains) {
                    sbEdges.append("\n <edge id=\"");
                    sbEdges.append(id);
                    sbEdges.append("\" source=\"");
                    sbEdges.append(domain);
                    sbEdges.append("\" target=\"");
                    sbEdges.append(range);
                    sbEdges.append("\">\n");
                    sbEdges.append("      <data key=\"d8\"/>\n");
                    sbEdges.append("      <data key=\"d9\"/>\n");
                    sbEdges.append("      <data key=\"d10\">\n");
                    sbEdges.append("        <y:PolyLineEdge>\n");
                    sbEdges.append("          <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n");
                    sbEdges.append("          <y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n");
                    sbEdges.append("          <y:Arrows source=\"none\" target=\"none\"/>\n");
                    sbEdges.append("          <y:EdgeLabel alignment=\"center\" configuration=\"AutoFlippingLabel\" distance=\"1.0\" fontFamily=\"Dialog\" fontSize=\"8\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" textColor=\"#000000\" visible=\"true\">");
                    sbEdges.append(getLabel(JSONDataModelGenerator.OBJECT_PROPERTIES_KEY,id));
                    sbEdges.append("          </y:EdgeLabel>\n");
                    sbEdges.append("        </y:PolyLineEdge>\n");
                    sbEdges.append("      </data>\n");
                    sbEdges.append("    </edge>\n");
                }
            }
        }
        return sbEdges.toString();
    }

    private String getIsaEdges(String id, Map<String, Collection<String>> cls) {
        StringBuilder sbEdges=new StringBuilder();
        Collection<String> superClasses=cls.get(JSONDataModelGenerator.SUPERCLASSES_KEY);
        if(superClasses!=null) {
            for(String superCls : superClasses) {
                sbEdges.append("\n  <edge id=\"");
                sbEdges.append("isa" + isaCounter);
                isaCounter++;
                sbEdges.append("\" source=\"");
                sbEdges.append(id);
                sbEdges.append("\" target=\"");
                sbEdges.append(superCls);
                sbEdges.append("\">\n");
                sbEdges.append("      <data key=\"d8\"/>\n");
                sbEdges.append("      <data key=\"d9\"/>\n");
                sbEdges.append("      <data key=\"d10\">\n");
                sbEdges.append("        <y:PolyLineEdge>\n");
                sbEdges.append("          <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n");
                sbEdges.append("          <y:LineStyle color=\"#000000\" type=\"line\" width=\"2.0\"/>\n");
                sbEdges.append("          <y:Arrows source=\"none\" target=\"white_delta\"/>\n");
                sbEdges.append("        </y:PolyLineEdge>\n");
                sbEdges.append("      </data>\n");
                sbEdges.append("    </edge>\n");
            }
        }
        return sbEdges.toString();
    }

}
