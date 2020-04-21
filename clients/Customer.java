import java.util.*;
import java.io.*;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.*;

public class Customer extends Thread{
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the customer id:");
			String customerId = br.readLine();
			String customerLocation = customerId.substring(0,3);
			System.out.println("Customer location: "+customerLocation);
			Customer.checkCustomerValid(customerId);
			EventOperations obj = lookup(customerLocation);
			System.out.println("Choose one option: 1.Book Event 2.Get booking schedule 3.Cancel Event 4.Swap Event");
			int option = new Integer(br.readLine());
			int eventLocInt, eventTypeInt, eventSlotInt;
			String eventType="",getBookingScheduleResult, eventId="";

			switch(option) {
	    	case 1:
				System.out.println("Enter the event type to book: 1.Conferences 2.Trade Shows 3.Seminars");
				eventTypeInt = new Integer(br.readLine());

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

				
				String events = obj.listEventAvailability(customerId, eventType);
				System.out.println("Available events:");
				int j=1;
				String[] eventsList = events.split(",");
				for(String row: eventsList) {
					String eventRow[] = row.split(":");
					System.out.print(j+". EventID:  "+eventRow[0]+", ");
					System.out.println("Max booking capacity: "+eventRow[1]+
					", Currently booked: "+eventRow[2]);
					j++;
				}
				System.out.println("Enter event id:");
				eventId = br.readLine();
				String bookresult = obj.bookEvent(customerId, eventId, eventType);
				System.out.println("Book event result: "+ bookresult);
				break;
			
			case 2:
				String bookingSchedule = obj.getBookingSchedule(customerId);
				System.out.println("Booking schedule: "+bookingSchedule);
				break;
				
			case 3: 	    	      
				String customerBookingSchedule = obj.getBookingSchedule(customerId);
				System.out.println("Booking schedule: "+customerBookingSchedule);

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
				System.out.println(cancelResult);
				break;
			case 4:
				System.out.println("Enter remove event type: 1.Conferences 2.Trade Shows 3.Seminars");
				eventTypeInt = new Integer(br.readLine());
				if(eventTypeInt==1){
					eventType="conferences";
				} else if(eventTypeInt==2){
					eventType="tradeshows";
				} else if(eventTypeInt==3){
					eventType="seminars";
				} else{
					System.out.println("event type error");
					System.exit(0);
				}
				System.out.println("Enter the remove eventId:");
				eventId = br.readLine();
				String bookEventId, bookEventType="";
				System.out.println("Enter book event type: 1.Conferences 2.Trade shows 3.Seminars");
				eventTypeInt = new Integer(br.readLine());
				if(eventTypeInt==1){
					bookEventType="conferences";
				} else if(eventTypeInt==2){
					bookEventType="tradeshows";
				} else if(eventTypeInt==3){
					bookEventType="seminars";
				} else {
					System.out.println("event type error");
					System.exit(0);
				}
				System.out.println("Enter book eventId:");
				bookEventId = br.readLine();
				String swapEventResponse = obj.swapEvent(customerId, eventType, eventId, bookEventType, bookEventId);
				System.out.println(swapEventResponse);
				break;
			default:
				break;
	    }
		} catch(Exception e) {
			System.out.println(e);
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
		URL addURL =new URL("http://localhost:"+port+"/test?wsdl");
		QName addQname =new QName("http://localhost/test","EventOperationsImplService");
		Service service = Service.create(addURL, addQname);
		EventOperations obj = service.getPort(EventOperations.class);
		return obj;
	}   
}
