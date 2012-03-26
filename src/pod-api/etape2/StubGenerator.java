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
        String text = "";
		text = "public class " + className + "_stub " + "extends SharedObject, ";
		text = text + "implements "   + className + "_itf, " + "java.io.Serializable {";
		text = text + '\n';

        // Constructor
		text = text + '\t'+ "public " + className + "_stub (int id, Object o){";
		text = text + '\n';
		text = text + '\t'+'\t'+"super(id,o);";
		text = text + '\n';
        text = text + '\t'+"}";
        text = text + '\n';

        // Methods
		Method[] methods = o.getClass().getMethods();

		for (Method m : methods){
			String method_name = m.getName();
		    int modifier = m.getModifiers();
            String return_type = m.getReturnType().getSimpleName();
            Type[] parameter = m.getParamaterType();
            Type[] exception = m.getExceptionTypes();
            
            // Access modifier
            if(Modifier.isPublic(modifier)){
                text = text + '\t'+"public ";
            }
            if(Modifier.isPrivate(modifier)){
               text = text + '\t'+"private ";
            }
		    if(Modifier.isProtected(modifier)){
                text =text + '\t'+"protected ";
            }
            
            // return type
            text = text + return_type + " " ;

            // name
            text = text + method_name +"(";
            
            // parameters
            int arg = 0;
            String parameter_line = "";
            for(Type t : parameter){
                parameter_line += t.getSimpleName + " arg"+arg+", ";
            }
            if(!parameter_line.equals("")){
                parameter_line = parameter_line.subString(0,parameter_line.length()-3);
            }
            text = text + parameter_line + ") ";
            
            // exception
            String exception_line = "";
            for(Type e : exception){
                exception_line += e.getSimpleName +", ";
            }
            if(!exception_line.equals("")){
                exception_line = "throws "+exception_line.subString(0,exception_line.length() - 3);
            }
            text = text + exception_line;
            text = text + "{";
            text = text = '\n';

            // body of method
                
            text = text + '\t' + '\t';
            text = className + "object = ("+className+").obj ;";
            text = text + '\n' + '\t' +'\t';
            if(return_type.equals("void")){
                text = text + "object."+method_name+parameter_line+";";
            }else{
                text = text + "return object."+method_name+paramater_line+";";
            }
            text = text + '\n' + '\t' + "}";
        }
        text = text + '\n' + '}';
        buffer.write(text);
        witer.close();
        return f;
	}

    public static void main(String args[]){
        Compteur c = new Compteur();
        this.introspection(c);
        System.exit(0);
    }
}	
	
	
	


