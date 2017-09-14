package lille3.refphoto.cli;

import lille3.refphoto.service.Photoservicecli;

public class Delete {
	
	public static void main(String[] argv) {
		
		
		Photoservicecli service = new Photoservicecli();
		
		service.delete();
		
		service.closeMc();
		
		System.exit(0);
	}
}