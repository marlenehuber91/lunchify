package backend.logic;

import backend.model.Invoice;
import backend.model.Reimbursement;
import backend.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontend.controller.ReimbursementHistoryController;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportService {//AI generated

    public void exportToJson(List<Reimbursement> data, File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Java 8 Date/Time-Unterstützung aktivieren
        mapper.registerModule(new JavaTimeModule());
        // Deaktiviert das Schreiben von Dates als Timestamps (z. B. 1623456000000)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(file, data);
    }

    public void exportToXml(List<Reimbursement> data, File file) throws Exception {
        // Kontext muss alle beteiligten Klassen kennen
        JAXBContext context = JAXBContext.newInstance(
                Wrapper.class,
                Reimbursement.class,
                Invoice.class,
                User.class
        );

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(new Wrapper(data), file);
    }

    // Wrapper-Klasse für die Liste
    @XmlRootElement(name = "reimbursements")
    private static class Wrapper {
        @XmlElement(name = "reimbursement")
        private List<Reimbursement> items;

        public Wrapper() {}

        public Wrapper(List<Reimbursement> items) {
            this.items = items;
        }
    }

}
