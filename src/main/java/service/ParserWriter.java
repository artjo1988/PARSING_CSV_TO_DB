package service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;

@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:path.properties")
})
@Service
public class ParserWriter {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${path.source}")
    private String pathSource;

    @Value("${path.result}")
    private String pathResult;


    public void parseAndWrite() {
        String pointStart = new File("").getAbsolutePath();
        pointStart = pointStart.substring(0, pointStart.indexOf(":") + 1);
        File sourceFolder = new File(pointStart + pathSource);
        File resultFolder = new File(pointStart + pathResult);
        boolean checkFolder = false;
        boolean checkFile = false;
        if (!sourceFolder.exists()) {
            if (sourceFolder.mkdir()) {
                System.out.println("\nБыла создана папка - " + sourceFolder.getAbsolutePath());
                checkFolder = true;
            } else {
                System.out.println("\nПапку - " + sourceFolder.getAbsolutePath() + " не удалось создать");
                checkFolder = true;
            }
        }
        if (!resultFolder.exists()) {
            if (resultFolder.mkdir()) {
                System.out.println("\nБыла создана папка - " + resultFolder.getAbsolutePath());
                checkFolder = true;
            } else {
                System.out.println("\nПапку - " + resultFolder.getAbsolutePath() + " не удалось создать");
                checkFolder = true;
            }
        }
        String[] files = sourceFolder.list(new SuffixFileFilter(".csv"));
        if (files == null) {
            System.out.println("\nПожалуйста подготовьте соответствующиие папки и файлы к работе");
            System.exit(-1);
        }
        if (files.length == 0) {
            checkFile = true;
        }
        if (checkFolder || checkFile) {
            System.out.println("\nПожалуйста подготовьте соответствующиие папки и файы к работе");
            System.exit(-1);
        }
        try {

//            jdbcTemplate.execute("COPY csv_tab (id_old, name_old,value_old) FROM '" + sourceFile.getAbsolutePath() + "' DELIMITER ',' CSV HEADER;");

            Connection connection = DriverManager.getConnection(url, username, password);
            String sql = "COPY csv_tab (id_old, name_old, value_old) FROM stdin CSV HEADER DELIMITER ','";
            BaseConnection baseConnection = (BaseConnection) connection;
            CopyManager manager = new CopyManager(baseConnection);
            Reader reader = null;
            for (String file : files) {
                File sourceFile = new File(sourceFolder.getAbsolutePath() + "/" + file);
                File resultFile = new File(resultFolder.getAbsolutePath() + "/" + file);
                done:
                {
                    if (resultFile.exists()) {
                        break done;
                    }
                    if (!resultFile.createNewFile()) {
                        System.out.println("\nРезультирующий файл - " + resultFile.getAbsolutePath() + " не удалось создать");
                        System.exit(-1);

                    }
                }
                reader = new BufferedReader(new FileReader(sourceFile));
                manager.copyIn(sql, reader);
                FileUtils.copyFile(sourceFile, resultFile);
                reader.close();
                if (!sourceFile.delete()) {
                    System.out.println("\nИсходный файл - " + sourceFile.getAbsolutePath() + " не удалось удалить при перемещении");
                    System.exit(-1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


