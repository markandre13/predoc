import java.awt.*;
import java.applet.*;

public class predoc
	extends Applet
{
	public static void main(String args[])
	{
		System.out.println("running as stand-alone program");
		Main.main(args, null);
	}
	
	public void init()
	{
		System.out.println("running as applet");
		Main.main(null, this);
	}
}