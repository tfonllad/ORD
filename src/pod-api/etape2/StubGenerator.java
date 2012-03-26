import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;

public class StubGenerator {
	public static File introspection(Object o) throws IOException{
		String className=o.getClass().getSimpleName();
		File f = new File(className + "_stub.java");
		BufferedWriter buffer = new BufferedWriter(new FileWriter(f));
		buffer.write("public class " + className + "_stub " + "extends SharedObject, ");
		buffer.write("implements " + className + "_itf, " + "java.io.Serializable {");
		buffer.newLine();
		buffer.write("public " + className + "_stub (int id, Object o){");
		buffer.newLine();
		buffer.write("super(id,o);}");
		buffer.newLine();
		Method[] methods = o.getClass().getMethods();
		for (Method m : methods){
			String methodName=m.getName();
			
		}
		
		
				
		
	}
	
	
	
	

}
