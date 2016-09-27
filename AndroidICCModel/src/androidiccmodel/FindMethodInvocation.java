package androidiccmodel;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import androidiccmodel.handlers.SampleHandler;


public class FindMethodInvocation {
		
	static ArrayList<String> tempInvoName = new ArrayList<String>();
	static ArrayList<String> tempInvoKey = new ArrayList<String>();
	static ArrayList<ClassInstanceCreation> tempInvoCic = new ArrayList<ClassInstanceCreation>();
	static int broadcastCount = 0;
	
	public static void checkMethodInvocation(Block block){
				
		block.accept(new ASTVisitor(){
					
			public boolean visit(MethodInvocation node){
				String invoName = node.getName().toString();
								
				//if class instance node is found within the method body then check for method invocation
				if(ParseSource.invoFlag && !ParseSource.returnFlag){
					//Ignore setResult(int resultCode) method because it does not contain intent in the argument
					if((invoName.equals("setResult") && node.arguments().size()>1)||invoName.equals("startActivity")
							||invoName.equals("startActivityForResult")||invoName.equals("startService")||invoName.equals("stopService")||
							invoName.equals("bindService")||invoName.equals("sendBroadcast")||invoName.equals("getActivity")||invoName.equals("getActivities")
							||invoName.equals("getBroadcast")||invoName.equals("getService") || invoName.equals("setIntent")
							|| invoName.equals("setContent")){
						
						//for intent matching						
						if(invoName.equals("setIntent")){
							String classType = node.resolveTypeBinding().getName();
							String declClass = node.resolveMethodBinding().getDeclaringClass().getName();
							if(classType.equals("MenuItem") || declClass.equals("Preference")){
								storeKeyorObject(node, invoName);
							}
						}						
						else {
							storeKeyorObject(node, invoName);							
						}												
					}
				}
				//For dynamically registered broadcast components				
				addDynamicBroadcast(node, invoName);								
				
				return true;
			}
			
		});
		
		keyorObjectMatching();		
	}
	
	public static void storeKeyorObject(MethodInvocation node, String invoName){
		for(int i=0; i<node.arguments().size(); i++){
			if(((Expression) node.arguments().get(i)).resolveTypeBinding().getName().equals("Intent")){
				if(node.arguments().get(i).getClass().getSimpleName().equals("ClassInstanceCreation")){
					//for example: startActivity(new intent())
					ClassInstanceCreation cnode = (ClassInstanceCreation) node.arguments().get(i);
					tempInvoName.add(invoName);
					tempInvoKey.add(null);
					tempInvoCic.add(cnode);					
				}
				else if(node.arguments().get(i).getClass().getSimpleName().equals("SimpleName")){
					//for example: startActivity(intent)
					String invokey = ((Name) node.arguments().get(i)).resolveBinding().getKey();
					tempInvoName.add(invoName);
					tempInvoKey.add(invokey);					
					tempInvoCic.add(null);
					
				}
				else if(node.arguments().get(i).getClass().getSimpleName().equals("MethodInvocation")){
					//for example: startActivity(Intent.createChooser(intent, null), )
					MethodInvocation mi = (MethodInvocation) node.arguments().get(i);					
					for(int j=0; j<mi.arguments().size(); j++){
						if(((Expression) mi.arguments().get(j)).resolveTypeBinding().getName().equals("Intent")){
							if(mi.arguments().get(j).getClass().getSimpleName().equals("SimpleName")){
								String invokey = ((Name) mi.arguments().get(j)).resolveBinding().getKey();
								tempInvoName.add(invoName);
								tempInvoKey.add(invokey);
								tempInvoCic.add(null);
							}							
						}												
					}					
				}
			}
			
		}
	}
	
	public static void addDynamicBroadcast(MethodInvocation node, String invoName){
		if(invoName.equals("registerReceiver")){
			broadcastCount++;
			//always implicit
			ParseSource.sourceNode.add("none");
			ParseSource.targetNode.add("BR"+broadcastCount);
			//context always
			ParseSource.contextName.add("onReceive");
			//filter node
			ParseSource.intentID.add("F"+ParseSource.filter);
			ParseSource.filter++;
			ParseSource.methodInvoName.add("sendBroadcast");
			ParseSource.putExtra.add("false");
			//check permission
			if(node.arguments().size() == 4 && node.arguments().get(2) != null){
				SampleHandler.dynBroad.add("BR"+broadcastCount);
				SampleHandler.broadPerm.add("true");
			}else{
				SampleHandler.dynBroad.add("BR"+broadcastCount);
				SampleHandler.broadPerm.add("false");
			}
		}
	}
	
	public static void keyorObjectMatching(){
				
		for(int i=0; i<tempInvoName.size(); i++){
			if(tempInvoKey.get(i) != null){
				for(int j=0; j<ParseSource.key.size(); j++){
					if(ParseSource.key.get(j) != null){
						if(ParseSource.key.get(j).equals(tempInvoKey.get(i))){
							if(ParseSource.tempSourceNode.size() == 0){
								//System.out.println("Context not resolved.");
							}
							else if(ParseSource.tempSourceNode.get(j) != null){
								ParseSource.sourceNode.add(ParseSource.tempSourceNode.get(j));
								ParseSource.targetNode.add(ParseSource.tempTargetNode.get(j));
								ParseSource.contextName.add(ParseSource.tempContextName.get(j));
								
								ParseSource.key.set(j, null);
								addInvo(tempInvoName.get(i));
								ParseSource.putExtra.add(ParseSource.extra.get(j));								
							}
							
						}
					}
				}
			}
			else {
				for(int j=0; j<ParseSource.cic.size(); j++){
					if(ParseSource.cic.get(j) != null){
						if(ParseSource.cic.get(j) == tempInvoCic.get(i)){
							if(ParseSource.tempSourceNode.size() == 0){
								//System.out.println("Context not resolved.");
							}
							else if(ParseSource.tempSourceNode.get(j) != null){
								ParseSource.sourceNode.add(ParseSource.tempSourceNode.get(j));
								ParseSource.targetNode.add(ParseSource.tempTargetNode.get(j));
								ParseSource.contextName.add(ParseSource.tempContextName.get(j));
								
								ParseSource.cic.set(j, null);
								addInvo(tempInvoName.get(i));
								ParseSource.putExtra.add(ParseSource.extra.get(j));
							}
							
						}
					}
				}
			}
		}
		
	}
	
	public static void addInvo(String name){
		
		ParseSource.methodInvoName.add(name);		
		//create intent ID
		if(name.equals("getActivity")||name.equals("getActivities")||name.equals("getBroadcast")
				||name.equals("getService")){
			ParseSource.intentID.add("P"+ParseSource.pendingintent);
			ParseSource.pendingintent++;
		}else{
			ParseSource.intentID.add("I"+ParseSource.intent);
			ParseSource.intent++;
		}		
	}
	
	
}
