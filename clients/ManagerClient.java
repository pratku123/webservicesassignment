
public class ManagerClient {
	public static void main(String args[]) throws Exception{
		try{
			Manager manager = new Manager();
			manager.start();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}