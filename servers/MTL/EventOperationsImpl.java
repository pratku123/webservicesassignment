import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.lang.*;
import java.util.ArrayList;
import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import javax.jws.WebService;

@WebService(endpointInterface="EventOperations", targetNamespace="http://localhost/test")
public class EventOperationsImpl implements EventOperations {
	public Map<String, Map<String,EventDetails> > eventDB;
	public Map<String, Map<String,EventDetails> > customerDB;
	private final int MAX_BOOKING_CAPACITY = 100;
	private final int INITIAL_BOOKING = 0;
	
	public EventOperationsImpl(){
		super();
		eventDB = new HashMap<String, Map<String, EventDetails>>();
		customerDB = new HashMap<String, Map<String, EventDetails> >();
	}
	private Map<String, Map<String,EventDetails> > getEventDB() {
		return this.eventDB;
	}
	
	private Map<String, Map<String,EventDetails> > getCustDB() {
		return this.customerDB;
	}
	
	public String getLocation(String id) {
		return id.substring(0,3);
	}
	
	public String addEvent(String managerId, String eventId, String eventType, int bookingCapacity){
		Map<String, EventDetails> eventDetailsMap = new HashMap<String, EventDetails>();
		Map<String, String> requestParams = new HashMap<String, String>();
		String response;
		Date requestDate = new Date();
		requestParams.put("eventId", eventId);
		requestParams.put("eventType", eventType);
		requestParams.put("bookingCapacity", Integer.toString(bookingCapacity));
		requestParams.put("managerId", managerId);
		if(eventDB.containsKey(eventType)) {
			eventDetailsMap = eventDB.get(eventType);
		} else {
			eventDB.put(eventType, eventDetailsMap);
		}

		if(!eventDetailsMap.containsKey(eventId)) {
			EventDetails eventDetails = new EventDetails(bookingCapacity, INITIAL_BOOKING);
			eventDetailsMap.put(eventId, eventDetails);
			eventDB.put(eventType, eventDetailsMap);
			response = "EVENT_ADDED";
		} else {
			EventDetails eventDetails = eventDetailsMap.get(eventId);
			eventDetails.setMaxBookingCapacity(eventDetails.getMaxBookingCapacity()+bookingCapacity);
			eventDetailsMap.put(eventId, eventDetails);
		    eventDB.put(eventType, eventDetailsMap);
			response = "EVENT_ALREADY_EXSITS";
		}
		Logs.logData(RequestType.ADD_EVENT, requestParams, requestDate, 1, response);
		return response;
	}
	
	public String removeEvent(String managerId, String eventId, String eventType){
		Map<String, String> requestParams = new HashMap<String, String>();
		String response;
		Date requestDate = new Date();
		requestParams.put("eventId", eventId);
		requestParams.put("eventType", eventType);
		requestParams.put("managerId", managerId);
		if(eventDB.containsKey(eventType)) {
			Map<String, EventDetails> eventDetailsMap = eventDB.get(eventType);
			if(eventDetailsMap.containsKey(eventId)) {
				EventDetails eventDetails = eventDetailsMap.get(eventId);
				List<String> customerIds = eventDetails.getBookedCustomerIds();
				for(String customerId: customerIds) {
					if(customerDB.containsKey(customerId)){
						Map<String, EventDetails> eventMap = customerDB.get(customerId);
						if(eventMap.containsKey(eventId+":"+eventType)) {
							eventMap.remove(eventId+":"+eventType);
							if(eventMap.size()==0) {
								customerDB.remove(customerId);
							}
						}
						this.addNextAvailable(customerId, eventType, eventId);
					}
				}
				eventDetailsMap.remove(eventId);
				if (eventDetailsMap.size()==0){
					eventDB.remove(eventType);
				}
				response = "EVENT_REMOVED";
			} else {
				response = "EVENT_NOT_FOUND";
			}
		} else {
			response = "EVENT_TYPE_NOT_FOUND";
		}
		Logs.logData(RequestType.REMOVE_EVENT, requestParams, requestDate, 1, response);
		return response;
	}
	
	public void addNextAvailable(String customerId, String eventType, String eventId){
		if (eventDB.containsKey(eventType)) {
			Map<String, EventDetails> eventMap = eventDB.get(eventType);
			ArrayList<String> eventIds = new ArrayList<String>();
			for(String eventIdKey: eventMap.keySet()) {
				eventIds.add(eventIdKey);
			}
			Collections.sort(eventIds);
			for(String eventIdKey1 : eventIds) {
				EventDetails eventDetails = eventMap.get(eventIdKey1);
				if(eventIdKey1.compareTo(eventId)>0 && eventDetails.getSeatsAvailable()>0){
					this.bookEvent(customerId, eventIdKey1, eventType);
					break;
				}
			}

		}
	}

	public String listEventAvailability(String managerId, String eventType){
		Map<String, String> requestParams = new HashMap<String, String>();
		String response;
		Date requestDate = new Date();
		requestParams.put("eventType", eventType);
		requestParams.put("managerId", managerId);
		requestParams.put("requestType", RequestType.LIST_EVENT_AVAILABILITY);
		String location = this.getLocation(managerId);
		List<String> events = new ArrayList<String>();
		String[] eventsList;
		if(!location.equals(Locations.MTL)) {
			String events1 = (String)this.sendRequest(requestParams, Locations.MTL);
			eventsList = events1.split(",");
			for(String event: eventsList) {
				if(event.length()>0){
					events.add(event);
				}
			}
		}
		if (!location.equals(Locations.QUE)) {
			String events2 = (String)this.sendRequest(requestParams, Locations.QUE);
			eventsList = events2.split(",");
			for(String event: eventsList) {
				if(event.length()>0){
					events.add(event);
				}
			}
		}
		if(!location.equals(Locations.SHE)) {
			String events3 = (String)this.sendRequest(requestParams, Locations.SHE);
			eventsList = events3.split(",");
			for(String event: eventsList) {
				if(event.length()>0){
					events.add(event);
				}
			}
		}
		String events4 = this.getServerEventsAvailable(eventType);
		eventsList = events4.split(",");
		for(String event: eventsList) {
			if(event.length()>0){
				events.add(event);
			}
		}
		Logs.logData(RequestType.LIST_EVENT_AVAILABILITY, requestParams, requestDate, 1, events.toString());
		return events.toString();
	}
	
	public String getServerEventsAvailable(String eventType){
		Map<String, EventDetails> eventDetailsMap = new HashMap<String, EventDetails>();
		Map<String, EventDetails> availableEventDetailsMap = new HashMap<String, EventDetails>();
		if(eventDB.containsKey(eventType)) {
			eventDetailsMap = eventDB.get(eventType);
		}
		for(String eventId: eventDetailsMap.keySet()) {
			EventDetails eventDetails = eventDetailsMap.get(eventId);
			if(eventDetails.getSeatsAvailable()>0) {
				availableEventDetailsMap.put(eventId, eventDetails);
			}
		}
		String events = "";
		for(String eventID: availableEventDetailsMap.keySet()) {
			EventDetails eventDetails = eventDetailsMap.get(eventID);
			if(events.length()>0)
			events = events+","+(eventID+":"+eventDetails.getMaxBookingCapacity()+":"+eventDetails.getCurrentlyBooked());
			else
			events = (eventID+":"+eventDetails.getMaxBookingCapacity()+":"+eventDetails.getCurrentlyBooked());
		}
		return events;
	}
	
	
	public String bookEvent(String customerId, String eventId, String eventType){
		Map<String, String> requestParams = new HashMap<String, String>();
		String response;
		Date requestDate = new Date();
		requestParams.put("eventType", eventType);
		requestParams.put("eventId", eventId);
		requestParams.put("customerId", customerId);
		requestParams.put("requestType", RequestType.BOOK_EVENT);
		String customerLocation = this.getLocation(customerId);
		String eventLocation = this.getLocation(eventId);
		if (eventLocation.compareTo(customerLocation)!=0) {
			response = (String) this.sendRequest(requestParams, eventLocation);
		} else {
			response = this.bookServerEvent(customerId, eventId, eventType);
		}
		Logs.logData(RequestType.BOOK_EVENT, requestParams, requestDate, 1, response);
		return response;
	}
	
	public String bookServerEvent(String customerId, String eventId, String eventType) {
		System.out.println("Server book Event:"+eventId);
		Map<String, String> logParams = new HashMap<String, String>();
		logParams.put("eventType", eventType);
		logParams.put("eventId", eventId);
		logParams.put("customerId", customerId);
		Date requestDate = new Date();
		String response="";
		Map<String, EventDetails> eventDetailsMap = new HashMap<String, EventDetails>();
		if(eventDB.containsKey(eventType)) {
			eventDetailsMap = eventDB.get(eventType);
			if(eventDetailsMap.containsKey(eventId)) {
				EventDetails eventDetails = eventDetailsMap.get(eventId);
				if(eventDetails.getSeatsAvailable()>0){
					Map<String, EventDetails> customerEventsMap = new HashMap<String, EventDetails>();
					if(customerDB.containsKey(customerId)) {
						customerEventsMap = customerDB.get(customerId);
					}
					if(!customerEventsMap.containsKey(eventId+":"+eventType)) {
						eventDetails.bookSeat(customerId);						
					} else {
						response = "ALREADY_BOOKED_EVENT_ID";
					}
					customerEventsMap.put((eventId+":"+eventType), eventDetails);
					customerDB.put(customerId, customerEventsMap);
					response = "SEAT_BOOKED";
				} else {
					response = "SEATS_NOT_AVAILABLE";
				}
			} else {
				response = "INVALID_EVENT";
			}
		} else {
			response = "EVENT_TYPE_NOT_AVAILABLE";
		}
		Logs.logData(RequestType.BOOK_SERVER_EVENT, logParams, requestDate, 1, response);
		return response;
	}
	
	public String getBookingSchedule(String customerId) {
		Map<String, String> requestParams = new HashMap<String, String>();
		String response;
		Date requestDate = new Date();
		requestParams.put("customerId", customerId);
		requestParams.put("requestType", RequestType.GET_BOOKING_SCHEDULE);
		String location = this.getLocation(customerId);
		List<String> events = new ArrayList<String>();
		String[] eventsList;
		if(!location.equals(Locations.MTL)) {
			String events1 = (String)this.sendRequest(requestParams, Locations.MTL);
			eventsList = events1.split(",");
			for(String event: eventsList) {
				if(event.length()>0){
					events.add(event);
				}
			}
		}
		
		if(!location.equals(Locations.QUE)) {
			String events2 = (String)this.sendRequest(requestParams, Locations.QUE);
			eventsList = events2.split(",");
			for(String event: eventsList) {
				if(event.length()>0) {
					events.add(event);
				}
			}
		}
		
		if(!location.equals(Locations.SHE)) {
			String events3 = (String)this.sendRequest(requestParams, Locations.SHE);
			eventsList = events3.split(",");
			for(String event: eventsList) {
				if(event.length()>0){
					events.add(event);
				}
			}
		}
		String events4 = (String)this.getServerBookingSchedule(customerId);
		eventsList = events4.split(",");
		for(String event: eventsList){
			if(event.length()>0){
				events.add(event);
			}
		}
		Logs.logData(RequestType.GET_BOOKING_SCHEDULE, requestParams, requestDate, 1, events.toString());
		return events.toString();
	}
	
	public String getServerBookingSchedule(String customerId) {
		String events = "";
		String customerLocation = this.getLocation(customerId);
		if(customerDB.containsKey(customerId)) {
			Map<String, EventDetails> eventMap = customerDB.get(customerId);
			for(String eventId: eventMap.keySet()) {
				if(events.length()>0) {
					events = events+","+eventId;
				} else {
					events = eventId;
				}
			}
		}
		return events;
	}
	
	public String cancelEvent(String customerId, String eventId, String eventType) {
		Map<String, String> requestParams = new HashMap<String, String>();
		String response;
		Date requestDate = new Date();
		requestParams.put("eventType", eventType);
		requestParams.put("eventId", eventId);
		requestParams.put("customerId", customerId);
		requestParams.put("requestType", RequestType.CANCEL_EVENT);
		String customerLocation = this.getLocation(customerId);
		String eventLocation = this.getLocation(eventId);
		if (eventLocation.compareTo(customerLocation)!=0) {
			response = (String)this.sendRequest(requestParams, eventLocation);
		} else {
			response = this.cancelServerEvent(customerId, eventId, eventType);
		}
		Logs.logData(RequestType.CANCEL_EVENT, requestParams, requestDate, 1, response);
		return response;
	}

	public String cancelServerEvent(String customerId, String eventId, String eventType) {
		Map<String, String> logParams = new HashMap<String, String>();
		logParams.put("eventType", eventType);
		logParams.put("eventId", eventId);
		logParams.put("customerId", customerId);
		Date requestDate = new Date();
		String response="";
		if (eventDB.containsKey(eventType)) {
			Map<String, EventDetails> eventMap = eventDB.get(eventType);
			if (eventMap.containsKey(eventId)) {
				EventDetails eventDetails = eventMap.get(eventId);
				List<String> bookedCustomerIds = eventDetails.getBookedCustomerIds();
				List<String> newCustomerIds = new ArrayList<String>();
				int i=0;
				for(String listCustomerId: bookedCustomerIds) {
					if (customerId.compareTo(listCustomerId) != 0) {
						newCustomerIds.add(listCustomerId);
					}
				}
				eventDetails.setBookedCustomerIds(newCustomerIds);
				eventDetails.setCurrentlyBooked(newCustomerIds.size());
				eventMap.put(eventId, eventDetails);
				if(eventDetails.getSeatsAvailable()<=0) {
					eventMap.remove(eventId);
				}
				if(eventMap.size()==0) {
					eventDB.remove(eventType);
				}
				if(customerDB.containsKey(customerId)) {
					Map<String, EventDetails> customerEventMap = customerDB.get(customerId);
					if(customerEventMap.containsKey((eventId+":"+eventType))) {
						customerEventMap.remove((eventId+":"+eventType));
					}
				}
				response = "EVENT_CANCELLED";
			} else {
				response = "INVALID_EVENT_ID";
			}
		} else {
			response = "INVALID_EVENT_TYPE";
		}
		Logs.logData(RequestType.CANCEL_SERVER_EVENT, logParams, requestDate, 1, response);
		return response;
	}
	

	public Object sendRequest(Map<String, String> requestParams, String location) {
		System.out.println("UDP===="+location);
		int port1 = 8002, port2 = 8007;
		if (location.equals(Locations.MTL)) {
			port1 = 8002;
			port2 = 8007;
		} else if (location.equals(Locations.QUE)) {
			port1 = 8004;
			port2 = 8008;
		} else if(location.equals(Locations.SHE)) {
			port1 = 8006;
			port2 = 8009;
		}
		byte[] data = new byte[1000000];
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream s = new ObjectOutputStream(b);
			s.writeObject(requestParams);
			
			DatagramSocket datagramSocket = new DatagramSocket();
			DatagramSocket datagramSocket1 = new DatagramSocket(port2);
			
			data = new byte[1000000];
			data = b.toByteArray();
			DatagramPacket sendData = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), port1);
			datagramSocket.send(sendData);
			datagramSocket.close();
			data = new byte[1000000];
			DatagramPacket responseData = new DatagramPacket(data, data.length);
			datagramSocket1.receive(responseData);
			ByteArrayInputStream b2 = new ByteArrayInputStream(responseData.getData());
			ObjectInputStream i = new ObjectInputStream(b2);
			datagramSocket1.close();
			Object returnObject = i.readObject();
			System.out.println("UDP response="+returnObject.toString());
			return returnObject;
		} catch(Exception e) {
			System.out.println(e);
		}
		return null;
	}
}
