package androidiccmodel.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
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
import androidiccmodel.GraphGenerator;
import androidiccmodel.ManifestParser;
import androidiccmodel.ParseSource;
import androidiccmodel.TestCaseGenerator;
import androidiccmodel.SecurityReport;

public class SampleHandler extends AbstractHandler {
	
	public static ArrayList<String> className = new ArrayList<String>();
	public static ArrayList<String> permission = new ArrayList<String>();
	public static ArrayList<String> exported = new ArrayList<String>();
	public static ArrayList<String> dynBroad = new ArrayList<String>();
	public static ArrayList<String> broadPerm = new ArrayList<String>();
	public static int pseudo = 1;
	public static String testCase = "D:\\ICCMATT_TestCases.txt";
	public static String securityReport = "D:\\ICCMATT_SecurityReport.txt";
	public static String iccGraph = "D:\\ICCMATT_ICCGraph.graphml";
	
	public static String projectName = "OpenSudoku";

	public SampleHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
				
		IProject project = getProject();
		
		//Add the file names to be parsed from manifest file into className arraylist.
		IFile manifestFile = project.getFile("AndroidManifest.xml");
		if(manifestFile.exists()){			
			ManifestParser mp = new ManifestParser();
			mp.manifestParse(manifestFile);
		}
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			ParseSource.contextNodeName.add(null);
		}
								
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IPackageFragment[] packages = javaProject.getPackageFragments();
			
			for(IPackageFragment mypackage : packages) {
				if(mypackage.getKind() == IPackageFragmentRoot.K_SOURCE){
					createAST(mypackage);
				}
			}
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		arrange();
		displayGraph();
		generateTest();
		securityReport();
		System.out.println("ICCMATT execution completed.");
		System.out.println("Please check an ICC graph at "+iccGraph+", test cases at "+testCase+", and a security report at "+securityReport);
		
		return null;
	}
	
	public void createAST(IPackageFragment mypackage) throws JavaModelException {
		for(ICompilationUnit unit : mypackage.getCompilationUnits()){
			
			for(int i=0; i<className.size(); i++){
				//parse only android application component files
				if(className.get(i).equals(unit.getElementName().toString())){					
					ParseSource ps = new ParseSource();
					ps.parse(unit);					
				}
			}			
						
		}
		
	}	
	
	private void arrange() {
					
		//set the destination of the activity called by startActivityForResult 
		for(int i=0; i<ParseSource.methodInvoName.size(); i++){
			if(ParseSource.methodInvoName.get(i).equals("setResult")){
				String source = ParseSource.sourceNode.get(i);
								
				for(int m=0; m<ParseSource.targetNode.size(); m++){
					if(ParseSource.targetNode.get(m).equals(source) && ParseSource.methodInvoName.get(m).equals("startActivityForResult")){
						String target = ParseSource.sourceNode.get(m);
						ParseSource.targetNode.set(i, target);						
					}
				}
			}
		}
		
		//set unique pseudo-node name for source and target		
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			if(ParseSource.sourceNode.get(i).equals("none")){
				ParseSource.sourceNode.set(i, "S"+pseudo);
				pseudo++;
			}
			
			if(ParseSource.targetNode.get(i).equals("none")){
				ParseSource.targetNode.set(i, "S"+pseudo);
				pseudo++;
			}
		}		
	}
	
	public static IProject getProject(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();		
		IProject project = root.getProject(projectName);		
		return project;
	}
	
	private void displayGraph(){
		GraphGenerator gg = new GraphGenerator();
		gg.graphGen();
	}
	
	private void generateTest(){		
		TestCaseGenerator tcg = new TestCaseGenerator();		
		tcg.generateTestCase();				
	}
	
	public void securityReport(){
		SecurityReport sr = new SecurityReport();
		sr.securityAnalysis();
	}
}
