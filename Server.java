import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
	public static void main(String args[]) throws Exception {   
		try {    
                        EventInterfaceImpl obj= new EventInterfaceImpl();
                        String location = args[0];
			int port = Server.getPort(location);
			Endpoint.publish("http://localhost:9876/hw", obj);
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