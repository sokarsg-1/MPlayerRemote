/*
MPlayer Remote
    Copyright (C) 2015  Rafał Kałęcki

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.mplayer_remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;


/**
 * Klasa pomocnicza służąca do przekształcania listy obiektów klasy {@link com.mplayer_remote.Server} w zaszyfrowany algorytmem AES plik XML, 
 * udostępnia ona również możliwość odszyfrowania tego pliku XML i przekształcenie go z powrotem w listę obiektów klasy {@link com.mplayer_remote.Server}.  
 * 
 * @author sokar
 *
 */
public class XMLReaderWriter {
		//w celach diagnostycznych nazwa logu dla tej klasy
	private static final String TAG = "XMLReaderWriter";
	
	/**
	 * Kontekst aplikacji. Interfejs do pozyskiwania ogólnych informacji na temat środowiska, w którym działa aplikacja.
	 * @see android.content.Contex
	 */
	private Context mContext;
	
			//kryptografia
	/**
	 * ,,Sól'' potrzebna do generowania klucza AES.
	 */
	private byte[] salt = null;
	
	/**
	 * Sekretny klucz kryptograficzny.
	 */
	private SecretKey secretKey = null;
	
	/**
	 * Dostarcza interfejs do szyfrowania i odszyfrowania danym szyfrem w tym przypadku AES. 
	 */
	private Cipher cipher = null;
	
	/**
	 * Wektor inicjujący potrzebny do generowania klucza AES.
	 */
	private static byte[] iv = null; 
	
	/**
	 * Konstruktor przekazujący do obiektu klasy XMLReaderWriter kontekst aplikacji(obiekt klasy {@link android.content.Contex}). 
	 * @param mContext obiekt klasy {@link android.content.Contex}, czyli interfejs do uzyskiwania informacji o środowisku, w którym działa aplikacja.
	 */
	public XMLReaderWriter(Context mContext){
		this.mContext = mContext;
	}
	
	/**
	 * Przekształca listę obiektów klasy {@link com.mplayer_remote.Server} w plik XML, następnie generuje klucz AES na podstawie hasła aplikacji, 
	 * którym szyfruje ten plik XML i zapisuje go do pliku <code>server.crypto</code>.
	 * @param serverList lista obiektów klasy <code>Server</code>.
	 * @param appPassword tablica znaków, na podstawie której będzie wygenerowany klucz szyfru AES.
	 */
	public void createEncryptedXMLFileWithServerList(List <Server> serverList, char[] appPassword) {
		ByteArrayOutputStream baos = null;
		byte[] bateArrayWithXML;
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder =  documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			
			Element rootElement = document.createElement(new String("servers"));
			document.appendChild(rootElement);
			
			for (int i = 0; i < serverList.size(); i++){
				Element server = document.createElement("server");
				rootElement.appendChild(server);
					
					Element server_nameElement = document.createElement("server_name");
					server.appendChild(server_nameElement);
					Text textNode_with_server_name = document.createTextNode(serverList.get(i).getServerName());
					server_nameElement.appendChild(textNode_with_server_name);
					
					Element IP_addressElement = document.createElement("IP_address");
					server.appendChild(IP_addressElement);
					Text textNode_with_IP_address = document.createTextNode(serverList.get(i).getIPAddress());
					IP_addressElement.appendChild(textNode_with_IP_address);
					
					Element usernameElement = document.createElement("username");
					server.appendChild(usernameElement);
					Text textNode_with_username = document.createTextNode(serverList.get(i).getUsername());
					usernameElement.appendChild(textNode_with_username);
					
					Element passwordElement = document.createElement("password");
					server.appendChild(passwordElement);
					Text textNode_with_password = document.createTextNode(new String(serverList.get(i).getPassword()));
					passwordElement.appendChild(textNode_with_password);
			}
					//transforming a DOM XML tree to byte[] bate_array_with_XML
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			baos = new ByteArrayOutputStream();
			StreamResult result =  new StreamResult(baos);
			transformer.transform(source, result);
			bateArrayWithXML = baos.toByteArray();
			
			createKey(appPassword);
			encryptBateArrayWithXMLAndSaveCryptogramToFile(bateArrayWithXML);
			
			Log.v(TAG,"create_encrypted_XMLFile_with_server_list zaszyfrowało dane hasłem: " + new String(appPassword));
			for (int i = 0; i < serverList.size(); i++){
				Log.v(TAG,serverList.get(i).getServerName());
				Log.v(TAG,serverList.get(i).getIPAddress());
				Log.v(TAG,serverList.get(i).getUsername());
				Log.v(TAG,new String(serverList.get(i).getPassword()));
			}
			
		} catch (TransformerException e){
			Log.v(TAG,"wystąpił TransformerException" + e);
		} catch (DOMException e){
			Log.v(TAG,"wystąpił DOMException" + e);
		} catch (ParserConfigurationException e) {
			Log.v(TAG,"wystąpił ParserConfigurationException" + e);
		}finally{
			if (baos != null) { 
		        try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
		    } else { 
		        System.out.println("baos not open");
		    } 
		}
		
		
		//decryptFileWithXML();
	}
	
	
	/**
	 * Tworzy klucz szyfru AES.
	 * @param appPassword hasło aplikacji na podstawie, którego jest tworzony szyfr.
	 */
	private void createKey(char[] appPassword){
       	//creating or geting a salt
	  File file = mContext.getFileStreamPath("salt");	//for storing a salt
	  
	  if (!file.exists()){
		  salt = new byte[20];
		  SecureRandom rnd = new SecureRandom ();
		  rnd.nextBytes (salt);
		  FileOutputStream salt_fos;
		  try {
			salt_fos = mContext.openFileOutput("salt", Context.MODE_PRIVATE);
			salt_fos.write(salt);
			salt_fos.flush();
			salt_fos.close();
		  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
	  }else{
		  try{
		  	FileInputStream salt_fis = mContext.openFileInput("salt");
	        // Get the size of the file
	        long length = salt_fis.available();

	        if (length > Integer.MAX_VALUE) {
	        // File is too large
	        }

	        // Create the byte array to hold the data
	        byte[] bytes = new byte[(int) length];

	        // Read in the bytes
	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead = salt_fis.read(bytes, offset, bytes.length - offset)) >= 0) {
	        offset += numRead;
	        }

	        // Ensure all the bytes have been read in
	        if (offset < bytes.length) {
	        throw new IOException("Could not completely read file ");
	        }

	        // Close the input stream and return bytes
	        salt_fis.close();
	        
	        salt = bytes;
		  }catch(Exception e){
	    	   e.printStackTrace();
	      }
	  }
	  		//creating a cipher
       try{
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        PBEKeySpec spec = new PBEKeySpec(appPassword, salt, 1024, 256);

	        SecretKey tmp = factory.generateSecret(spec);
	        secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
	        
	        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey,new IvParameterSpec(new byte[cipher.getBlockSize()]));
	        AlgorithmParameters params = cipher.getParameters();
	        iv = params.getParameterSpec(IvParameterSpec.class).getIV();
	        
       }catch(Exception e){
    	   e.printStackTrace();
       }
   }
   
   	/**
   	 * Szyfruje daną tablice byte' ów i zapisuje szyfrogram do pliku <code>servers.crypted</code>.
   	 * @param bateArrayWithXML
   	 */
   	private void encryptBateArrayWithXMLAndSaveCryptogramToFile(byte[] bateArrayWithXML){		//szyfrowanie bate_array_with_XML kluczem i zapisanie szyfrogramu do pliku
   		CipherOutputStream cos = null;
   		ByteArrayInputStream bais = null;
	   	try{
	   		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
	   		cos = new CipherOutputStream(mContext.openFileOutput("servers.crypted", Context.MODE_PRIVATE), cipher);
			int blockSize = cipher.getBlockSize();
		   	byte[] inBytes = new byte[blockSize];
		   	bais = new ByteArrayInputStream(bateArrayWithXML);
		   	int i;
		   	while ((i = bais.read(inBytes)) != -1){
		   		cos.write(inBytes,0,i);
		   	}
		   	
		   	Log.v(TAG, "zaszyfrowany xml do servers.xml:" + new String(bateArrayWithXML));
		   	
	   	}catch (Exception e){
	   		e.printStackTrace();
	   	} finally {
	   		if (cos != null) {
               try {
            	   cos.close();
               } catch (IOException e) {
            	   // TODO Auto-generated catch block
            	   e.printStackTrace();
               }
            }
	   		if (bais != null) {
               try {
					bais.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
           }
	   	}
   	}
   	
   	/**
	 * Zwraca listę obiektów klasy <code>Server</code> odszyfrowaną (kluczem utworzonym na podstawie tablicy znaków <code>appPassword</code>) z pliku XML o nazwie servers.crypted.   
	 * @param appPassword tablica znaków z hasłem, na podstawie którego będzie generowany klucz AES. 
	 * @return serverList lista obiektów klasy <code>Server</code> odszyfrowana z pliku XML o nazwie servers.crypted.
	 * @throws WrongPasswordException gdy nie można zdeszyfrować pliku XML <code>servers.crypted</code> kluczem wygenerowanym na podstawie <code>appPassword</code>.
	 */
	public List<Server> decryptFileWithXMLAndParseItToServerList(char[] appPassword) throws WrongPasswordException {
		List<Server> serverList = new ArrayList<Server>();			//returned decrypted and parsed server_list
		createKey(appPassword);
		byte[] decrypted_XML = decryptFileWithXML();		//decrypted XML
		if (decrypted_XML == null){
			throw new WrongPasswordException();
		}
		ByteArrayInputStream bais = null;			//InputStream with decrypted XML for parser
		
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			bais = new ByteArrayInputStream(decrypted_XML);
			Document document = builder.parse(bais);
			
			Element root = document.getDocumentElement();		//<servers>
			NodeList list_of_server_elements = document.getElementsByTagName("server");				//list contain all <server> tags
			for ( int i  =  0; i < list_of_server_elements.getLength(); i++){
				Log.v(TAG, "ilość serverów w XML wynosi: " + list_of_server_elements.getLength());
				Server server = new Server();
				Node server_tag = list_of_server_elements.item(i);	//server_tag is a concrete <server>
				if (server_tag instanceof Element){
					NodeList list_of_elements_in_server_tag = server_tag.getChildNodes();	//<server_name><IP_address><username><password>
					Log.v(TAG, "list_of_elements_in_server_tag NodeList zawiera: " + list_of_elements_in_server_tag.getLength());
					for ( int j = 0; j < list_of_elements_in_server_tag.getLength(); j++){
						
						Element child_of_server_tagElement = (Element) list_of_elements_in_server_tag.item(j);
						Text textNode = (Text) child_of_server_tagElement.getFirstChild();
						//String Text = textNode.getData().trim();
						if (textNode != null){
							if ( child_of_server_tagElement.getTagName().equals("server_name")){
									server.setServerName(textNode.getTextContent());
									child_of_server_tagElement.getNextSibling();
							} 
							if (child_of_server_tagElement.getTagName().equals("IP_address")){
									Log.v(TAG, "textNode.getTextContent() " + textNode.getTextContent().length());
									server.setIPAddress(textNode.getTextContent());
									child_of_server_tagElement.getNextSibling();
							} 
							if (child_of_server_tagElement.getTagName().equals("username")){
									server.setUsername(textNode.getTextContent());
									child_of_server_tagElement.getNextSibling();
							} 
							if (child_of_server_tagElement.getTagName().equals("password")){
									server.setPassword((textNode.getTextContent()).toCharArray());
							}
						}
					}
				}
				serverList.add(server);
			}
			
			Log.v(TAG,"decryptFileWithXMLAndParseItTo_server_list odczytało dane za pomocą hasła: " + new String(appPassword));
			for (int i = 0; i < serverList.size(); i++){
				if (serverList.get(i).getServerName() != null)
					Log.v(TAG,serverList.get(i).getServerName());
				if (serverList.get(i).getIPAddress() != null)
					Log.v(TAG,serverList.get(i).getIPAddress());
				if (serverList.get(i).getUsername() != null)
					Log.v(TAG,serverList.get(i).getUsername());
				if (serverList.get(i).getPassword() != null)
					Log.v(TAG,new String(serverList.get(i).getPassword()));
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bais != null) {
		        try {
					bais.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//Log.v(TAG, "server_list[0] : " + server_list.get(0).getServer_name() +server_list.get(0).getIP_address() + server_list.get(0).getUsername() + server_list.get(0).getFile_with_password());
		//Log.v(TAG, "server_list[1] : " + server_list.get(1).getServer_name() +server_list.get(1).getIP_address() + server_list.get(1).getUsername() + server_list.get(1).getFile_with_password());
		return serverList;
	}
	
   	
   	/**
   	 * Deszyfruje plik <code>server.crypted</code>.
   	 * @return Tablica byte' ów z treścią zdeszyfrowanego pliku <code>server.crypted</code>. 
   	 */
   	private byte[] decryptFileWithXML() {
	   byte[] decryptedXmlByteArrey = null;
	   CipherInputStream cis = null;
	   ByteArrayOutputStream baos = null;
	   try{
		   cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
		   cis = new CipherInputStream(mContext.openFileInput("servers.crypted"), cipher); 
		   int blockSize = cipher.getBlockSize();
		   byte[] inBytes = new byte[blockSize];
		   baos = new ByteArrayOutputStream(); 
		   int i;
		   while ((i = cis.read(inBytes)) != -1) {
			   baos.write(inBytes, 0, i);
		   }
		   decryptedXmlByteArrey = baos.toByteArray();
		   
		   
		   Log.v(TAG, "odczytany xml z servers.xml i zdeszyfrowany:" + new String(decryptedXmlByteArrey));
		   	       
	   } catch (Exception e){
		   e.printStackTrace();
	   } finally {
		   if (cis != null) {
               try {
            	   cis.close();
               } catch (IOException e) {
            	   // TODO Auto-generated catch block
            	   e.printStackTrace();
               }
           }
           if (baos != null) {
               try {
            	   baos.close();
               } catch (IOException e) {
            	   // TODO Auto-generated catch block
            	   e.printStackTrace();
               }
           }
		   
	   }
	   
	   return decryptedXmlByteArrey;
	}

}

/**
 * Wyjątek informujący klasy korzystające z metody {@link com.mplayer_remote.XMLReaderWriter#decryptFileWithXMLAndParseItToServerList} o podaniu jako parametr tej funkcji niepoprawnego hasła aplikacji.
 * @author sokar
 */
class WrongPasswordException extends Exception{
	
}