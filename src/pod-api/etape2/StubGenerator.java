import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.tools.*;

public class StubGenerator {
  
    public static ArrayList<String> registre ;
    public static JavaCompiler compiler;
    public static StandardJavaFileManager fm;
    public static DiagnosticCollector<JavaFileObject> diagnostics;

    public StubGenerator(){
        registre = new ArrayList<String>();   
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics =  new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);
    } 
    
    public static void generate_and_compile(Object o){
        String name = o.getClass().getSimpleName();
         
        if(registre.contains(name)){
            // le _stub.java a déja été créé et compilé
        }else{
            try{
                create_stub_file(o);
            }catch(IOException e){
                System.out.println("File_stub not found");
                System.exit(-1);
            }
            int compilationResult = compiler.run(null,null,null,"-d",".",name+"_stub.java");
            if(compilationResult == 0){
                System.out.println("Compilation is successful");
                 registre.add(name);
            }else{
                System.out.println("Compilation Failed");
            }
           
        }
    }
	public static File create_stub_file(Object o) throws IOException{

		String className=o.getClass().getSimpleName();
        String stub_name = className + "_stub.java";
		File f = new File(stub_name);
		BufferedWriter buffer = new BufferedWriter(new FileWriter(f));
        String text = "";
		text += "public class " + className + "_stub " + "extends SharedObject ";
		text += "implements "   + className + "_itf, " + "java.io.Serializable {";
		text += "\n" + "\n";

        // Constructor 1
		text += "\t"+ "public " + className + "_stub (int id, Object o){ ";
		text += "\n";
		text += "\t"+"\t"+"super(id,o);";
		text += "\n";
        text += "\t"+"}";
        text += "\n";
 
        // Constructor 2
		text += "\t"+ "public " + className + "_stub (int id){ ";
		text += "\n";
		text += "\t"+"\t"+"super(id);";
		text += "\n";
        text += "\t"+"}";
        text += "\n";
       
        // Methods // A revoir : redéfinir toutes les méthodes appelables y compris hérités (mais pas private ou finale)
		Method[] methods = o.getClass().getDeclaredMethods();

		for (Method m : methods){
			String method_name = m.getName();
		    int modifier = m.getModifiers();
            String return_type = m.getReturnType().getSimpleName();
            Class[] parameter = m.getParameterTypes();
            Class[] exception = m.getExceptionTypes();
            text += "\n";
            // Access modifier
            if(Modifier.isPublic(modifier)){
                text += "\t"+"public ";
            }
            if(Modifier.isPrivate(modifier)){
                text += "\t"+"private ";
            }
		    if(Modifier.isProtected(modifier)){
                text += "\t"+"protected ";
            }
            
            // return type
            text += return_type + " " ;

            // name
            text += method_name +"(";
            
            // parameters
            int arg = 0;
            String arg_line = "";
            String parameter_line = "";
            for(Class t : parameter){
                parameter_line += t.getSimpleName() + " arg"+arg+", ";
                arg_line += " arg"+arg+", ";
                arg+=1;
            }
            if(!parameter_line.equals("")){
                parameter_line = parameter_line.substring(0,parameter_line.length()-2);
                arg_line = arg_line.substring(0,arg_line.length()-2);
            }
            
            text += parameter_line + ") ";
            
            // exception
            String exception_line = "";
            for(Class e : exception){
                exception_line += e.getSimpleName()+", ";
            }
            if(!exception_line.equals("")){
                exception_line = "throws "+exception_line.substring(0,exception_line.length() - 2);
            }
            text += exception_line;
            text += "{";
            text += "\n";

            // body of method
                
                 text += "\n" + "\t" +"\t";
            if(return_type.equals("void")){
                text += "( ("+className+") obj )."+method_name+"("+arg_line+");";
            }else{
                text += "return ( ("+className+") obj )."+method_name+"("+arg_line+");";

            }
            text += "\n" + "\t" + "}";
            text += "\n";
        }
        text += "\n" + '}';
        buffer.write(text);
        buffer.close();
        return f;
	}
}	
	
	
	


