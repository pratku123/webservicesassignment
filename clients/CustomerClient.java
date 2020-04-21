
public class CustomerClient {
	public static void main(String args[]) throws Exception{
		try{
			Customer customer = new Customer();
			customer.start();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}