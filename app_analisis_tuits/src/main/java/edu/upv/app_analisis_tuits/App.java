package edu.upv.app_analisis_tuits;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import opennlp.tools.util.normalizer.EmojiCharSequenceNormalizer;
import opennlp.tools.util.normalizer.TwitterCharSequenceNormalizer;

/**
* Clase principal: Genera las instancias de los modulos que componen la aplicación
*
*/


public class App 
{
	Utils util = new Utils();
	public HashMap<Integer, ArrayList<String> > hashtags = new HashMap<Integer, ArrayList<String> >();
	
    public static void main( String[] args ) throws org.json.simple.parser.ParseException, FileNotFoundException, IOException, java.text.ParseException
    {
    	App app = new App();
        
        ArrayList<String> archivos = new ArrayList<String>();
        final File folder = new File("C:\\Users\\froy1\\Desktop\\tuits\\peje");
        
        for (final File fileEntry : folder.listFiles())
        {
            archivos.add(fileEntry.getName());
        }
        
        for(String a: archivos)
        {
            app.convertirArchivo("C:\\Users\\froy1\\Desktop\\tuits\\peje\\"+a);
        }
        
        
    	//String ruta = "C:\\Users\\froy1\\Desktop\\tuits\\AMLO\\AMLO_2019_10_02_10_19_05";
    	//app.convertirArchivo(ruta);
        
        
    	//ArrayList<Tuit> tuits = app.obtenerTuits(ruta);
    	
    	//System.out.println(tuits.get(3).getTuit());
    	
    	//for(int i=0; i < app.hashtags.size(); i++ )
    	//{
    	//	System.out.println(  );
    	//}
	  
    	
        //System.out.println("Tweet donde hay hashtags " + app.hashtags.get(1).get(0) );
        //System.out.println( dateFormat.format(new Date()) );
	    
	    
    }
    
    ArrayList<Tuit> obtenerTuits(String ruta) throws ParseException, java.text.ParseException
    {
    	String tweet = "";
    	ArrayList<Tuit> tuits_array = new ArrayList<Tuit>();
    	
    	JSONParser parser = new JSONParser();
    	ArrayList<String> hash = new ArrayList<String>();
    	
        try ( Reader reader = new FileReader(ruta + ".json") )
        {
        	
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray tuits = (JSONArray) jsonObject.get("tuits");
            
            for (int i = 0; i < tuits.size(); ++i) 
            {
                JSONObject tuit = (JSONObject) tuits.get(i);
                Tuit t = new Tuit();
                
                
                t.setNumero( ((Long) tuit.get("Numero")).intValue() );
                t.setFecha( util.convertirFecha((String) tuit.get("Fecha") ) );
                t.setAutor( (String) tuit.get("Autor") );
                
                //////////////////////// PREPROCESAMIENTO DE TUITS ////////////////////////////
                
                tweet = (String) tuit.get("Tweet");
                

                String expresionRegular = "(#\\w+)";
                boolean hayHash = false;
                
                Pattern p = Pattern.compile(expresionRegular);
                Matcher m = p.matcher(tweet);
                
                while (m.find()) 
                {
                	String h = m.group(1);
                	hash.add(h);
                	hayHash = true;
                }
                
                if(hayHash) {
                	 hashtags.put(i+1, hash);
                }
               
                
                //tweet = (String) emoji.normalize( (String) tuit.get("Tweet") );
                //tweet = (String) tcs.normalize( tweet );
                
                //System.out.println(tweet);
                
                t.setTuit( tweet );
                
                tuits_array.add(t);
                
                hash.clear();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return tuits_array;
    }
    
    void convertirArchivo(String ruta) throws FileNotFoundException, IOException
    {
    	App app = new App();
    	String ts = app.muestraContenido(ruta);
    	String t = "";
    	
    	String ts_2 = ts.substring(1);
    	
    	int total_caractetes = ts_2.length();
    	ts = ts_2.substring(0, total_caractetes-2);
    	
    	String[] tuits = ts.split("},");
    	int i = 0;
    	String aux = "";
    	for(i=0; i < tuits.length; i++)
    	{
    		int numero_cifras = 19 + Integer.toString(i+1).length();
    		
    		if(i==0) {
    			//aux =  "{\"tuits\":[" + tuits[i].substring(0, numero_cifras) + "\"";
                        aux =  tuits[i].substring(0, numero_cifras) + "\"";
    		}else {
    			aux =  tuits[i].substring(0, numero_cifras) + "\"";
    		}
    		
    		String aux2 = tuits[i].substring(numero_cifras,numero_cifras+28) + "\",";
    		tuits[i] = aux + aux2 + tuits[i].substring(numero_cifras+29);
    		tuits[i] = tuits[i] + "}";
    		
    		/***** Preprocesamiento de tuits *********/
    		
    		t = tuits[i].replace("\\n", " ");
    		t = t.replaceAll("\\\\u[0-9a-fA-F]{4}","");
    		t = t.replace("\\\"", "");
    		t = t.replace("\\", "");
    		t = (String) util.url_normalizer.normalize(t);
    		t = t.replace("??", "");
    		
    		tuits[i] = t;
        	/****************************************/

    	}
    	
    	tuits[i-1] = tuits[i-1]; //+ "]}";
    	
    	
    	
    	app.escribirArchivoJson(tuits, ruta);
    }
    
    String muestraContenido(String archivo) throws FileNotFoundException, IOException {
    	String cadena;
    	File f = new File(archivo);
    	BufferedReader b = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), "ISO-8859-1"));
    	
    	cadena = b.readLine();
    	b.close();
    	return cadena;
    }
    
    void escribirArchivoJson(String[] tuits, String ruta) 
    {
    	FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
        	
        	fichero = new FileWriter(ruta + ".json");
            pw = new PrintWriter(fichero);
        	
        	for(String t : tuits)
        	{
        		pw.println(t);
        	}
        	
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
    }
    
    
   
    
    
    
}
