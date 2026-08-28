package com.hometax.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelTitleCopy {

    // =========================================================
    // 기본 폴더
    // =========================================================

    private static final String BASE_DIR =
            "C:\\hometax_download";


    // =========================================================
    // 업로드용 원본 양식
    //
    // 이 파일 자체는 C:\hometax_download에 그대로 유지
    // =========================================================

    private static final String TEMPLATE_FILE =
            BASE_DIR
            + "\\신용카드매입자료_업로드.xls";


    // =========================================================
    // 행 번호
    //
    // POI는 0부터 시작
    //
    // 홈택스 다운로드 파일
    // 2행 = TITLE
    // 3행부터 = DATA
    //
    // 업로드 양식
    // 4행 = 사업자 정보
    // 8행 = TITLE
    // 10행부터 = DATA
    // =========================================================

    private static final int SOURCE_TITLE_ROW = 1;

    private static final int SOURCE_DATA_START_ROW = 2;

    private static final int TARGET_INFO_ROW = 3;

    private static final int TARGET_TITLE_ROW = 7;

    private static final int TARGET_DATA_START_ROW = 9;


    // =========================================================
    // 메인 처리
    //
    // HometaxService에서 반환된 파일을 받음
    //
    // 예:
    //
    // 법인설립연구소_679-19-02150_20251231.xls
    // =========================================================

    public static File copyExcelByTitle(
            File sourceFile) throws Exception {

        return copyExcelByTitle(
                sourceFile,
                new VatCardCondition2Classifier.BusinessContext()
        );
    }


    public static File copyExcelByTitle(
            File sourceFile,
            VatCardCondition2Classifier.BusinessContext condition2BusinessContext) throws Exception {


        if (sourceFile == null) {

            throw new IllegalArgumentException(
                    "sourceFile이 null입니다."
            );
        }


        Path originalSourcePath =
                sourceFile.toPath();


        Path templatePath =
                Paths.get(
                        TEMPLATE_FILE
                );


        // =====================================================
        // 1. 파일 존재 확인
        // =====================================================

        if (!Files.exists(
                originalSourcePath)) {

            throw new RuntimeException(
                    "홈택스 다운로드 파일을 찾을 수 없습니다.\n"
                    + originalSourcePath
            );
        }


        if (!Files.exists(
                templatePath)) {

            throw new RuntimeException(
                    "신용카드매입자료 업로드 양식을 찾을 수 없습니다.\n"
                    + templatePath
            );
        }


        System.out.println();

        System.out.println(
                "======================================"
        );

        System.out.println(
                "ExcelTitleCopy 시작"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "[EXCEL-1] 홈택스 파일 = "
                + originalSourcePath
        );


        // =====================================================
        // 2. 파일명 분석
        //
        // 사업자명_사업자번호_최초파일명
        // =====================================================

        BusinessInfo businessInfo =
                parseBusinessInfo(
                        originalSourcePath
                );


        System.out.println(
                "[EXCEL-2] 상호명 = "
                + businessInfo.getBusinessName()
        );


        System.out.println(
                "[EXCEL-3] 사업자번호 = "
                + businessInfo.getBusinessNumber()
        );


        System.out.println(
                "[EXCEL-4] 최초 파일명 = "
                + businessInfo.getOriginalFileName()
        );


        // =====================================================
        // 3. 상호명 폴더 생성
        //
        // 예:
        //
        // C:\hometax_download\법인설립연구소
        // =====================================================

        String safeBusinessName =
                cleanFileName(
                        businessInfo
                                .getBusinessName()
                );


        Path businessFolder =
                Paths.get(
                        BASE_DIR,
                        safeBusinessName
                );


        Files.createDirectories(
                businessFolder
        );


        System.out.println(
                "[EXCEL-5] 상호명 폴더 생성/확인 완료"
        );


        System.out.println(
                "          "
                + businessFolder
        );


        // =====================================================
        // 4. HometaxService가 만든 파일을
        //    상호명 폴더 안으로 이동
        //
        // 예:
        //
        // C:\hometax_download\
        // 법인설립연구소_679-19-02150_20251231.xls
        //
        // ↓
        //
        // C:\hometax_download\법인설립연구소\
        // 법인설립연구소_679-19-02150_20251231.xls
        // =====================================================

        Path sourcePath =
                businessFolder.resolve(
                        sourceFile.getName()
                );


        if (!originalSourcePath
                .toAbsolutePath()
                .normalize()
                .equals(
                        sourcePath
                                .toAbsolutePath()
                                .normalize()
                )) {


            Files.move(
                    originalSourcePath,
                    sourcePath,
                    StandardCopyOption
                            .REPLACE_EXISTING
            );
        }


        System.out.println(
                "[EXCEL-6] 홈택스 원본 파일 이동 완료"
        );


        System.out.println(
                "          "
                + sourcePath
        );


        // =====================================================
        // 5. ExcelTitleCopy 최종 결과 파일명
        //
        // 앞에
        //
        // 신용카드매입자료_업로드-
        //
        // 를 붙임
        //
        // 예:
        //
        // 신용카드매입자료_업로드-
        // 법인설립연구소_679-19-02150_20251231.xls
        // =====================================================

        String outputFileName =
                "신용카드매입자료_업로드-"
                + sourcePath
                        .getFileName()
                        .toString();


        Path outputPath =
                businessFolder.resolve(
                        outputFileName
                );


        System.out.println(
                "[EXCEL-7] 최종 가공 파일명 = "
                + outputFileName
        );


        // =====================================================
        // 6. 엑셀 처리 시작
        //
        // SOURCE:
        // 상호명 폴더로 이동한 홈택스 파일
        //
        // TARGET:
        // 신용카드매입자료_업로드.xls 양식
        // =====================================================

        try (
                FileInputStream sourceFis =
                        new FileInputStream(
                                sourcePath.toFile()
                        );

                FileInputStream targetFis =
                        new FileInputStream(
                                templatePath.toFile()
                        );

                Workbook sourceWorkbook =
                        new HSSFWorkbook(
                                sourceFis
                        );

                Workbook targetWorkbook =
                        new HSSFWorkbook(
                                targetFis
                        )
        ) {


            Sheet sourceSheet =
                    sourceWorkbook
                            .getSheetAt(0);


            Sheet targetSheet =
                    targetWorkbook
                            .getSheetAt(0);


            System.out.println(
                    "[EXCEL-8] 엑셀 파일 열기 완료"
            );


            // =================================================
            // 사업자 정보 입력
            //
            // A4:B4 병합셀 = 상호명
            // C4 = 사업자등록번호
            // =================================================

            writeBusinessInfo(
                    targetSheet,
                    businessInfo
            );


            System.out.println(
                    "[EXCEL-9] 사업자 정보 입력 완료"
            );


            System.out.println(
                    "          A4 = "
                    + businessInfo
                            .getBusinessName()
            );


            System.out.println(
                    "          C4 = "
                    + businessInfo
                            .getBusinessNumber()
            );


            // =================================================
            // TITLE 행
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
                        "홈택스 파일의 2번째 행 TITLE을 찾을 수 없습니다."
                );
            }


            if (targetTitleRow == null) {

                throw new RuntimeException(
                        "업로드 양식의 8번째 행 TITLE을 찾을 수 없습니다."
                );
            }


            DataFormatter formatter =
                    new DataFormatter();

            // 조건2 ROW 배경색용 스타일 캐시.
            // HSSF 스타일 개수 제한을 피하기 위해 원본스타일+색상 조합을 재사용한다.
            Map<String, CellStyle> condition2RowStyleCache =
                    new HashMap<String, CellStyle>();


            // =================================================
            // 대상 TITLE → COLUMN
            // =================================================

            Map<String, Integer> targetTitleMap =
                    new LinkedHashMap<String, Integer>();


            for (
                    int col =
                            targetTitleRow
                                    .getFirstCellNum();

                    col <
                            targetTitleRow
                                    .getLastCellNum();

                    col++
            ) {


                Cell cell =
                        targetTitleRow
                                .getCell(col);


                if (cell == null) {

                    continue;
                }


                String title =
                        normalizeTitle(
                                formatter
                                        .formatCellValue(
                                                cell
                                        )
                        );


                if (title.isEmpty()) {

                    continue;
                }


                if (!targetTitleMap
                        .containsKey(title)) {

                    targetTitleMap.put(
                            title,
                            col
                    );
                }
            }


            System.out.println(
                    "[EXCEL-10] 대상 TITLE 분석 완료"
            );


            // =================================================
            // TITLE 강제 매핑
            // =================================================

            Map<String, String> customTitleMapping =
                    new LinkedHashMap<String, String>();


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


            customTitleMapping.put(
                    "비과세",
                    "봉사료"
            );


            // =================================================
            // SOURCE COLUMN → TARGET COLUMN
            // =================================================

            Map<Integer, Integer> columnMapping =
                    new LinkedHashMap<Integer, Integer>();


            Map<Integer, String> sourceTitleByColumn =
                    new LinkedHashMap<Integer, String>();


            Map<String, Integer> sourceColumnByTitle =
                    new LinkedHashMap<String, Integer>();


            System.out.println();

            System.out.println(
                    "===== TITLE 매칭 결과 ====="
            );


            for (
                    int sourceCol =
                            sourceTitleRow
                                    .getFirstCellNum();

                    sourceCol <
                            sourceTitleRow
                                    .getLastCellNum();

                    sourceCol++
            ) {


                Cell sourceTitleCell =
                        sourceTitleRow
                                .getCell(
                                        sourceCol
                                );


                if (sourceTitleCell == null) {

                    continue;
                }


                String sourceTitle =
                        normalizeTitle(
                                formatter
                                        .formatCellValue(
                                                sourceTitleCell
                                        )
                        );


                if (sourceTitle.isEmpty()) {

                    continue;
                }


                if (!sourceColumnByTitle.containsKey(sourceTitle)) {
                    sourceColumnByTitle.put(sourceTitle, sourceCol);
                }


                String targetTitle =
                        sourceTitle;


                if (customTitleMapping
                        .containsKey(
                                sourceTitle
                        )) {


                    targetTitle =
                            customTitleMapping
                                    .get(
                                            sourceTitle
                                    );
                }


                Integer targetCol =
                        targetTitleMap
                                .get(
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
                    );


                } else {


                    System.out.println(
                            "[SKIP] "
                            + sourceTitle
                            + " -> 대상 TITLE 없음"
                    );
                }
            }


            if (columnMapping
                    .isEmpty()) {


                throw new RuntimeException(
                        "일치하는 TITLE이 없습니다."
                );
            }


            // =================================================
            // 고정값 COLUMN
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
                        "'카드종류 (1자리)' TITLE을 찾을 수 없습니다."
                );
            }


            if (vatDeductionCol == null) {

                throw new RuntimeException(
                        "'부가세공제여부' TITLE을 찾을 수 없습니다."
                );
            }


            if (vatTypeCol == null) {

                throw new RuntimeException(
                        "'부가세유형 (2자리)' TITLE을 찾을 수 없습니다."
                );
            }


            if (accountCol == null) {

                throw new RuntimeException(
                        "'계정과목' TITLE을 찾을 수 없습니다."
                );
            }


            // =================================================
            // DATA 복사
            // =================================================

            int sourceLastRow =
                    sourceSheet
                            .getLastRowNum();


            int targetRowIndex =
                    TARGET_DATA_START_ROW;


            int copiedRowCount = 0;


            // =================================================
            // 요구사항 1
            // 거래처명(홈택스 가맹점명) 가나다순 정렬
            // =================================================
            List<Row> sourceDataRows =
                    new ArrayList<Row>();


            for (int sourceRowIndex = SOURCE_DATA_START_ROW;
                    sourceRowIndex <= sourceLastRow;
                    sourceRowIndex++) {

                Row row = sourceSheet.getRow(sourceRowIndex);

                if (row == null
                        || isEmptyRow(row, columnMapping, formatter)) {
                    continue;
                }

                sourceDataRows.add(row);
            }


            final Integer merchantNameSourceCol =
                    sourceColumnByTitle.get(
                            normalizeTitle("가맹점명")
                    );


            final Collator koreanCollator =
                    Collator.getInstance(
                            Locale.KOREAN
                    );


            koreanCollator.setStrength(
                    Collator.PRIMARY
            );


            sourceDataRows.sort(
                    new Comparator<Row>() {
                        @Override
                        public int compare(Row row1, Row row2) {
                            String name1 = getSourceValueByColumn(
                                    row1,
                                    merchantNameSourceCol,
                                    formatter
                            );

                            String name2 = getSourceValueByColumn(
                                    row2,
                                    merchantNameSourceCol,
                                    formatter
                            );

                            if (name1.length() == 0 && name2.length() > 0) {
                                return 1;
                            }

                            if (name1.length() > 0 && name2.length() == 0) {
                                return -1;
                            }

                            int compared = koreanCollator.compare(name1, name2);

                            if (compared != 0) {
                                return compared;
                            }

                            return Integer.compare(
                                    row1.getRowNum(),
                                    row2.getRowNum()
                            );
                        }
                    }
            );


            for (Row sourceRow : sourceDataRows) {


                Row targetRow =
                        targetSheet
                                .getRow(
                                        targetRowIndex
                                );


                if (targetRow == null) {


                    targetRow =
                            targetSheet
                                    .createRow(
                                            targetRowIndex
                                    );


                    Row templateDataRow =
                            targetSheet
                                    .getRow(
                                            TARGET_DATA_START_ROW
                                    );


                    if (templateDataRow != null) {


                        targetRow.setHeight(
                                templateDataRow
                                        .getHeight()
                        );
                    }
                }


                // =============================================
                // TITLE 기준 데이터 복사
                // =============================================

                for (
                        Map.Entry<Integer, Integer> mapping
                        : columnMapping
                                .entrySet()
                ) {


                    int sourceCol =
                            mapping.getKey();


                    int targetCol =
                            mapping.getValue();


                    Cell sourceCell =
                            sourceRow
                                    .getCell(
                                            sourceCol
                                    );


                    Cell targetCell =
                            getOrCreateTargetCell(
                                    targetSheet,
                                    targetRow,
                                    targetCol
                            );


                    String sourceTitle =
                            sourceTitleByColumn
                                    .get(
                                            sourceCol
                                    );


                    // =========================================
                    // 가맹점유형 → 거래처유형
                    //
                    // 앞 두 글자만 복사
                    // =========================================

                    if ("가맹점유형"
                            .equals(
                                    sourceTitle
                            )) {


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


                        targetCell
                                .setCellValue(
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
                // 카드종류 고정값 + 조건2 분류 결과
                // =============================================

                // 카드종류 = 3
                setFixedNumberValue(
                        targetSheet,
                        targetRow,
                        cardTypeCol,
                        3
                );


                VatCardCondition2Classifier.RowContext rowContext =
                        new VatCardCondition2Classifier.RowContext()
                                .setMerchantName(
                                        getSourceValueByTitle(
                                                sourceRow,
                                                sourceColumnByTitle,
                                                "가맹점명",
                                                formatter
                                        )
                                )
                                .setMerchantType(
                                        getSourceValueByTitle(
                                                sourceRow,
                                                sourceColumnByTitle,
                                                "가맹점유형",
                                                formatter
                                        )
                                )
                                .setSupplyAmount(
                                        getSourceNumberByTitle(
                                                sourceRow,
                                                sourceColumnByTitle,
                                                "공급가액",
                                                formatter
                                        )
                                );


                VatCardCondition2Classifier.Result condition2Result =
                        VatCardCondition2Classifier.classify(
                                condition2BusinessContext,
                                rowContext
                        );


                setNullableTextValue(
                        targetSheet,
                        targetRow,
                        vatDeductionCol,
                        condition2Result.getVatDeduction()
                );


                setNullableNumberValue(
                        targetSheet,
                        targetRow,
                        vatTypeCol,
                        condition2Result.getVatType()
                );


                setNullableNumberValue(
                        targetSheet,
                        targetRow,
                        accountCol,
                        condition2Result.getAccountCode()
                );


                applyCondition2RowBackground(
                        targetWorkbook,
                        targetSheet,
                        targetRow,
                        condition2Result.getRowColor(),
                        condition2RowStyleCache
                );


                targetRowIndex++;

                copiedRowCount++;
            }


            System.out.println(
                    "[EXCEL-11] 데이터 복사 완료 = "
                    + copiedRowCount
                    + "건"
            );


            // =================================================
            // 최종 가공 파일 저장
            //
            // 신용카드매입자료_업로드-
            // + HometaxService 최종 파일명
            // =================================================

            try (
                    FileOutputStream fos =
                            new FileOutputStream(
                                    outputPath
                                            .toFile()
                            )
            ) {


                targetWorkbook.write(
                        fos
                );
            }


            System.out.println(
                    "[EXCEL-12] 최종 가공 파일 저장 완료"
            );


            System.out.println(
                    "           "
                    + outputPath
            );
        }


        // =====================================================
        // 최종 결과
        // =====================================================

        System.out.println();

        System.out.println(
                "======================================"
        );

        System.out.println(
                "ExcelTitleCopy 전체 작업 완료"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "상호명 폴더 = "
                + businessFolder
        );


        System.out.println(
                "홈택스 원본 = "
                + sourcePath
        );


        System.out.println(
                "가공 파일 = "
                + outputPath
        );


        return outputPath.toFile();
    }


    // =========================================================
    // 파일명 분석
    //
    // 매우 중요:
    //
    // 첫 번째 "_" 앞
    // = 상호명
    //
    // 첫 번째 "_" ~ 두 번째 "_"
    // = 사업자번호
    //
    // 두 번째 "_" 이후 전체
    // = 최초 다운로드 파일명
    //
    // 따라서 최초 파일명에 "_"가 있어도 문제 없음
    //
    // 예:
    //
    // 법인설립연구소_679-19-02150_20251231.xls
    //
    // 또는
    //
    // 법인설립연구소_679-19-02150_2025_1분기.xls
    // =========================================================

    private static BusinessInfo parseBusinessInfo(
            Path sourcePath) {


        String fileName =
                sourcePath
                        .getFileName()
                        .toString();


        int firstUnderscore =
                fileName.indexOf('_');


        if (firstUnderscore < 0) {


            throw new RuntimeException(
                    "파일명에 첫 번째 '_' 구분자가 없습니다.\n"
                    + fileName
            );
        }


        int secondUnderscore =
                fileName.indexOf(
                        '_',
                        firstUnderscore + 1
                );


        if (secondUnderscore < 0) {


            throw new RuntimeException(
                    "파일명에 두 번째 '_' 구분자가 없습니다.\n"
                    + fileName
            );
        }


        String businessName =
                fileName.substring(
                        0,
                        firstUnderscore
                );


        String businessNumber =
                fileName.substring(
                        firstUnderscore + 1,
                        secondUnderscore
                );


        String originalFileName =
                fileName.substring(
                        secondUnderscore + 1
                );


        if (businessName
                .trim()
                .isEmpty()) {


            throw new RuntimeException(
                    "파일명에서 상호명을 찾지 못했습니다."
            );
        }


        if (businessNumber
                .trim()
                .isEmpty()) {


            throw new RuntimeException(
                    "파일명에서 사업자번호를 찾지 못했습니다."
            );
        }


        if (originalFileName
                .trim()
                .isEmpty()) {


            throw new RuntimeException(
                    "파일명에서 최초 다운로드 파일명을 찾지 못했습니다."
            );
        }


        return new BusinessInfo(
                businessName.trim(),
                businessNumber.trim(),
                originalFileName.trim()
        );
    }


    // =========================================================
    // 사업자 정보 입력
    //
    // A4:B4 병합 = 상호명
    // C4 = 사업자번호
    // =========================================================

    private static void writeBusinessInfo(
            Sheet targetSheet,
            BusinessInfo businessInfo) {


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


        // A4
        Cell businessNameCell =
                row.getCell(0);


        if (businessNameCell == null) {


            businessNameCell =
                    row.createCell(0);
        }


        businessNameCell.setCellValue(
                businessInfo
                        .getBusinessName()
        );


        // C4
        Cell businessNumberCell =
                row.getCell(2);


        if (businessNumberCell == null) {


            businessNumberCell =
                    row.createCell(2);
        }


        businessNumberCell.setCellValue(
                businessInfo
                        .getBusinessNumber()
        );
    }


    // =========================================================
    // TARGET CELL
    // =========================================================

    private static Cell getOrCreateTargetCell(
            Sheet targetSheet,
            Row targetRow,
            int targetCol) {


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


                if (templateCell != null) {


                    targetCell.setCellStyle(
                            templateCell
                                    .getCellStyle()
                    );
                }
            }
        }


        return targetCell;
    }


    // =========================================================
    // 조건2 ROW 배경색 적용
    //
    // 조건2(3).xlsx의 'ROW 색' 컬럼 기준:
    //  - 1번 사업자(법인/개인) : 노란색
    //  - 5번 등록 X            : 주황색 (조건1 DB 연결 후)
    //  - 6번 100만 초과        : 초록색
    //  - X                      : 별도 색 변경 없음
    //
    // ROW 전체는 업로드 양식의 TITLE 열 범위까지 칠한다.
    // =========================================================

    private static void applyCondition2RowBackground(
            Workbook workbook,
            Sheet targetSheet,
            Row targetRow,
            VatCardCondition2Classifier.RowColor rowColor,
            Map<String, CellStyle> styleCache) {

        if (workbook == null
                || targetSheet == null
                || targetRow == null
                || rowColor == null
                || VatCardCondition2Classifier.RowColor.NONE.equals(rowColor)) {
            return;
        }

        short indexedColor;

        if (VatCardCondition2Classifier.RowColor.YELLOW.equals(rowColor)) {
            indexedColor = IndexedColors.YELLOW.getIndex();
        } else if (VatCardCondition2Classifier.RowColor.ORANGE.equals(rowColor)) {
            indexedColor = IndexedColors.ORANGE.getIndex();
        } else if (VatCardCondition2Classifier.RowColor.GREEN.equals(rowColor)) {
            indexedColor = IndexedColors.BRIGHT_GREEN.getIndex();
        } else {
            return;
        }

        Row titleRow = targetSheet.getRow(TARGET_TITLE_ROW);
        if (titleRow == null || titleRow.getLastCellNum() <= 0) {
            return;
        }

        int lastColumnExclusive = titleRow.getLastCellNum();

        for (int col = 0; col < lastColumnExclusive; col++) {
            Cell cell = getOrCreateTargetCell(targetSheet, targetRow, col);
            CellStyle baseStyle = cell.getCellStyle();
            short baseStyleIndex = baseStyle == null ? 0 : baseStyle.getIndex();
            String cacheKey = baseStyleIndex + "_" + indexedColor;

            CellStyle coloredStyle = styleCache.get(cacheKey);
            if (coloredStyle == null) {
                coloredStyle = workbook.createCellStyle();
                if (baseStyle != null) {
                    coloredStyle.cloneStyleFrom(baseStyle);
                }
                coloredStyle.setFillForegroundColor(indexedColor);
                coloredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                styleCache.put(cacheKey, coloredStyle);
            }

            cell.setCellStyle(coloredStyle);
        }
    }


    // =========================================================
    // SOURCE 조건값 조회
    // =========================================================

    private static String getSourceValueByTitle(
            Row row,
            Map<String, Integer> sourceColumnByTitle,
            String title,
            DataFormatter formatter) {

        Integer sourceCol =
                sourceColumnByTitle.get(
                        normalizeTitle(title)
                );

        return getSourceValueByColumn(
                row,
                sourceCol,
                formatter
        );
    }


    private static String getSourceValueByColumn(
            Row row,
            Integer sourceCol,
            DataFormatter formatter) {

        if (row == null || sourceCol == null) {
            return "";
        }

        return getCellAsString(
                row.getCell(sourceCol.intValue()),
                formatter
        );
    }


    private static double getSourceNumberByTitle(
            Row row,
            Map<String, Integer> sourceColumnByTitle,
            String title,
            DataFormatter formatter) {

        String value = getSourceValueByTitle(
                row,
                sourceColumnByTitle,
                title,
                formatter
        );

        if (value.length() == 0) {
            return 0d;
        }

        String numberOnly =
                value.replaceAll(
                        "[^0-9\\.-]",
                        ""
                );

        if (numberOnly.length() == 0
                || "-".equals(numberOnly)
                || ".".equals(numberOnly)) {
            return 0d;
        }

        try {
            return Double.parseDouble(numberOnly);
        } catch (NumberFormatException e) {
            return 0d;
        }
    }


    private static void setNullableTextValue(
            Sheet targetSheet,
            Row targetRow,
            int targetCol,
            String value) {

        Cell cell = getOrCreateTargetCell(
                targetSheet,
                targetRow,
                targetCol
        );

        if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value);
        }
    }


    private static void setNullableNumberValue(
            Sheet targetSheet,
            Row targetRow,
            int targetCol,
            Integer value) {

        Cell cell = getOrCreateTargetCell(
                targetSheet,
                targetRow,
                targetCol
        );

        if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value.doubleValue());
        }
    }


    // =========================================================
    // 고정 문자값
    // =========================================================

    private static void setFixedTextValue(
            Sheet targetSheet,
            Row targetRow,
            int targetCol,
            String value) {


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
    // 고정 숫자값
    // =========================================================

    private static void setFixedNumberValue(
            Sheet targetSheet,
            Row targetRow,
            int targetCol,
            double value) {


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
    // TITLE 정규화
    // =========================================================

    private static String normalizeTitle(
            String title) {


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
    // 빈 행 체크
    // =========================================================

    private static boolean isEmptyRow(
            Row row,
            Map<Integer, Integer> columnMapping,
            DataFormatter formatter) {


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
                            .formatCellValue(
                                    cell
                            )
                            .trim();


            if (!value.isEmpty()) {

                return false;
            }
        }


        return true;
    }


    // =========================================================
    // CELL → 문자열
    // =========================================================

    private static String getCellAsString(
            Cell cell,
            DataFormatter formatter) {


        if (cell == null) {

            return "";
        }


        return formatter
                .formatCellValue(
                        cell
                )
                .trim();
    }


    // =========================================================
    // 일반 CELL 값 복사
    // =========================================================

    private static void copyCellValue(
            Cell sourceCell,
            Cell targetCell,
            Workbook sourceWorkbook) {


        if (sourceCell == null) {


            targetCell.setBlank();

            return;
        }


        CellType cellType =
                sourceCell
                        .getCellType();


        switch (cellType) {


            case STRING:


                targetCell.setCellValue(
                        sourceCell
                                .getStringCellValue()
                );

                break;


            case NUMERIC:


                if (DateUtil
                        .isCellDateFormatted(
                                sourceCell
                        )) {


                    targetCell.setCellValue(
                            sourceCell
                                    .getDateCellValue()
                    );


                } else {


                    targetCell.setCellValue(
                            sourceCell
                                    .getNumericCellValue()
                    );
                }

                break;


            case BOOLEAN:


                targetCell.setCellValue(
                        sourceCell
                                .getBooleanCellValue()
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
                        formatter
                                .formatCellValue(
                                        sourceCell
                                )
                );

                break;
        }
    }


    // =========================================================
    // 수식 결과값 복사
    // =========================================================

    private static void copyFormulaResult(
            Cell sourceCell,
            Cell targetCell,
            Workbook workbook) {


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


        switch (
                value.getCellType()
        ) {


            case STRING:


                targetCell.setCellValue(
                        value
                                .getStringValue()
                );

                break;


            case NUMERIC:


                targetCell.setCellValue(
                        value
                                .getNumberValue()
                );

                break;


            case BOOLEAN:


                targetCell.setCellValue(
                        value
                                .getBooleanValue()
                );

                break;


            default:


                targetCell.setBlank();

                break;
        }
    }


    // =========================================================
    // Windows 파일명 불가 문자 정리
    // =========================================================

    private static String cleanFileName(
            String value) {


        if (value == null) {

            return "";
        }


        return value
                .trim()
                .replaceAll(
                        "[\\\\/:*?\"<>|]",
                        "_"
                );
    }


    // =========================================================
    // 사업자 정보
    // =========================================================

    private static class BusinessInfo {


        private final String businessName;

        private final String businessNumber;

        private final String originalFileName;


        public BusinessInfo(
                String businessName,
                String businessNumber,
                String originalFileName) {


            this.businessName =
                    businessName;


            this.businessNumber =
                    businessNumber;


            this.originalFileName =
                    originalFileName;
        }


        public String getBusinessName() {

            return businessName;
        }


        public String getBusinessNumber() {

            return businessNumber;
        }


        public String getOriginalFileName() {

            return originalFileName;
        }
    }
}