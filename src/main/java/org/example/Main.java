package org.example;

import jdk.jfr.Event;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, ParseException, SAXException {
        List<MyData> data = getXML();
        for (MyData entry: data) {
            System.out.println(entry);
        }
        getHeldenMitHöherenEinfluss(data, 0.5);
        getGalaktischeKonfrontationen(data);
    }

    public static List<MyData> getFromXMLFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(MyDataList.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File xmlFile = new File(System.getProperty("user.dir") + "/data/" + "marvel_konfrontationen.xml");
            MyDataList myDataList = (MyDataList) unmarshaller.unmarshal(xmlFile);
            return myDataList.getLogs();

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<MyData> getXML() throws IOException, SAXException, ParserConfigurationException, ParseException {
        List<MyData> events = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(System.getProperty("user.dir") + "/data/" + "marvel_konfrontationen.xml"));

        NodeList logList = doc.getElementsByTagName("log");
        for (int i = 0; i < logList.getLength(); i++) {
            MyData event = new MyData();
            event.Id = Integer.parseInt(doc.getElementsByTagName("Id").item(i).getTextContent());
            event.Held = doc.getElementsByTagName("Held").item(i).getTextContent();
            event.Antagonist = doc.getElementsByTagName("Antagonist").item(i).getTextContent();
            event.Konfrontationstyp = Konfrontationstyp.valueOf(doc.getElementsByTagName("Konfrontationstyp").item(i).getTextContent());
            event.Ort = doc.getElementsByTagName("Ort").item(i).getTextContent();
            event.Datum = new SimpleDateFormat("yyyy-MM-dd").parse(doc.getElementsByTagName("Datum").item(i).getTextContent());
            event.GlobalerEinfluss = Double.parseDouble(doc.getElementsByTagName("GlobalerEinfluss").item(i).getTextContent());
            events.add(event);
        }
        return events;
    }

//    Anzeige von Helden mit einem höheren Globalen Einfluss als ein gegebener Wert (0.5 Punkte) mit stream
    public static void getHeldenMitHöherenEinfluss(List<MyData> data, double einfluss) {
        data.stream()
                .filter(entry -> entry.getGlobalerEinfluss() > einfluss)
                .forEach(System.out::println);
    }

//    Anzeige von "Galaktischen" Konfrontationen in absteigender Reihenfolge (0.5 Punkte)
    public static void getGalaktischeKonfrontationen(List<MyData> data) {
        data.stream()
                .filter(entry -> entry.getKonfrontationstyp().equals(Konfrontationstyp.Galaktisch))
                .sorted((e1, e2) -> e2.getDatum().compareTo(e1.getDatum()))
                .forEach(System.out::println);
    }


    public enum Konfrontationstyp{
        Individuell, Team, Galaktisch, Multiversal
    }

    @XmlRootElement(name = "logs")
    public static class MyDataList {
        private List<MyData> logs;

        @XmlElement(name = "log")
        public List<MyData> getLogs() {
            return logs;
        }

        public void setLogs(List<MyData> logs) {
            this.logs = logs;
        }
    }

    @XmlRootElement(name = "log")
    public static class MyData{
        int Id;
        String Held;
        String Antagonist;
        Konfrontationstyp Konfrontationstyp;
        String Ort;
        Date Datum;
        Double GlobalerEinfluss;

        @XmlElement(name = "Id")
        public int getId() {
            return Id;
        }

        @XmlElement(name = "Held")
        public String getHeld() {
            return Held;
        }

        @XmlElement(name = "Antagonist")
        public String getAntagonist() {
            return Antagonist;
        }

        @XmlElement(name = "Konfrontationstyp")
        public Main.Konfrontationstyp getKonfrontationstyp() {
            return Konfrontationstyp;
        }

        @XmlElement(name = "Ort")
        public String getOrt() {
            return Ort;
        }

        @XmlElement(name = "Datum")
        public Date getDatum() {
            return Datum;
        }

        @XmlElement(name = "GlobalerEinfluss")
        public Double getGlobalerEinfluss() {
            return GlobalerEinfluss;
        }

        @Override
        public String toString() {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return "MyData{" +
                    "Id=" + Id +
                    ", Held='" + Held + '\'' +
                    ", Antagonist='" + Antagonist + '\'' +
                    ", Konfrontationstyp=" + Konfrontationstyp +
                    ", Ort='" + Ort + '\'' +
                    ", Datum=" + Datum +
                    ", GlobalerEinfluss=" + GlobalerEinfluss +
                    '}';
        }
    }

}


