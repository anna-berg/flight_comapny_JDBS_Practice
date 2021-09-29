import org.postgresql.Driver;
import util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {
//        GeneratedKey();
//        String flight_id = "2";

       /* Long flight_id = 2L;
        var rezult = getTicketsByFlightId(flight_id);
        System.out.println(rezult);
*/

        /*var flightsBetween = getFlightsBetween(LocalDate.of(2020, 1, 1).atStartOfDay(), LocalDateTime.now());
        System.out.println(flightsBetween);
        */

//        checkMetaData();

        long flightId = 9;
        var deleteFlightSql = "DELETE FROM flight WHERE id = " + flightId;
        var deleteTicketSql = " DELETE FROM ticket WHERE flight_id = " + flightId;

        Connection connection = null;
        Statement statement = null;
        try {
            connection = ConnectionManager.open();

            //убрать auto commit mode для того что бы каждый из наших запросов не выполнялся автоматически
            //таким образом мы берем выполнение всех транзакций на себя, в данном случае что бы обеспечить
            //атомарность выполнения обеих запросов на удаления - или удаляются из flight м из ticket
            // или не удаляются нигде
            //делать это надо до выполнения каких-либо запросов.
            connection.setAutoCommit(false);

            statement = connection.createStatement();

            statement.addBatch(deleteTicketSql);
            statement.addBatch(deleteFlightSql);

            var ints = statement.executeBatch();

            //нельзя вызывать commit если автокомит стоит в тру
            connection.commit();
        }
        catch (Exception e){
            if (connection != null){
                connection.rollback();
            }
            throw e;
        }
        finally {
            if (connection != null) {
                connection.close();
            }

            if(statement != null){
                statement.close();
            }

        }
    }

//    private static void prepareStatementExample () throws SQLException {
//        long flightId = 9;
//        var deleteFlightSql = """
//                DELETE
//                FROM flight
//                WHERE id = ?
//                """;
//        var deleteTicketSql = """
//                DELETE
//                FROM ticket
//                WHERE flight_id = ?
//                """;
//
//        Connection connection = null;
//        PreparedStatement deleteFlightStatement = null;
//        PreparedStatement deleteTicketStatement = null;
//        try {
//            connection = ConnectionManager.open();
//            deleteFlightStatement = connection.prepareStatement(deleteFlightSql);
//            deleteTicketStatement = connection.prepareStatement(deleteTicketSql);
//
//            //убрать auto commit mode для того что бы каждый из наших запросов не выполнялся автоматически
//            //таким образом мы берем выполнение всех транзакций на себя, в данном случае что бы обеспечить
//            //атомарность выполнения обеих запросов на удаления - или удаляются из flight м из ticket
//            // или не удаляются нигде
//            //делать это надо до выполнения каких-либо запросов.
//            connection.setAutoCommit(false);
//
//            // устанавливаем идентификатор - что ставить вместо ?
//            deleteTicketStatement.setLong(1, flightId);
//            deleteFlightStatement.setLong(1, flightId);
//
//            // для операций  Insert, Update, Delete используем executeUpdate2
//            deleteTicketStatement.executeUpdate();
//            deleteFlightStatement.executeUpdate();
//
//            //нельзя вызывать commit если автокомит стоит в тру
//            connection.commit();
//        }
//        catch (Exception e){
//            if (connection != null){
//                connection.rollback();
//            }
//            throw e;
//        }
//        finally {
//            if (connection != null) {
//                connection.close();
//            }
//
//            if(deleteFlightStatement != null){
//                deleteFlightStatement.close();
//            }
//
//            if (deleteTicketStatement != null){
//                deleteTicketStatement.close();
//            }
//        }
//    }


    private static void checkMetaData (){
        try (var connection = ConnectionManager.open()) {
            var metaData = connection.getMetaData();
            var catalogs = metaData.getCatalogs();
            while (catalogs.next()){
                var catalog = catalogs.getString(1);
//                System.out.println(catalog);

                var schemas = metaData.getSchemas();
                while (schemas.next()){
                    var schem = schemas.getString("TABLE_SCHEM");
//                    System.out.println(schem);
                    if (schem.equals("public")){

                        //получаем только метаданные из схемы паблик
                        // если нас не интересует какой-то конкретный столбец или схема передаем "%" - он обозначает все
                        var metaDataTables = metaData.getTables(catalog, schem, "%", null);
                        while (metaDataTables.next()){
                            System.out.println(metaDataTables.getString("TABLE_NAME"));
                            // точно так же можно получить колонки - metaData.getColumns()
                        }
                    }

                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    //запрос, который возвр. айдишники всех наших перелетов
    // через ? преобразовать дату в нужную строку которую понимает наша бд
    private static List<Long> getFlightsBetween (LocalDateTime start, LocalDateTime end){
        String sql = """
                SELECT id
                FROM flight
                WHERE departure_date BETWEEN ? AND ?
                """;
        List <Long> rezult = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            System.out.println(preparedStatement);
            // для каждого запроса (preparedStatement or Statement) указываем колличество строк
            // которые он будет возвращать за раз (50), потом после считывания блока в 50,
            // опять будет брать следующий блок в 50 - FetchSize
            // записи получаем итерационно
            preparedStatement.setFetchSize(50);

            //указываем сколько максимально мы можем жать ответа и занимать соеденение, в сек
            preparedStatement.setQueryTimeout(10);

            //устанавливает лимит для всех наших запросов, сколько максимально мы можем взять,
            //что бы не было переполнения памяти
            preparedStatement.setMaxRows(100);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(start));
            System.out.println(preparedStatement);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(end));
            System.out.println(preparedStatement);

            final var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                rezult.add(resultSet.getLong("id"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rezult;
    }

    // в лонг нельзя передать sql injection, это безопаснее
    private static List<Long> getTicketsByFlightId(Long flight_id) throws SQLException {
      // все неизвесные параметры подставляем через ?
        String sql = """
                SELECT id
                FROM ticket
                WHERE flight_id = ?
                """;

        List <Long> rezult = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            //устанавливаем что подставлять вместо ? --- номер вопросика, и из какой колонки брать значения
            preparedStatement.setLong(1, flight_id);
            final var resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                rezult.add(resultSet.getObject("id", Long.class));
            }
            return rezult;
        }

    }
//    private static List<Long> getTicketsByFlightId(String flight_id) throws SQLException {
//        String sql = """
//                SELECT id
//                FROM ticket
//                WHERE flight_id = %s
//                """.formatted(flight_id);
//
//        List <Long> rezult = new ArrayList<>();
//        try (var connection = ConnectionManager.open();
//             var statement = connection.createStatement()) {
//            final var resultSet = statement.executeQuery(sql);
//            while (resultSet.next()){
//                //для того что бы не вылетала ошибка если в одной из строк значение NULL, берем обьект, и указываем
//                // в какой тип его конвертнуть
//                rezult.add(resultSet.getObject("id", Long.class));
//            }
//            return rezult;
//        }
//
//    }

    private static void GeneratedKey() throws SQLException {
        Class<Driver> driverClass = Driver.class;
        String sql = """
                INSERT INTO info (data)
                VALUES 
                ('autogenerated')
                """;

        try (var connection = ConnectionManager.open();
//            var statement = connection.createStatement()) {
             //есть перегруженый метод, где в парам передаем первым зн-ем то как мы будем перемещаться по выборке,
             //по умолчанию - строго с низу вверх,TYPE_SCROLL_INSENSITIVE - значит можем подниматься вверх или вниз
             //вторым парам передаем можем ли изменять что-то в полученой выборке - по умолчанию не можем
             var statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            System.out.println(connection.getSchema());
            System.out.println(connection.getTransactionIsolation()
            );
            var executeResult = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()){
                var generatedId = generatedKeys.getInt("id");
                System.out.println(generatedId);
            }

            // executeQuery - используется когда нам нужно сделать SELECT
//            var executeResult = statement.executeQuery(sql);
//            while (executeResult.next()){
//                System.out.println(executeResult.getLong("id"));
//                System.out.println(executeResult.getString("passenger_no"));
//                System.out.println(executeResult.getBigDecimal("cost"));
//                System.out.println("------------------------------------------");
//                executeResult.updateLong("id", 1000); //можем изменить полученное значение по-тому что поставили updateble при создании statement
//                executeResult.afterLast(); //можем изменять положение "курсора" по-тому что поставили TYPE_SCROLL_INSENSITIVE при создании statement
//            }
        }
    }
}
