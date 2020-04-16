import java.util.Date;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Map;

public class Logs {
	public Logs() {
	}
	public static void logData(String requestType, 
									  Map<String, String> requestParams, 
									  Date requestDate, 
									  int requestStatus, 
									  String response) {
		try{
			SimpleDateFormat f = new SimpleDateFormat("YYYY-MM-dd");
			String date = f.format(requestDate);
			String fileName = "serverLogs/requests/"+date+".txt";
			
			String logData = "------------------------------------\n";
			logData+="Request Type: "+requestType+"\n";
			logData+="Request TimeStamp: "+requestDate.toString()+"\n";
			logData+="Request Status: "+requestStatus+"\n";
			logData+="Request Params: "+requestParams.toString()+"\n";
			logData+="Response: "+response+"\n";
			
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
			bufferedWriter.write(logData);
			bufferedWriter.close();
			
			if(requestParams.containsKey("managerId")) {
				String managerId = requestParams.get("managerId");
				fileName = "serverLogs/managers/"+date+".txt";
				BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(fileName, true));
				bufferedWriter2.write(logData);
				bufferedWriter2.close();
			}
			
			if(requestParams.containsKey("customerId")){ 
				String customerId = requestParams.get("customerId");
				fileName = "serverLogs/customers/"+date+".txt";
				BufferedWriter bufferedWriter3 = new BufferedWriter(new FileWriter(fileName, true));
				bufferedWriter3.write(logData);
				bufferedWriter3.close();
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}