package androidiccmodel;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import androidiccmodel.handlers.SampleHandler;

public class ParseSource {
	
	public static ArrayList<String> sourceNode = new ArrayList<String>();
	public static ArrayList<String> targetNode = new ArrayList<String>();
	public static ArrayList<String> contextName = new ArrayList<String>();
	public static ArrayList<MethodDeclaration> contextNodeName = new ArrayList<MethodDeclaration>();
	public static ArrayList<String> methodInvoName = new ArrayList<String>();
	public static ArrayList<String> intentID = new ArrayList<String>();
	public static ArrayList<String> putExtra = new ArrayList<String>();
	
	static ArrayList<String> tempSourceNode = new ArrayList<String>();
	static ArrayList<String> tempTargetNode = new ArrayList<String>();
	static ArrayList<String> tempContextName = new ArrayList<String>();
	static ArrayList<String> key = new ArrayList<String>();
	static ArrayList<ClassInstanceCreation> cic = new ArrayList<ClassInstanceCreation>();
	static ArrayList<String> extra = new ArrayList<String>();
	
	
	static int intent = 1, pendingintent = 1, pseudonode = 1, filter = 1;
	static boolean contextFlag;
	static boolean invoFlag, returnFlag, returnwithInvo;	
	int intentInstanceCount;
	boolean callFromSize2;
	boolean listener;
	static String isExtra;
	int size;
	String mlistenerName;
	
	public static int intentCount = 0;
		
	
	public void parse(final ICompilationUnit unit){
		
		ASTParser parser = createParser(unit);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		 
		cu.accept(new ASTVisitor() {
			
			public synchronized boolean visit(final MethodDeclaration mnode){
				
				clearTempArrayList();
								
				final String methodName = mnode.getName().toString();
				final int parameterSize = mnode.parameters().size();				
				String tempname = unit.getElementName().toString();
				final String filename = tempname.substring(0, tempname.lastIndexOf("."));
				mlistenerName = methodName;
				returnwithInvo = false;
								
				checkreturnTypeAndsetFlag(mnode);
								
				if(returnFlag){
					checkmethodinvoinReturnmethod(mnode);
				}
				
				invoFlag = false;
				listener = false;
				
				//get the Context position in the parameter
				final int contextIndex = contextPosition(mnode);
												
				final Block block = mnode.getBody();
				size = sourceNode.size();
				if(block != null){
					block.accept(new ASTVisitor(){
						
						public boolean visit(ClassInstanceCreation node){
													
							if(node.getType().toString().equals("Intent")){
								intentCount++;
								checkListenerEvent(node, mnode.getName().toString());
																
								String instanceName = getInstanceName(node);
								isExtra = checkExtra(block, instanceName);							
								addKey(node);													
								
								contextFlag = false;								
								invoFlag = true;	
																																				
								if(node.arguments().isEmpty()){
									//check for explicit argument setting such as setClass, setClassName
									CheckArgExplicitSetup.checkArgExplicit(mnode, methodName, contextIndex, parameterSize, unit, instanceName);
									
									if(CheckArgExplicitSetup.explicitargFlag && contextFlag){
										for(int i=0; i<ResolveContext.contextCount-1; i++){										
											addKey(node);
										}
									}
									//if no explicit argument
									if((!CheckArgExplicitSetup.explicitargFlag) && (!CheckArgExplicitSetup.setclassname)){
										String fileName = unit.getElementName().toString();
										String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
										tempSourceNode.add(sourceName);
										tempTargetNode.add("none");
										tempContextName.add(methodName);										
									}
									
									if((!CheckArgExplicitSetup.explicitargFlag) && (CheckArgExplicitSetup.setclassname)){
										key.remove(key.size()-1);
										cic.remove(cic.size()-1);
										extra.remove(extra.size()-1);
										//System.out.println("Not resolved.");
									}
									
									//check for setComponent(ComponentName component)
									//check for setPackage(String packageName)
								}
								else if(node.arguments().size()==1){
									CheckArgExplicitSetup.checkArgExplicit(mnode, methodName, contextIndex, parameterSize, unit, instanceName);
									
									if(CheckArgExplicitSetup.explicitargFlag && contextFlag){
										for(int i=0; i<ResolveContext.contextCount-1; i++){										
											addKey(node);
										}
									}
									
									if((!CheckArgExplicitSetup.explicitargFlag) && (!CheckArgExplicitSetup.setclassname)){										
										String fileName = unit.getElementName().toString();
										String sourceName = fileName.substring(0, fileName.lastIndexOf("."));										
										tempSourceNode.add(sourceName);
										tempTargetNode.add("none");
										tempContextName.add(methodName);										
									}
									
									if((!CheckArgExplicitSetup.explicitargFlag) && (CheckArgExplicitSetup.setclassname)){
										key.remove(key.size()-1);
										cic.remove(cic.size()-1);
										extra.remove(extra.size()-1);
										//System.out.println("Not resolved.");
									}
									//check for setComponent(ComponentName component)
									//check for setPackage(String packageName)
								}
								else if(node.arguments().size()==2){									
									callFromSize2 = true;
									//get the argument type
									String arg0type = (String) ((Expression) node.arguments().get(0)).resolveTypeBinding().getName();
									String arg1type = (String) ((Expression) node.arguments().get(1)).resolveTypeBinding().getName();
																											
									String arg1 = node.arguments().get(1).toString();
									String type = "temp";
									if(arg1.contains(".")){
										int dotcount = getDotCount(arg1);
										String targetName = null;
										//in case of package name
										if(dotcount>1){
											int index = getIndex(arg1, dotcount);
											targetName = arg1.substring(index, arg1.lastIndexOf("."));
										}
										//in case of class name
										else{
											targetName = arg1.substring(0, arg1.lastIndexOf("."));
										}
										
										if(SampleHandler.className.contains(targetName.concat(".java"))){
											type = arg1.substring(arg1.lastIndexOf(".")+1);
										}										
									}
																	
									processIntent(unit, mnode, node, type, arg0type, arg1type, methodName, contextIndex, parameterSize, filename, callFromSize2);
									//if condition 1 and 2 are not true then
									//3: check for setClass(Context packageContext, Class<?> cls)
									//if 1, 2, and 3 are not true then Intent(String action, Uri uri)														
								}
								else if(node.arguments().size()==4){
									callFromSize2 = false;
									//check 3rd argument for context or this and 4th argument for class file
									String arg3type = (String) ((Expression) node.arguments().get(2)).resolveTypeBinding().getName();								
									String arg4type = (String) ((Expression) node.arguments().get(3)).resolveTypeBinding().getName();
									
									String arg4 = node.arguments().get(3).toString();
									String type = "temp";
									if(arg4.contains(".")){
										int dotcount = getDotCount(arg4);
										String targetName = null;
										//in case of package name
										if(dotcount>1){
											int index = getIndex(arg4, dotcount);
											targetName = arg4.substring(index, arg4.lastIndexOf("."));
										}
										//in case of class name
										else{
											targetName = arg4.substring(0, arg4.lastIndexOf("."));
										}
										if(SampleHandler.className.contains(targetName.concat(".java"))){
											type = arg4.substring(arg4.lastIndexOf(".")+1);
										}										
									}
									
									processIntent(unit, mnode, node, type, arg3type, arg4type, methodName, contextIndex, parameterSize, filename, callFromSize2);									
								}
								
							}
							
							
							
							return true;
						}					
											
											
					});				
										
					FindMethodInvocation.checkMethodInvocation(block);
					//for anonymous listener event					
					if(sourceNode.size()>size){
						if(size>=1){
							for(int i=1; i<=(sourceNode.size()-size); i++){
								contextNodeName.add(mnode);																
							}							
						}
						
						if(contextName.get(contextName.size()-1).equals(methodName) && sourceNode.get(sourceNode.size()-1).equals(filename)){
							if(listener){
								contextName.set(contextName.size()-1, methodName+"-"+mlistenerName);								
							}
						}
						if(returnwithInvo){							
							TestCaseGenerator tcg = new TestCaseGenerator();
							String fileName = unit.getElementName().toString();
							String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
							for(int i=1; i<=(sourceNode.size()-size); i++){
								String context = tcg.getCallGraph(sourceNode.get(sourceNode.size()-i)+".java", methodName, mnode, parameterSize, contextIndex, sourceName);
								if(context != null){
									contextName.set(contextName.size()-i, context);
								}																
							}														
						}
					}
					
				}
								
				return false;
			}				
			
		});		
		
	}
	
	public static void clearTempArrayList(){
		tempSourceNode.clear();
		tempTargetNode.clear();
		tempContextName.clear();
		key.clear();
		cic.clear();
		extra.clear();
	}
	
	public static int getDotCount(String arg){
		int counter = 0;
		for(int i=0; i<arg.length(); i++){
			if(arg.charAt(i) == '.'){
				counter++;
			}
		}
		return counter;
	}
	
	public static int getIndex(String arg1, int dotcount){
		int index = 0;
		int counter = 0;
		for(int i=0; i<arg1.length(); i++){
			if(arg1.charAt(i) == '.'){
				counter++;
				if(counter == (dotcount-1)){
					index = i+1;
				}
			}
		}
		return index;
	}
	
	public static void checkreturnTypeAndsetFlag(MethodDeclaration mnode){
		String type = "temp";
		if(mnode.getReturnType2() != null){
			type = mnode.getReturnType2().toString();
		}
		final String returnType = type;		
		
		if(returnType.equals("Intent") || returnType.equals("PendingIntent")){
			returnFlag = true;					
		}else{
			returnFlag = false;
		}
	}
	
	public static void checkmethodinvoinReturnmethod(MethodDeclaration mnode){
		final Block blk = mnode.getBody();
		blk.accept(new ASTVisitor(){
			
			public boolean visit(MethodInvocation node){
				String invoName = node.getName().toString();
					
				//Ignore setResult(int resultCode) method because it does not contain intent in the argument
				if((invoName.equals("setResult") && node.arguments().size()>1)||invoName.equals("startActivity")
						||invoName.equals("startActivityForResult")||invoName.equals("startService")||invoName.equals("stopService")||
						invoName.equals("bindService")||invoName.equals("sendBroadcast")||invoName.equals("getActivity")||invoName.equals("getActivities")
						||invoName.equals("getBroadcast")||invoName.equals("getService") || invoName.equals("setIntent")){
					
					returnFlag = false;
					returnwithInvo = true;
				}															
				
				return true;
			}
			
		});
	}
	
	public static synchronized int contextPosition(MethodDeclaration md){
		int position = 0;
						
		if(md.parameters().toString().contains("Context")){	
			for(int i=0; i<md.parameters().size(); i++){
				if(md.parameters().get(i).toString().contains("Context")){
					position = i+1;
				}
			}						
		}
		//context can be Activity also
		else if(md.parameters().toString().contains("Activity")){
			for(int i=0; i<md.parameters().size(); i++){
				if(md.parameters().get(i).toString().contains("Activity")){
					position = i+1;
				}
			}								
		}
		
		return position;
	}
	
	
	public static ASTParser createParser(ICompilationUnit unit){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setSource(unit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		return parser;
	}
		
	public static void addKey(ClassInstanceCreation node){
		
		if(node.getParent().getClass().getSimpleName().equals("MethodInvocation")){
			cic.add(node);
			key.add(null);
			extra.add(isExtra);			
		}else if(node.getParent().getClass().getSimpleName().equals("VariableDeclarationFragment")){
			String keyString = ((VariableDeclarationFragment) node.getParent()).resolveBinding().getKey();			
			key.add(keyString);			
			cic.add(null);
			extra.add(isExtra);			
		}else if(node.getParent().getClass().getSimpleName().equals("Assignment")){
			String keyString = ((Name) ((Assignment) node.getParent()).getLeftHandSide()).resolveBinding().getKey();			
			key.add(keyString);			
			cic.add(null);
			extra.add(isExtra);			
		}
		
	}
	
	private void checkListenerEvent(ClassInstanceCreation node, String mName){
		if(node.getParent().getClass().getSimpleName().equals("VariableDeclarationFragment")){
			mlistenerName = ((VariableDeclarationFragment) node.getParent()).resolveBinding().getDeclaringMethod().getName();
			if(!mName.equals(mlistenerName)){
				listener = true;				
			}
		}
	}
	
	private static String getInstanceName(ClassInstanceCreation node){
		String name = null;
		
		if(node.getParent().getClass().getSimpleName().equals("VariableDeclarationFragment")){
			name = ((VariableDeclarationFragment) node.getParent()).resolveBinding().getName();		
		}else if(node.getParent().getClass().getSimpleName().equals("Assignment")){
			name = ((Name) ((Assignment) node.getParent()).getLeftHandSide()).resolveBinding().getName();			
		}else if(node.getParent().getClass().getSimpleName().equals("MethodInvocation")){
			name = null;
		}		
		return name;
	}
	
	public static String checkExtra(Block block, final String instanceName){
		isExtra = "false";
		block.accept(new ASTVisitor(){
			public boolean visit(MethodInvocation node){
				if(node.getName().toString().equals("putExtra") || node.getName().toString().equals("putExtras")){
					if((instanceName != null) && instanceName.equals(node.getExpression().toString())){						
						isExtra = "true";
					}					
				}
				return true;
			}
		});		
		return isExtra;
	}
	
	public static void processIntent(ICompilationUnit unit, MethodDeclaration mnode, ClassInstanceCreation node, String type, String contextArg, String classArg,
			String methodName, int contextIndex, int parameterSize, String filename, boolean callFromSize2){
		//1: check 2nd or 4th argument for class file
		if(type.equals("class")){
			
			String superclass = null;
			if(callFromSize2){
				superclass = ((Expression) node.arguments().get(0)).resolveTypeBinding().getSuperclass().getName();
			}
			else{
				superclass = ((Expression) node.arguments().get(2)).resolveTypeBinding().getSuperclass().getName();
			}
			
			//2: check 1st or 3rd argument for this class
			if(SampleHandler.className.contains(contextArg.concat(".java"))){
								
				if(callFromSize2){
					String sourceName = (String) ((Expression) node.arguments().get(0)).resolveTypeBinding().getName();										
					tempSourceNode.add(sourceName);
					//remove trailing .class from name
					String target = node.arguments().get(1).toString();
					int dotcount = getDotCount(target);
					String targetName = null;
					//in case of package name
					if(dotcount>1){
						int index = getIndex(target, dotcount);
						targetName = target.substring(index, target.lastIndexOf("."));
					}
					//in case of class name
					else{
						targetName = target.substring(0, target.lastIndexOf("."));
					}
					
					tempTargetNode.add(targetName);					
				}
				else{
					String sourceName = (String) ((Expression) node.arguments().get(2)).resolveTypeBinding().getName();										
					tempSourceNode.add(sourceName);
					//remove trailing .class from name
					String target = node.arguments().get(3).toString();
					int dotcount = getDotCount(target);
					String targetName = null;
					//in case of package name
					if(dotcount>1){
						int index = getIndex(target, dotcount);
						targetName = target.substring(index, target.lastIndexOf("."));
					}
					//in case of class name
					else{
						targetName = target.substring(0, target.lastIndexOf("."));
					}
										
					tempTargetNode.add(targetName);
				}								
				
				tempContextName.add(methodName);				
			}								
			//if 1st arguments contain Context then resolve that context
			else if(("Context").equals(contextArg) || ("Activity").equals(contextArg) 
					|| ("Application").equals(contextArg) ||("FragmentActivity").equals(contextArg)){												
																				
				contextFlag = true;
				String target = null;
				if(callFromSize2){
					//remove trailing .class from name
					target = node.arguments().get(1).toString();
				}
				else{
					target = node.arguments().get(3).toString();
				}
				int dotcount = getDotCount(target);
				String targetName = null;
				//in case of package name
				if(dotcount>1){
					int index = getIndex(target, dotcount);
					targetName = target.substring(index, target.lastIndexOf("."));
				}
				//in case of class name
				else{
					targetName = target.substring(0, target.lastIndexOf("."));
				}
				
				try {
					if(returnFlag){
						ReturnMethodDeclaration rmd = new ReturnMethodDeclaration();												
						rmd.returnDeclaration(mnode, targetName);
						rmd.resolveContext(targetName);
					}
					else {	
						//for condition such as calling getApplicationContext()/getBaseContext() directly in the argument
						if(contextIndex == 0){
							
							//don't need to resolve context
							ResolveContext.invoNotFound = false;
							//either contextFlag = false or ResolveContext.contextCount = 0
							contextFlag = false;
							
							String fileName = unit.getElementName().toString();
							String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
																					
							tempSourceNode.add(sourceName);
							tempTargetNode.add(targetName);
							tempContextName.add(methodName);							
						}
						else{							
							ResolveContext rc = new ResolveContext();							
							rc.resolveContext(mnode, methodName, targetName, contextIndex, parameterSize, filename);
							
						}
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(contextFlag){
					for(int i=0; i<ResolveContext.contextCount-1; i++){
						addKey(node);						
					}
				}
				//if matching method invocation with reference to the context is not found
				if(ResolveContext.invoNotFound){
					contextFlag = false;
					String sourceName = mnode.resolveBinding().getDeclaringClass().getName();
															
					if(!sourceName.equals(targetName)){												
						tempSourceNode.add(sourceName);
						tempTargetNode.add(targetName);
						tempContextName.add(methodName);						
					}else if((methodName.equals("onReceive")||methodName.equals("onUpdate")) && sourceName.equals(targetName)){
						tempSourceNode.add(sourceName);
						tempTargetNode.add(targetName);
						tempContextName.add(methodName);						
					}else if(sourceName.equals(targetName)){
						tempSourceNode.add(null);
						tempTargetNode.add(null);
						tempContextName.add(null);						
						//System.out.println("Not resolved.");
					}
					
				}
			
			}
			// any class can extend Application. In this case contextArg will return the name of the class
			else if((superclass != null) && (superclass.equals("Application"))){
				
				String target = null;
				if(callFromSize2){
					//remove trailing .class from name
					target = node.arguments().get(1).toString();
				}
				else{
					target = node.arguments().get(3).toString();
				}
				int dotcount = getDotCount(target);
				String targetName = null;
				//in case of package name
				if(dotcount>1){
					int index = getIndex(target, dotcount);
					targetName = target.substring(index, target.lastIndexOf("."));
				}
				//in case of class name
				else{
					targetName = target.substring(0, target.lastIndexOf("."));
				}
				
				String fileName = unit.getElementName().toString();
				String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
				
				tempSourceNode.add(sourceName);
				tempTargetNode.add(targetName);
				tempContextName.add(methodName);
			}
			//for other situations
			else {
				//remove the last added key of intent				
				key.remove(key.size()-1);
				cic.remove(cic.size()-1);
				extra.remove(extra.size()-1);
				//System.out.println("Not resolved.");
			}
		}
		//if the first argument is String action and 2nd argument is uri
		else if(contextArg.equals("String") && classArg.equals("Uri")){																	
			String instanceName = getInstanceName(node);
			CheckArgExplicitSetup.checkArgExplicit(mnode, methodName, contextIndex, parameterSize, unit, instanceName);
			
			if(CheckArgExplicitSetup.explicitargFlag && contextFlag){
				for(int i=0; i<ResolveContext.contextCount-1; i++){										
					addKey(node);
				}
			}
			
			if((!CheckArgExplicitSetup.explicitargFlag) && (!CheckArgExplicitSetup.setclassname)){
				String fileName = unit.getElementName().toString();
				String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
				tempSourceNode.add(sourceName);
				tempTargetNode.add("none");
				tempContextName.add(methodName);
			}
			
			if((!CheckArgExplicitSetup.explicitargFlag) && (CheckArgExplicitSetup.setclassname)){
				key.remove(key.size()-1);
				cic.remove(cic.size()-1);
				extra.remove(extra.size()-1);
				//System.out.println("Not resolved.");
			}			
			
		}
		//if the first argument is String action and 2nd argument is null instead of uri
		else if(contextArg.equals("String") && classArg.equals("null")){																	
			
			String fileName = unit.getElementName().toString();
			String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
			tempSourceNode.add(sourceName);
			tempTargetNode.add("none");
			tempContextName.add(methodName);			
		}
		//if the first argument is null and 2nd argument is uri
		else if(contextArg.equals("null") && classArg.equals("Uri")){																	
				
			String fileName = unit.getElementName().toString();
			String sourceName = fileName.substring(0, fileName.lastIndexOf("."));
			tempSourceNode.add(sourceName);
			tempTargetNode.add("none");
			tempContextName.add(methodName);		
				
		}
		//for Intent(Context, Class<?>)
		else{
			key.remove(key.size()-1);
			cic.remove(cic.size()-1);
			extra.remove(extra.size()-1);
			//dontcheckInvo = true;
			//System.out.println("Not resolved.");
		}
	}

	
}
