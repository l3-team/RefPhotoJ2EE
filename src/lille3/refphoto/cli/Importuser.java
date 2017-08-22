package lille3.refphoto.cli;

import lille3.refphoto.service.Photoservicecli;

public class Importuser {
	
	public static void main(String[] argv) {
		
		if (argv.length != 1) {
			System.out.println("syntax:");
			System.out.println("java lille3.refphoto.cli.Importuser <uid>");
			System.exit(1);
		}
		
		Photoservicecli service = new Photoservicecli();
		
		service.importUserPhoto(argv[0]);
		
		service.closeMc();
		
		System.exit(0);
	}
}