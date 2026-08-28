package com.hometax.com;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HometaxService {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String downloadDir;


    private static final String TARGET_URL =
            "https://hometax.go.kr/websquare/websquare.html"
            + "?w2xPath=/ui/pp/index_pp.xml"
            + "&tmIdx=46"
            + "&tm2lIdx=4608020000"
            + "&tm3lIdx=4608020100";


    public HometaxService(
            WebDriver driver,
            String downloadDir) {

        this.driver = driver;
        this.downloadDir = downloadDir;

        this.wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(30)
                );
    }


    // =============================================================
    // 메인
    //
    // 예:
    // downloadExcel(2025, 1)
    // downloadExcel(2024, 3)
    // =============================================================

    public File downloadExcel(
            int year,
            int quarter,
            String selectedBusinessNumber) {

        validatePeriod(
                year,
                quarter
        );

        try {

            System.out.println();
            System.out.println(
                    "======================================"
            );

            System.out.println(
                    year
                    + "년 "
                    + quarter
                    + "분기 조회 시작"
            );

            System.out.println(
                    "======================================"
            );


            // =====================================================
            // 1. 대상 메뉴 이동
            // =====================================================

            driver.get(TARGET_URL);

            System.out.println(
                    "[WORK-1] 대상 메뉴 이동 완료"
            );


            wait.until(d ->
                    d.findElements(
                            By.cssSelector(
                                    "body *"
                            )
                    ).size() > 30
            );


            // =====================================================
            // 2. 상호명 / 사업자등록번호
            // =====================================================

            String businessNumber =
                    findBusinessNumber(
                            selectedBusinessNumber
                    );

            String businessName =
                    findBusinessName();


            System.out.println(
                    "[WORK-2] 상호명 = "
                    + businessName
            );

            System.out.println(
                    "[WORK-2-1] 사업자등록번호 = "
                    + businessNumber
            );


            // =====================================================
            // 3. 조회기간 = 분기별
            // =====================================================

            selectQuarterPeriod();

            System.out.println(
                    "[WORK-3] 조회기간 = 분기별"
            );

            sleep(300);


            // =====================================================
            // 4. 연도
            // =====================================================

            selectComboByOrder(
                    0,
                    year + "년",
                    String.valueOf(year)
            );

            System.out.println(
                    "[WORK-4] 연도 = "
                    + year
                    + "년"
            );

            sleep(300);


            // =====================================================
            // 5. 분기
            // =====================================================

            selectComboByOrder(
                    1,
                    quarter + "분기",
                    String.valueOf(quarter)
            );

            System.out.println(
                    "[WORK-5] 분기 = "
                    + quarter
                    + "분기"
            );

            sleep(300);


            // =====================================================
            // 6. 조회 버튼
            // =====================================================

            WebElement searchButton =
                    wait.until(
                            ExpectedConditions
                                    .elementToBeClickable(
                                            By.id(
                                                    "mf_txppWframe_btnSearch"
                                            )
                                    )
                    );


            System.out.println(
                    "[WORK-6] 조회 버튼 발견"
            );


            scrollTo(
                    searchButton
            );

            searchButton.click();


            System.out.println(
                    "[WORK-7] 조회 클릭 완료"
            );


            // =====================================================
            // 7. 조회 완료 대기
            // =====================================================

            // 조회 직후 홈택스가 "조회된 내역이 없습니다." Alert를 띄우는 경우
            // Selenium의 UnhandledAlertException으로 번지기 전에 여기서 직접 처리한다.
            checkSearchResultAlert();

            waitForSearchResult();

            System.out.println(
                    "[WORK-8] 조회 완료"
            );


            // =====================================================
            // 8. 내려받기
            // =====================================================

            WebElement downloadButton =
                    wait.until(
                            ExpectedConditions
                                    .elementToBeClickable(
                                            By.id(
                                                    "mf_txppWframe_trigger12"
                                            )
                                    )
                    );


            System.out.println(
                    "[WORK-9] 내려받기 버튼 발견"
            );


            scrollTo(
                    downloadButton
            );

            downloadButton.click();


            System.out.println(
                    "[WORK-10] 내려받기 클릭 완료"
            );


            // =====================================================
            // 9. 팝업의 "엑셀" 버튼
            // =====================================================

            WebElement excelButton =
                    wait.until(d -> {

                        List<WebElement> candidates =
                                d.findElements(
                                        By.xpath(
                                                "//*["
                                                + "self::a "
                                                + "or self::button "
                                                + "or self::input "
                                                + "or self::span "
                                                + "or self::div"
                                                + "]"
                                                + "["
                                                + "normalize-space(.)='엑셀' "
                                                + "or @value='엑셀' "
                                                + "or @title='엑셀'"
                                                + "]"
                                        )
                                );


                        for (WebElement element
                                : candidates) {

                            try {

                                if (!element.isDisplayed()
                                        || !element.isEnabled()) {

                                    continue;
                                }


                                WebElement clickable =
                                        findClickableParent(
                                                element
                                        );


                                if (clickable != null
                                        && clickable.isDisplayed()
                                        && clickable.isEnabled()) {

                                    return clickable;
                                }

                            } catch (Exception ignored) {
                            }
                        }


                        return null;
                    });


            System.out.println(
                    "[WORK-11] 엑셀 버튼 발견"
            );


            System.out.println(
                    "엑셀 버튼 ID = "
                    + excelButton.getAttribute(
                            "id"
                    )
            );


            scrollTo(
                    excelButton
            );

            excelButton.click();


            System.out.println(
                    "[WORK-12] 엑셀 클릭 완료"
            );


            // =====================================================
            // 10. Alert
            //
            // "파일을 받으시겠습니까?"
            // =====================================================

            Alert alert =
                    wait.until(
                            ExpectedConditions
                                    .alertIsPresent()
                    );


            System.out.println(
                    "[WORK-13] Alert 내용 = "
                    + alert.getText()
            );


            // 실제 다운로드 직전 시간
            long downloadStart =
                    System.currentTimeMillis();


            alert.accept();


            System.out.println(
                    "[WORK-14] 다운로드 확인 클릭 완료"
            );


            // =====================================================
            // 11. 파일 다운로드 완료
            // =====================================================

            File downloadedFile =
                    waitForExcelDownload(
                            new File(
                                    downloadDir
                            ),
                            downloadStart,
                            60
                    );


            if (downloadedFile == null) {

                throw new RuntimeException(
                        "엑셀 다운로드 파일을 찾지 못했습니다."
                );
            }


            System.out.println(
                    "[WORK-15] 원본 다운로드 완료"
            );


            System.out.println(
                    "원본 파일명 = "
                    + downloadedFile.getName()
            );


            // =====================================================
            // 12. 파일명 변경
            //
            // 상호명_사업자등록번호_기존파일명
            //
            // 예:
            // 법인설립연구소_679-19-02150_20251231.xls
            // =====================================================

            File renamedFile =
                    renameDownloadedFile(
                            downloadedFile,
                            businessName,
                            businessNumber
                    );


            System.out.println();
            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "[WORK-16] 전체 작업 성공"
            );

            System.out.println(
                    "======================================"
            );


            System.out.println(
                    "최종 파일명 = "
                    + renamedFile.getName()
            );


            System.out.println(
                    "최종 파일경로 = "
                    + renamedFile.getAbsolutePath()
            );


            return renamedFile;


        } catch (UnhandledAlertException e) {

            String alertText = null;

            try {
                if (e.getAlertText() != null) {
                    alertText = e.getAlertText().trim();
                }
            } catch (Exception ignore) {
                // alertText 추출 실패 시 아래 일반 메시지 사용
            }

            if (alertText != null
                    && alertText.contains("조회된 내역이 없습니다")) {

                throw new RuntimeException(
                        "조회된 내역이 없습니다."
                );
            }

            throw new RuntimeException(
                    year
                    + "년 "
                    + quarter
                    + "분기 다운로드 실패",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    year
                    + "년 "
                    + quarter
                    + "분기 다운로드 실패",
                    e
            );
        }
    }


    // =============================================================
    // 상호명 가져오기
    //
    // ID:
    // mf_txppWframe_txprNm
    // =============================================================

    private String findBusinessName() {

        WebElement element =
                wait.until(
                        ExpectedConditions
                                .visibilityOfElementLocated(
                                        By.id(
                                                "mf_txppWframe_txprNm"
                                        )
                                )
                );


        String businessName =
                element.getText();


        // getText()가 비어 있을 경우 value 속성 확인
        if (businessName == null
                || businessName
                        .trim()
                        .isEmpty()) {

            businessName =
                    element.getAttribute(
                            "value"
                    );
        }


        if (businessName == null
                || businessName
                        .trim()
                        .isEmpty()) {

            throw new RuntimeException(
                    "상호명을 가져오지 못했습니다."
            );
        }


        return cleanFileNameValue(
                businessName
        );
    }


    // =============================================================
    // 사업자등록번호 가져오기
    //
    // ID:
    // mf_txppWframe_bmanTxprNo
    // =============================================================

    private String findBusinessNumber(
            String selectedBusinessNumber) {

        String normalizedSelected =
                onlyNumber(
                        selectedBusinessNumber
                );

        if (normalizedSelected.length() != 10) {

            throw new RuntimeException(
                    "선택한 기업의 사업자등록번호가 올바르지 않습니다. "
                    + "사업자번호="
                    + selectedBusinessNumber
            );
        }

        String formattedBusinessNumber =
                formatBusinessNumber(
                        normalizedSelected
                );


        // =========================================================
        // 1. WebSquare 사업자 영역 렌더링 대기
        //
        // 둘 중 하나가 준비될 때까지 기다린다.
        // 1) bmanTxprNo에 실제 사업자번호 값이 표시됨
        // 2) bmanSelectBox가 실제 화면에 표시됨
        // =========================================================

        wait.until(d -> {

            List<WebElement> numberElements =
                    d.findElements(
                            By.id(
                                    "mf_txppWframe_bmanTxprNo"
                            )
                    );

            for (WebElement element : numberElements) {

                try {

                    if (!element.isDisplayed()) {
                        continue;
                    }

                    String value =
                            element.getText();

                    if (value == null
                            || value.trim().isEmpty()) {

                        value =
                                element.getAttribute(
                                        "value"
                                );
                    }

                    if (value != null
                            && !value.trim().isEmpty()) {

                        return true;
                    }

                } catch (Exception ignored) {
                }
            }


            List<WebElement> comboElements =
                    d.findElements(
                            By.id(
                                    "mf_txppWframe_bmanSelectBox"
                            )
                    );

            for (WebElement combo : comboElements) {

                try {

                    if (combo.isDisplayed()) {
                        return true;
                    }

                } catch (Exception ignored) {
                }
            }


            return false;
        });


        // =========================================================
        // 2. bmanTxprNo 우선 확인
        //    일치하면 PASS, 콤보는 건드리지 않는다.
        // =========================================================

        List<WebElement> numberElements =
                driver.findElements(
                        By.id(
                                "mf_txppWframe_bmanTxprNo"
                        )
                );

        for (WebElement element : numberElements) {

            try {

                if (!element.isDisplayed()) {
                    continue;
                }

                String currentBusinessNumber =
                        element.getText();

                if (currentBusinessNumber == null
                        || currentBusinessNumber.trim().isEmpty()) {

                    currentBusinessNumber =
                            element.getAttribute(
                                    "value"
                            );
                }

                if (currentBusinessNumber == null
                        || currentBusinessNumber.trim().isEmpty()) {

                    continue;
                }

                System.out.println(
                        "[BUSINESS] mf_txppWframe_bmanTxprNo = "
                        + currentBusinessNumber
                );

                if (normalizedSelected.equals(
                        onlyNumber(
                                currentBusinessNumber
                        ))) {

                    System.out.println(
                            "[BUSINESS] 현재 사업자번호가 요청값과 일치 - PASS = "
                            + currentBusinessNumber
                    );

                    return cleanFileNameValue(
                            currentBusinessNumber
                    );
                }

                System.out.println(
                        "[BUSINESS] 현재 사업자번호가 요청값과 다름. "
                        + "현재="
                        + currentBusinessNumber
                        + ", 요청="
                        + formattedBusinessNumber
                );

            } catch (Exception ignored) {
            }
        }


        // =========================================================
        // 3. bmanTxprNo가 없거나 불일치한 경우에만
        //    bmanSelectBox에서 대상 사업자번호 선택
        // =========================================================

        WebElement comboElement =
                wait.until(d -> {

                    List<WebElement> combos =
                            d.findElements(
                                    By.id(
                                            "mf_txppWframe_bmanSelectBox"
                                    )
                            );

                    for (WebElement combo : combos) {

                        try {

                            if (combo.isDisplayed()
                                    && combo.isEnabled()) {

                                return combo;
                            }

                        } catch (Exception ignored) {
                        }
                    }

                    return null;
                });


        boolean selected = false;
        String selectedText = "";


        // =========================================================
        // 3-1. 일반 HTML <select>
        // =========================================================

        try {

            if ("select".equalsIgnoreCase(
                    comboElement.getTagName())) {

                Select select =
                        new Select(
                                comboElement
                        );

                List<WebElement> options =
                        select.getOptions();

                for (int i = 0;
                     i < options.size();
                     i++) {

                    WebElement option =
                            options.get(i);

                    String optionText =
                            option.getText();

                    String optionValue =
                            option.getAttribute(
                                    "value"
                            );

                    if (!isSameBusinessNumber(
                            normalizedSelected,
                            formattedBusinessNumber,
                            optionText,
                            optionValue)) {

                        continue;
                    }

                    try {

                        if (optionValue != null
                                && !optionValue.trim().isEmpty()) {

                            select.selectByValue(
                                    optionValue
                            );

                        } else {

                            select.selectByIndex(
                                    i
                            );
                        }

                    } catch (Exception e) {

                        select.selectByIndex(
                                i
                        );
                    }

                    selected = true;
                    selectedText =
                            optionText;

                    System.out.println(
                            "[BUSINESS] bmanSelectBox 사업자 선택 완료 = "
                            + optionText
                    );

                    break;
                }
            }

        } catch (Exception ignored) {
        }


        // =========================================================
        // 3-2. WebSquare SelectBox
        // =========================================================

        if (!selected) {

            try {

                scrollTo(
                        comboElement
                );

                comboElement.click();

                sleep(500);

                List<WebElement> candidates =
                        driver.findElements(
                                By.xpath(
                                        "//*["
                                        + "self::a "
                                        + "or self::span "
                                        + "or self::div "
                                        + "or self::li "
                                        + "or self::option"
                                        + "]"
                                )
                        );

                for (WebElement candidate
                        : candidates) {

                    try {

                        if (!candidate.isDisplayed()) {
                            continue;
                        }

                        String candidateText =
                                candidate.getText();

                        String candidateValue =
                                candidate.getAttribute(
                                        "value"
                                );

                        if (!isSameBusinessNumber(
                                normalizedSelected,
                                formattedBusinessNumber,
                                candidateText,
                                candidateValue)) {

                            continue;
                        }

                        WebElement clickable =
                                findClickableParent(
                                        candidate
                                );

                        scrollTo(
                                clickable
                        );

                        clickable.click();

                        selected = true;
                        selectedText =
                                candidateText;

                        System.out.println(
                                "[BUSINESS] bmanSelectBox 사업자 선택 완료 = "
                                + candidateText
                        );

                        break;

                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception ignored) {
            }
        }


        if (!selected) {

            throw new RuntimeException(
                    "홈택스 사업자번호를 찾지 못했습니다. "
                    + "mf_txppWframe_bmanTxprNo 불일치 또는 없음, "
                    + "mf_txppWframe_bmanSelectBox에서도 찾지 못함. "
                    + "원본="
                    + selectedBusinessNumber
                    + ", 하이픈형="
                    + formattedBusinessNumber
            );
        }


        sleep(1000);


        // =========================================================
        // 4. 콤보 선택 후 bmanTxprNo 값 재확인
        // =========================================================

        try {

            WebElement selectedNumberElement =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(5)
                    ).until(d -> {

                        List<WebElement> elements =
                                d.findElements(
                                        By.id(
                                                "mf_txppWframe_bmanTxprNo"
                                        )
                                );

                        for (WebElement element : elements) {

                            try {

                                if (!element.isDisplayed()) {
                                    continue;
                                }

                                String value =
                                        element.getText();

                                if (value == null
                                        || value.trim().isEmpty()) {

                                    value =
                                            element.getAttribute(
                                                    "value"
                                            );
                                }

                                if (value != null
                                        && normalizedSelected.equals(
                                                onlyNumber(
                                                        value
                                                ))) {

                                    return element;
                                }

                            } catch (Exception ignored) {
                            }
                        }

                        return null;
                    });


            String currentBusinessNumber =
                    selectedNumberElement.getText();

            if (currentBusinessNumber == null
                    || currentBusinessNumber.trim().isEmpty()) {

                currentBusinessNumber =
                        selectedNumberElement.getAttribute(
                                "value"
                        );
            }

            System.out.println(
                    "[BUSINESS] 선택 후 사업자번호 확인 완료 = "
                    + currentBusinessNumber
            );

            return cleanFileNameValue(
                    currentBusinessNumber
            );

        } catch (Exception ignored) {
        }


        if (selectedText == null
                || selectedText.trim().isEmpty()) {

            selectedText =
                    formattedBusinessNumber;
        }

        return cleanFileNameValue(
                selectedText
        );
    }


    private boolean isSameBusinessNumber(
            String normalizedSelected,
            String formattedBusinessNumber,
            String text,
            String value) {

        String normalizedText =
                onlyNumber(
                        text
                );

        String normalizedValue =
                onlyNumber(
                        value
                );

        String trimmedText =
                text == null
                ? ""
                : text.trim();

        String trimmedValue =
                value == null
                ? ""
                : value.trim();


        return normalizedSelected.equals(
                    normalizedText)
                || normalizedSelected.equals(
                    normalizedValue)
                || formattedBusinessNumber.equals(
                    trimmedText)
                || formattedBusinessNumber.equals(
                    trimmedValue);
    }


    private String formatBusinessNumber(
            String businessNumber) {

        String onlyNumber =
                onlyNumber(
                        businessNumber
                );

        if (onlyNumber.length() != 10) {

            return businessNumber == null
                    ? ""
                    : businessNumber.trim();
        }


        return onlyNumber.substring(
                    0,
                    3)
                + "-"
                + onlyNumber.substring(
                    3,
                    5)
                + "-"
                + onlyNumber.substring(
                    5);
    }


    private String onlyNumber(
            String value) {

        if (value == null) {

            return "";
        }

        return value.replaceAll(
                "[^0-9]",
                ""
        );
    }


    // =============================================================
    // 다운로드 파일명 변경
    //
    // 기존:
    // 20251231.xls
    //
    // 변경:
    // 법인설립연구소_679-19-02150_20251231.xls
    // =============================================================

    private File renameDownloadedFile(
            File originalFile,
            String businessName,
            String businessNumber)
            throws Exception {


        String safeBusinessName =
                cleanFileNameValue(
                        businessName
                );


        String safeBusinessNumber =
                cleanFileNameValue(
                        businessNumber
                );


        String originalFileName =
                originalFile.getName();


        String newFileName =
                safeBusinessName
                + "_"
                + safeBusinessNumber
                + "_"
                + originalFileName;


        File targetFile =
                new File(
                        originalFile
                                .getParentFile(),
                        newFileName
                );


        Path movedPath =
                Files.move(
                        originalFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption
                                .REPLACE_EXISTING
                );


        System.out.println(
                "[RENAME] 파일명 변경 완료"
        );


        System.out.println(
                "상호명 = "
                + safeBusinessName
        );


        System.out.println(
                "사업자등록번호 = "
                + safeBusinessNumber
        );


        System.out.println(
                "기존 = "
                + originalFileName
        );


        System.out.println(
                "변경 = "
                + movedPath.getFileName()
        );


        return movedPath.toFile();
    }


    // =============================================================
    // Windows 파일명에 사용할 수 없는 문자 변경
    //
    // \ / : * ? " < > |
    //
    // 위 문자는 "_"로 변경
    // =============================================================

    private String cleanFileNameValue(
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


    // =============================================================
    // 조회기간 = 분기별
    // =============================================================

    private void selectQuarterPeriod() {

        List<WebElement> elements =
                driver.findElements(
                        By.xpath(
                                "//*[normalize-space(text())='분기별']"
                        )
                );


        for (WebElement element
                : elements) {

            try {

                if (!element.isDisplayed()) {

                    continue;
                }


                WebElement clickable =
                        findClickableParent(
                                element
                        );


                scrollTo(
                        clickable
                );


                clickable.click();


                return;


            } catch (Exception ignored) {
            }
        }


        throw new RuntimeException(
                "조회기간 '분기별'을 찾지 못했습니다."
        );
    }


    // =============================================================
    // 연도/분기 콤보박스
    //
    // order
    // 0 = 연도
    // 1 = 분기
    // =============================================================

    private void selectComboByOrder(
            int order,
            String visibleText,
            String alternativeValue) {


        // ---------------------------------------------------------
        // 일반 HTML select
        // ---------------------------------------------------------

        List<WebElement> selectElements =
                driver.findElements(
                        By.tagName(
                                "select"
                        )
                );


        /*
         * Java 16 이상:
         *
         * .toList()
         *
         * 를 사용할 수 있지만,
         * 현재 프로젝트 호환성을 위해
         *
         * .collect(Collectors.toList())
         *
         * 사용
         */

        List<WebElement> visibleSelects =
                selectElements
                        .stream()
                        .filter(element -> {

                            try {

                                return element.isDisplayed();

                            } catch (Exception e) {

                                return false;
                            }
                        })
                        .collect(
                                Collectors.toList()
                        );


        if (visibleSelects.size()
                > order) {


            Select select =
                    new Select(
                            visibleSelects.get(
                                    order
                            )
                    );


            try {

                select.selectByVisibleText(
                        visibleText
                );

                return;

            } catch (Exception ignored) {
            }


            try {

                select.selectByValue(
                        alternativeValue
                );

                return;

            } catch (Exception ignored) {
            }
        }


        // ---------------------------------------------------------
        // WebSquare selectbox
        // ---------------------------------------------------------

        List<WebElement> combos =
                driver.findElements(
                        By.cssSelector(
                                ".w2selectbox, "
                                + "[class*='selectbox'], "
                                + "[role='combobox']"
                        )
                );


        List<WebElement> visibleCombos =
                combos
                        .stream()
                        .filter(element -> {

                            try {

                                return element.isDisplayed();

                            } catch (Exception e) {

                                return false;
                            }
                        })
                        .collect(
                                Collectors.toList()
                        );


        if (visibleCombos.size()
                <= order) {

            throw new RuntimeException(
                    (order + 1)
                    + "번째 콤보박스를 찾지 못했습니다."
            );
        }


        WebElement combo =
                visibleCombos.get(
                        order
                );


        scrollTo(
                combo
        );

        combo.click();

        sleep(300);


        // ---------------------------------------------------------
        // 화면 표시값으로 찾기
        // ---------------------------------------------------------

        WebElement option =
                findVisibleTextElement(
                        visibleText
                );


        if (option != null) {

            WebElement clickable =
                    findClickableParent(
                            option
                    );


            clickable.click();

            return;
        }


        // ---------------------------------------------------------
        // 대체값으로 찾기
        // ---------------------------------------------------------

        option =
                findVisibleTextElement(
                        alternativeValue
                );


        if (option != null) {

            WebElement clickable =
                    findClickableParent(
                            option
                    );


            clickable.click();

            return;
        }


        throw new RuntimeException(
                "콤보박스에서 "
                + visibleText
                + " 값을 찾지 못했습니다."
        );
    }


    // =============================================================
    // 화면에서 표시된 텍스트 요소 찾기
    // =============================================================

    private WebElement findVisibleTextElement(
            String text) {

        List<WebElement> elements =
                driver.findElements(
                        By.xpath(
                                "//*[normalize-space(text())='"
                                + text
                                + "']"
                        )
                );


        for (WebElement element
                : elements) {

            try {

                if (element.isDisplayed()) {

                    return element;
                }

            } catch (Exception ignored) {
            }
        }


        return null;
    }


    // =============================================================
    // 클릭 가능한 부모 요소 찾기
    // =============================================================

    private WebElement findClickableParent(
            WebElement element) {

        try {

            String tag =
                    element.getTagName();


            if ("a".equalsIgnoreCase(tag)
                    || "button".equalsIgnoreCase(tag)
                    || "input".equalsIgnoreCase(tag)
                    || "option".equalsIgnoreCase(tag)) {

                return element;
            }


            WebElement current =
                    element;


            for (int i = 0;
                 i < 5;
                 i++) {


                current =
                        current.findElement(
                                By.xpath("..")
                        );


                String parentTag =
                        current.getTagName();


                String role =
                        current.getAttribute(
                                "role"
                        );


                if ("a".equalsIgnoreCase(parentTag)
                        || "button".equalsIgnoreCase(parentTag)
                        || "input".equalsIgnoreCase(parentTag)
                        || "li".equalsIgnoreCase(parentTag)
                        || "option".equalsIgnoreCase(parentTag)
                        || "button".equalsIgnoreCase(role)
                        || "option".equalsIgnoreCase(role)) {

                    return current;
                }
            }


            return element;


        } catch (Exception e) {

            return element;
        }
    }


    // =============================================================
    // 조회 결과 Alert 확인
    // =============================================================

    private void checkSearchResultAlert() {

        try {
            Alert resultAlert =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(2)
                    ).until(
                            ExpectedConditions.alertIsPresent()
                    );

            String alertText = resultAlert.getText();
            resultAlert.accept();

            if (alertText != null
                    && !alertText.trim().isEmpty()) {

                System.out.println(
                        "[WORK-7-1] 조회 결과 Alert = "
                        + alertText
                );

                throw new RuntimeException(alertText.trim());
            }

        } catch (org.openqa.selenium.TimeoutException e) {
            // Alert가 없으면 정상 조회 흐름이므로 그대로 진행한다.
        }
    }


    // =============================================================
    // 조회 완료 대기
    // =============================================================

    private void waitForSearchResult() {

        sleep(1000);


        try {

            wait.until(d -> {

                List<WebElement> loading =
                        d.findElements(
                                By.xpath(
                                        "//*[contains("
                                        + "normalize-space(.),"
                                        + "'조회중')]"
                                )
                        );


                for (WebElement element
                        : loading) {

                    try {

                        if (element.isDisplayed()) {

                            return false;
                        }

                    } catch (Exception ignored) {
                    }
                }


                return true;
            });

        } catch (Exception ignored) {
        }


        sleep(1000);
    }


    // =============================================================
    // 다운로드 완료 확인
    // =============================================================

    private File waitForExcelDownload(
            File folder,
            long startTime,
            int timeoutSeconds) {


        long timeout =
                System.currentTimeMillis()
                + timeoutSeconds
                * 1000L;


        while (System.currentTimeMillis()
                < timeout) {


            File[] files =
                    folder.listFiles();


            if (files != null) {


                // Chrome 다운로드 중 파일이 있는지 확인
            	boolean downloading =
            	        Arrays.stream(files)
            	            .anyMatch(file ->
            	                file.getName()
            	                    .toLowerCase()
            	                    .endsWith(".crdownload")
            	                &&
            	                file.lastModified() >= startTime
            	            );


                if (!downloading) {


                    File result =
                            Arrays.stream(files)

                                    .filter(
                                            File::isFile
                                    )

                                    .filter(file ->
                                            file.lastModified()
                                                    >= startTime
                                    )

                                    .filter(file -> {

                                        String name =
                                                file.getName()
                                                        .toLowerCase();


                                        return name.endsWith(
                                                ".xlsx"
                                        )
                                        ||
                                        name.endsWith(
                                                ".xls"
                                        )
                                        ||
                                        name.endsWith(
                                                ".csv"
                                        );
                                    })

                                    .max(
                                            Comparator
                                                    .comparingLong(
                                                            File::lastModified
                                                    )
                                    )

                                    .orElse(null);


                    if (result != null) {

                        return result;
                    }
                }
            }


            sleep(500);
        }


        return null;
    }


    // =============================================================
    // 연도 / 분기 검증
    // =============================================================

    private void validatePeriod(
            int year,
            int quarter) {


        if (year < 2000
                || year > 2100) {

            throw new IllegalArgumentException(
                    "잘못된 연도입니다: "
                    + year
            );
        }


        if (quarter < 1
                || quarter > 4) {

            throw new IllegalArgumentException(
                    "분기는 1~4만 가능합니다: "
                    + quarter
            );
        }
    }


    // =============================================================
    // 요소 위치로 스크롤
    // =============================================================

    private void scrollTo(
            WebElement element) {


        ((JavascriptExecutor) driver)
                .executeScript(
                        "arguments[0].scrollIntoView("
                        + "{block:'center'}"
                        + ");",
                        element
                );


        sleep(200);
    }


    // =============================================================
    // sleep
    // =============================================================

    private void sleep(
            long millis) {

        try {

            Thread.sleep(
                    millis
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }
}