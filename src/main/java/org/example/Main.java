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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, ParseException, SAXException {
        List<MyData> data = getXML();
//        for (MyData entry: data) {
//            System.out.println(entry);
//        }
//        getHeldenMitHöherenEinfluss(data);
//        getGalaktischeKonfrontationen(data);
        saveReport(data);
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
//            event.Datum = new SimpleDateFormat("yyyy-MM-dd").parse(doc.getElementsByTagName("Datum").item(i).getTextContent());
            event.Datum = LocalDate.parse(doc.getElementsByTagName("Datum").item(i).getTextContent());
            event.GlobalerEinfluss = Double.parseDouble(doc.getElementsByTagName("GlobalerEinfluss").item(i).getTextContent());
            events.add(event);
        }
        return events;
    }

    /**
     * Get all heroes with a higher influence than the given value
     * @param data
     */
    public static void getHeldenMitHöherenEinfluss(List<MyData> data) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the minimum global influence: ");
        double minGlobalInfluence = scanner.nextDouble();
        Set<String> helden = new LinkedHashSet<>();
        data.stream()
                .filter(entry -> entry.getGlobalerEinfluss() > minGlobalInfluence)
                .forEach(entry -> helden.add(entry.getHeld()));
        helden.forEach(System.out::println);
    }

    /**
     * Get all galactic confrontations sorted by date
     * @param data
     */
    public static void getGalaktischeKonfrontationen(List<MyData> data) {
        data.stream()
                .filter(entry -> entry.getKonfrontationstyp().equals(Konfrontationstyp.Galaktisch))
                .sorted((e1, e2) -> e2.getDatum().compareTo(e1.getDatum()))
                .forEach(entry -> System.out.println(entry.getDatum() + ": " + entry.getHeld() + " vs. " + entry.getAntagonist() + " - " + entry.getOrt()));
    }

    /**
     * Save the report to a file in the given rules
     * @param data
     * @throws FileNotFoundException
     */
    public static void saveReport(List<MyData> data) throws FileNotFoundException {
        Map<Konfrontationstyp, Integer> konfrontationen = new HashMap<>();
        Map<Konfrontationstyp, Double> impact = new HashMap<>();
        for (MyData entry: data) {
            konfrontationen.put(entry.getKonfrontationstyp(), konfrontationen.getOrDefault(entry.getKonfrontationstyp(), 0) + 1);
            impact.put(entry.getKonfrontationstyp(), impact.getOrDefault(entry.getKonfrontationstyp(), 0.0) + entry.getGlobalerEinfluss());
        }
        List<Map.Entry<Konfrontationstyp, Integer>> sorted = new ArrayList<>(konfrontationen.entrySet());
        sorted.sort((e1, e2) -> {
            if (e1.getValue().equals(e2.getValue())) {
                return Double.compare(impact.get(e1.getKey()), impact.get(e2.getKey()));
            }
            return Integer.compare(e2.getValue(), e1.getValue());
        });
        PrintWriter writer = new PrintWriter(System.getProperty("user.dir") + "/data/" + "bericht_konfrontationen.txt");
        for (Map.Entry<Konfrontationstyp, Integer> entry: sorted) {
            writer.println(entry.getKey() + "&" + entry.getValue() + "$" + impact.get(entry.getKey()));
        }
        writer.close();
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
        LocalDate Datum;
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
        public LocalDate getDatum() {
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


