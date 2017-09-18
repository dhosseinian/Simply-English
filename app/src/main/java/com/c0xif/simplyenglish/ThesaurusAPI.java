package com.c0xif.simplyenglish;

// For API GET request
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import android.os.StrictMode;

// For XML parsing
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class ThesaurusAPI {

    private final String intermediateKey = "4c87d5ff-0704-4fcc-9ffb-578ef8ac378c";
    private final String intermediateURL = "http://www.dictionaryapi.com/api/v1/references/ithesaurus/xml/";
    private final String collegiateKey = "b1ddcb1e-2725-46d4-9091-565d5ff9fc89";
    private final String collegiateURL = "http://www.dictionaryapi.com/api/v1/references/thesaurus/xml/";

    private String key;
    private String url;

    public ThesaurusAPI(){
        key = intermediateKey;
        url = intermediateURL;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public ThesaurusAPI(boolean intermediate){
        if (intermediate) {
            key = intermediateKey;
            url = intermediateURL;
        }
        else {
            key = collegiateKey;
            url = collegiateURL;
        }
    }

    public ArrayList<String> fetchSynonyms(String word)throws Exception{
        return parseXML(getXML(word), word);
    }

    private String getXML(String word) throws Exception{
        StringBuilder result = new StringBuilder();
        String call = url + word.toLowerCase() + "?key=" + key;
        URL u = new URL(call);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while((line = rd.readLine()) != null){
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private ArrayList<String> parseXML(String xml, String word) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = builder.parse(is);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("syn");

        ArrayList<String> synonyms = new ArrayList<>();
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String syn = eElement.getTextContent();
                syn = syn.replaceAll("\\[.*?\\]", ""); // Removes all instances inside of brackets
                String[] syns = syn.split("(,|;) ");
                for (String s : syns) {
                    // Remove redundant translations
                    if (!s.contains(word)){
                        synonyms.add(s);
                    }
                }
            }
        }
        return synonyms;
    }

}
