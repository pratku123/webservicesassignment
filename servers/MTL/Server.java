import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.xml.ws.Endpoint;
public class Server {
	public static void main(String args[]) throws Exception {   
		try {    
            EventOperationsImpl obj= new EventOperationsImpl();
			String location = args[0];
			int port = Server.getPort(location);
			String endPoint = "http://localhost:"+port+"/test";
			Endpoint.publish(endPoint, obj);
			System.out.println("Server started: "+location);

	    } catch(Exception e) {
		   System.out.println("Server not started, RETRY!!" + e.getMessage());
		}
	}

	public static int getPort(String location) {
		int port = 8000;
		if(location.equals(Locations.MTL)) {
			port = 8000;
		} else if(location.equals(Locations.QUE)) {
			port = 8080;
		} else if(location.equals(Locations.SHE)) {
			port = 8081;
		}
		return port;
	}
}