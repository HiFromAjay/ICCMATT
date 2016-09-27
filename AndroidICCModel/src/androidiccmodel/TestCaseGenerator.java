package androidiccmodel;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import androidiccmodel.handlers.SampleHandler;

public class TestCaseGenerator {

	List<String> entryNodes = new ArrayList<String>();
	List<String> exitNodes = new ArrayList<String>();
	List<String> edgeList = new ArrayList<String>();
	List<String> visitedEdge = new ArrayList<String>();
	List<String> testCase = new ArrayList<String>();
	List<String> eTestCase = new ArrayList<String>();
	List<String> methodCallList = new ArrayList<String>();
	List<String> methodNames = new ArrayList<String>();
	String callList;
	MethodDeclaration md;	
	
	private void getEntryNodes(){
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			if(!ParseSource.targetNode.contains(ParseSource.sourceNode.get(i))){
				if(!entryNodes.contains(ParseSource.sourceNode.get(i))){
					entryNodes.add(ParseSource.sourceNode.get(i));
				}								
			}
		}
	}		
		
	private void getExitNodes(){
		for(int i=0; i<ParseSource.targetNode.size(); i++){
			if(!ParseSource.sourceNode.contains(ParseSource.targetNode.get(i))){
				exitNodes.add(ParseSource.targetNode.get(i));				
			}
		}
	}
	
	private void createEdgeList(){
		for(int i=0; i<ParseSource.intentID.size(); i++){
			edgeList.add(ParseSource.intentID.get(i)+"/"+ParseSource.contextName.get(i));
		}
	}
	
	private String getCallSequence(String compName, String compType, String methodName, String intentMethod, String intent){
		ParseSource.returnwithInvo = false;
		callList = null;
		methodCallList.clear();		
		String aoncreate = "", aonstart = "", aonresume = "", aonrestart = "";
		String soncreate = "", sonstartcommand = "", sonbind = "";
		String callSeq = null;
		String mainMethod = methodName;
		String listenerMethod = null;
		if(methodName.contains("-")){
			mainMethod = methodName.substring(0, methodName.indexOf("-"));
			listenerMethod = methodName.substring(methodName.indexOf("-")+1);			
		}
		
		if(getMethodDecl(compName, mainMethod, intent) == null){			
			return methodName;
		}
		
		MethodDeclaration md = getMethodDecl(compName, mainMethod, intent);
		int paramSize = md.parameters().size();
		int contextPos = ParseSource.contextPosition(md);
		
		if(compType.equals("A:")){
			if(methodNames.contains("onCreate")){
				aoncreate = "onCreate";
			}
			if(methodNames.contains("onStart")){
				aonstart = "onStart";
			}
			if(methodNames.contains("onResume")){
				aonresume = "onResume";
			}
			if(methodNames.contains("onRestart")){
				aonrestart = "onRestart";
			}
			if(mainMethod.equals("onCreate")){
				if(listenerMethod == null){
					callSeq = "onCreate";
				}else{
					callSeq = aoncreate+"  "+aonstart+"  "+aonresume+"  "+listenerMethod;
				}				
			}else if(mainMethod.equals("onStart")){
				if(listenerMethod == null){
					callSeq = aoncreate+"  "+"onStart";
				}else{
					callSeq = aoncreate+"  "+aonstart+"  "+aonresume+"  "+listenerMethod;
				}							
			}else if(mainMethod.equals("onResume")){
				if(listenerMethod == null){
					callSeq = aoncreate+"  "+aonstart+"  "+"onResume";
				}else{
					callSeq = aoncreate+"  "+aonstart+"  "+aonresume+"  "+listenerMethod;
				}				
			}else if(mainMethod.equals("setResult")){
				callSeq = aonrestart+"  "+aonstart+"  "+aonresume+"  "+"onActivityResult";
			}else {
				//check call graph				
				if(listenerMethod == null){
					callSeq = aoncreate+"  "+aonstart+"  "+aonresume+"  "+getCallGraph(compName, mainMethod, md, paramSize, contextPos, null);
				}else{
					callSeq = aoncreate+"  "+aonstart+"  "+aonresume+"  "+getCallGraph(compName, mainMethod, md, paramSize, contextPos, null)+"  "+listenerMethod;
				}				
			}
		}else if(compType.equals("S:")){
			if(methodNames.contains("onCreate")){
				soncreate = "onCreate";
			}
			if(methodNames.contains("onStartCommand")){
				sonstartcommand = "onStartCommand";
			}
			if(methodNames.contains("onBind")){
				sonbind = "onBind";
			}
			if(mainMethod.equals("onCreate")){
				callSeq = "onCreate";
			}else if(mainMethod.equals("onStartCommand")){
				callSeq = soncreate+"  "+"onStartCommand";
			}else if(mainMethod.equals("onBind")){
				callSeq = soncreate+"  "+"onBind";
			}else{
				//check call graph				
				if(intentMethod.equals("startService") || intentMethod.equals("getService")){
					callSeq = soncreate+"  "+sonstartcommand+"  "+getCallGraph(compName, mainMethod, md, paramSize, contextPos, null);
				}else if(intentMethod.equals("bindService")){
					callSeq = soncreate+"  "+sonbind+"  "+getCallGraph(compName, mainMethod, md, paramSize, contextPos, null);
				}else if(intentMethod.equals("stopService")){
					callSeq = getCallGraph(compName, mainMethod, md, paramSize, contextPos, null)+"  "+"onDestroy";
				}
				
			}			
		}else if(compType.equals("BR:")){
			if(mainMethod.equals("onReceive")){
				callSeq = "onReceive";
			}else{
				//check call graph
				callSeq = "onReceive"+"  "+getCallGraph(compName, mainMethod, md, paramSize, contextPos, null);
			}			
		}
		
		if(callSeq == null){
			if(listenerMethod == null){
				callSeq = mainMethod;
			}else{
				callSeq = mainMethod+"  "+listenerMethod;
			}
		}
		return callSeq;
	}
	
	private MethodDeclaration getMethodDecl(String compName, final String methodName, final String intent){		
		md = null;
		methodNames.clear();
		if(SampleHandler.className.contains(compName)){
						
			IProject project = SampleHandler.getProject();
			IJavaProject javaProject = JavaCore.create(project);
			try {
				IPackageFragment[] packages = javaProject.getPackageFragments();
				
				for(IPackageFragment mypackage : packages) {
					if(mypackage.getKind() == IPackageFragmentRoot.K_SOURCE){
						for(ICompilationUnit unit : mypackage.getCompilationUnits()){
							
							if(unit.getElementName().toString().equals(compName)){
								ASTParser parser = ParseSource.createParser(unit);
								final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
								cu.accept(new ASTVisitor(){
									public boolean visit(MethodDeclaration med){
										final String tempMethod = med.getName().toString();
										methodNames.add(tempMethod);
										if(tempMethod.equals(methodName)){
											md = med;
										}
										return false;
									}
									
								});
							}							
										
						}
						
					}
				}
				
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(md == null){
			int position = 0;
			for(int k=0; k<ParseSource.intentID.size(); k++){
				if(ParseSource.intentID.get(k).equals(intent)){
					position = k;
				}
			}
			MethodDeclaration medcl = ParseSource.contextNodeName.get(position);
			String name = medcl.getName().toString();
			int param = medcl.parameters().size();
			String deClass = medcl.resolveBinding().getDeclaringClass().getName().toString();
			ParseSource.returnwithInvo = true;
			String context = getCallGraph(compName, methodName, medcl, param, ParseSource.contextPosition(medcl), deClass);
			ParseSource.returnwithInvo = false;
			
			if(context != null){
				md = getMethodDecl(compName, context, intent);
			}			
		}
		return md;
	}
	
	public String getCallGraph(final String compName, final String methodName, final MethodDeclaration med, final int paramSize, final int contextPos, final String dclass){		
		String callGraph = null;		
		final String className = compName.substring(0, compName.lastIndexOf("."));		
		if(SampleHandler.className.contains(compName)){
			//get call graph			
			IProject project = SampleHandler.getProject();
			IJavaProject javaProject = JavaCore.create(project);
			try {
				IPackageFragment[] packages = javaProject.getPackageFragments();
				
				for(IPackageFragment mypackage : packages) {
					if(mypackage.getKind() == IPackageFragmentRoot.K_SOURCE){
						for(ICompilationUnit unit : mypackage.getCompilationUnits()){
							
							if(unit.getElementName().toString().equals(compName)){
								ASTParser parser = ParseSource.createParser(unit);
								final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
								cu.accept(new ASTVisitor(){
									public boolean visit(final MethodDeclaration md){										
										final String tempMethod = md.getName().toString();
										final int para = md.parameters().size();
										final int pos = ParseSource.contextPosition(md);
										
										Block block = md.getBody();
										if(block != null){
											block.accept(new ASTVisitor(){
												public boolean visit(MethodInvocation node){													
													String declaringClass = node.resolveMethodBinding().getDeclaringClass().getName().toString();
													String cName;
													if(ParseSource.returnwithInvo){
														cName = dclass;
													}else{
														cName = className;
													}
													if(node.getName().toString().equals(methodName) && paramSize == node.arguments().size() 
															&& cName.equals(declaringClass)){														
														boolean overloaded = false;
														if(!node.arguments().isEmpty()){
															ITypeBinding[] paramtype = med.resolveBinding().getMethodDeclaration().getParameterTypes();
															for(int i=0; i<paramSize; i++){
																String argtype = ((Expression) node.arguments().get(i)).resolveTypeBinding().getName();
																//ignore Context position
																if(i != (contextPos-1)){
																	//ignore unknown class ex: Class<?>
																	if(!paramtype[i].getName().equals("Class<?>")){
																		//ignore if null is passed as argument
																		if(!("null").equals(argtype)){
																			if(!paramtype[i].getName().equals(argtype)){
																				//ignore if its integer for int
																				if(!(paramtype[i].getName().equals("int") && argtype.equals("Integer"))){
																					//match with the interface of the class in the argument
																					boolean match = false;
																					for(ITypeBinding k: ((Expression) node.arguments().get(i)).resolveTypeBinding().getInterfaces()){																					
																						if(k.getName().equals(paramtype[i].getName())){
																							match = true;
																						}
																					}
																					if(!match){
																						overloaded = true;
																					}
																				}
																			}
																		}																																		
																	}																
																}															
															}
														}
														if(!overloaded){
															if(ParseSource.returnwithInvo){
																callList = tempMethod;																
															}else{
																if(!methodCallList.contains(methodName)){
																	if(callList == null){
																		callList = tempMethod;
																	}else{
																		callList = tempMethod+"  "+callList;
																	}
																	getCallGraph(compName, tempMethod, md, para, pos, null);																
																}															
																methodCallList.add(methodName);															
															}	
														}
													}
													return true;
												}
											});
										}
										return false;
									}
									
								});
							}							
										
						}
						
					}
				}
				
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(callList == null){
			callGraph = methodName;
		}else{			
			callGraph = callList+"  "+methodName;
		}
		if(ParseSource.returnwithInvo){
			callGraph = callList;
		}
		return callGraph;
	}
	
	private boolean checkPreCondition(){		
		boolean preCondition = true;		
		
		List<String> uniqueNodes = new ArrayList<String>();		
		List<String> visitedNodes = new ArrayList<String>();		
		List<String> nodes = new ArrayList<String>();
		List<String> tempnodes = new ArrayList<String>();
		List<String> exnodes = new ArrayList<String>();
		//populate all unique nodes
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			if(!uniqueNodes.contains(ParseSource.sourceNode.get(i))){
				uniqueNodes.add(ParseSource.sourceNode.get(i));
			}
			if(!uniqueNodes.contains(ParseSource.targetNode.get(i))){
				uniqueNodes.add(ParseSource.targetNode.get(i));
			}
		}
		//check for node reachability from entry nodes
		for(int l=0; l<entryNodes.size(); l++){
			nodes.add(entryNodes.get(l));			
		}
		while(!nodes.isEmpty()){
			for(int k=0; k<nodes.size(); k++){
				//add entry nodes as visited
				if(!visitedNodes.contains(nodes.get(k))){
					visitedNodes.add(nodes.get(k));
				}
				//find target nodes of a source node
				boolean exit = true;
				for(int m=0; m<ParseSource.sourceNode.size(); m++){
					if(nodes.get(k).equals(ParseSource.sourceNode.get(m))){
						exit = false;
						if(!visitedNodes.contains(ParseSource.targetNode.get(m))){
							tempnodes.add(ParseSource.targetNode.get(m));
							visitedNodes.add(ParseSource.targetNode.get(m));							
						}
					}
				}
				if(exit){
					exnodes.add(nodes.get(k));
				}
			}
			nodes.clear();			
			for(int j=0; j<tempnodes.size(); j++){
				nodes.add(tempnodes.get(j));				
			}			
			tempnodes.clear();
		}		
		for(int n=0; n<uniqueNodes.size(); n++){
			if(!visitedNodes.contains(uniqueNodes.get(n))){
				preCondition = false;
			}
		}
		//exit node for pseudoloop graph
		for(int i=0; i<uniqueNodes.size(); i++){
			boolean same = false, different = false;
			for(int j=0; j<ParseSource.sourceNode.size(); j++){
				if(uniqueNodes.get(i).equals(ParseSource.sourceNode.get(j))){
					if(uniqueNodes.get(i).equals(ParseSource.targetNode.get(j))){
						same = true;
					}else{
						different = true;
					}
				}
			}
			if(!different && same){
				exnodes.add(uniqueNodes.get(i));
			}
		}
		//each node should be reachable to one of the exit nodes
		for(int i=0; i<uniqueNodes.size(); i++){
			if(!exnodes.contains(uniqueNodes.get(i))){			
				List<String> temp = new ArrayList<String>();			
				List<String> target = new ArrayList<String>();
				List<String> temptarget = new ArrayList<String>();
				
				temp.add(uniqueNodes.get(i));
			
				while(!temp.isEmpty()){
					temptarget.clear();
					for(int k=0; k<temp.size(); k++){
						for(int l=0; l<ParseSource.sourceNode.size(); l++){
							if(temp.get(k).equals(ParseSource.sourceNode.get(l))){
								if(!target.contains(ParseSource.targetNode.get(l))){
									target.add(ParseSource.targetNode.get(l));
									temptarget.add(ParseSource.targetNode.get(l));
								}
							}
						}
					}
					temp.clear();
					for(int m=0; m<temptarget.size(); m++){
						temp.add(temptarget.get(m));
					}
				}
				boolean targ = false;
				for(int n=0; n<exnodes.size(); n++){
					if(target.contains(exnodes.get(n))){
						targ = true;
					}
				}
				if(!targ){
					preCondition = false;
				}
			}
		}
		
		return preCondition;		
	}
	
	public void generateTestCase(){		
		getEntryNodes();
		boolean preCondition = checkPreCondition();		
		getExitNodes();
		createEdgeList();
		int testCaseCount = 0;		
		
		if(preCondition){
			while(!(edgeList.size()==visitedEdge.size())){
				for(int i=0; i<entryNodes.size(); i++){				
					testCase.clear();
					eTestCase.clear();
					String chosenEdge = null;
					int index = 0;
					boolean newEdge;
					boolean generateTestCase = false;					
								
					String source = entryNodes.get(i);				
					testCase.add(source);					
				
					while(!exitNodes.contains(source)){
						boolean exitFound = false;				
						newEdge = false;
						//get all the edges of the source node
						List<Integer> indeces = new ArrayList<Integer>(); 
						for(int j=0; j<ParseSource.sourceNode.size(); j++){
							if(ParseSource.sourceNode.get(j).equals(source)){
								indeces.add(j);
							}
						}
					
						//choose unvisited edge
						for(int s= 0; s<indeces.size(); s++){
							String intentid = edgeList.get(indeces.get(s));
							if(!visitedEdge.contains(intentid)){
								newEdge = true;
								generateTestCase = true;
								chosenEdge = intentid;
								index = indeces.get(s);
							}
						}
						//if unvisited not found then choose any of visited edge randomly
						if(!newEdge){
						
							boolean available = false;
							// to avoid visiting the same edge multiple times within the same test case
							for(int m=0; m<indeces.size(); m++){
								if(!testCase.contains(edgeList.get(indeces.get(m)))){
									available = true;
								}
							}	
						
							if(available){
								do{
									Random r = new Random();
									int num = indeces.get(r.nextInt(indeces.size()));
									chosenEdge = edgeList.get(num);
									index = num;							
								}while(testCase.contains(chosenEdge));
							}
							else{
								Random r = new Random();
								int num = indeces.get(r.nextInt(indeces.size()));
								chosenEdge = edgeList.get(num);
								index = num;
							}
												
						}
						//make target node the source node
						if(chosenEdge != null){
							//dynamically detecting exit node for example register->login->login
							//here login is an exit node
							if(testCase.size()>1){
								int pos = testCase.size()-2;
								if(chosenEdge.equals(testCase.get(pos))){
									exitFound = true;
									exitNodes.add(ParseSource.targetNode.get(index));
								}
							}
							
							if(!exitFound){
								testCase.add(chosenEdge);
								testCase.add(ParseSource.targetNode.get(index));
								if(newEdge){
									visitedEdge.add(chosenEdge);
								}
							}												
							source = ParseSource.targetNode.get(index);							
						}
					}		
					
					//only generate test case if there is at least one new edge				
					if(generateTestCase){						
						//making test case executable
						eTestCase.add(testCase.get(0));
						eTestCase.add(testCase.get(1).substring(0,testCase.get(1).indexOf("/")));					
						for(int k=2; k<testCase.size(); k++){
							//add component name
							eTestCase.add(testCase.get(k));
							//get component class name
							String componentName = testCase.get(k);
							if(SampleHandler.className.contains(componentName.concat(".java"))){
								componentName = componentName.concat(".java");
							}
							//get component type
							String compType = null;
							for(int l=0; l<SampleHandler.className.size(); l++){
								if(componentName.equals(SampleHandler.className.get(l))){
									compType = ManifestParser.componentType.get(l);
								}
							}
							//get intent dispatcher method
							String intentMethod = ParseSource.methodInvoName.get(edgeList.indexOf(testCase.get(k-1)));									
						
							//get method name
							String methodName = null;
							int lastElement = testCase.size()-1;
							if(k<lastElement){
								methodName = testCase.get(k+1).substring(testCase.get(k+1).indexOf("/")+1);
								String intent = testCase.get(k+1).substring(0, testCase.get(k+1).indexOf("/"));
								String callSeq = getCallSequence(componentName, compType, methodName, intentMethod, intent)+"/"+intent;								
								eTestCase.add(callSeq);
							}							
							k++;
						}						
					
						try {
							boolean append = true;
							if(testCaseCount==0){
								append = false;
							}
						
							FileWriter fw = new FileWriter(SampleHandler.testCase, append);
							BufferedWriter br = new BufferedWriter(fw);						
							for(int l=0; l<eTestCase.size(); l++){
								br.append(eTestCase.get(l)+"  ");								
							}
							br.newLine();
							br.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						testCaseCount++;					
					}			
					
				}
			}
		}else{
			System.out.println("Test cases cann't be generated due to improper ICC graph.");
		}				
		
	}
	
}
