import java.util.*;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
public class Customer {
	public static void main(String[]args) throws Exception {
	
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the customer id:");
		String customerId = br.readLine();
		String customerLocation = customerId.substring(0,3);
		System.out.println("Customer location: "+customerLocation);
		Customer.checkCustomerValid(customerId);
	    EventOperations obj = lookup(customerLocation);

	    System.out.println("Choose one option: 1.Book Event 2.Get booking schedule 3.Cancel Event");
	    int option = new Integer(br.readLine());
	    
	    int eventLocInt, eventTypeInt, eventSlotInt;
	    String eventType="",getBookingScheduleResult, eventId="";
	    
	    switch(option) {
	    	case 1:
				//System.out.println("Enter the event location to book: 1.MTL 2.QUE 3.SHE");
				//eventLocInt = new Integer(br.readLine());

				System.out.println("Enter the event type to book: 1.Conferences 2.Trade Shows 3.Seminars");
				eventTypeInt = new Integer(br.readLine());

				//System.out.println("Enter the event slot to book: 1.Morning 2.Afternoon 3.evening");
				//eventSlotInt = new Integer(br.readLine());

				if(eventTypeInt==1){
					eventType="conferences";
				} else if(eventTypeInt==2) {
					eventType="tradeshows";
				} else if(eventTypeInt==3) {
					eventType="seminars";
				} else {
					System.out.println("Invalid event type entered");
					System.exit(0);
				}

				/*StringBuilder eventIDBD = new StringBuilder("");
				if(eventLocInt==1) {
					eventIDBD.append(Locations.MTL);
				} else if(eventLocInt==2) {
					eventIDBD.append(Locations.QUE);
				} else if(eventLocInt==3) {
					eventIDBD.append(Locations.SHE);
				} else {
					System.out.println("Invalid event location entered");
					System.exit(0);
				}

				if(eventSlotInt==1) {
					eventIDBD.append("M");
				} else if(eventSlotInt==2) {
					eventIDBD.append("A");
				} else if(eventSlotInt==3) {
					eventIDBD.append("E");
				} else {
					System.out.println("Invalid event slot entered");
					System.exit(0);
				}*/

				Map<String, EventDetails> events = obj.listEventAvailability(customerId, eventType);
				System.out.println("Available Events:");
				int j=0;
				for(String eventId1: events.keySet()) {
					j++;
					System.out.print(j+". Event Id: "+eventId1+", ");
					EventDetails eventDetails = events.get(eventId1);
					System.out.println("Max booking capacity: "+eventDetails.getMaxBookingCapacity()+
					", Currently booked: "+eventDetails.getCurrentlyBooked());
				}
				System.out.println("Enter event id:");
				eventId = br.readLine();
				String bookresult = obj.bookEvent(customerId, eventId, eventType);
				System.out.println("Book event result: "+ bookresult);
				break;
			
			case 2:
				List<String> bookingSchedule = obj.getBookingSchedule(customerId);
				System.out.println("Booking schedule: "+bookingSchedule.toString());
				break;
				
			case 3: 	    	      
				List<String> customerBookingSchedule = obj.getBookingSchedule(customerId);
				System.out.println("Booking schedule: "+customerBookingSchedule.toString());

				System.out.println("Choose the event type: 1.Conferences 2.Trade Shows 3.Seminars");
				eventTypeInt = new Integer(br.readLine());

				if(eventTypeInt==1) {
					eventType="conferences";
				} else if(eventTypeInt==2){
					eventType="tradeshows";
				} else if(eventTypeInt==3) {
					eventType="seminars";
				} else {
					System.out.println("Invalid event type entered");
					System.exit(0);
				}

				System.out.println("Enter the eventId to be cancelled");
				eventId = br.readLine();
				String cancelResult = obj.cancelEvent(customerId, eventId, eventType);
				System.out.println("Result of cancelled event"+cancelResult);
				break;
	    }
	}
	
	public static void checkCustomerValid(String customerId) {
		String customerIdLocation = customerId.substring(0,3);
		if ((customerId.length()!=8)||
		    ((!customerIdLocation.equals("QUE")) &&
			 (!customerIdLocation.equals("MTL")) &&
			 (!customerIdLocation.equals("SHE"))) ) {
			
			System.out.println("Invalid customer Id");
			System.exit(0);
		}
	}
	
	private static EventOperations lookup(String customerLocation) throws Exception {
		int port = 8000;
		if (customerLocation.equals(Locations.MTL)) {
			port = 8000;
		} else if(customerLocation.equals(Locations.QUE)) {
			port = 8080;
		} else if(customerLocation.equals(Locations.SHE)) {
			port = 8081;
		}
		Registry registry = LocateRegistry.getRegistry(port);
		EventOperations obj = (EventOperations)registry.lookup(customerLocation);
		return obj;
	}   
}
