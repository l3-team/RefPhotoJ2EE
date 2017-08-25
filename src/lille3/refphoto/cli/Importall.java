package lille3.refphoto.cli;

import lille3.refphoto.service.Photoservicecli;

public class Importall {
	
	public static void main(String[] argv) {
		
		Photoservicecli service = new Photoservicecli();
		
		service.importAllPhoto();
		
		service.closeMc();
		
		System.exit(0);
	}
}