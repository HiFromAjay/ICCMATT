package androidiccmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import androidiccmodel.handlers.SampleHandler;

public class ManifestParser {
	
	public static List<String> componentType = new ArrayList<String>();
	
	public void manifestParse(IFile manifest){
		try{			
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(manifest.getContents());
			doc.getDocumentElement().normalize();
			
			//get list of activities
			NodeList activityList = doc.getElementsByTagName("activity");
			for(int i=0; i<activityList.getLength(); i++){
				Node nNode = activityList.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) nNode;
					//check for use permission
					if(eElement.hasAttribute("android:permission")){
						SampleHandler.permission.add("true");						
					}else{
						SampleHandler.permission.add("false");						
					}
					//check exported flag
					if(eElement.hasAttribute("android:exported")){
						SampleHandler.exported.add(eElement.getAttribute("android:exported"));
					}else{
						SampleHandler.exported.add("false");
					}
					//get name
					String activityName = eElement.getAttribute("android:name");					
					if(activityName.contains(".")){
						String filename = activityName.substring(activityName.lastIndexOf(".")+1).concat(".java").trim();
						if(filename.contains("$")){
							filename = filename.substring(filename.lastIndexOf("$")+1);
						}
						SampleHandler.className.add(filename);
						componentType.add("A:");
						
					} else {
						String filename = activityName.concat(".java").trim();
						SampleHandler.className.add(filename);
						componentType.add("A:");
						
					}					
				}
			}
			
			//get list of services
			NodeList serviceList = doc.getElementsByTagName("service");
			for(int i=0; i<serviceList.getLength(); i++){
				Node sNode = serviceList.item(i);
				if(sNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) sNode;
					//check for use permission
					if(eElement.hasAttribute("android:permission")){
						SampleHandler.permission.add("true");						
					}else{
						SampleHandler.permission.add("false");						
					}
					//check exported flag
					if(eElement.hasAttribute("android:exported")){
						SampleHandler.exported.add(eElement.getAttribute("android:exported"));
					}else{
						SampleHandler.exported.add("false");
					}
					//get name
					String serviceName = eElement.getAttribute("android:name");
					if(serviceName.contains(".")){
						String filename = serviceName.substring(serviceName.lastIndexOf(".")+1).concat(".java").trim();
						if(filename.contains("$")){
							filename = filename.substring(filename.lastIndexOf("$")+1);
						}
						SampleHandler.className.add(filename);
						componentType.add("S:");
						
					} else {
						String filename = serviceName.concat(".java").trim();
						SampleHandler.className.add(filename);
						componentType.add("S:");
						
					}					
				}
			}
			
			//get list of broadcast
			NodeList castList = doc.getElementsByTagName("receiver");
			for(int i=0; i<castList.getLength(); i++){
				Node bNode = castList.item(i);
				if(bNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) bNode;
					//check for use permission
					if(eElement.hasAttribute("android:permission")){
						SampleHandler.permission.add("true");						
					}else{
						SampleHandler.permission.add("false");						
					}
					//check exported flag
					if(eElement.hasAttribute("android:exported")){
						SampleHandler.exported.add(eElement.getAttribute("android:exported"));
					}else{
						SampleHandler.exported.add("false");
					}
					//get name
					String castName = eElement.getAttribute("android:name");										
					if(castName.contains(".")){
						String filename = castName.substring(castName.lastIndexOf(".")+1).concat(".java").trim();
						if(filename.contains("$")){
							filename = filename.substring(filename.lastIndexOf("$")+1);
						}
						SampleHandler.className.add(filename);
						componentType.add("BR:");
						
					} else {
						String filename = castName.concat(".java").trim();
						SampleHandler.className.add(filename);
						componentType.add("BR:");
						
					}					
				}
			}
			
			//get launcher along with associated component name
			NodeList categoryList = doc.getElementsByTagName("category");
			for(int i=0; i<categoryList.getLength(); i++){
				Node catNode = categoryList.item(i);
				if(catNode.getNodeType() == Node.ELEMENT_NODE){
					Element elem = (Element) catNode;
					String type = elem.getAttribute("android:name");
					if((type.substring(type.lastIndexOf(".")+1)).equals("LAUNCHER")){
						Element parent = (Element) elem.getParentNode().getParentNode();
						String temp = parent.getAttribute("android:name");
						String mainActivity = temp.substring(temp.lastIndexOf(".")+1);
						
						//add LAUNCHER and the associated activity in the list
						ParseSource.sourceNode.add("LAUNCHER");
						ParseSource.targetNode.add(mainActivity);
						ParseSource.contextName.add("onCreate");
						ParseSource.methodInvoName.add("startActivity");
						ParseSource.intentID.add("F"+ParseSource.filter);
						ParseSource.filter++;
						ParseSource.putExtra.add("false");
						
					}
				}
			}
			
			//get shortcuts along with associated component name
			NodeList actionList = doc.getElementsByTagName("action");
			for(int i=0; i<actionList.getLength(); i++){
				Node actNode = actionList.item(i);
				if(actNode.getNodeType() == Node.ELEMENT_NODE){
					Element elem = (Element) actNode;
					String type = elem.getAttribute("android:name");
					if((type.substring(type.lastIndexOf(".")+1)).equals("CREATE_SHORTCUT")){
						Element parent = (Element) elem.getParentNode().getParentNode();
						String temp = parent.getAttribute("android:name");
						String shortActivity = temp.substring(temp.lastIndexOf(".")+1);
						
						//SHORTCUT can be more than one in an application						
						String shortcut = "SHORTCUT";
												
						//add SHORTCUT and the associated activity in the list
						ParseSource.sourceNode.add(shortcut);
						ParseSource.targetNode.add(shortActivity);
						ParseSource.contextName.add("onCreate");
						ParseSource.methodInvoName.add("startActivity");
						ParseSource.intentID.add("F"+ParseSource.filter);
						ParseSource.filter++;
						ParseSource.putExtra.add("false");
						
					}
				}
			}
			
			//get intent filters and the components associated with them
			NodeList filterList = doc.getElementsByTagName("intent-filter");
			for(int i=0; i<filterList.getLength(); i++){
				Node filterNode = filterList.item(i);
				
				//If the filter has already been covered in LAUNCHER and SHORTCUTs then ignore that filter
				boolean flag = false;
				NodeList childList = filterNode.getChildNodes();
				for(int j=0; j<childList.getLength(); j++){
					Node iter = childList.item(j);
					if(iter.getNodeType() == Node.ELEMENT_NODE){
						Element iterElement = (Element) iter;
						String attr = iterElement.getAttribute("android:name");
						if(attr.equals("android.intent.category.LAUNCHER")||attr.equals("android.intent.action.CREATE_SHORTCUT")){
							flag = true;
						}
					}					
				}
				
				Node filterParent = filterNode.getParentNode();				
				if(filterParent.getNodeType() == Node.ELEMENT_NODE && flag == false){
					Element elem = (Element) filterParent;
					String tempName = elem.getAttribute("android:name");
					String compName = tempName.substring(tempName.lastIndexOf(".")+1);
					String componentType = filterParent.getNodeName().toString();
					
					if(!elem.hasAttribute("android:exported")){
						int index = SampleHandler.className.indexOf(compName+".java");
						SampleHandler.exported.set(index, "true");
					}
					
					//add filters and the associated components in the list
					ParseSource.sourceNode.add("none");
					ParseSource.targetNode.add(compName);
					ParseSource.intentID.add("F"+ParseSource.filter);
					ParseSource.filter++;
					ParseSource.putExtra.add("false");
					
					if(componentType.equals("activity")){
						ParseSource.contextName.add("onCreate");
						ParseSource.methodInvoName.add("startActivity");						
					}
					else if(componentType.equals("receiver")){
						ParseSource.contextName.add("onReceive");
						ParseSource.methodInvoName.add("sendBroadcast");
					}
					else if(componentType.equals("service")){
						ParseSource.contextName.add("onCreate");
						ParseSource.methodInvoName.add("startService");
					}					
					
					
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}

}
