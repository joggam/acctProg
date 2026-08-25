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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
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

    public static List<File> execute(
            List<HometaxMergeParameter> parameters,
            int year,
            int quarter,
            String downloadDir,
            String jobId) throws Exception {

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
        int totalSuccessRowCount = 0;
        List<String> successMessages = new ArrayList<String>();
        List<String> loginFailMessages = new ArrayList<String>();
        List<String> downloadFailMessages = new ArrayList<String>();

        for (HometaxMergeParameter parameter : parameters) {

            HometaxProgressTracker.setCurrent(
                    jobId,
                    parameter.getCompanyName()
            );

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

                    String message = getDetailedLoginFailReason(loginException);

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

                int rowCount =
                        countExcelDataRows(
                                downloadedFile
                        );

                if (rowCount >= 0) {
                    totalSuccessRowCount += rowCount;
                }

                successMessages.add(
                        buildSuccessMessage(
                                parameter,
                                rowCount
                        )
                );

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

                HometaxProgressTracker.completeOne(
                        jobId
                );
            }
        }

        File successFile = null;

        if (!successMessages.isEmpty()) {
            successFile = writeSuccessText(
                    successMessages,
                    year,
                    quarter,
                    downloadFolder,
                    totalSuccessRowCount
            );

            System.out.println(
                    "[MERGE-SUCCESS-TXT] "
                    + successFile.getAbsolutePath()
            );
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

            // 분류내려받기 과정에서 Chrome/홈택스가 부수적으로 생성한
            // downloads.htm 계열 파일을 정리한다.
            deleteDownloadsHtml(
                    downloadFolder
            );

            // =========================================================
            // 모든 대상이 "조회된 내역이 없습니다."인 경우는
            // 시스템 오류가 아니라 정상적인 조회 결과로 처리한다.
            //
            // - 로그인 실패가 없어야 함
            // - 다운로드 실패가 1건 이상 있어야 함
            // - 모든 다운로드 실패 사유가 "조회된 내역이 없습니다."여야 함
            //
            // 실패 TXT는 위에서 이미 생성했으므로 그대로 남긴다.
            // Controller에서는 빈 List를 받아 "조회된 내역 없음"으로 안내한다.
            // =========================================================
            if (loginFailMessages.isEmpty()
                    && allDownloadFailuresAreNoData(downloadFailMessages)) {

                System.out.println(
                        "[MERGE-NO-DATA] 모든 대상의 조회된 내역이 없습니다."
                );

                return new ArrayList<File>();
            }

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

        List<File> mergedFiles = mergeExcelFiles(
                downloadedFiles,
                year,
                quarter,
                downloadFolder
        );

        // 최종 병합 성공 후 개별 홈택스 다운로드 원본 XLS 제거.
        // 최종 결과는 분할된 XLSX 파일들만 남긴다.
        for (File file : downloadedFiles) {
            if (file == null) {
                continue;
            }

            try {
                if (file.exists()) {
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

        // 분류내려받기 과정에서 생성된 불필요한 downloads.htm 계열만 제거.
        // 최종 XLSX / 성공·실패 TXT는 삭제하지 않는다.
        deleteDownloadsHtml(
                downloadFolder
        );

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

        return mergedFiles;
    }

    /**
     * 다운로드 실패 목록이 전부 "조회된 내역이 없습니다."인지 확인한다.
     */
    private static boolean allDownloadFailuresAreNoData(
            List<String> downloadFailMessages) {

        if (downloadFailMessages == null
                || downloadFailMessages.isEmpty()) {
            return false;
        }

        for (String failMessage : downloadFailMessages) {

            if (failMessage == null
                    || !failMessage.contains("조회된 내역이 없습니다.")) {
                return false;
            }
        }

        return true;
    }


    /**
     * 분류내려받기 중 Chrome/홈택스가 부수적으로 생성하는
     * downloads.htm, downloads (1).htm ... 및
     * downloads.htm.crdownload, downloads (1).htm.crdownload ...
     * 파일만 삭제한다.
     *
     * .crdownload는 Chrome이 파일을 잡고 있는 순간 바로 삭제가 안 될 수 있어
     * 최대 5회, 500ms 간격으로 재시도한다.
     *
     * 일반 내려받기에는 이 메서드가 호출되지 않는다.
     */
    private static void deleteDownloadsHtml(
            File downloadFolder) {

        if (downloadFolder == null
                || !downloadFolder.exists()
                || !downloadFolder.isDirectory()) {

            return;
        }

        File[] files =
                downloadFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file == null
                    || !file.isFile()) {

                continue;
            }

            String fileName =
                    file.getName();

            boolean target =
                    fileName.matches(
                            "(?i)^downloads(?: \\(\\d+\\))?\\.htm(?:\\.crdownload)?$"
                    );

            if (!target) {
                continue;
            }

            boolean deleted = false;

            for (int retry = 1;
                 retry <= 5;
                 retry++) {

                try {

                    if (!file.exists()) {
                        deleted = true;
                        break;
                    }

                    deleted =
                            file.delete();

                    if (deleted) {
                        break;
                    }

                    // Chrome이 .crdownload 파일 핸들을 아직 놓지 않은 경우 대기 후 재시도
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                } catch (Exception e) {

                    if (retry == 5) {

                        System.out.println(
                                "[MERGE-CLEAN-HTML-FAIL] "
                                + fileName
                                + " / "
                                + e.getMessage()
                        );
                    }
                }
            }

            System.out.println(
                    "[MERGE-CLEAN-HTML] "
                    + fileName
                    + " 삭제="
                    + deleted
            );
        }
    }


    /**
     * 분류내려받기에서 성공한 업체 정보를 UTF-8 TXT로 저장한다.
     * 비밀번호와 주민등록번호는 기록하지 않는다.
     */
    private static File writeSuccessText(
            List<String> successMessages,
            int year,
            int quarter,
            File downloadFolder,
            int totalSuccessRowCount) throws Exception {

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());

        File outputFile = new File(
                downloadFolder,
                "홈택스_분류내려받기성공_"
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
            writer.write("홈택스 분류내려받기 성공 업체");
            writer.newLine();
            writer.write("조회기간 : "
                    + year + "년 " + quarter + "분기");
            writer.newLine();
            writer.write("성공건수 : "
                    + successMessages.size() + "건");
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();
            writer.write("상호명 | 아이디 | 사업자등록번호 | 다운로드 ROW수");
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();

            for (String successMessage : successMessages) {
                writer.write(successMessage);
                writer.newLine();
            }

            writer.write(
                    "============================================================"
            );
            writer.newLine();
            writer.write(
                    "총 성공 업체 : "
                    + successMessages.size()
                    + "건"
            );
            writer.newLine();
            writer.write(
                    "총 다운로드 ROW수 : "
                    + totalSuccessRowCount
                    + "건"
            );
            writer.newLine();
        }

        return outputFile;
    }

    private static String buildSuccessMessage(
            HometaxMergeParameter parameter,
            int rowCount) {

        String companyName = parameter.getCompanyName();

        if (companyName == null || companyName.trim().length() == 0) {
            companyName = "-";
        }

        String hometaxId = parameter.getHometaxId();

        if (hometaxId == null || hometaxId.trim().length() == 0) {
            hometaxId = "-";
        }

        String rowCountText =
                rowCount >= 0
                ? rowCount + "건"
                : "확인실패";

        return companyName
                + " | "
                + hometaxId
                + " | "
                + formatBusinessNumber(parameter.getBusinessNumber())
                + " | "
                + rowCountText;
    }


    /**
     * 홈택스 XLS의 데이터 행 수를 계산한다.
     * 병합 로직과 동일하게 3행(index 2)부터 DATA로 보고 빈 행은 제외한다.
     * 오류가 발생해도 다운로드 성공 업체를 실패 처리하지 않도록 -1을 반환한다.
     */
    private static int countExcelDataRows(File excelFile) {

        if (excelFile == null || !excelFile.exists()) {
            return -1;
        }

        try (
                FileInputStream fis =
                        new FileInputStream(excelFile);
                Workbook workbook =
                        new HSSFWorkbook(fis)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;

            for (int rowIndex = SOURCE_DATA_START_ROW;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                count++;
            }

            return count;

        } catch (Exception e) {

            System.out.println(
                    "[MERGE-ROW-COUNT-FAIL] "
                    + excelFile.getAbsolutePath()
                    + " / "
                    + e.getMessage()
            );

            return -1;
        }
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

        if (value.contains("홈택스 로그인 화면 로딩 실패(ID 입력란 미표시)")) {
            return "홈택스 로그인 화면 로딩 실패(ID 입력란 미표시)";
        }

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

    /**
     * 분류내려받기 최종 병합.
     *
     * - 홈택스 개별 원본은 .xls 그대로 읽는다.
     * - 최종 결과만 .xlsx로 생성한다.
     * - XLSX 한 파일당 DATA 최대 1,000,000 ROW로 분할한다.
     * - 결과 파일명은 ..._001.xlsx, ..._002.xlsx 형식이다.
     * - 각 분할 파일에는 첫 번째 원본의 상단 2행(헤더)을 다시 복사한다.
     */
    private static List<File> mergeExcelFiles(
            List<File> sourceFiles,
            int year,
            int quarter,
            File downloadFolder) throws Exception {

        final int MAX_DATA_ROWS_PER_XLSX = 1000000;

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());

        List<File> outputFiles =
                new ArrayList<File>();

        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return outputFiles;
        }

        // 첫 번째 원본 XLS의 헤더 2행을 각 분할 XLSX에 반복 사용한다.
        File headerSourceFile = sourceFiles.get(0);

        MergeXlsxPart currentPart =
                createNewXlsxPart(
                        headerSourceFile,
                        downloadFolder,
                        year,
                        quarter,
                        timestamp,
                        1
                );

        int partIndex = 1;
        int currentDataRowCount = 0;

        try {

            for (int fileIndex = 0;
                 fileIndex < sourceFiles.size();
                 fileIndex++) {

                File sourceFile =
                        sourceFiles.get(fileIndex);

                try (
                        FileInputStream sourceFis =
                                new FileInputStream(sourceFile);
                        Workbook sourceWorkbook =
                                new HSSFWorkbook(sourceFis)
                ) {

                    Sheet sourceSheet =
                            sourceWorkbook.getSheetAt(0);

                    Map<Short, CellStyle> styleMap =
                            new HashMap<Short, CellStyle>();

                    Map<Short, Font> fontMap =
                            new HashMap<Short, Font>();

                    for (int sourceRowIndex = SOURCE_DATA_START_ROW;
                         sourceRowIndex <= sourceSheet.getLastRowNum();
                         sourceRowIndex++) {

                        Row sourceRow =
                                sourceSheet.getRow(sourceRowIndex);

                        if (sourceRow == null
                                || isEmptyRow(sourceRow)) {

                            continue;
                        }

                        // 현재 XLSX가 100만 DATA ROW에 도달하면
                        // 저장 후 다음 002, 003... 파일을 만든다.
                        if (currentDataRowCount
                                >= MAX_DATA_ROWS_PER_XLSX) {

                            closeAndSaveXlsxPart(
                                    currentPart
                            );

                            outputFiles.add(
                                    currentPart.outputFile
                            );

                            partIndex++;

                            currentPart =
                                    createNewXlsxPart(
                                            headerSourceFile,
                                            downloadFolder,
                                            year,
                                            quarter,
                                            timestamp,
                                            partIndex
                                    );

                            currentDataRowCount = 0;

                            styleMap =
                                    new HashMap<Short, CellStyle>();

                            fontMap =
                                    new HashMap<Short, Font>();
                        }

                        Row targetRow =
                                currentPart.sheet.createRow(
                                        currentPart.nextRowIndex++
                                );

                        targetRow.setHeight(
                                sourceRow.getHeight()
                        );

                        copyRowToXlsx(
                                sourceRow,
                                targetRow,
                                sourceWorkbook,
                                currentPart.xssfWorkbook,
                                styleMap,
                                fontMap
                        );

                        currentDataRowCount++;
                    }
                }
            }

            closeAndSaveXlsxPart(
                    currentPart
            );

            outputFiles.add(
                    currentPart.outputFile
            );

        } catch (Exception e) {

            if (currentPart != null) {
                try {
                    currentPart.workbook.close();
                } catch (Exception ignored) {
                }

                try {
                    currentPart.workbook.dispose();
                } catch (Exception ignored) {
                }
            }

            throw e;
        }


        System.out.println();
        System.out.println(
                "======================================"
        );
        System.out.println(
                "[MERGE-SUCCESS] 분류내려받기 XLSX 병합 완료"
        );
        System.out.println(
                "생성 파일 수 = "
                + outputFiles.size()
        );

        for (File outputFile : outputFiles) {

            System.out.println(
                    "최종 파일 = "
                    + outputFile.getAbsolutePath()
            );
        }

        System.out.println(
                "======================================"
        );

        return outputFiles;
    }


    private static MergeXlsxPart createNewXlsxPart(
            File headerSourceFile,
            File downloadFolder,
            int year,
            int quarter,
            String timestamp,
            int partIndex) throws Exception {

        String index =
                String.format(
                        "%03d",
                        partIndex
                );

        File outputFile =
                new File(
                        downloadFolder,
                        "분류내려받기_"
                        + year
                        + "년_"
                        + quarter
                        + "분기_"
                        + timestamp
                        + "_"
                        + index
                        + ".xlsx"
                );

        XSSFWorkbook xssfWorkbook =
                new XSSFWorkbook();

        // 메모리에 최근 500행만 유지.
        // 오래된 행은 임시파일로 내려 대용량 XLSX 메모리 사용량을 줄인다.
        SXSSFWorkbook workbook =
                new SXSSFWorkbook(
                        xssfWorkbook,
                        500
                );

        workbook.setCompressTempFiles(true);

        Sheet targetSheet =
                workbook.createSheet(
                        "분류내려받기"
                );

        MergeXlsxPart part =
                new MergeXlsxPart();

        part.outputFile = outputFile;
        part.workbook = workbook;
        part.xssfWorkbook = xssfWorkbook;
        part.sheet = targetSheet;
        part.nextRowIndex = 0;


        // 첫 번째 원본 XLS에서 상단 헤더 2행과 열 너비를 복사한다.
        try (
                FileInputStream fis =
                        new FileInputStream(
                                headerSourceFile
                        );
                Workbook headerWorkbook =
                        new HSSFWorkbook(fis)
        ) {

            Sheet headerSheet =
                    headerWorkbook.getSheetAt(0);

            int maxColumn =
                    findMaxColumnCount(
                            headerSheet
                    );

            for (int col = 0;
                 col < maxColumn;
                 col++) {

                targetSheet.setColumnWidth(
                        col,
                        headerSheet.getColumnWidth(col)
                );
            }

            Map<Short, CellStyle> styleMap =
                    new HashMap<Short, CellStyle>();

            Map<Short, Font> fontMap =
                    new HashMap<Short, Font>();

            int headerLastRow =
                    Math.min(
                            SOURCE_DATA_START_ROW - 1,
                            headerSheet.getLastRowNum()
                    );

            for (int rowIndex = 0;
                 rowIndex <= headerLastRow;
                 rowIndex++) {

                Row sourceRow =
                        headerSheet.getRow(rowIndex);

                if (sourceRow == null) {
                    part.nextRowIndex++;
                    continue;
                }

                Row targetRow =
                        targetSheet.createRow(
                                part.nextRowIndex++
                        );

                targetRow.setHeight(
                        sourceRow.getHeight()
                );

                copyRowToXlsx(
                        sourceRow,
                        targetRow,
                        headerWorkbook,
                        xssfWorkbook,
                        styleMap,
                        fontMap
                );
            }


            // 헤더 영역 안에 포함된 병합 셀만 복사한다.
            for (int i = 0;
                 i < headerSheet.getNumMergedRegions();
                 i++) {

                CellRangeAddress region =
                        headerSheet.getMergedRegion(i);

                if (region.getLastRow()
                        < SOURCE_DATA_START_ROW) {

                    targetSheet.addMergedRegion(
                            new CellRangeAddress(
                                    region.getFirstRow(),
                                    region.getLastRow(),
                                    region.getFirstColumn(),
                                    region.getLastColumn()
                            )
                    );
                }
            }
        }

        return part;
    }


    private static void closeAndSaveXlsxPart(
            MergeXlsxPart part) throws Exception {

        if (part == null) {
            return;
        }

        try (
                FileOutputStream fos =
                        new FileOutputStream(
                                part.outputFile
                        )
        ) {

            part.workbook.write(
                    fos
            );

        } finally {

            try {
                part.workbook.close();
            } finally {
                part.workbook.dispose();
            }
        }

        System.out.println(
                "[MERGE-XLSX-PART] "
                + part.outputFile.getAbsolutePath()
        );
    }


    private static void copyRowToXlsx(
            Row sourceRow,
            Row targetRow,
            Workbook sourceWorkbook,
            XSSFWorkbook targetWorkbook,
            Map<Short, CellStyle> styleMap,
            Map<Short, Font> fontMap) {

        short firstCell =
                sourceRow.getFirstCellNum();

        short lastCell =
                sourceRow.getLastCellNum();

        if (firstCell < 0
                || lastCell < 0) {

            return;
        }

        for (int col = firstCell;
             col < lastCell;
             col++) {

            Cell sourceCell =
                    sourceRow.getCell(col);

            if (sourceCell == null) {
                continue;
            }

            Cell targetCell =
                    targetRow.createCell(col);

            copyCellStyleToXlsx(
                    sourceCell,
                    targetCell,
                    sourceWorkbook,
                    targetWorkbook,
                    styleMap,
                    fontMap
            );

            copyCellValue(
                    sourceCell,
                    targetCell
            );
        }
    }


    private static int findMaxColumnCount(
            Sheet sheet) {

        int maxColumn = 0;

        for (int rowIndex = 0;
             rowIndex <= sheet.getLastRowNum();
             rowIndex++) {

            Row row =
                    sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            short lastCell =
                    row.getLastCellNum();

            if (lastCell > maxColumn) {
                maxColumn = lastCell;
            }
        }

        return maxColumn;
    }


    private static void copyCellStyleToXlsx(
            Cell sourceCell,
            Cell targetCell,
            Workbook sourceWorkbook,
            XSSFWorkbook targetWorkbook,
            Map<Short, CellStyle> styleMap,
            Map<Short, Font> fontMap) {

        CellStyle sourceStyle =
                sourceCell.getCellStyle();

        if (sourceStyle == null) {
            return;
        }

        short styleIndex =
                sourceStyle.getIndex();

        CellStyle targetStyle =
                styleMap.get(styleIndex);

        if (targetStyle == null) {

            targetStyle =
                    targetWorkbook.createCellStyle();

            targetStyle.setAlignment(
                    sourceStyle.getAlignment()
            );

            targetStyle.setVerticalAlignment(
                    sourceStyle.getVerticalAlignment()
            );

            targetStyle.setBorderTop(
                    sourceStyle.getBorderTop()
            );

            targetStyle.setBorderBottom(
                    sourceStyle.getBorderBottom()
            );

            targetStyle.setBorderLeft(
                    sourceStyle.getBorderLeft()
            );

            targetStyle.setBorderRight(
                    sourceStyle.getBorderRight()
            );

            targetStyle.setTopBorderColor(
                    sourceStyle.getTopBorderColor()
            );

            targetStyle.setBottomBorderColor(
                    sourceStyle.getBottomBorderColor()
            );

            targetStyle.setLeftBorderColor(
                    sourceStyle.getLeftBorderColor()
            );

            targetStyle.setRightBorderColor(
                    sourceStyle.getRightBorderColor()
            );

            targetStyle.setFillPattern(
                    sourceStyle.getFillPattern()
            );

            targetStyle.setFillForegroundColor(
                    sourceStyle.getFillForegroundColor()
            );

            targetStyle.setFillBackgroundColor(
                    sourceStyle.getFillBackgroundColor()
            );

            targetStyle.setWrapText(
                    sourceStyle.getWrapText()
            );

            targetStyle.setRotation(
                    sourceStyle.getRotation()
            );

            targetStyle.setIndention(
                    sourceStyle.getIndention()
            );

            targetStyle.setLocked(
                    sourceStyle.getLocked()
            );

            targetStyle.setHidden(
                    sourceStyle.getHidden()
            );


            String dataFormatString =
                    sourceStyle.getDataFormatString();

            if (dataFormatString != null
                    && dataFormatString.trim().length() > 0) {

                targetStyle.setDataFormat(
                        targetWorkbook
                                .createDataFormat()
                                .getFormat(
                                        dataFormatString
                                )
                );
            }


            short sourceFontIndex =
                    (short) sourceStyle
                            .getFontIndexAsInt();

            Font targetFont =
                    fontMap.get(
                            sourceFontIndex
                    );

            if (targetFont == null) {

                Font sourceFont =
                        sourceWorkbook.getFontAt(
                                sourceFontIndex
                        );

                targetFont =
                        targetWorkbook.createFont();

                targetFont.setFontName(
                        sourceFont.getFontName()
                );

                targetFont.setFontHeight(
                        sourceFont.getFontHeight()
                );

                targetFont.setBold(
                        sourceFont.getBold()
                );

                targetFont.setItalic(
                        sourceFont.getItalic()
                );

                targetFont.setStrikeout(
                        sourceFont.getStrikeout()
                );

                targetFont.setUnderline(
                        sourceFont.getUnderline()
                );

                targetFont.setTypeOffset(
                        sourceFont.getTypeOffset()
                );

                targetFont.setColor(
                        sourceFont.getColor()
                );

                fontMap.put(
                        sourceFontIndex,
                        targetFont
                );
            }

            targetStyle.setFont(
                    targetFont
            );

            styleMap.put(
                    styleIndex,
                    targetStyle
            );
        }

        targetCell.setCellStyle(
                targetStyle
        );
    }


    private static class MergeXlsxPart {

        private File outputFile;

        private SXSSFWorkbook workbook;

        private XSSFWorkbook xssfWorkbook;

        private Sheet sheet;

        private int nextRowIndex;
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

    private static String getDetailedLoginFailReason(
            Throwable throwable) {

        if (throwable == null) {
            return "로그인 실패";
        }

        Throwable current = throwable;
        int depth = 0;
        String fallback = null;

        while (current != null && depth < 20) {

            String message = current.getMessage();

            if (message != null && message.trim().length() > 0) {

                String value = message.trim();

                if (value.contains(
                        "홈택스 로그인 화면 로딩 실패(ID 입력란 미표시)")) {
                    return "홈택스 로그인 화면 로딩 실패(ID 입력란 미표시)";
                }

                if (value.contains(
                        "입력하신 아이디, 비밀번호 또는 주민번호가 정확하지 않습니다")) {
                    return "입력하신 아이디, 비밀번호 또는 주민번호가 정확하지 않습니다";
                }

                fallback = value;
            }

            current = current.getCause();
            depth++;
        }

        if (fallback != null) {
            return fallback;
        }

        return rootMessage(throwable);
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
