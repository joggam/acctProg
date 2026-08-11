package com.hometax.com;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelTitleCopy {

    // =========================================================
    // 파일 경로
    // =========================================================

    private static final String SOURCE_FILE =
            "C:\\hometax_download\\법인설립연구소_679-19-02150_20251231.xls";

    private static final String TARGET_FILE =
            "C:\\hometax_download\\신용카드매입자료_업로드.xls";


    // =========================================================
    // 행 번호
    //
    // POI는 0부터 시작
    //
    // 원본
    // 2행 = TITLE
    // 3행부터 = DATA
    //
    // 대상
    // 4행 = 사업자 정보
    // 8행 = TITLE
    // 10행부터 = DATA
    // =========================================================

    private static final int SOURCE_TITLE_ROW = 1;
    private static final int SOURCE_DATA_START_ROW = 2;

    private static final int TARGET_INFO_ROW = 3;
    private static final int TARGET_TITLE_ROW = 7;
    private static final int TARGET_DATA_START_ROW = 9;


    public static void main(String[] args) {

        try {

            copyExcelByTitle();

            System.out.println();
            System.out.println("======================================");
            System.out.println("엑셀 데이터 복사 완료");
            System.out.println("======================================");

        } catch (Exception e) {

            System.err.println();
            System.err.println("======================================");
            System.err.println("엑셀 데이터 복사 실패");
            System.err.println("======================================");

            e.printStackTrace();
        }
    }


    public static void copyExcelByTitle() throws Exception {

        Path sourcePath = Paths.get(SOURCE_FILE);
        Path targetPath = Paths.get(TARGET_FILE);


        // =====================================================
        // 파일 존재 확인
        // =====================================================

        if (!Files.exists(sourcePath)) {
            throw new RuntimeException(
                    "원본 파일을 찾을 수 없습니다.\n" + SOURCE_FILE
            );
        }

        if (!Files.exists(targetPath)) {
            throw new RuntimeException(
                    "대상 파일을 찾을 수 없습니다.\n" + TARGET_FILE
            );
        }


        System.out.println("[EXCEL-1] 파일 확인 완료");


        // =====================================================
        // 파일명에서 사업자 정보 추출
        // =====================================================

        BusinessInfo businessInfo =
                parseBusinessInfo(sourcePath);


        System.out.println(
                "[EXCEL-2] 사업자명 : "
                + businessInfo.getBusinessName()
        );

        System.out.println(
                "[EXCEL-3] 사업자번호 : "
                + businessInfo.getBusinessNumber()
        );

        System.out.println(
                "[EXCEL-4] 파일 날짜 : "
                + businessInfo.getDate()
        );


        try (
                FileInputStream sourceFis =
                        new FileInputStream(sourcePath.toFile());

                FileInputStream targetFis =
                        new FileInputStream(targetPath.toFile());

                Workbook sourceWorkbook =
                        new HSSFWorkbook(sourceFis);

                Workbook targetWorkbook =
                        new HSSFWorkbook(targetFis)
        ) {

            Sheet sourceSheet =
                    sourceWorkbook.getSheetAt(0);

            Sheet targetSheet =
                    targetWorkbook.getSheetAt(0);


            System.out.println("[EXCEL-5] 엑셀 파일 열기 완료");


            // =================================================
            // 사업자 정보 입력
            //
            // A4:B4 병합셀 = 사업자명
            // C4           = 사업자등록번호
            // =================================================

            writeBusinessInfo(
                    targetSheet,
                    businessInfo
            );


            System.out.println(
                    "[EXCEL-6] 사업자 정보 입력 완료"
            );

            System.out.println(
                    "          A4 = "
                    + businessInfo.getBusinessName()
            );

            System.out.println(
                    "          C4 = "
                    + businessInfo.getBusinessNumber()
            );


            // =================================================
            // TITLE 행 확인
            // =================================================

            Row sourceTitleRow =
                    sourceSheet.getRow(
                            SOURCE_TITLE_ROW
                    );

            Row targetTitleRow =
                    targetSheet.getRow(
                            TARGET_TITLE_ROW
                    );


            if (sourceTitleRow == null) {
                throw new RuntimeException(
                        "원본 파일의 2번째 행을 찾을 수 없습니다."
                );
            }

            if (targetTitleRow == null) {
                throw new RuntimeException(
                        "대상 파일의 8번째 행을 찾을 수 없습니다."
                );
            }


            DataFormatter formatter =
                    new DataFormatter();


            // =================================================
            // 대상 TITLE -> 열번호
            // =================================================

            Map<String, Integer> targetTitleMap =
                    new LinkedHashMap<>();


            for (
                    int col = targetTitleRow.getFirstCellNum();
                    col < targetTitleRow.getLastCellNum();
                    col++
            ) {

                Cell cell =
                        targetTitleRow.getCell(col);

                if (cell == null) {
                    continue;
                }


                String title =
                        normalizeTitle(
                                formatter.formatCellValue(cell)
                        );


                if (title.isEmpty()) {
                    continue;
                }


                targetTitleMap.putIfAbsent(
                        title,
                        col
                );
            }


            System.out.println(
                    "[EXCEL-7] 대상 TITLE 분석 완료"
            );


            // =================================================
            // 강제 TITLE 매핑
            //
            // 원본 -> 대상
            // =================================================

            Map<String, String> customTitleMapping =
                    new LinkedHashMap<>();


            customTitleMapping.put(
                    "카드사",
                    "신용카드사명(30자리이내)"
            );

            customTitleMapping.put(
                    "카드번호",
                    "신용카드번호(19자리이내)"
            );

            customTitleMapping.put(
                    "가맹점사업자번호",
                    "사업자등록번호"
            );

            customTitleMapping.put(
                    "가맹점명",
                    "거래처명(15자리이내)"
            );

            customTitleMapping.put(
                    "합계",
                    "합계금액"
            );

            customTitleMapping.put(
                    "가맹점유형",
                    "거래처유형"
            );


            // =================================================
            // 원본 열 -> 대상 열 매핑
            // =================================================

            Map<Integer, Integer> columnMapping =
                    new LinkedHashMap<>();

            Map<Integer, String> sourceTitleByColumn =
                    new LinkedHashMap<>();


            System.out.println();
            System.out.println(
                    "===== TITLE 매칭 결과 ====="
            );


            for (
                    int sourceCol =
                            sourceTitleRow.getFirstCellNum();

                    sourceCol <
                            sourceTitleRow.getLastCellNum();

                    sourceCol++
            ) {

                Cell sourceTitleCell =
                        sourceTitleRow.getCell(
                                sourceCol
                        );

                if (sourceTitleCell == null) {
                    continue;
                }


                String sourceTitle =
                        normalizeTitle(
                                formatter.formatCellValue(
                                        sourceTitleCell
                                )
                        );


                if (sourceTitle.isEmpty()) {
                    continue;
                }


                String targetTitle =
                        sourceTitle;


                if (
                        customTitleMapping
                                .containsKey(sourceTitle)
                ) {

                    targetTitle =
                            customTitleMapping.get(
                                    sourceTitle
                            );
                }


                Integer targetCol =
                        targetTitleMap.get(
                                targetTitle
                        );


                if (targetCol != null) {

                    columnMapping.put(
                            sourceCol,
                            targetCol
                    );

                    sourceTitleByColumn.put(
                            sourceCol,
                            sourceTitle
                    );


                    System.out.println(
                            "[MATCH] "
                            + sourceTitle
                            + " -> "
                            + targetTitle
                            + " | 원본 "
                            + (sourceCol + 1)
                            + "열 -> 대상 "
                            + (targetCol + 1)
                            + "열"
                    );

                } else {

                    System.out.println(
                            "[SKIP] "
                            + sourceTitle
                            + " -> 대상 TITLE 없음"
                    );
                }
            }


            if (columnMapping.isEmpty()) {
                throw new RuntimeException(
                        "일치하는 TITLE이 없습니다."
                );
            }


            System.out.println();
            System.out.println(
                    "[EXCEL-8] TITLE 매칭 완료 - "
                    + columnMapping.size()
                    + "개"
            );


            // =================================================
            // 고정값 대상 열 찾기
            // =================================================

            Integer cardTypeCol =
                    targetTitleMap.get(
                            normalizeTitle(
                                    "카드종류 (1자리)"
                            )
                    );

            Integer vatDeductionCol =
                    targetTitleMap.get(
                            normalizeTitle(
                                    "부가세공제여부"
                            )
                    );

            Integer vatTypeCol =
                    targetTitleMap.get(
                            normalizeTitle(
                                    "부가세유형 (2자리)"
                            )
                    );

            Integer accountCol =
                    targetTitleMap.get(
                            normalizeTitle(
                                    "계정과목"
                            )
                    );


            if (cardTypeCol == null) {
                throw new RuntimeException(
                        "대상 파일에서 '카드종류 (1자리)' TITLE을 찾을 수 없습니다."
                );
            }

            if (vatDeductionCol == null) {
                throw new RuntimeException(
                        "대상 파일에서 '부가세공제여부' TITLE을 찾을 수 없습니다."
                );
            }

            if (vatTypeCol == null) {
                throw new RuntimeException(
                        "대상 파일에서 '부가세유형 (2자리)' TITLE을 찾을 수 없습니다."
                );
            }

            if (accountCol == null) {
                throw new RuntimeException(
                        "대상 파일에서 '계정과목' TITLE을 찾을 수 없습니다."
                );
            }


            System.out.println(
                    "[EXCEL-9] 고정값 TITLE 확인 완료"
            );


            // =================================================
            // 데이터 복사
            // =================================================

            int sourceLastRow =
                    sourceSheet.getLastRowNum();

            int targetRowIndex =
                    TARGET_DATA_START_ROW;

            int copiedRowCount = 0;


            for (
                    int sourceRowIndex =
                            SOURCE_DATA_START_ROW;

                    sourceRowIndex <= sourceLastRow;

                    sourceRowIndex++
            ) {

                Row sourceRow =
                        sourceSheet.getRow(
                                sourceRowIndex
                        );


                if (
                        sourceRow == null
                        || isEmptyRow(
                                sourceRow,
                                columnMapping,
                                formatter
                        )
                ) {

                    continue;
                }


                Row targetRow =
                        targetSheet.getRow(
                                targetRowIndex
                        );


                if (targetRow == null) {

                    targetRow =
                            targetSheet.createRow(
                                    targetRowIndex
                            );


                    Row templateRow =
                            targetSheet.getRow(
                                    TARGET_DATA_START_ROW
                            );


                    if (templateRow != null) {

                        targetRow.setHeight(
                                templateRow.getHeight()
                        );
                    }
                }


                // =============================================
                // TITLE 매칭 데이터 복사
                // =============================================

                for (
                        Map.Entry<Integer, Integer> mapping
                        : columnMapping.entrySet()
                ) {

                    int sourceCol =
                            mapping.getKey();

                    int targetCol =
                            mapping.getValue();


                    Cell sourceCell =
                            sourceRow.getCell(
                                    sourceCol
                            );


                    Cell targetCell =
                            getOrCreateTargetCell(
                                    targetSheet,
                                    targetRow,
                                    targetCol
                            );


                    String sourceTitle =
                            sourceTitleByColumn.get(
                                    sourceCol
                            );


                    // =========================================
                    // 가맹점유형 -> 거래처유형
                    //
                    // 앞 2글자만 사용
                    // =========================================

                    if (
                            "가맹점유형"
                                    .equals(sourceTitle)
                    ) {

                        String value =
                                getCellAsString(
                                        sourceCell,
                                        formatter
                                );


                        if (value.length() > 2) {

                            value =
                                    value.substring(
                                            0,
                                            2
                                    );
                        }


                        targetCell.setCellValue(
                                value
                        );

                    } else {

                        copyCellValue(
                                sourceCell,
                                targetCell,
                                sourceWorkbook
                        );
                    }
                }


                // =============================================
                // 고정값 입력
                //
                // 숫자는 실제 NUMERIC으로 입력
                // =============================================

                // 카드종류 = 3
                setFixedNumberValue(
                        targetSheet,
                        targetRow,
                        cardTypeCol,
                        3
                );


                // 부가세공제여부 = 공제
                setFixedTextValue(
                        targetSheet,
                        targetRow,
                        vatDeductionCol,
                        "공제"
                );


                // 부가세유형 = 57
                setFixedNumberValue(
                        targetSheet,
                        targetRow,
                        vatTypeCol,
                        57
                );


                // 계정과목 = 830
                setFixedNumberValue(
                        targetSheet,
                        targetRow,
                        accountCol,
                        830
                );


                targetRowIndex++;
                copiedRowCount++;
            }


            System.out.println(
                    "[EXCEL-10] 데이터 복사 완료 - "
                    + copiedRowCount
                    + "건"
            );


            // =================================================
            // 임시 파일 저장
            // =================================================

            Path tempPath =
                    Paths.get(
                            TARGET_FILE
                            + ".tmp.xls"
                    );


            try (
                    FileOutputStream fos =
                            new FileOutputStream(
                                    tempPath.toFile()
                            )
            ) {

                targetWorkbook.write(fos);
            }


            // =================================================
            // 대상 파일 교체
            // =================================================

            Files.move(
                    tempPath,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            System.out.println(
                    "[EXCEL-11] 저장 완료"
            );

            System.out.println(
                    "저장 위치 : "
                    + TARGET_FILE
            );
        }
    }


    // =========================================================
    // 대상 셀 생성
    //
    // 대상 10행의 스타일을 기준으로 적용
    // =========================================================

    private static Cell getOrCreateTargetCell(
            Sheet targetSheet,
            Row targetRow,
            int targetCol
    ) {

        Cell targetCell =
                targetRow.getCell(
                        targetCol
                );


        if (targetCell == null) {

            targetCell =
                    targetRow.createCell(
                            targetCol
                    );


            Row templateRow =
                    targetSheet.getRow(
                            TARGET_DATA_START_ROW
                    );


            if (templateRow != null) {

                Cell templateCell =
                        templateRow.getCell(
                                targetCol
                        );


                if (
                        templateCell != null
                        && templateCell.getCellStyle() != null
                ) {

                    targetCell.setCellStyle(
                            templateCell.getCellStyle()
                    );
                }
            }
        }


        return targetCell;
    }


    // =========================================================
    // 고정 문자값 입력
    // =========================================================

    private static void setFixedTextValue(
            Sheet targetSheet,
            Row targetRow,
            int targetCol,
            String value
    ) {

        Cell cell =
                getOrCreateTargetCell(
                        targetSheet,
                        targetRow,
                        targetCol
                );


        cell.setCellValue(
                value
        );
    }


    // =========================================================
    // 고정 숫자값 입력
    //
    // String이 아니라 double로 저장하기 때문에
    // Excel에서 "숫자가 텍스트로 저장됨" 경고가 발생하지 않음
    // =========================================================

    private static void setFixedNumberValue(
            Sheet targetSheet,
            Row targetRow,
            int targetCol,
            double value
    ) {

        Cell cell =
                getOrCreateTargetCell(
                        targetSheet,
                        targetRow,
                        targetCol
                );


        cell.setCellValue(
                value
        );
    }


    // =========================================================
    // 파일명에서 사업자 정보 추출
    //
    // 예:
    //
    // 법인설립연구소_679-19-02150_20251231.xls
    //
    // 사업자명
    // = 법인설립연구소
    //
    // 사업자등록번호
    // = 679-19-02150
    //
    // 날짜
    // = 20251231
    //
    // 뒤의 "_" 2개를 기준으로 분리
    // =========================================================

    private static BusinessInfo parseBusinessInfo(
            Path sourcePath
    ) {

        String fileName =
                sourcePath
                        .getFileName()
                        .toString();


        int dotIndex =
                fileName.lastIndexOf('.');


        String nameWithoutExtension;


        if (dotIndex > 0) {

            nameWithoutExtension =
                    fileName.substring(
                            0,
                            dotIndex
                    );

        } else {

            nameWithoutExtension =
                    fileName;
        }


        int lastUnderscore =
                nameWithoutExtension
                        .lastIndexOf('_');


        if (lastUnderscore < 0) {

            throw new RuntimeException(
                    "원본 파일명 형식이 올바르지 않습니다.\n"
                    + "필요 형식: 사업자명_사업자번호_날짜.xls"
            );
        }


        int secondLastUnderscore =
                nameWithoutExtension
                        .lastIndexOf(
                                '_',
                                lastUnderscore - 1
                        );


        if (secondLastUnderscore < 0) {

            throw new RuntimeException(
                    "원본 파일명 형식이 올바르지 않습니다.\n"
                    + "필요 형식: 사업자명_사업자번호_날짜.xls"
            );
        }


        String businessName =
                nameWithoutExtension.substring(
                        0,
                        secondLastUnderscore
                );


        String businessNumber =
                nameWithoutExtension.substring(
                        secondLastUnderscore + 1,
                        lastUnderscore
                );


        String date =
                nameWithoutExtension.substring(
                        lastUnderscore + 1
                );


        if (businessName.trim().isEmpty()) {

            throw new RuntimeException(
                    "파일명에서 사업자명을 찾을 수 없습니다."
            );
        }


        if (businessNumber.trim().isEmpty()) {

            throw new RuntimeException(
                    "파일명에서 사업자등록번호를 찾을 수 없습니다."
            );
        }


        if (date.trim().isEmpty()) {

            throw new RuntimeException(
                    "파일명에서 날짜를 찾을 수 없습니다."
            );
        }


        return new BusinessInfo(
                businessName.trim(),
                businessNumber.trim(),
                date.trim()
        );
    }


    // =========================================================
    // 대상 파일 사업자 정보 입력
    //
    // A4:B4 병합셀 = 사업자명
    // C4           = 사업자등록번호
    // =========================================================

    private static void writeBusinessInfo(
            Sheet targetSheet,
            BusinessInfo businessInfo
    ) {

        Row row =
                targetSheet.getRow(
                        TARGET_INFO_ROW
                );


        if (row == null) {

            row =
                    targetSheet.createRow(
                            TARGET_INFO_ROW
                    );
        }


        // -----------------------------------------------------
        // A4
        // A4:B4 병합셀의 좌측 상단 셀
        // -----------------------------------------------------

        Cell businessNameCell =
                row.getCell(0);


        if (businessNameCell == null) {

            businessNameCell =
                    row.createCell(0);
        }


        businessNameCell.setCellValue(
                businessInfo.getBusinessName()
        );


        // -----------------------------------------------------
        // C4
        // 사업자등록번호는 하이픈 포함 문자열
        // -----------------------------------------------------

        Cell businessNumberCell =
                row.getCell(2);


        if (businessNumberCell == null) {

            businessNumberCell =
                    row.createCell(2);
        }


        businessNumberCell.setCellValue(
                businessInfo.getBusinessNumber()
        );
    }


    // =========================================================
    // TITLE 정규화
    //
    // 공백 / 줄바꿈 / 탭 / NBSP 제거
    //
    // 예:
    //
    // "신용카드사명       (30자리이내)"
    //
    // ->
    //
    // "신용카드사명(30자리이내)"
    // =========================================================

    private static String normalizeTitle(
            String title
    ) {

        if (title == null) {
            return "";
        }


        return title
                .replace("\r", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace("\u00A0", "")
                .replace(" ", "")
                .trim();
    }


    // =========================================================
    // 빈 행 확인
    // =========================================================

    private static boolean isEmptyRow(
            Row row,
            Map<Integer, Integer> columnMapping,
            DataFormatter formatter
    ) {

        for (
                Integer sourceCol
                : columnMapping.keySet()
        ) {

            Cell cell =
                    row.getCell(
                            sourceCol
                    );


            if (cell == null) {
                continue;
            }


            String value =
                    formatter
                            .formatCellValue(cell)
                            .trim();


            if (!value.isEmpty()) {
                return false;
            }
        }


        return true;
    }


    // =========================================================
    // 셀 문자열 변환
    // =========================================================

    private static String getCellAsString(
            Cell cell,
            DataFormatter formatter
    ) {

        if (cell == null) {
            return "";
        }


        return formatter
                .formatCellValue(cell)
                .trim();
    }


    // =========================================================
    // 일반 셀 값 복사
    // =========================================================

    private static void copyCellValue(
            Cell sourceCell,
            Cell targetCell,
            Workbook sourceWorkbook
    ) {

        if (sourceCell == null) {

            targetCell.setBlank();
            return;
        }


        CellType cellType =
                sourceCell.getCellType();


        switch (cellType) {

            case STRING:

                targetCell.setCellValue(
                        sourceCell.getStringCellValue()
                );

                break;


            case NUMERIC:

                if (
                        DateUtil.isCellDateFormatted(
                                sourceCell
                        )
                ) {

                    targetCell.setCellValue(
                            sourceCell.getDateCellValue()
                    );

                } else {

                    targetCell.setCellValue(
                            sourceCell.getNumericCellValue()
                    );
                }

                break;


            case BOOLEAN:

                targetCell.setCellValue(
                        sourceCell.getBooleanCellValue()
                );

                break;


            case FORMULA:

                copyFormulaResult(
                        sourceCell,
                        targetCell,
                        sourceWorkbook
                );

                break;


            case BLANK:

                targetCell.setBlank();

                break;


            default:

                DataFormatter formatter =
                        new DataFormatter();


                targetCell.setCellValue(
                        formatter.formatCellValue(
                                sourceCell
                        )
                );

                break;
        }
    }


    // =========================================================
    // 수식 셀은 수식 자체가 아니라 결과값 복사
    // =========================================================

    private static void copyFormulaResult(
            Cell sourceCell,
            Cell targetCell,
            Workbook workbook
    ) {

        FormulaEvaluator evaluator =
                workbook
                        .getCreationHelper()
                        .createFormulaEvaluator();


        org.apache.poi.ss.usermodel.CellValue value =
                evaluator.evaluate(
                        sourceCell
                );


        if (value == null) {

            targetCell.setBlank();
            return;
        }


        switch (value.getCellType()) {

            case STRING:

                targetCell.setCellValue(
                        value.getStringValue()
                );

                break;


            case NUMERIC:

                targetCell.setCellValue(
                        value.getNumberValue()
                );

                break;


            case BOOLEAN:

                targetCell.setCellValue(
                        value.getBooleanValue()
                );

                break;


            default:

                targetCell.setBlank();

                break;
        }
    }


    // =========================================================
    // 사업자 정보 클래스
    // =========================================================

    private static class BusinessInfo {

        private final String businessName;
        private final String businessNumber;
        private final String date;


        public BusinessInfo(
                String businessName,
                String businessNumber,
                String date
        ) {

            this.businessName =
                    businessName;

            this.businessNumber =
                    businessNumber;

            this.date =
                    date;
        }


        public String getBusinessName() {
            return businessName;
        }


        public String getBusinessNumber() {
            return businessNumber;
        }


        public String getDate() {
            return date;
        }
    }
}
