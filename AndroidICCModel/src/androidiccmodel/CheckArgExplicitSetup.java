package androidiccmodel;

import java.io.IOException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import androidiccmodel.handlers.SampleHandler;

public class CheckArgExplicitSetup {
	
	static boolean explicitargFlag;
	static boolean setclassname;
	
	public static void checkArgExplicit(final MethodDeclaration mdnode, final String methodName, final int contextIndex, final int parameterSize, 
			final ICompilationUnit unit, final String instanceName){
		
		explicitargFlag = false;
		setclassname = false;
		if(instanceName != null){
			Block block = mdnode.getBody();
			block.accept(new ASTVisitor(){
				public boolean visit(MethodInvocation node){
									
					//check for setClass(Context packageContext, Class<?> cls)
					if(node.getName().toString().equals("setClass") || node.getName().toString().equals("setClassName")){
						
						if((instanceName != null) && instanceName.equals(node.getExpression().toString())){
						
							String arg0type = null;
							String arg1type = null;
							
							if(node.getName().toString().equals("setClassName")){
								setclassname = true;
								//resolve 1st argument which can be Context, StringLiteral, SimpleName
								//resolve 2nd argument which can be MethodInvocation, StringLiteral, SimpleName
								//currently it does not handle MethodInvocation and package name passed in variable
								
								if(node.arguments().get(0).getClass().getSimpleName().equals("StringLiteral")){								
									String arg1 = node.arguments().get(0).toString();
									arg0type = arg1.substring(arg1.lastIndexOf(".")+1, arg1.length()-1);								
								}
								else{								
									arg0type = (String) ((Expression) node.arguments().get(0)).resolveTypeBinding().getName();								
								}
								
								if(node.arguments().get(1).getClass().getSimpleName().equals("StringLiteral")){								
									String arg2 = node.arguments().get(1).toString();
									arg1type = arg2.substring(arg2.lastIndexOf(".")+1, arg2.length()-1);								
								}							
							}
							
							if(node.getName().toString().equals("setClass")){
								arg0type = (String) ((Expression) node.arguments().get(0)).resolveTypeBinding().getName();
								String arg2 = node.arguments().get(1).toString();
								int dotcount = ParseSource.getDotCount(arg2);
								String targetName = null;
								//in case of package name
								if(dotcount>1){
									int index = ParseSource.getIndex(arg2, dotcount);
									targetName = arg2.substring(index, arg2.lastIndexOf("."));
								}
								//in case of class name
								else{
									targetName = arg2.substring(0, arg2.lastIndexOf("."));
								}
								arg1type = targetName;
							}
								
													
							if(SampleHandler.className.contains(arg0type.concat(".java")) && (arg1type != null)){
								explicitargFlag = true;
								String sourceName = (String) ((Expression) node.arguments().get(0)).resolveTypeBinding().getName();										
																						
								String targetName = arg1type;								
								
								ParseSource.tempSourceNode.add(sourceName);
								ParseSource.tempTargetNode.add(targetName);
								ParseSource.tempContextName.add(methodName);
								
							}
							else if((("Context").equals(arg0type) || ("Activity").equals(arg0type)) && (arg1type != null)){
								explicitargFlag = true;
								ParseSource.contextFlag = true;
								
								String targetName = arg1type;
								
								String tempname = unit.getElementName().toString();
								String filename = tempname.substring(0, tempname.lastIndexOf("."));
								
								ResolveContext rc = new ResolveContext();
								try {							
									rc.resolveContext(mdnode, methodName, targetName, contextIndex, parameterSize, filename);
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}					
														
								//if matching method invocation with reference to the context is not found including recursive case
								if(ResolveContext.invoNotFound){
									ParseSource.contextFlag = false;
									
									String fileName = unit.getElementName().toString();
									String sourceName;
									if(ResolveContext.sourceName != null){
										sourceName = ResolveContext.sourceName;
										
									}else{
										sourceName = fileName.substring(0, fileName.lastIndexOf("."));
									}
													
									
									if(!sourceName.equals(targetName)){	
										ParseSource.tempSourceNode.add(sourceName);
										ParseSource.tempTargetNode.add(targetName);
										ParseSource.tempContextName.add(methodName);								
										
									}else if(sourceName.equals(targetName)){
										ParseSource.tempSourceNode.add(null);
										ParseSource.tempTargetNode.add(null);
										ParseSource.tempContextName.add(null);										
										//System.out.println("Not resolved.");
									}
									
								}
								
								else if(!ResolveContext.invoNotFound && ResolveContext.sourceName != null){
									ParseSource.tempSourceNode.add(ResolveContext.sourceName);
									ParseSource.tempTargetNode.add(targetName);
									ParseSource.tempContextName.add(methodName);
									ResolveContext.contextCount++;
																
								}
								
							}
							//first argument can be package name (context) instead of component or context class
							else if((!SampleHandler.className.contains(arg0type.concat(".java"))) && (arg1type != null)
									&& node.arguments().get(0).getClass().getSimpleName().equals("StringLiteral")){
								
								explicitargFlag = true;
								String fileName = unit.getElementName().toString();
								String sourceName = fileName.substring(0, fileName.lastIndexOf("."));										
																						
								String targetName = arg1type;								
								
								ParseSource.tempSourceNode.add(sourceName);
								ParseSource.tempTargetNode.add(targetName);
								ParseSource.tempContextName.add(methodName);
								
							}
						}
						
					}else if(node.getName().toString().equals("setComponent") || node.getName().toString().equals("setPackage")){
						if((instanceName != null) && instanceName.equals(node.getExpression().toString())){							
							ParseSource.key.remove(ParseSource.key.size()-1);
							ParseSource.cic.remove(ParseSource.cic.size()-1);
							ParseSource.extra.remove(ParseSource.extra.size()-1);						
							//System.out.println("setComponent/setPackage Not resolved.");
						}												
					}
					
					return true;
				}
			});
		}
	}

}
