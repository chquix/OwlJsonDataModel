var myont=new Ontology(ontology);

var allClasses;

var nowShown="";



function getTree() {
  var classes=myont.getClasses();
  var treeData=new Array();
  allClasses=new Object();
  var j=0;
  if(typeof topClasses != 'undefined' && topClasses!=null) {
    for(var i=0;i<topClasses.length;i++) {
      var cls=myont.getClass(topClasses[i]);
      console.log(topClasses[i] + " --> " + cls);
      treeData[j] = getClassTree(cls,0,(typeof showObjProps != 'undefined' && showObjProps));
      j++;
    }
  } else {
    for(var i=0;i<classes.length;i++) {
      if(classes[i].getSuperClasses()==null || classes[i].getSuperClasses().length==0 ) {
        if(allClasses[classes[i].getName()]==null) {
          treeData[j] = getClassTree(classes[i],0,(typeof showObjProps != 'undefined' && showObjProps));
          j++;
        }
      }
    }
  }
  return treeData;
}

function getClassTree(cls,level,showProps) {
  // console.log(level + ' ' + cls.getName());

  if(level>4 || allClasses[cls.getName()]!=null) {
    return { text : 'tree is cut off here'};
  }
  var res=new Object();
  res.id=cls.getId();
  res.text = cls.getLabel();
  allClasses[cls.getName()]=1;
  var subs=cls.getSubClasses();
  var j=0;
  var props=null;
  if(showProps) {
    props=cls.getObjectProperties();
    if(props!=null && props.length>0) {
      if(res.nodes==null) {
        res.nodes=new Array();
      }
      for(var i=0;i<props.length;i++) {
        var node=new Object();
        node.id=props[i].getId();
        node.text=props[i].getLabel();
        node.icon = 'glyphicon glyphicon-one-fine-empty-dot';
        res.nodes[j]=node;
        j++;
      }
    }
  }
  if(subs!=null && subs.length>0 ) {
    if(res.nodes==null) {
      res.nodes = new Array();
    }
    for(var i=0;i<subs.length;i++) {
      if(allClasses[subs[i].getName()]==null) {
        var node=getClassTree(subs[i],level+1,showProps);
        if(node!=null) {
          res.nodes[j]=node;
          j++;
        }
      }
    }
  }
  if(j==0) {
    res.icon = 'glyphicon glyphicon-one-fine-full-dot';
  }
  /* if(j==0) {
    res.icon = 'glyphicon glyphicon-one-fine-full-dot';
  } */
  return res;
}


function showDetails(uri) {
  top.location="#" + uri;
  nowShown=uri;
  var cls = myont.getClass(uri);
  var desc='';
  if(cls!=null) {
    desc=getHtmlForClass(uri);
  } else {
    prop = myont.getObjectProperty(uri);
    if(prop!=null) {
      desc=getHtmlForProp(uri, true, prop);
    } else {
      prop = myont.getDatatypeProperty(uri);
      if(prop!=null) {
        desc=getHtmlForProp(uri,false,prop);
      }
    }
  }

  $('#detailsview1').html(desc);
  /* This is optional, this can be used with the PHP commentics package to enable comments for ontology elements
  $('#comframe').hide();
  $('#comframe').attr('src','comments.php?ontname=' + cls.getName() + '&ontid=' + rewriteUri(uri));
  */
}


function getHtmlForClass(uri) {
  var cls = myont.getClass(uri);
  var desc = '<h3>' + cls.getLabel() + '</h3>ID: ' + uri;
  var labels = cls.getLabels();
  if (labels != null && labels.length > 1) {
    desc += '<h4>Alternative Bezeichnungen</h4>';
    desc += '<ul>';
    for (var j = 0; j < labels.length; j++) {
      desc += '<li>' + labels[j] + '</li>';
    }
    desc += '</ul>';
  }
  if (cls.data.comment != null) {
    desc += '<h4>Beschreibung</h4>' + removeQuotes(cls.data.comment[0]);
  }



  var props = cls.getObjectProperties();
  if (props.length > 0) {
    desc += '<h4>Beziehungen</h4><ul>';
    for (var i = 0; i < props.length; i++) {
      desc += '<li><a href="javascript:showPropDetails(\'' + props[i].getId() + '\',true)">' + props[i].getLabel() + '</a>';
      var ranges = props[i].getRanges();
      if (ranges != null && ranges.length > 0) {
        desc += ' zu ';
        for (var j = 0; j < ranges.length; j++) {
          desc += '<a href="javascript:showDetails(\'' + ranges[j].getId() + '\')">' + ranges[j].getLabel() + '</a>';
          if ((j + 1) < ranges.length) {
            desc += ', ';
          }
        }
      }
      desc += '</li>';
    }
    desc += '</ul>';
  }

  var dtprops = cls.getDatatypeProperties();
  if (dtprops.length > 0) {
    desc += '<h4>Eigenschaften</h4><ul>';
    for (var i = 0; i < dtprops.length; i++) {
      desc += '<li><a href="javascript:showPropDetails(\'' + dtprops[i].getId() + '\',false)">' + dtprops[i].getLabel() + '</a>';
      var ranges = dtprops[i].getRanges();
      if (ranges != null && ranges.length > 0) {
        desc += ' : ';
        for (var j = 0; j < ranges.length; j++) {
          desc += ranges[j];
          if ((j + 1) < ranges.length) {
            desc += ', ';
          }
        }
      }
      desc += '</li>';
    }
    desc += '</ul>';
  }

  var subs = cls.getSubClasses();
  if (subs.length > 0) {
    desc += '<h4>Spezialisierungen</h4><ul>';
    desc += getHtmlForClassList(subs);
    desc += '</ul>';
  }

  var supers = cls.getSuperClasses();
  if (supers.length > 0) {
    desc += '<h4>Generalisierungen</h4><ul>';
    desc += getHtmlForClassList(supers);
    desc += '</ul>';
  }
  return desc;
}



function showPropDetails(uri,isObjProp) {
  top.location = "#" + uri;
  nowShown = uri;
  var prop;
  if (isObjProp) {
    prop = myont.getObjectProperty(uri);
  } else {
    prop = myont.getDatatypeProperty(uri);
  }
  var desc=getHtmlForProp(uri, isObjProp, prop);
  $('#detailsview1').html(desc);
  /* This is optional, this can be used with the PHP commentics package to enable comments for ontology elements
  $('#comframe').hide();
  $('#comframe').attr('src','comments.php?ontname=' + prop.getName() + '&ontid=' + rewriteUri(uri));
  */
}


function getHtmlForProp(uri, isObjProp, prop) {
  var desc = '<h3>' + prop.getLabel() + '</h3>ID: ' + uri;
  var labels = prop.getLabels();
  if (labels != null && labels.length > 1) {
    desc += '<h4>Alternative Bezeichnungen</h4>';
    desc += '<ul>';
    for (var j = 0; j < labels.length; j++) {
      desc += '<li>' + labels[j] + '</li>';
    }
    desc += '</ul>';
  }
  if (prop.data.comment != null) {
    desc += '<h4>Beschreibung</h4>' + removeQuotes(prop.data.comment[0]);
  }

  var domains = prop.getDomains();
  if (domains.length > 0) {
    desc += '<h4>Definiert für</h4><ul>';
    desc += getHtmlForClassList(domains);
    desc += '</ul>';
  }

  var ranges = prop.getRanges();
  if (ranges.length > 0) {
    desc += '<h4>Ziel / Wertebereich</h4><ul>';
    if (isObjProp) {
      desc += getHtmlForClassList(ranges);
    } else {
      for (var i = 0; i < ranges.length; i++) {
        desc += '<li>' + ranges[i] + '</li>';
      }
    }
    desc += '</ul>';
  }
  return desc;
}


function getHtmlForClassList(clslist) {
  var ht='';
  for(var i=0;i<clslist.length;i++) {
    ht+='<li><a href="javascript:showDetails(\'' + clslist[i].getId() + '\')">' + clslist[i].getLabel() + '</a></li>';
  }
  return ht;
}

function removeQuotes(str) {
  if(str.length>0 && str.charAt(0)=='"' && str.charAt(str.length-1)=='"') {
    return str.substr(1,str.length-2);
  }
  return str;
}

function getLocalName(str) {
  if(str!=null && str.lastIndexOf('#')>=0) {
    return str.substr(str.lastIndexOf('#')+1);
  }
  return str;
}

function rewriteUri(uri) {
  return uri.replace(/[äöüÄÖÜß#_]/g,"_");
}

$('#classtree').treeview(
  {data: getTree(),
   levels: 1,
   expandIcon: 'glyphicon glyphicon-chevron-right',
   nodeIcon : '',
   collapseIcon : 'glyphicon glyphicon-chevron-down'
   }
);


$('#classtree').on('nodeSelected', function(event, node) {
  $('#overview').hide();
  $('#detailsview').show();
  showDetails(node.id);
});

$(window).bind('hashchange', function() {
  var newuri=top.location.href.substr(top.location.href.indexOf("#")+1);
  if(newuri!=nowShown) {
    var obj=myont.getClass(newuri);
    if(obj!=null) {
      showDetails(newuri);
    } else {
      obj=myont.getDatatypeProperty(newuri);
      if(obj!=null) {
        showPropDetails(newuri,false);
      } else {
        showPropDetails(newuri,true);
      }
    }
  }
});


$('#comframe').load(function() {
  $('#comframe').show();
});


function getSinglePage() {
  var classes = myont.getClasses();
  var htmlTexts = '';
  allClasses=new Object();
  for(var i=0;i<classes.length;i++) {
    if(classes[i].getSuperClasses()==null || classes[i].getSuperClasses().length==0 ) {
      if(allClasses[classes[i].getName()]==null) {
        htmlTexts += getHtmlForClass(classes[i].getId());
        htmlTexts += '<hr/>';
        htmlTexts += getHtmlForSubClasses(classes[i]);
      }
    }
  }

  var objProps=myont.getObjectProperties();
  for (var i = 0; i < objProps.length; i++) {
    htmlTexts+=getHtmlForProp(objProps[i].getId(),true,objProps[i]);
    htmlTexts+='<hr/>';
  }

  var dataProps=myont.getDatatypeProperties();
  for (var i = 0; i < dataProps.length; i++) {
    htmlTexts+=getHtmlForProp(dataProps[i].getId(),false,dataProps[i]);
    htmlTexts+='<hr/>';
  }
  return htmlTexts;
}




function getHtmlForSubClasses(cls) {
  // console.log(level + ' ' + cls.getName());

  var h='';
  if(allClasses[cls.getName()]!=null) {
    h='tree is cut off here';
  } else {
    allClasses[cls.getName()] = 1;
    var subs = cls.getSubClasses();
    if (subs != null && subs.length > 0) {
      for (var i = 0; i < subs.length; i++) {
        if (allClasses[subs[i].getName()] == null) {
          // h += '<div class="subclassdiv">';
          h += getHtmlForClass(subs[i].getId());
          // h += '</div>';
          h+='<hr/>';
        }
      }
    }
  }
  return h;
}