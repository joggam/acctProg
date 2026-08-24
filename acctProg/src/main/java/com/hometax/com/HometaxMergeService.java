package com.hometax.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.WebDriver;

/**
 * 분류내려받기 전용 서비스.
 *
 * 기존 HometaxService의 단건 다운로드 로직은 수정하지 않고 재사용한다.
 * 선택한 여러 기업의 홈택스 XLS를 내려받은 뒤 첫 번째 파일의 형식을 기준으로
 * 데이터 행을 한 시트에 계속 이어 붙여 최종 XLS 한 파일을 만든다.
 *
 * ExcelTitleCopy는 호출하지 않는다.
 */
public class HometaxMergeService {

    /**
     * 홈택스 다운로드 파일 구조
     * 1행(index 0) : 안내/정보 영역
     * 2행(index 1) : TITLE
     * 3행(index 2) : DATA 시작
     */
    private static final int SOURCE_DATA_START_ROW = 2;

    private HometaxMergeService() {
    }

    public static File execute(
            List<HometaxMergeParameter> parameters,
            int year,
            int quarter,
            String downloadDir) throws Exception {

        if (parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException(
                    "분류내려받기 대상이 없습니다."
            );
        }

        File downloadFolder = new File(downloadDir);
        if (!downloadFolder.exists() && !downloadFolder.mkdirs()) {
            throw new RuntimeException(
                    "다운로드 폴더를 생성할 수 없습니다. "
                    + downloadFolder.getAbsolutePath()
            );
        }

        List<File> downloadedFiles = new ArrayList<File>();
        List<String> loginFailMessages = new ArrayList<String>();
        List<String> downloadFailMessages = new ArrayList<String>();

        for (HometaxMergeParameter parameter : parameters) {

            WebDriver driver = null;

            try {
                validateParameter(parameter, year, quarter);

                System.out.println();
                System.out.println("======================================");
                System.out.println("[MERGE-DOWNLOAD] 대상 사업자번호 = "
                        + parameter.getBusinessNumber());
                System.out.println("======================================");

                try {
                    driver = HometaxLogin.login(
                            parameter.getHometaxId(),
                            parameter.getHometaxPassword(),
                            parameter.getJuminFirst6(),
                            parameter.getJumin7th(),
                            downloadDir
                    );
                } catch (Exception loginException) {

                    String message = rootMessage(loginException);

                    loginFailMessages.add(
                            buildLoginFailMessage(parameter, message)
                    );

                    System.out.println(
                            "[MERGE-LOGIN-FAIL] "
                            + parameter.getHometaxId()
                            + " / "
                            + message
                    );

                    continue;
                }

                HometaxService hometaxService =
                        new HometaxService(driver, downloadDir);

                File downloadedFile =
                        hometaxService.downloadExcel(
                                year,
                                quarter,
                                parameter.getBusinessNumber()
                        );

                if (downloadedFile == null || !downloadedFile.exists()) {
                    throw new RuntimeException(
                            "홈택스 다운로드 결과 파일이 없습니다."
                    );
                }

                downloadedFiles.add(downloadedFile);

            } catch (Exception e) {

                String message = getDetailedDownloadFailReason(e);

                downloadFailMessages.add(
                        buildDownloadFailMessage(parameter, message)
                );

                System.out.println(
                        "[MERGE-DOWNLOAD-FAIL] "
                        + parameter.getHometaxId()
                        + " / "
                        + message
                );

            } finally {
                if (driver != null) {
                    try {
                        driver.quit();
                        System.out.println("Chrome 종료 완료");
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        File loginFailFile = null;

        if (!loginFailMessages.isEmpty()) {
            loginFailFile = writeLoginFailText(
                    loginFailMessages,
                    year,
                    quarter,
                    downloadFolder
            );

            System.out.println(
                    "[MERGE-LOGIN-FAIL-TXT] "
                    + loginFailFile.getAbsolutePath()
            );
        }

        File downloadFailFile = null;

        if (!downloadFailMessages.isEmpty()) {
            downloadFailFile = writeDownloadFailText(
                    downloadFailMessages,
                    year,
                    quarter,
                    downloadFolder
            );

            System.out.println(
                    "[MERGE-DOWNLOAD-FAIL-TXT] "
                    + downloadFailFile.getAbsolutePath()
            );
        }

        if (downloadedFiles.isEmpty()) {
            String firstError = "";

            if (!loginFailMessages.isEmpty()) {
                firstError = loginFailMessages.get(0);
            } else if (!downloadFailMessages.isEmpty()) {
                firstError = downloadFailMessages.get(0);
            }

            throw new RuntimeException(
                    "분류내려받기에 성공한 홈택스 파일이 없습니다."
                    + (firstError.length() == 0
                            ? ""
                            : " 첫 오류: " + firstError)
                    + (loginFailFile == null
                            ? ""
                            : " 로그인 실패 TXT: "
                            + loginFailFile.getAbsolutePath())
                    + (downloadFailFile == null
                            ? ""
                            : " 다운로드 실패 TXT: "
                            + downloadFailFile.getAbsolutePath())
            );
        }

        File mergedFile = mergeExcelFiles(
                downloadedFiles,
                year,
                quarter,
                downloadFolder
        );

        // 최종 병합 성공 후 개별 홈택스 다운로드 파일 제거.
        // 사용자가 보는 결과 파일은 병합 XLS 한 개만 남긴다.
        for (File file : downloadedFiles) {
            if (file == null) {
                continue;
            }

            try {
                if (file.exists() && !sameFile(file, mergedFile)) {
                    boolean deleted = file.delete();
                    System.out.println(
                            "[MERGE-CLEAN] "
                            + file.getName()
                            + " 삭제="
                            + deleted
                    );
                }
            } catch (Exception ignored) {
            }
        }

        if (!loginFailMessages.isEmpty()) {
            System.out.println();
            System.out.println("[MERGE-WARN] 일부 대상 홈택스 로그인 실패");
            for (String failMessage : loginFailMessages) {
                System.out.println(" - " + failMessage);
            }
        }

        if (!downloadFailMessages.isEmpty()) {
            System.out.println();
            System.out.println("[MERGE-WARN] 로그인 성공 후 일부 다운로드 실패");
            for (String failMessage : downloadFailMessages) {
                System.out.println(" - " + failMessage);
            }
        }

        return mergedFile;
    }

    /**
     * 로그인 성공 후 다운로드 단계에서 실패한 업체를 UTF-8 TXT로 저장한다.
     * 비밀번호와 주민등록번호는 기록하지 않는다.
     */
    private static File writeDownloadFailText(
            List<String> downloadFailMessages,
            int year,
            int quarter,
            File downloadFolder) throws Exception {

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());

        File outputFile = new File(
                downloadFolder,
                "홈택스_분류내려받기실패_"
                + year
                + "년_"
                + quarter
                + "분기_"
                + timestamp
                + ".txt"
        );

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(outputFile),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            writer.write('\uFEFF');
            writer.write("홈택스 분류내려받기 실패 업체");
            writer.newLine();
            writer.write("조회기간 : "
                    + year + "년 " + quarter + "분기");
            writer.newLine();
            writer.write("실패건수 : "
                    + downloadFailMessages.size() + "건");
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();
            writer.write("상호명 | 아이디 | 사업자등록번호 | 실패사유");
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();

            for (String failMessage : downloadFailMessages) {
                writer.write(failMessage);
                writer.newLine();
            }
        }

        return outputFile;
    }

    private static String buildDownloadFailMessage(
            HometaxMergeParameter parameter,
            String message) {

        String companyName = parameter.getCompanyName();

        if (companyName == null || companyName.trim().length() == 0) {
            companyName = "-";
        }

        return companyName
                + " | "
                + parameter.getHometaxId()
                + " | "
                + formatBusinessNumber(parameter.getBusinessNumber())
                + " | "
                + (message == null || message.trim().length() == 0
                        ? "내려받기 실패"
                        : message.trim());
    }

    /**
     * 분류내려받기 중 로그인 이후 발생한 실패의 실제 원인을 cause 체인까지 확인한다.
     * Selenium Alert 문구가 있으면 가장 우선해서 사용한다.
     */
    private static String getDetailedDownloadFailReason(Throwable throwable) {

        if (throwable == null) {
            return "내려받기 실패";
        }

        String fallback = null;
        Throwable current = throwable;
        int depth = 0;

        while (current != null && depth < 20) {

            String message = current.getMessage();
            String className = current.getClass().getName();
            String text = message == null ? "" : message.trim();

            String alertText = extractAlertText(text);
            if (alertText.length() > 0) {
                return alertText;
            }

            if (className.contains("TimeoutException")
                    || text.contains("TimeoutException")
                    || text.contains("Expected condition failed")) {
                return appendDetail(
                        "홈택스 처리시간 초과",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("NoSuchElementException")
                    || text.contains("NoSuchElementException")) {
                return appendDetail(
                        "홈택스 화면 요소를 찾지 못함",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("StaleElementReferenceException")
                    || text.contains("StaleElementReferenceException")) {
                return appendDetail(
                        "홈택스 화면이 갱신되어 요소 참조가 끊어짐",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("ElementClickInterceptedException")
                    || text.contains("ElementClickInterceptedException")) {
                return appendDetail(
                        "홈택스 화면에서 클릭이 다른 요소에 가로막힘",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("NoSuchWindowException")
                    || text.contains("NoSuchWindowException")) {
                return appendDetail(
                        "홈택스 브라우저 창을 찾지 못함",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("SessionNotCreatedException")
                    || text.contains("SessionNotCreatedException")) {
                return appendDetail(
                        "Chrome WebDriver 세션 생성 실패",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("WebDriverException")
                    || text.contains("WebDriverException")) {
                fallback = appendDetail(
                        "브라우저 처리 오류",
                        firstMeaningfulLine(text)
                );
            } else if (text.length() > 0
                    && !isGenericDownloadFailMessage(text)) {
                fallback = firstMeaningfulLine(text);
            }

            current = current.getCause();
            depth++;
        }

        if (fallback != null && fallback.trim().length() > 0) {
            return fallback.trim();
        }

        String topMessage = throwable.getMessage();
        if (topMessage != null && topMessage.trim().length() > 0) {
            return firstMeaningfulLine(topMessage);
        }

        return throwable.getClass().getSimpleName();
    }

    private static String extractAlertText(String text) {

        if (text == null || text.trim().length() == 0) {
            return "";
        }

        String value = text.trim();
        String marker = "Alert text :";
        int start = value.indexOf(marker);

        if (start < 0) {
            return "";
        }

        String alertText = value.substring(start + marker.length()).trim();

        int braceIndex = alertText.indexOf('}');
        if (braceIndex >= 0) {
            alertText = alertText.substring(0, braceIndex).trim();
        }

        int lineIndex = alertText.indexOf('\n');
        if (lineIndex >= 0) {
            alertText = alertText.substring(0, lineIndex).trim();
        }

        return alertText;
    }

    private static boolean isGenericDownloadFailMessage(String text) {
        if (text == null) {
            return false;
        }

        String value = text.trim();
        return value.matches("\\d{4}년\\s*\\d+분기\\s*다운로드 실패")
                || "다운로드 실패".equals(value)
                || "홈택스 다운로드 실패".equals(value);
    }

    private static String firstMeaningfulLine(String text) {

        if (text == null) {
            return "";
        }

        String value = text.trim();
        int newLine = value.indexOf('\n');

        if (newLine >= 0) {
            value = value.substring(0, newLine).trim();
        }

        if (value.length() > 250) {
            value = value.substring(0, 250);
        }

        return value;
    }

    private static String appendDetail(String summary, String detail) {

        if (detail == null || detail.trim().length() == 0) {
            return summary;
        }

        String value = detail.trim();

        if (value.equals(summary)) {
            return summary;
        }

        return summary + " - " + value;
    }

    /**
     * HometaxLogin.login() 단계에서 실패한 업체만 UTF-8 TXT로 저장한다.
     * 비밀번호와 주민등록번호는 기록하지 않는다.
     */
    private static File writeLoginFailText(
            List<String> loginFailMessages,
            int year,
            int quarter,
            File downloadFolder) throws Exception {

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());

        File outputFile = new File(
                downloadFolder,
                "홈택스_로그인실패_"
                + year
                + "년_"
                + quarter
                + "분기_"
                + timestamp
                + ".txt"
        );

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(outputFile),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            writer.write('\uFEFF');
            writer.write("홈택스 로그인 실패 업체");
            writer.newLine();
            writer.write("조회기간 : "
                    + year + "년 " + quarter + "분기");
            writer.newLine();
            writer.write("실패건수 : "
                    + loginFailMessages.size() + "건");
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();
            writer.write("상호명 | 아이디 | 사업자등록번호 | 실패사유");
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();

            for (String failMessage : loginFailMessages) {
                writer.write(failMessage);
                writer.newLine();
            }
        }

        return outputFile;
    }

    private static String buildLoginFailMessage(
            HometaxMergeParameter parameter,
            String message) {

        String companyName = parameter.getCompanyName();

        if (companyName == null || companyName.trim().length() == 0) {
            companyName = "-";
        }

        return companyName
                + " | "
                + parameter.getHometaxId()
                + " | "
                + formatBusinessNumber(parameter.getBusinessNumber())
                + " | "
                + simplifyLoginFailReason(message);
    }

    /**
     * Selenium 예외의 Build info / Driver info / Capabilities / Session ID 등
     * 불필요한 상세정보는 TXT에 기록하지 않고 사용자에게 필요한 사유만 남긴다.
     */
    private static String simplifyLoginFailReason(String message) {

        if (message == null || message.trim().length() == 0) {
            return "로그인 실패";
        }

        String value = message.trim();

        if (value.contains("입력하신 아이디, 비밀번호 또는 주민번호가 정확하지 않습니다")) {
            return "아이디/비밀번호/주민번호 불일치";
        }

        if (value.contains("mf_txppWframe_iptUserId")
                && value.contains("Expected condition failed")) {
            return "로그인 화면 로딩 실패";
        }

        if (value.contains("unexpected alert open")) {
            int alertStart = value.indexOf("Alert text :");

            if (alertStart >= 0) {
                String alertText = value.substring(
                        alertStart + "Alert text :".length()
                );

                int end = alertText.indexOf("}");
                if (end >= 0) {
                    alertText = alertText.substring(0, end);
                }

                alertText = alertText.trim();

                if (alertText.length() > 0) {
                    return alertText;
                }
            }

            return "홈택스 알림 발생으로 로그인 실패";
        }

        if (value.contains("TimeoutException")
                || value.contains("Expected condition failed")) {
            return "로그인 처리시간 초과";
        }

        if (value.contains("NoSuchElementException")) {
            return "로그인 화면 요소를 찾지 못함";
        }

        if (value.contains("WebDriverException")) {
            return "브라우저 처리 오류";
        }

        // 기타 예외도 첫 줄만 사용하고 Selenium 상세정보는 제거
        int newLine = value.indexOf('\n');
        if (newLine >= 0) {
            value = value.substring(0, newLine).trim();
        }

        if (value.length() > 120) {
            value = value.substring(0, 120) + "...";
        }

        return value;
    }

    private static String formatBusinessNumber(String businessNumber) {

        String value = onlyNumber(businessNumber);

        if (value.length() != 10) {
            return value;
        }

        return value.substring(0, 3)
                + "-"
                + value.substring(3, 5)
                + "-"
                + value.substring(5);
    }

    private static File mergeExcelFiles(
            List<File> sourceFiles,
            int year,
            int quarter,
            File downloadFolder) throws Exception {

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());

        File outputFile = new File(
                downloadFolder,
                "분류내려받기_"
                + year
                + "년_"
                + quarter
                + "분기_"
                + timestamp
                + ".xls"
        );

        File firstFile = sourceFiles.get(0);

        try (
                FileInputStream firstFis =
                        new FileInputStream(firstFile);
                Workbook targetWorkbook =
                        new HSSFWorkbook(firstFis)
        ) {

            Sheet targetSheet = targetWorkbook.getSheetAt(0);
            int targetRowIndex = targetSheet.getLastRowNum() + 1;

            // 첫 번째 파일은 워크북 전체를 기준 파일로 사용한다.
            // 두 번째 파일부터 3행(index 2) 이후의 DATA만 이어 붙인다.
            for (int fileIndex = 1;
                 fileIndex < sourceFiles.size();
                 fileIndex++) {

                File sourceFile = sourceFiles.get(fileIndex);

                try (
                        FileInputStream sourceFis =
                                new FileInputStream(sourceFile);
                        Workbook sourceWorkbook =
                                new HSSFWorkbook(sourceFis)
                ) {

                    Sheet sourceSheet = sourceWorkbook.getSheetAt(0);
                    Map<Short, CellStyle> styleMap =
                            new HashMap<Short, CellStyle>();

                    for (int sourceRowIndex = SOURCE_DATA_START_ROW;
                         sourceRowIndex <= sourceSheet.getLastRowNum();
                         sourceRowIndex++) {

                        Row sourceRow = sourceSheet.getRow(sourceRowIndex);

                        if (sourceRow == null || isEmptyRow(sourceRow)) {
                            continue;
                        }

                        Row targetRow = targetSheet.createRow(targetRowIndex++);
                        targetRow.setHeight(sourceRow.getHeight());

                        short firstCell = sourceRow.getFirstCellNum();
                        short lastCell = sourceRow.getLastCellNum();

                        if (firstCell < 0 || lastCell < 0) {
                            continue;
                        }

                        for (int col = firstCell; col < lastCell; col++) {
                            Cell sourceCell = sourceRow.getCell(col);
                            if (sourceCell == null) {
                                continue;
                            }

                            Cell targetCell = targetRow.createCell(col);

                            copyCellStyle(
                                    sourceCell,
                                    targetCell,
                                    targetWorkbook,
                                    styleMap
                            );

                            copyCellValue(sourceCell, targetCell);
                        }
                    }
                }
            }

            try (
                    FileOutputStream fos =
                            new FileOutputStream(outputFile)
            ) {
                targetWorkbook.write(fos);
            }
        }

        System.out.println();
        System.out.println("======================================");
        System.out.println("[MERGE-SUCCESS] 분류내려받기 병합 완료");
        System.out.println("최종 파일 = " + outputFile.getAbsolutePath());
        System.out.println("======================================");

        return outputFile;
    }

    private static void copyCellStyle(
            Cell sourceCell,
            Cell targetCell,
            Workbook targetWorkbook,
            Map<Short, CellStyle> styleMap) {

        CellStyle sourceStyle = sourceCell.getCellStyle();
        if (sourceStyle == null) {
            return;
        }

        short styleIndex = sourceStyle.getIndex();
        CellStyle targetStyle = styleMap.get(styleIndex);

        if (targetStyle == null) {
            targetStyle = targetWorkbook.createCellStyle();
            targetStyle.cloneStyleFrom(sourceStyle);
            styleMap.put(styleIndex, targetStyle);
        }

        targetCell.setCellStyle(targetStyle);
    }

    private static void copyCellValue(
            Cell sourceCell,
            Cell targetCell) {

        CellType cellType = sourceCell.getCellType();

        if (cellType == CellType.STRING) {
            targetCell.setCellValue(sourceCell.getStringCellValue());

        } else if (cellType == CellType.NUMERIC) {
            targetCell.setCellValue(sourceCell.getNumericCellValue());

        } else if (cellType == CellType.BOOLEAN) {
            targetCell.setCellValue(sourceCell.getBooleanCellValue());

        } else if (cellType == CellType.FORMULA) {
            targetCell.setCellFormula(sourceCell.getCellFormula());

        } else if (cellType == CellType.ERROR) {
            targetCell.setCellErrorValue(sourceCell.getErrorCellValue());

        } else {
            // BLANK 또는 기타 타입은 빈 셀 유지
        }
    }

    private static boolean isEmptyRow(Row row) {

        short firstCell = row.getFirstCellNum();
        short lastCell = row.getLastCellNum();

        if (firstCell < 0 || lastCell < 0) {
            return true;
        }

        for (int col = firstCell; col < lastCell; col++) {
            Cell cell = row.getCell(col);
            if (cell == null) {
                continue;
            }

            if (cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }

        return true;
    }

    private static void validateParameter(
            HometaxMergeParameter parameter,
            int year,
            int quarter) {

        if (parameter == null) {
            throw new IllegalArgumentException(
                    "홈택스 다운로드 파라미터가 null입니다."
            );
        }

        if (isEmpty(parameter.getHometaxId())) {
            throw new IllegalArgumentException("홈택스 아이디가 없습니다.");
        }

        if (isEmpty(parameter.getHometaxPassword())) {
            throw new IllegalArgumentException("홈택스 비밀번호가 없습니다.");
        }

        if (parameter.getJuminFirst6() == null
                || !parameter.getJuminFirst6().matches("[0-9]{6}")) {
            throw new IllegalArgumentException(
                    "주민등록번호 앞 6자리가 올바르지 않습니다."
            );
        }

        if (parameter.getJumin7th() == null
                || !parameter.getJumin7th().matches("[0-9]")) {
            throw new IllegalArgumentException(
                    "주민등록번호 7번째 숫자가 올바르지 않습니다."
            );
        }

        String businessNumber = onlyNumber(parameter.getBusinessNumber());
        if (!businessNumber.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "사업자등록번호가 올바르지 않습니다."
            );
        }

        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("조회연도가 올바르지 않습니다.");
        }

        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("분기가 올바르지 않습니다.");
        }
    }

    private static String rootMessage(Throwable throwable) {

        Throwable current = throwable;
        Throwable last = throwable;

        while (current != null) {
            last = current;
            current = current.getCause();
        }

        String message = last == null ? null : last.getMessage();
        if (message == null || message.trim().length() == 0) {
            return throwable.getClass().getSimpleName();
        }

        return message;
    }

    private static boolean sameFile(File file1, File file2) {
        try {
            return file1.getCanonicalFile().equals(file2.getCanonicalFile());
        } catch (Exception e) {
            return file1.getAbsolutePath().equals(file2.getAbsolutePath());
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private static String onlyNumber(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }
}
