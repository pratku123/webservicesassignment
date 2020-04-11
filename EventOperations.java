import java.rmi.*;
import java.util.HashMap; 
import java.util.Map; 
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)@WebService

public interface EventOperations extends Remote {
        @WebMethod
	public String addEvent(String managerId, String eventId, String eventType, int bookingCapacity) ;
        @WebMethod
	public String removeEvent(String managerId, String eventID, String eventType) ;
        @WebMethod
	public Map<String, EventDetails> listEventAvailability(String managerId, String eventType) ;
        @WebMethod
	public String bookEvent(String customerID, String eventID, String eventType) ;
        @WebMethod
	public List<String> getBookingSchedule(String customerId) throws RemoteException;
        @WebMethod
	public String cancelEvent(String customerID, String eventID, String eventType) ;
        @WebMethod
	public Map<String, EventDetails> getServerEventsAvailable(String eventType) ;
        @WebMethod
	public String bookServerEvent(String customerId, String eventId, String eventType) ;
        @WebMethod
	public List<String> getServerBookingSchedule(String customerId) ;
        @WebMethod
	public String cancelServerEvent(String customerId, String eventId, String eventType) ;
}

