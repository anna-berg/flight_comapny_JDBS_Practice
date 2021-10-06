import dao.TicketDao;
import dto.TicketFilter;
import entity.TicketEntity;

import java.math.BigDecimal;
import java.util.Optional;

public class DaoRunner {
    public static void main(String[] args) {
        var ticket = TicketDao.getInstance().findById(5L);
        System.out.println(ticket);
    }

    private static void filterTest() {
        var ticketFilter = new TicketFilter(3, 0, null, "A1");
        var tickets = TicketDao.getInstance().findAll(ticketFilter);
        System.out.println(tickets);
    }

    private static void updateTest() {
        var ticketDao = TicketDao.getInstance();
        var ticketById = ticketDao.findById(2L);
        System.out.println(ticketById);

        ticketById.ifPresent(ticket -> {
            ticket.setCost(BigDecimal.valueOf(188.88));
            ticketDao.update(ticket);
        });
    }

    private static void saveTest() {
        var ticketDao = TicketDao.getInstance();
        var ticket = new TicketEntity();
        ticket.setPassengerNo("1234567");
        ticket.setPassengerName("Test");
//        ticket.setFlight(3L);
        ticket.setSeatNo("B3");
        ticket.setCost(BigDecimal.TEN);
        var savedTicket = ticketDao.save(ticket);
        System.out.println(savedTicket);
    }
}
