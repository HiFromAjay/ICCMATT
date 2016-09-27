package androidiccmodel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import androidiccmodel.handlers.SampleHandler;

public class SecurityReport {
	
	static ArrayList<String> tempReceivingComp = new ArrayList<String>();	
	static ArrayList<String> receivingComp = new ArrayList<String>();
	static ArrayList<String> tempReceivingIntent = new ArrayList<String>();
	static ArrayList<String> receivingIntent = new ArrayList<String>();
	static ArrayList<String> sendingComp = new ArrayList<String>();
	static ArrayList<String> sendingIntent = new ArrayList<String>();
	static ArrayList<String> pseudoNameList = new ArrayList<String>();
		
	public void securityAnalysis(){
		//populate pseudo name
		for(int i=1; i<=SampleHandler.pseudo; i++){
			pseudoNameList.add("S"+i);
		}
		
		//components which send intent to third party app with extra data
		for(int i=0; i<ParseSource.targetNode.size(); i++){
			if(pseudoNameList.contains(ParseSource.targetNode.get(i)) && ParseSource.putExtra.get(i).equals("true")){
				sendingIntent.add(ParseSource.intentID.get(i));
				String name = ParseSource.sourceNode.get(i);
				if(!sendingComp.contains(name)){
					sendingComp.add(name);
				}				
			}
		}
		
		//components which receive intents from third party app
		for(int i=0; i<ParseSource.sourceNode.size(); i++){
			if(pseudoNameList.contains(ParseSource.sourceNode.get(i))){
				tempReceivingIntent.add(ParseSource.intentID.get(i));
				String name = ParseSource.targetNode.get(i);
				if(!tempReceivingComp.contains(name)){
					tempReceivingComp.add(name);
				}				
			}
		}
				
		//components which receive intents from third party app and are not protected by use permission
		for(int j=0; j<SampleHandler.className.size(); j++){
			if(SampleHandler.exported.get(j).equals("true") && SampleHandler.permission.get(j).equals("false")){
				String name = SampleHandler.className.get(j).substring(0, SampleHandler.className.get(j).lastIndexOf("."));
				receivingComp.add(name);
			}			
		}
		for(int i=0; i<SampleHandler.dynBroad.size(); i++){
			if(SampleHandler.broadPerm.get(i).equals("false")){
				receivingComp.add(SampleHandler.dynBroad.get(i));
			}
		}
		
		
		for(int i=0; i<tempReceivingIntent.size(); i++){
			for(int j=0; j<ParseSource.intentID.size(); j++){
				if(tempReceivingIntent.get(i).equals(ParseSource.intentID.get(j))){
					if(receivingComp.contains(ParseSource.targetNode.get(j))){
						receivingIntent.add(tempReceivingIntent.get(i));
					}
				}
			}
		}
		
		// Generate file report of security risk components including intent ids
		try{
			FileWriter fw = new FileWriter(SampleHandler.securityReport);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("Application Name: "+SampleHandler.projectName);
			bw.newLine();
			
			bw.append("Components which send intent to third party app with extra data:");
			bw.newLine();
			for(int i=0; i<sendingComp.size(); i++){
				bw.append(sendingComp.get(i));
				bw.newLine();
			}
			bw.append("Intents which carry extra data to third party app are: ");
			for(int i=0; i<sendingIntent.size(); i++){
				bw.append(sendingIntent.get(i)+" ");
			}
			bw.newLine();
			bw.newLine();
			
			bw.append("components which receive intents from third party app and are not protected by permissions:");
			bw.newLine();
			for(int i=0; i<receivingComp.size(); i++){
				bw.append(receivingComp.get(i));
				bw.newLine();
			}
			bw.append("Intent filters used by unprotected components: ");
			for(int i=0; i<receivingIntent.size(); i++){
				bw.append(receivingIntent.get(i)+" ");
			}
			bw.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		
	}	

}
