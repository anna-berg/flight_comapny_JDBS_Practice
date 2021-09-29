import util.ConnectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BlobRunner {
    public static void main(String[] args) throws SQLException, IOException {
        //blob -  binary large object (значит что можно в него засунуть все что угодно ч
        // то можно представить в виде БАЙТ, обычно используется для таких обьектов как картинки видео аудио
        // в postgresql он представлен в виде bytea (byte array)

        //clob - character large object - можно в него запихнуть что угодно что можно
        // представить в виде символов
        // в postgresql он представлен в виде TEXT

        saveImage();
    }

    private static void getImage(){
        var sql = """
                UPDATE aircraft
                SET image = ?
                WHERE id = 1
                """;
    }

    private static void saveImage() throws SQLException, IOException {

        var sql = """
                UPDATE aircraft
                SET image = ?
                WHERE id = 1
                """;

        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {


            preparedStatement.setBytes(1, Files.readAllBytes(Path.of("resources", "Boeing_777.jpg")));
            preparedStatement.executeUpdate();
        }
    }

    /*private static void saveImage() throws SQLException, IOException {

        var sql = """
                UPDATE aircraft
                SET image = ?
                WHERE id = 1
                """;

        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);
            var blob = connection.createBlob();
            blob.setBytes(1, Files.readAllBytes(Path.of("resources", "Boeing_777.jpg")));

            preparedStatement.setBlob(1,blob);
            preparedStatement.executeUpdate();
            connection.commit();
        }
    }*/
}
