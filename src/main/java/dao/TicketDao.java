package dao;

import dto.TicketFilter;
import entity.Flight;
import entity.TicketEntity;
import exeption.DaoExeption;
import util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class TicketDao {
    public static final TicketDao INSTANCE = new TicketDao();
    public static final String DELETE_SQL = """
            DELETE FROM ticket
            WHERE id = ?
            """;
    public static final String SAVE_SQL = """
            INSERT INTO ticket (passenger_no, passenger_name, flight_id, seat_no, cost) 
            VALUES (?, ?, ?, ?, ?);
            """;

    public static final String UPDATE_SQL = """
            UPDATE ticket
            SET passenger_no = ?,
            passenger_name = ?,
            flight_id = ?,
            seat_no = ?,
            cost = ?
            WHERE  id = ?
            """;

    public static final String FIND_ALL = """
            SELECT ticket.id, 
                    passenger_no, 
                    passenger_name, 
                    flight_id, 
                    seat_no, 
                    cost,
                    f.status,
                    f.aircraft_id,
                    f.arrival_airport_code,
                    f.arrival_date,
                    f.departure_airport_code,
                    f.departure_date                    
            FROM ticket
            JOIN flight f 
                ON ticket.flight_id = f.id
            """;

    public static final String FIND_BY_ID = FIND_ALL + """
            WHERE ticket.id = ?
            """;

    private TicketDao(){
    }

    public List<TicketEntity> findAll(TicketFilter filter){
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if(filter.seatNo() != null){
            whereSql.add("seat_no LIKE ?");
            parameters.add("%" + filter.seatNo() + "%");
        }
        if (filter.passengerName() != null){
            whereSql.add("passenger_name = ?");
            parameters.add(filter.passengerName());
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());
        var where = whereSql.stream()
                .collect(joining(" AND ", " WHERE ", " LIMIT ? OFFSET ? "));
        var sql = FIND_ALL + where;
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i+1, parameters.get(i));
            }
            System.out.println(preparedStatement);
            var resultSet = preparedStatement.executeQuery();
            List <TicketEntity> tickets = new ArrayList<>();
            while (resultSet.next()){
                tickets.add(buildTicket(resultSet));
            }
            return tickets;
        } catch (SQLException throwables) {
            throw new DaoExeption(throwables);
        }
    }

    public List<TicketEntity> findAll(){
        try (var connection = ConnectionManager.get();
                var preparedStatement = connection.prepareStatement(FIND_ALL)) {
            var resultSet = preparedStatement.executeQuery();
            List<TicketEntity> ticketList = new ArrayList<>();
            while (resultSet.next()){
                ticketList.add(buildTicket(resultSet));
            }
            return ticketList;
        } catch (SQLException throwables) {
            throw new DaoExeption(throwables);
        }
    }

    public Optional<TicketEntity> findById(Long id){
        try (var connection = ConnectionManager.get()) {
            var preparedStatement = connection.prepareStatement(FIND_BY_ID);
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();
            TicketEntity ticket = null;
            if(resultSet.next()){
                ticket = buildTicket(resultSet);
            }
            return Optional.ofNullable(ticket);
        } catch (SQLException throwables) {
            throw new DaoExeption(throwables);
        }
    }

    private TicketEntity buildTicket(ResultSet resultSet) throws SQLException {
        
        var flight = new Flight(
                resultSet.getLong("flight_id"),
                resultSet.getString("flight_no"),
                resultSet.getTimestamp("departure_date").toLocalDateTime(),
                resultSet.getString("departure_airport_code"),
                resultSet.getTimestamp("arrival_date").toLocalDateTime(),
                resultSet.getString("arrival_airport_code"),
                resultSet.getInt("aircraft_id"),
                resultSet.getString("status")
        );
        return new TicketEntity(
                resultSet.getLong("id"),
                resultSet.getString("passenger_no"),
                resultSet.getString("passenger_name"),
                flight,
                resultSet.getString("seat_no"),
                resultSet.getBigDecimal("cost")
        );
    }

    public void update (TicketEntity ticket){
        try (var connection = ConnectionManager.get()) {
            var preparedStatement = connection.prepareStatement(UPDATE_SQL);
            preparedStatement.setString(1, ticket.getPassengerNo());
            preparedStatement.setString(2, ticket.getPassengerName());
            preparedStatement.setLong(3, ticket.getFlight().id());
            preparedStatement.setString(4, ticket.getSeatNo());
            preparedStatement.setBigDecimal(5, ticket.getCost());
            preparedStatement.setLong(6, ticket.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throw new DaoExeption(throwables);
        }
    }

    public TicketEntity save(TicketEntity ticket){
        try (var connection = ConnectionManager.get()) {
            var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, ticket.getPassengerNo());
            preparedStatement.setString(2, ticket.getPassengerName());
            preparedStatement.setLong(3, ticket.getFlight().id());
            preparedStatement.setString(4, ticket.getSeatNo());
            preparedStatement.setBigDecimal(5, ticket.getCost());

            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()){
                ticket.setId(generatedKeys.getLong("id"));
            }
            return ticket;
        } catch (SQLException throwables) {
            throw new DaoExeption(throwables);
        }

    }

    public boolean delete (Long id){
        try (var connection = ConnectionManager.get()) {
            var preparedStatement = connection.prepareStatement(DELETE_SQL);
            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new DaoExeption(throwables);
        }
    }

    public static TicketDao getInstance(){
        return INSTANCE;
    }
}
