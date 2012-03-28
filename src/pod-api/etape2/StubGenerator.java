import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;

public class StubGenerator {
	
	/**Generation du fichier nom_stub.java
	 * 
	 * @param id
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static File genJava(int id, Object o) throws IOException{
		String className=o.getClass().getSimpleName();
		File f = new File(className + "_stub.java");
		String code="";
		code+="public class " + className + "_stub " + "extends SharedObject, ";
		code+="implements " + className + "_itf, " + "java.io.Serializable {\n";
		code+="public " + className + "_stub (int id, Object o){super(id,o);}";
		Method[] methods = o.getClass().getMethods();
		for (Method m : methods){
			code+=genMethod(m,className);
		}
		code+="}\n";
		FileWriter fw = new FileWriter(f);
		fw.write(code);
		return f;
	}
	/**Generation d'une methode du stub a partir de celle de l'objet introspecté
	 * ex dans Sentence la methode :
	 * 	public void write(String text) {
	 *	data = text;
	 *  }
	 * va donner dans Sentence_stub :
	 *  public void write(String arg1){
	 *  Sentence tata = (Sentence)obj;
	 *  tata.write(arg1);
	 *  }
	 * @param m methode d'origine
	 * @param className
	 * @return String contenant le code de la methode du stub
	 */
	public static String genMethod(Method m,String className){
		String str="";
		//modifier (public, private etc)
		str+=genModifiers(m.getModifiers());
		//type de retour
		String ret = m.getReturnType().getName();
		str+=ret+" ";
		//nom methode
		String nom=m.getName();
		str+=nom+"(";
		//arguments(Type + nom)
		Class args[] = m.getParameterTypes();
		int i;
		if(args!=null && args.length > 0){
			for(i=0;i<args.length;i++){
				str+=args[i].getSimpleName()+" arg"+i+", ";
			}
			str=str.substring(0,str.length()-2); //enleve la derniere virgule
			str+=")";
		}
		//exceptions
		Class exc[] = m.getExceptionTypes();
		if(exc!=null && exc.length > 0){
			str+=" throws ";
			for(i=0;i<exc.length;i++){
				str+=exc[i].getName()+", ";
			}
			str=str.substring(0,str.length()-2); 
		}
		str+="{\n";
		//corps de la methode
		str+=className+ "tata = ("+className+")obj;\n";
		if(ret.equals("void")){
			str+="tata."+nom+"(";
			if(args!=null && args.length > 0){
				for(i=0;i<args.length;i++){
					str+="arg"+i+", ";
				}
				str=str.substring(0,str.length()-2); 
			}
			str+=");";
		}
		else{
			str+="return tata."+nom+"(";
			if(args!=null && args.length > 0){
				for(i=0;i<args.length;i++){
					str+="arg"+i+", ";
				}
				str=str.substring(0,str.length()-2); 
			}
			str+=");";
		}
		str+="\n}\n";
		return str;
	}
	
	/**Generation du String du modifieur à partir de l'entier issu de method.getModifiers()
	 * 
	 * @param modifiers
	 * @return "public " pour l'instant
	 */
	private static String genModifiers(int modifiers) {	
		return "public ";
	}
	
	
	
	

}
