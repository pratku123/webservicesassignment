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

public class UDPServerClient {

	public static void main(String[] args) {
		String location = args[0];
	    try{
	    	while(true) {
				UDPServer udpServer = new UDPServer(location);
				udpServer.start();
				udpServer.join();
	    	}
	    }catch(Exception e) {
			System.out.println("Exception: "+ e.toString());
		}
	}
}
