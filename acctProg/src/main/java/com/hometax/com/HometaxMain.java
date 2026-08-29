package com.hometax.com;

import java.io.File;
import java.util.List;

import org.openqa.selenium.WebDriver;

/**
 * Controller/Service에서 전달받은 로그인 정보로 홈택스 다운로드를 실행한다.
 * Globals.Hometax* 프로퍼티는 더 이상 사용하지 않는다.
 */
public class HometaxMain {

    private static final String DOWNLOAD_DIR =
            "C:\\hometax_download";

    private HometaxMain() {
    }

    public static File execute(
            String hometaxId,
            String hometaxPassword,
            String juminFirst6,
            String jumin7th,
            String businessNumber,
            int year,
            int quarter) throws Exception {

        return execute(
                hometaxId,
                hometaxPassword,
                juminFirst6,
                jumin7th,
                businessNumber,
                year,
                quarter,
                new VatCardCondition2Classifier.BusinessContext()
        );
    }


    public static File execute(
            String hometaxId,
            String hometaxPassword,
            String juminFirst6,
            String jumin7th,
            String businessNumber,
            int year,
            int quarter,
            VatCardCondition2Classifier.BusinessContext condition2BusinessContext) throws Exception {

        return execute(
                hometaxId,
                hometaxPassword,
                juminFirst6,
                jumin7th,
                businessNumber,
                year,
                quarter,
                condition2BusinessContext,
                null
        );
    }


    public static File execute(
            String hometaxId,
            String hometaxPassword,
            String juminFirst6,
            String jumin7th,
            String businessNumber,
            int year,
            int quarter,
            VatCardCondition2Classifier.BusinessContext condition2BusinessContext,
            VatCardDownloadClassifier downloadClassifier) throws Exception {

        WebDriver driver = null;

        try {
            validateLoginParameter(
                    hometaxId,
                    hometaxPassword,
                    juminFirst6,
                    jumin7th,
                    businessNumber,
                    year,
                    quarter
            );

            if (HometaxProgressTracker.isCurrentJobCancelled()) {
                throw new RuntimeException("홈택스 작업 취소됨");
            }

            driver = HometaxLogin.login(
                    hometaxId,
                    hometaxPassword,
                    juminFirst6,
                    jumin7th,
                    DOWNLOAD_DIR
            );

            if (HometaxProgressTracker.isCurrentJobCancelled()) {
                throw new RuntimeException("홈택스 작업 취소됨");
            }

            HometaxService hometax =
                    new HometaxService(
                            driver,
                            DOWNLOAD_DIR
                    );

            File excelFile =
                    hometax.downloadExcel(
                            year,
                            quarter,
                            businessNumber
                    );

            if (HometaxProgressTracker.isCurrentJobCancelled()) {
                throw new RuntimeException("홈택스 작업 취소됨");
            }

            // 기존 내려받기 기능은 기존 로직 그대로 유지
            return ExcelTitleCopy.copyExcelByTitle(
                    excelFile,
                    condition2BusinessContext,
                    downloadClassifier
            );

        } finally {
            HometaxProgressTracker.unregisterCurrentDriver(
                    driver
            );

            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("Chrome 종료 완료");
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 분류내려받기 전용.
     * ExcelTitleCopy는 호출하지 않는다.
     * 선택한 여러 기업의 홈택스 원본 XLS를 최종 XLSX 파일로 병합한다.
     * 데이터가 많으면 001.xlsx, 002.xlsx 형식으로 자동 분할한다.
     */
    public static List<File> executeMerged(
            List<HometaxMergeParameter> parameters,
            int year,
            int quarter,
            String jobId) throws Exception {

        if (parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException(
                    "분류내려받기 대상이 없습니다."
            );
        }

        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException(
                    "조회연도가 올바르지 않습니다."
            );
        }

        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException(
                    "분기가 올바르지 않습니다."
            );
        }

        return HometaxMergeService.execute(
                parameters,
                year,
                quarter,
                DOWNLOAD_DIR,
                jobId
        );
    }

    private static void validateLoginParameter(
            String hometaxId,
            String hometaxPassword,
            String juminFirst6,
            String jumin7th,
            String businessNumber,
            int year,
            int quarter) {

        if (isEmpty(hometaxId)) {
            throw new IllegalArgumentException("홈택스 아이디가 없습니다.");
        }

        if (isEmpty(hometaxPassword)) {
            throw new IllegalArgumentException("홈택스 비밀번호가 없습니다.");
        }

        if (juminFirst6 == null
                || !juminFirst6.matches("[0-9]{6}")) {
            throw new IllegalArgumentException(
                    "주민등록번호 앞 6자리가 올바르지 않습니다."
            );
        }

        if (jumin7th == null
                || !jumin7th.matches("[0-9]")) {
            throw new IllegalArgumentException(
                    "주민등록번호 7번째 숫자가 올바르지 않습니다."
            );
        }

        if (isEmpty(businessNumber)
                || !businessNumber
                        .replaceAll("[^0-9]", "")
                        .matches("[0-9]{10}")) {
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

    private static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
