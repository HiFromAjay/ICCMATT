package androidiccmodel;

import java.io.IOException;
import java.util.ArrayList;

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
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import androidiccmodel.handlers.SampleHandler;

public class ReturnMethodDeclaration {
	
	static ArrayList<String> mDecl = new ArrayList<String>();
	static ArrayList<MethodDeclaration> mDeclNode = new ArrayList<MethodDeclaration>();
	static ArrayList<String> returnName = new ArrayList<String>();	
	static ArrayList<Integer> contextPos = new ArrayList<Integer>();
	static ArrayList<Integer> pSize = new ArrayList<Integer>();
	static ArrayList<String> sourceName = new ArrayList<String>();	
	static ArrayList<String> filename = new ArrayList<String>();
	static ArrayList<String> noresolveDecl = new ArrayList<String>();
	static ArrayList<String> noresolvereturnName = new ArrayList<String>();
	
	static boolean dontResolveContext = false;
	boolean assignment;
	static int invocontainingContext;
	
	public void returnDeclaration(MethodDeclaration node, final String target){
		
		clearArrayList();
		
		final String methodName = node.getName().toString();
		final int contextPosition = ParseSource.contextPosition(node);
		final int paramSize = node.parameters().size();
				
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
									public boolean visit(MethodDeclaration md){
											
										final String name = md.getName().toString();										
										int pos = ParseSource.contextPosition(md);
										int size = md.parameters().size();
										assignment = false;
										invocontainingContext = 0;
										
										final ArrayList<String> invoName = new ArrayList<String>();
										final ArrayList<MethodInvocation> invoNode = new ArrayList<MethodInvocation>();
										
										
											if(md.getBody()!= null){
												md.getBody().accept(new ASTVisitor(){
													
													public boolean visit(MethodInvocation node){
														String tempName = node.getName().toString();
														if(tempName.equals(methodName) && paramSize == node.arguments().size()){
															
															String source = (String) ((Expression) node.arguments().get(contextPosition-1)).resolveTypeBinding().getName();
															if(SampleHandler.className.contains(source.concat(".java"))){
																
																if(node.getParent().getClass().getSimpleName().equals("Assignment")){
																	assignment = true;
																	String keyString = ((Name) ((Assignment) node.getParent()).getLeftHandSide()).resolveBinding().getKey();
																	ParseSource.key.add(keyString);
																	ParseSource.cic.add(null);
																	ParseSource.extra.add(ParseSource.isExtra);
																	
																	ParseSource.tempSourceNode.add(source);
																	ParseSource.tempTargetNode.add(target);
																	ParseSource.tempContextName.add(name);
																}else{
																	sourceName.add(source);																	
																}
																
																dontResolveContext = true;																
															}
															else{
																dontResolveContext = false;
																invocontainingContext++;
																String tempname = unit.getElementName().toString();
																filename.add(tempname.substring(0, tempname.lastIndexOf(".")));																
															}
														}
														invoName.add(tempName);
														invoNode.add(node);
														return true;
													}
												});
											}
											
											if(invoName.contains(methodName)){
												int tempSize = 0;
												for(int i=0; i<invoName.size(); i++){
													if(invoName.get(i).equals("setResult")||invoName.get(i).equals("startActivity")
															||invoName.get(i).equals("startActivityForResult")||invoName.get(i).equals("startService")
															||invoName.get(i).equals("stopService")||invoName.get(i).equals("bindService")||invoName.get(i).equals("sendBroadcast")
															||invoName.get(i).equals("getActivity")||invoName.get(i).equals("getActivities")
															||invoName.get(i).equals("getBroadcast")||invoName.get(i).equals("getService")
															|| invoName.equals("setIntent")){
																												
														if((!dontResolveContext) && (tempSize<invocontainingContext)){
															mDecl.add(name);
															mDeclNode.add(md);
															contextPos.add(pos);
															pSize.add(size);
															returnName.add(invoName.get(i));
															tempSize++;															
														}
														else if(assignment){
															FindMethodInvocation.storeKeyorObject(invoNode.get(i), invoName.get(i));
														}
														else {
															noresolveDecl.add(name);
															noresolvereturnName.add(invoName.get(i));															
														}
														
														
													}
												}
												
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
		
	}
	
	public void clearArrayList(){
		mDecl.clear();
		mDeclNode.clear();
		returnName.clear();
		contextPos.clear();
		pSize.clear();
		filename.clear();
		sourceName.clear();
		noresolveDecl.clear();
		noresolvereturnName.clear();		
	}
	
	public void resolveContext(String targetName){
		ResolveContext rc = new ResolveContext();
		
		if(filename.size()>0){
			for(int i=0; i<mDecl.size(); i++){
				
					try {
						rc.setinvoName(returnName.get(i), mDecl.get(i));						
						rc.resolveContext(mDeclNode.get(i), mDecl.get(i), targetName, contextPos.get(i), pSize.get(i), filename.get(i));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
			}	
		}
		
		for(int i=0; i<sourceName.size(); i++){
			resolveThisDirectly(targetName, i);
		}
	}
	
	public void resolveThisDirectly(String targetName, int i){
		ParseSource.sourceNode.add(sourceName.get(i));
		ParseSource.targetNode.add(targetName);
		ParseSource.contextName.add(noresolveDecl.get(i));
		FindMethodInvocation.addInvo(noresolvereturnName.get(i));
		ParseSource.putExtra.add(ParseSource.isExtra);
	}

}
