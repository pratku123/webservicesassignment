import java.rmi.*;
import java.util.Scanner;
import java.util.Map;
import java.rmi.registry.*;
import java.util.*;
import java.io.*;

public class Manager {

	public static void main(String args[]) throws Exception{ 
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int i;
		System.out.println("Enter Your Manager Id:");
		String eventManagerID = br.readLine();
		String managerLocation = eventManagerID.substring(0,3);
		System.out.println("Manager location = "+managerLocation);
		System.out.println("Choose one option: 1. Add Event 2. Remove event 3. List event availability");
		i = new Integer(br.readLine());
		EventOperations obj = lookup(managerLocation);
		String eventId, eventType="";
		int eventType1, eventSlot, bookingCapacity;
		
		switch(i)
		{
			case 1:
				System.out.println("Enter the event type: 1.Conferences 2.Trade Shows 3.Seminars");
				eventType1 = new Integer(br.readLine());
				System.out.println("Choose the slot: 1.Morning 2.Afternoon 3.Evening");
				eventSlot = new Integer(br.readLine());
				System.out.println("Enter the booking capacity:");
				bookingCapacity = new Integer(br.readLine());
				if(eventType1 == 1){
					eventType="conferences";
				} else if(eventType1 == 2){ 
					eventType="tradeshows";
				} else if(eventType1 == 3){
					eventType="seminars";
				} else {
					System.out.println("Invalid event selected\n");
					System.exit(0);
				}

				System.out.println("Enter the date in format DDMMYY:");
				String eventDate = br.readLine();
				eventId = managerLocation;

				if(eventSlot==1){
					eventId+="M";
				} else if(eventSlot==2){
					eventId+="A";
				} else if(eventSlot==3){
					eventId+="E";
				} else {
					System.out.println("Invalid eventSlot selected\n");
					System.exit(0); 
				}
				 
				eventId+=eventDate;
				String addEventResult = obj.addEvent(eventManagerID, eventId, eventType, bookingCapacity);
				System.out.println(addEventResult);
				break;
			case 2:
				System.out.println("Enter the event type to be deleted: 1.Conferences 2.Trade Shows 3. Seminars");
				eventType1=new Integer(br.readLine());

				if(eventType1==1)
				 eventType="conferences";
				else if(eventType1==2)
				 eventType="tradeshows";
				else if(eventType1==3)
				 eventType="seminars";
				else {
				 System.out.println("Invalid event selected\n");
				 System.exit(0);
				}
				System.out.println("Eneter the event Id to be deleted");
				eventId = br.readLine();
				String response = obj.removeEvent(eventManagerID, eventId, eventType);
				System.out.println(response);
			    break;
			case 3:
				System.out.println("Enter the event type: 1.Conferences 2.Trade Shows 3.Seminars");
				eventType1 = new Integer(br.readLine());
				if(eventType1==1){
					eventType="conferences";
				} else if(eventType1==2){
					eventType="tradeshows";
				} else if(eventType1==3){
					eventType="seminars";
				} else {
					System.out.println("Invalid event type entered");
					System.exit(0);
				}

				Map<String, EventDetails> events = obj.listEventAvailability(eventManagerID, eventType);
				System.out.println("The events availability is as follows:");
				int j=0;
				for(String eventID: events.keySet()) {
					j++;
					System.out.print(j+". EventID:  "+eventID+", ");
					EventDetails eventDetails = events.get(eventID);
					System.out.println("Max booking capacity: "+eventDetails.getMaxBookingCapacity()+
					", Currently booked: "+eventDetails.getCurrentlyBooked());
				}
	    }
	}
	
	
	public static EventOperations lookup(String managerLocation) throws Exception {   
		int port=8000;
		if(managerLocation.equals(Locations.MTL)) {
			port=8000;
		} else if(managerLocation.equals(Locations.QUE)) {
			port=8080;
		} else if(managerLocation.equals(Locations.SHE)) {
			port=8081;
		}
		Registry registry = LocateRegistry.getRegistry(port);
		EventOperations obj = (EventOperations)registry.lookup(managerLocation);
		return obj;
	}
}
