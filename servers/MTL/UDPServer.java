import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.*;

public class UDPServer {

	public static void main(String[] args) {		
		DatagramSocket aSocket= null;
		String location = args[0];
		int port1 = UDPServer.getReceivePort(location);
		int port2 = UDPServer.getSendPort(location);
	    try{
	    	byte[] buffer= new byte[1000000];
	    	while(true) {
				buffer = new byte[1000000];
				aSocket = new DatagramSocket(port1);
	    		DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	    		aSocket.receive(request);
				ByteArrayInputStream b = new ByteArrayInputStream(request.getData());
				ObjectInputStream i = new ObjectInputStream(b);
				Map<String, String> requestParams =  (Map<String, String>) i.readObject();
				EventOperations obj = UDPServer.lookup(args[0]);
				if(requestParams.containsKey("requestType")) {
					String requestType = requestParams.get("requestType");
					System.out.println("UDP: Request Type: "+requestType);
					System.out.println("UDP: Request Params: "+requestParams.toString());
					buffer = new byte[1000000];
					DatagramSocket socket = new DatagramSocket();
					ByteArrayOutputStream b2 = new ByteArrayOutputStream();
					ObjectOutputStream s2 = new ObjectOutputStream(b2);
					if(requestType.equals(RequestType.LIST_EVENT_AVAILABILITY)) {
						Object events = obj.getServerEventsAvailable(requestParams.get("eventType"));
						s2.writeObject(events);
					} else if(requestType.equals(RequestType.BOOK_EVENT)) {						
						String book_event_response = obj.bookServerEvent(requestParams.get("customerId"), 
																		 requestParams.get("eventId"), 
																		 requestParams.get("eventType"));
						s2.writeObject(book_event_response);
					} else if(requestType.equals(RequestType.GET_BOOKING_SCHEDULE)) {
						String events = obj.getServerBookingSchedule(requestParams.get("customerId"));
						s2.writeObject(events);
					} else if(requestType.equals(RequestType.CANCEL_EVENT)) {
						String cancel_event_response = obj.cancelServerEvent(requestParams.get("customerId"), 
																			 requestParams.get("eventId"), 
																			 requestParams.get("eventType"));
						s2.writeObject(cancel_event_response);
					}

					buffer = b2.toByteArray();
					DatagramPacket response = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), port2);
					socket.send(response);
					socket.close();
				}
				aSocket.close();
	    	}
	    } catch(SocketException e){
	    	System.out.println("Socket error in UDP server "+e.toString());
	    } catch(IOException e){
	    	System.out.println("Socket error in UDP server "+e.toString());
	    } catch(Exception e) {
			System.out.println("Exception: "+ e.toString());
		}
	}   
	
	public static EventOperations lookup(String location) throws RemoteException, Exception {
		int port = 8000;
		if(location.equals(Locations.MTL)) {
			port = 8000;
		} else if(location.equals(Locations.QUE)) {
			port = 8080;
		} else if(location.equals(Locations.SHE)) {
			port = 8081;
		}
		URL addURL = new URL("http://localhost:"+port+"/test?wsdl");
		QName addQname = new QName("http://localhost/test", "EventOperationsImplService");
		Service service = Service.create(addURL, addQname);
		EventOperations obj = service.getPort(EventOperations.class);
		return obj;
	}
	
	public static int getReceivePort(String location) {
		int port = 8002;
		if(location.equals(Locations.MTL)) {
			port = 8002;
		} else if(location.equals(Locations.QUE)) {
			port = 8004;     
		} else if(location.equals(Locations.SHE)) {
			port = 8006;
		}
		return port;
	}
	
	public static int getSendPort(String location) {
		int port = 8007;
		if(location.equals(Locations.MTL)){
			port = 8007;
		} else if(location.equals(Locations.QUE)) {
			port = 8008;
		} else if(location.equals(Locations.SHE)) {
			port = 8009;
		}
		return port;
	}
}
