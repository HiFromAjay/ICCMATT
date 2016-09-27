package androidiccmodel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidiccmodel.handlers.SampleHandler;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.util.io.graphml.GraphMLTokens;
import com.tinkerpop.blueprints.pgm.util.io.graphml.GraphMLWriter;

public class GraphGenerator {

	public void graphGen(){
		List<String> temp = new ArrayList<String>();
		List<String> vertices = new ArrayList<String>();
				
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			
			if(!temp.contains(ParseSource.sourceNode.get(i))){
				temp.add(ParseSource.sourceNode.get(i));
			}
			
			if(!temp.contains(ParseSource.targetNode.get(i))){
				temp.add(ParseSource.targetNode.get(i));
			}
		}
		
		for(int i=0; i<temp.size(); i++){
			if(SampleHandler.className.contains(temp.get(i).concat(".java"))){
				int position = SampleHandler.className.indexOf(temp.get(i).concat(".java"));				 
				String vertex = ManifestParser.componentType.get(position)+temp.get(i);
				
				if(SampleHandler.permission.get(position).equals("true")){
					vertex = vertex+"[P]";
				}
				if(SampleHandler.exported.get(position).equals("true")){
					vertex = vertex+"[E]";
				}
				vertices.add(vertex);
				
			}else if(temp.get(i).substring(0,2).equals("BR")){
				vertices.add("BR:"+temp.get(i)+"[E]");
			}
			else{
				vertices.add(temp.get(i));
			}
		}
		
		Graph graph = new TinkerGraph();
				
		Map<String, String> vertexKeyTypes = new HashMap<String, String>();
		vertexKeyTypes.put("name", GraphMLTokens.STRING);				
		
		Map<String, String> edgeKeyTypes = new HashMap<String, String>();		
		edgeKeyTypes.put("context", GraphMLTokens.STRING);
		
		for(int i=0; i<vertices.size(); i++){
			Vertex v = graph.addVertex(vertices.get(i));
			v.setProperty("name", vertices.get(i));			
		}
		
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			String source;
			String target;
			
			if(SampleHandler.className.contains(ParseSource.sourceNode.get(i).concat(".java"))){
				int position = SampleHandler.className.indexOf(ParseSource.sourceNode.get(i).concat(".java"));
				source = ManifestParser.componentType.get(position)+ParseSource.sourceNode.get(i);				
				if(SampleHandler.permission.get(position).equals("true")){
					source = source+"[P]";
				}
				if(SampleHandler.exported.get(position).equals("true")){
					source = source+"[E]";
				}
			}else if(ParseSource.sourceNode.get(i).substring(0, 2).endsWith("BR")){
				source = "BR:"+ParseSource.sourceNode.get(i)+"[E]";
				for(int k=0; k<SampleHandler.dynBroad.size(); k++){
					if(SampleHandler.dynBroad.get(k).equals(ParseSource.sourceNode.get(i))){
						if(SampleHandler.broadPerm.get(k).equals("true")){
							source = source+"[P]";
						}
					}
				}
			}else{
				source = ParseSource.sourceNode.get(i);
			}
			
			if(SampleHandler.className.contains(ParseSource.targetNode.get(i).concat(".java"))){
				int position = SampleHandler.className.indexOf(ParseSource.targetNode.get(i).concat(".java"));
				target = ManifestParser.componentType.get(position)+ParseSource.targetNode.get(i);				
				if(SampleHandler.permission.get(position).equals("true")){
					target = target+"[P]";
				}
				if(SampleHandler.exported.get(position).equals("true")){
					target = target+"[E]";
				}
			}else if(ParseSource.targetNode.get(i).substring(0, 2).endsWith("BR")){
				target = "BR:"+ParseSource.targetNode.get(i)+"[E]";
				for(int k=0; k<SampleHandler.dynBroad.size(); k++){
					if(SampleHandler.dynBroad.get(k).equals(ParseSource.targetNode.get(i))){
						if(SampleHandler.broadPerm.get(k).equals("true")){
							target = target+"[P]";
						}
					}
				}
			}else{
				target = ParseSource.targetNode.get(i);
			}
			
			Edge ed = graph.addEdge(null, graph.getVertex(source), graph.getVertex(target), "knows");
			String myContext = ParseSource.contextName.get(i);
			if(myContext.contains("-")){
				myContext = myContext.substring(myContext.indexOf("-")+1);
			}
			String intentID = ParseSource.intentID.get(i);
			if(ParseSource.putExtra.get(i).equals("true")){
				intentID = intentID+"[D]";
			}
			ed.setProperty("context", intentID+"/"+myContext);
		}
		
		GraphMLWriter writer = new GraphMLWriter(graph);
		writer.setVertexKeyTypes(vertexKeyTypes);
		writer.setEdgeKeyTypes(edgeKeyTypes);		
		
		try {
			OutputStream out = new FileOutputStream(SampleHandler.iccGraph);
			try {
				writer.outputGraph(out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
}
