package androidiccmodel;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import androidiccmodel.handlers.SampleHandler;


public class ResolveContext {
	
	static int contextCount;
	static boolean invoNotFound;
	boolean recursiveCall = false;	
	static String sourceName;
	static String returnName;
	static String returnMethodName;
	String sName = null;
	
	public synchronized void resolveContext(final MethodDeclaration mdnode, final String mName, final String tName, final int index, final int parameterSize, final String className) throws IOException {
				
		if(!recursiveCall){
			contextCount = 0;
		}		
		
		invoNotFound = true;		
		
		IProject project = SampleHandler.getProject();
		
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IPackageFragment[] packages = javaProject.getPackageFragments();
			
			for(IPackageFragment mypackage : packages) {
				if(mypackage.getKind() == IPackageFragmentRoot.K_SOURCE){					
					for(final ICompilationUnit unit : mypackage.getCompilationUnits()){						
						for(int i=0; i<SampleHandler.className.size(); i++){
							
							if(SampleHandler.className.get(i).equals(unit.getElementName().toString())){
																
								ASTParser parser = ParseSource.createParser(unit);
								final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
								 
								cu.accept(new ASTVisitor() {
									
									public boolean visit(final MethodDeclaration md){
										
										final String methodName = md.getName().toString();										
										final int contextPosition = ParseSource.contextPosition(md);
										final int paramSize = md.parameters().size();
																				
										Block block = md.getBody();
										if(block != null){
											block.accept(new ASTVisitor(){
											
												public boolean visit(MethodInvocation node){
													
													String declaringClass = node.resolveMethodBinding().getDeclaringClass().getName().toString();																							
													//parameter and argument size should be equal
													if(node.getName().toString().equals(mName) && parameterSize == node.arguments().size() 
															&& !(node.arguments().isEmpty()) && className.equals(declaringClass)){
														
														boolean overloadedCheck = false;
														ITypeBinding[] paramtype = mdnode.resolveBinding().getMethodDeclaration().getParameterTypes();
														
														for(int i=0; i<parameterSize; i++){
															String argtype = ((Expression) node.arguments().get(i)).resolveTypeBinding().getName();
															//ignore Context position
															if(i != (index-1)){
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
																					overloadedCheck = true;
																				}
																			}
																		}
																	}																																		
																}																
															}															
														}
														
														if(!overloadedCheck){																																							
															String argtype = (String) ((Expression) node.arguments().get(index-1)).resolveTypeBinding().getName();															
															
															if(SampleHandler.className.contains(argtype.concat(".java"))){
																
																recursiveCall = false;
																invoNotFound = false;
																//sourceName = null;
																String sourceName = (String) ((Expression) node.arguments().get(index-1)).resolveTypeBinding().getName();
																																												
																if(ParseSource.returnFlag){
																	ParseSource.sourceNode.add(sourceName);
																	ParseSource.targetNode.add(tName);
																	ParseSource.contextName.add(mName);
																	FindMethodInvocation.addInvo(returnName);
																	ParseSource.putExtra.add(ParseSource.isExtra);
																	
																}
																else {
																	ParseSource.tempSourceNode.add(sourceName);
																	ParseSource.tempTargetNode.add(tName);
																	ParseSource.tempContextName.add(mName);
																	
																}
																contextCount++;		
																
															}
															else if(argtype.equals("Context") || argtype.equals("Application")){
																
																recursiveCall = true;														
																
																String source = unit.getElementName().toString();
																sName = source.substring(0, source.lastIndexOf("."));
																																
																try {																
																	resolveContext(md, methodName, tName, contextPosition, paramSize, sName);								
																	
																} catch (IOException e) {
																	// TODO Auto-generated catch block
																	e.printStackTrace();
																}
																invoNotFound = true;
																
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
			}
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		sourceName = sName;
		
		if(recursiveCall && invoNotFound && ParseSource.returnFlag){
			if(SampleHandler.className.contains(sourceName.concat(".java"))){
								
				ParseSource.sourceNode.add(sourceName);
				ParseSource.targetNode.add(tName);
				ParseSource.contextName.add(returnMethodName);
				ParseSource.methodInvoName.add(returnName);
				ParseSource.putExtra.add(ParseSource.isExtra);
				
				if(returnName.equals("getActivity")||returnName.equals("getActivities")||returnName.equals("getBroadcast")
						||returnName.equals("getService")){
					ParseSource.intentID.add("P"+ParseSource.pendingintent);
					ParseSource.pendingintent++;
				}else{
					ParseSource.intentID.add("I"+ParseSource.intent);								
					ParseSource.intent++;
				}
			}			
		}
		
		
	}
	
	public void setinvoName(String name, String decl){
		returnName = name;
		returnMethodName = decl;		
	}
		
}
