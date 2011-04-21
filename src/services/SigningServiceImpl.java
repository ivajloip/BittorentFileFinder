package services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class SigningServiceImpl extends SigningService {
	private final char[] password;
	private final String file;
	
	public SigningServiceImpl(String file, String password) {
		this.file = file;
		this.password = password.toCharArray();
	}
	
	/**
	 * Makes a document that contains the xml that is given in <strong>in</strong> argument.
	 * @param in The xml that will be used for making the resulting Document.
	 * @return A Document that represents the given xml.
	 */
    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
    /**
     * 
     * @param keystore KeyStore from where the keys should be taken
     * @param alias Alias of the key that is to be taken from the KeyStore.
     * @param password Password for taking the key.
     * @return KeyPair representing the public and private key corresponding to the alias in the keystore.
     */
    private static KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            // Get private key
            Key key = keystore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey)key);
            }
        } catch (UnrecoverableKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyStoreException e) {
        }
        return null;
    }
    
    private XMLSignature getXMLSignature(Document doc) {
    	try {
            // Create a DOM XMLSignatureFactory that will be used to generate the 
    		// enveloped signature
    		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    	    // Create a Reference to the enveloped document (in this case we are
    		// signing the whole document, so a URI of "" signifies that) and
    		// also specify the SHA1 digest algorithm and the ENVELOPED Transform.
    		Reference ref = fac.newReference
    		    ("", fac.newDigestMethod(DigestMethod.SHA1, null),
    	             Collections.singletonList
    		      (fac.newTransform
    			(Transform.ENVELOPED, (TransformParameterSpec) null)), 
    		     null, null);

    		// Create the SignedInfo
    		SignedInfo si = fac.newSignedInfo
    		    (fac.newCanonicalizationMethod
    		     (CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, 
    		      (C14NMethodParameterSpec) null), 
    		     fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
    		     Collections.singletonList(ref));

			KeyStore ks = null;
			ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(file), password);
    		
			KeyPair kp = getPrivateKey(ks, ks.aliases().nextElement(), password);
    		
			KeyStore.PrivateKeyEntry ke = (KeyStore.PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), 
					new KeyStore.PasswordProtection(password));
			
			X509Certificate cert = (X509Certificate) ke.getCertificate();
			List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
			x509Content.add(cert);

    	    // Create a KeyValue containing the DSA PublicKey that was generated
    		KeyInfoFactory kif = fac.getKeyInfoFactory();
    		X509Data xd = kif.newX509Data(x509Content);
    		
	        KeyValue kv = null;
			try {
				kv = kif.newKeyValue(kp.getPublic());
			} catch (KeyException e1) {
				e1.printStackTrace();
			}
			
			ArrayList<XMLStructure> keyInfoContent = new ArrayList<XMLStructure>();
			keyInfoContent.add(kv);
			keyInfoContent.add(xd);
			
    		// Create a KeyInfo and add the KeyValue to it
    	    KeyInfo ki = kif.newKeyInfo(Collections.unmodifiableList(keyInfoContent));

    		// Create the XMLSignature (but don't sign it yet)
    		return fac.newXMLSignature(si, ki);
    	}
    	catch (Exception e) {
    		System.out.println("Error while signing\n" + e.getStackTrace());
    		return null;
		}
    }
    
    private KeyPair getKeyPair() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, 
    		FileNotFoundException, IOException {
    	KeyStore ks = null;
		ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(file), password);
		
		return getPrivateKey(ks, ks.aliases().nextElement(), password);
    }
    
    /**
     * Signs a document (somewhat unusable for now)
     * @param doc Document to be signed
     */
	private void doSign(Document doc) {
    	try {
    		XMLSignature signature = getXMLSignature(doc);
    		
    		
    	    // Create a DOMSignContext and specify the RSA PrivateKey and
    	    // location of the resulting XMLSignature's parent element
    		DOMSignContext dsc = new DOMSignContext(getKeyPair().getPrivate(), doc.getDocumentElement());
    		dsc.setDefaultNamespacePrefix("ds");

    	    // Marshal, generate (and sign) the enveloped signature
    	    signature.sign(dsc);

    		// output the resulting document
    		StringWriter outputStream = new StringWriter();

    		TransformerFactory tf = TransformerFactory.newInstance();
   			Transformer trans = tf.newTransformer();
			trans.transform(new DOMSource(doc), new StreamResult(outputStream));
		} catch (DOMException e) {
			e.printStackTrace();
			System.out.println(e.code);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
    
	/**
	 * Makes a xml beautiful.
	 * @param unformattedXml An xml that is ugly.
	 * @return A well-formatted xml.
	 */
	public String sign(String unformattedXml) {
        try {
            final Document document = parseXmlFile(unformattedXml);
            doSign(document);
            OutputFormat format = new OutputFormat(document);
//            format.setLineWidth(65);
//            format.setIndenting(true);
//            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
	public boolean validate(String xml) {
		Document doc = parseXmlFile(xml);
		
		try {
			Key key = getKeyPair().getPublic();
			XMLValidateContext validateContext = new DOMValidateContext(key, doc.getChildNodes().item(0).getLastChild());
			XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
			
			XMLSignature signature = signatureFactory.unmarshalXMLSignature(validateContext);
			return signature.validate(validateContext);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
