package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.Invoice;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import database.DatabaseConnection;
import frontend.controller.ReimbursementHistoryController;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ExportService {//AI generated


    public static ConnectionProvider connectionProvider = new ConnectionProvider() {
        @Override
        public Connection getConnection() {
            return DatabaseConnection.connect();
        }
    };


    public void exportToJson(List<Reimbursement> data, File file) throws Exception { //AI generated
        ObjectMapper mapper = new ObjectMapper();
        // Java 8 Date/Time-Unterstützung aktivieren
        mapper.registerModule(new JavaTimeModule());
        // Deaktiviert das Schreiben von Dates als Timestamps (z. B. 1623456000000)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

    private class ExportData {
            private int userId;
            private String userName;
            private List<Reimbursement> reimbursements;
            private long totalAmount;

            public ExportData(int userId, String userName, List<Reimbursement> reimbursements, long totalAmount) {
                this.userId = userId;
                this.userName = userName;
                this.reimbursements = reimbursements;
                this.totalAmount = totalAmount;
            }

            public int getUserId() {
                return userId;
            }
            public String getUserName() {
                return userName;
            }

        public long getTotalAmount(List<Reimbursement> reimbursements) {
            return reimbursements.stream()
                    .filter(r -> r.getStatus() == ReimbursementState.APPROVED)
                    .mapToLong(r -> (long) r.getApprovedAmount())
                    .sum();
        }

        public ExportData(User user, List<Reimbursement> reimbursements) {
                userId = user.getId();
                userName = user.getName();




        }



    }
}
