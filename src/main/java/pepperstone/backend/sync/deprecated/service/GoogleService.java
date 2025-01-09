package pepperstone.backend.sync.deprecated.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleService.class); // Google Sheets와 상호작용하기 위한 메서드를 포함
    private static final String APPLICATION_NAME = "My First Project"; // Google Sheets 애플리케이션의 이름을 나타냄
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance(); // 상수는 JSON 데이터를 처리하기 위한 JsonFactory 인스턴스를 제공
    private static final String CREDENTIALS_FILE_PATH = "/googlesheet/google.json"; // 인증에 사용되는 JSON 파링릐 경로를 지정
    private Sheets sheetsService;

    // 현재의 메소드는 Sheets 인스턴스를 얻는 데 사용
    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        if (sheetsService == null) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ClassPathResource(CREDENTIALS_FILE_PATH).getInputStream())
                    // Google API를 호출할 떄 필요한 권한을 지정하는 부분 , 읽기/쓰기 권한을 나타냄
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));
            sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return sheetsService;
    }

    public void writeToSheet(String spreadsheetId, String range, List<List<Object>> values) {
        try {
            Sheets service = getSheetsService();
            ValueRange body = new ValueRange().setValues(values);
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            logger.info("Updated rows: {}", result.getUpdatedRows());
        } catch (Exception e) {
            logger.error("Failed to write data to the spreadsheet", e);
            throw new RuntimeException("Failed to write data to the spreadsheet: " + e.getMessage(), e);
        }
    }

    public List<List<Object>> readSheet(String spreadsheetId, String range) {
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            logger.info("Data read successfully from the spreadsheet");
            return values != null ? values : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to read data from the spreadsheet", e);
            throw new RuntimeException("Failed to read data from the spreadsheet: " + e.getMessage(), e);
        }
    }

    public List<List<Object>> readSheetByColumn(String spreadsheetId, String range) {
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .setMajorDimension("COLUMNS") // 열 단위로 데이터를 가져오기 위한 설정
                    .execute();
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                logger.info("No data found in the spreadsheet for range: {}", range);
                return Collections.emptyList();
            }

            logger.info("Data read successfully from the spreadsheet by column, range: {}", range);
            return values;
        } catch (Exception e) {
            logger.error("Failed to read data from the spreadsheet by column, range: {}", range, e);
            throw new RuntimeException("Failed to read data from the spreadsheet: " + e.getMessage(), e);
        }
    }

    public List<String> getSheetNames(String spreadsheetId) {
        try {
            Sheets service = getSheetsService();
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
            List<Sheet> sheets = spreadsheet.getSheets();

            List<String> sheetNames = new ArrayList<>();
            for (Sheet sheet : sheets) {
                sheetNames.add(sheet.getProperties().getTitle()); // 시트 이름 추가
            }

            logger.info("Sheet names retrieved successfully: {}", sheetNames);
            return sheetNames;
        } catch (Exception e) {
            logger.error("Failed to retrieve sheet names", e);
            throw new RuntimeException("Failed to retrieve sheet names: " + e.getMessage(), e);
        }
    }

}