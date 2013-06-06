import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public class LogFile {
	public LogFile(String fileName) {
		try {
			File file = new File(fileName);
			if (!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			fos = new FileOutputStream(file);
			p = new PrintStream(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLog(String text) {
		p.println(text);
	}
	
	private FileOutputStream fos;
	private PrintStream p;
}
