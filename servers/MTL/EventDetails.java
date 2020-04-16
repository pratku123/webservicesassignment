import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class EventDetails implements Serializable{
	private int maxBookingCapacity;
	private int currentlyBooked;
	List<String> bookedCustomerIds;
	
	public EventDetails(int maxBookingCapacity, int currentlyBooked) {
		this.maxBookingCapacity = maxBookingCapacity;
		this.currentlyBooked = currentlyBooked;
		this.bookedCustomerIds = new ArrayList<>();
	}
	
	int getMaxBookingCapacity() {
		return this.maxBookingCapacity;
	}

	void setMaxBookingCapacity(int maxBookingCapacity){
		this.maxBookingCapacity = maxBookingCapacity;
	}

	int getCurrentlyBooked() {
		return this.currentlyBooked;
	}
	void setCurrentlyBooked(int currentlyBooked) {
		this.currentlyBooked = currentlyBooked;
	}
	List<String> getBookedCustomerIds() {
		return this.bookedCustomerIds;
	}

	int getSeatsAvailable() {
		return this.maxBookingCapacity - this.currentlyBooked;
	}

	void setBookedCustomerIds(List<String> bookedCustomerIds) {
		this.bookedCustomerIds = bookedCustomerIds;
	}

	void bookSeat(String customerId) {
		this.currentlyBooked = this.currentlyBooked+1;
		this.bookedCustomerIds.add(customerId);
	}
}