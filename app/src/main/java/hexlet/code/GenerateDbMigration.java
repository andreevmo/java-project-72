package hexlet.code;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;

public class GenerateDbMigration {

    public static void main(String[] args) throws IOException {

        DbMigration dbMigration = DbMigration.create();
        dbMigration.addPlatform(Platform.H2, "db_h2");
        dbMigration.addPlatform(Platform.POSTGRES, "db_postgres");

        dbMigration.generateMigration();
    }
}
