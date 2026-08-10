package com.hometax;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
    // 연도 / 분기 조회 → Excel 다운로드
    // =============================================================

    public File downloadExcel(
            int year,
            int quarter) {

        validatePeriod(year, quarter);

        try {

            System.out.println();
            System.out.println("======================================");
            System.out.println(
                    year + "년 "
                    + quarter + "분기 조회 시작"
            );
            System.out.println("======================================");


            // =====================================================
            // 1. 대상 메뉴 이동
            // =====================================================

            driver.get(TARGET_URL);

            System.out.println(
                    "[WORK-1] 대상 메뉴 이동 완료"
            );

            wait.until(d ->
                    d.findElements(
                            By.cssSelector("body *")
                    ).size() > 30
            );

            sleep(1500);


            // =====================================================
            // 2. 상호명 가져오기
            // =====================================================

            String businessName =
                    findBusinessName();

            System.out.println(
                    "[WORK-2] 상호명 = "
                    + businessName
            );


            // =====================================================
            // 3. 조회기간 = 분기별
            // =====================================================

            selectQuarterPeriod();

            System.out.println(
                    "[WORK-3] 조회기간 = 분기별"
            );

            sleep(500);


            // =====================================================
            // 4. 연도 선택
            // =====================================================

            selectComboByOrder(
                    0,
                    year + "년",
                    String.valueOf(year)
            );

            System.out.println(
                    "[WORK-4] 연도 = "
                    + year + "년"
            );

            sleep(500);


            // =====================================================
            // 5. 분기 선택
            // =====================================================

            selectComboByOrder(
                    1,
                    quarter + "분기",
                    String.valueOf(quarter)
            );

            System.out.println(
                    "[WORK-5] 분기 = "
                    + quarter + "분기"
            );

            sleep(500);


            // =====================================================
            // 6. 조회
            // =====================================================

            WebElement searchButton =
                    findVisibleButton("조회");

            if (searchButton == null) {

                throw new RuntimeException(
                        "조회 버튼을 찾지 못했습니다."
                );
            }

            System.out.println(
                    "[WORK-6] 조회 버튼 ID = "
                    + searchButton.getAttribute("id")
            );

            scrollTo(searchButton);

            searchButton.click();

            System.out.println(
                    "[WORK-7] 조회 클릭 완료"
            );


            // =====================================================
            // 7. 조회 결과 대기
            // =====================================================

            waitForSearchResult();

            System.out.println(
                    "[WORK-8] 조회 완료"
            );


            // =====================================================
            // 8. 내려받기
            // =====================================================

            WebElement downloadButton =
                    findVisibleButton(
                            "내려받기"
                    );

            if (downloadButton == null) {
                downloadButton =
                        findVisibleButton(
                                "다운로드"
                        );
            }

            if (downloadButton == null) {
                downloadButton =
                        findVisibleButton(
                                "엑셀 내려받기"
                        );
            }

            if (downloadButton == null) {
                downloadButton =
                        findVisibleButton(
                                "엑셀다운로드"
                        );
            }

            if (downloadButton == null) {

                throw new RuntimeException(
                        "내려받기 버튼을 찾지 못했습니다."
                );
            }


            System.out.println(
                    "[WORK-9] 내려받기 버튼 ID = "
                    + downloadButton.getAttribute("id")
            );


            scrollTo(downloadButton);

            downloadButton.click();


            System.out.println(
                    "[WORK-10] 내려받기 클릭 완료"
            );


            // =====================================================
            // 9. 팝업의 엑셀 버튼
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

                                if (element.isDisplayed()
                                        && element.isEnabled()) {

                                    WebElement clickable =
                                            findClickableParent(
                                                    element
                                            );

                                    if (clickable != null
                                            && clickable.isDisplayed()
                                            && clickable.isEnabled()) {

                                        return clickable;
                                    }
                                }

                            } catch (Exception ignored) {
                            }
                        }

                        return null;
                    });


            System.out.println(
                    "[WORK-11] 엑셀 버튼 ID = "
                    + excelButton.getAttribute("id")
            );


            scrollTo(excelButton);

            excelButton.click();

            System.out.println(
                    "[WORK-12] 엑셀 클릭 완료"
            );


            // =====================================================
            // 10. Alert
            // "파일을 받으시겠습니까?"
            // =====================================================

            Alert alert =
                    wait.until(
                        ExpectedConditions.alertIsPresent()
                    );


            System.out.println(
                    "[WORK-13] Alert = "
                    + alert.getText()
            );


            long downloadStart =
                    System.currentTimeMillis();


            alert.accept();


            System.out.println(
                    "[WORK-14] 다운로드 확인 완료"
            );


            // =====================================================
            // 11. 원본 Excel 다운로드 완료 대기
            // =====================================================

            File downloadedFile =
                    waitForExcelDownload(
                            new File(downloadDir),
                            downloadStart,
                            60
                    );


            if (downloadedFile == null) {

                throw new RuntimeException(
                        "다운로드 파일을 찾지 못했습니다."
                );
            }


            System.out.println(
                    "[WORK-15] 원본 파일 다운로드 완료"
            );

            System.out.println(
                    "원본 파일명 = "
                    + downloadedFile.getName()
            );


            // =====================================================
            // 12. 파일명 변경
            //
            // 상호명_yyyyMMdd.xlsx
            // =====================================================

            File renamedFile =
                    renameDownloadedFile(
                            downloadedFile,
                            businessName
                    );


            System.out.println();
            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "[WORK-16] 최종 작업 성공"
            );

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "상호 = "
                    + businessName
            );

            System.out.println(
                    "파일명 = "
                    + renamedFile.getName()
            );

            System.out.println(
                    "파일 위치 = "
                    + renamedFile.getAbsolutePath()
            );


            return renamedFile;


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
    // =============================================================

    private String findBusinessName() {

        WebElement businessNameElement =
                wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                        By.id("mf_txppWframe_txprNm")
                    )
                );

        String businessName =
                businessNameElement.getText();

        if (businessName == null
                || businessName.trim().isEmpty()) {

            businessName =
                    businessNameElement.getAttribute("value");
        }

        if (businessName == null
                || businessName.trim().isEmpty()) {

            throw new RuntimeException(
                    "상호명을 가져오지 못했습니다."
            );
        }

        businessName =
                cleanBusinessName(
                        businessName
                );

        System.out.println(
                "[WORK-2] 상호명 = "
                + businessName
        );

        return businessName;
    }


    

    // =============================================================
    // 파일명에 사용할 수 있도록 정리
    // =============================================================

    private String cleanBusinessName(
            String businessName) {

        return businessName
                .trim()
                .replaceAll(
                    "[\\\\/:*?\"<>|]",
                    "_"
                );
    }


	 // =============================================================
	 // 다운로드 파일명 변경
	 //
	 // 예:
	 // 기존 파일명 : 부가가치세자료.xlsx
	 // 상호명      : 가나다상사
	 //
	 // 최종 파일명 : 가나다상사_부가가치세자료.xlsx
	 // =============================================================

    private File renameDownloadedFile(
	         File originalFile,
	         String businessName)
	         throws Exception {


	     // =========================================================
	     // 상호명 정리
	     // Windows 파일명에 사용할 수 없는 문자 제거
	     // =========================================================
	
	     String safeBusinessName =
	             cleanBusinessName(
	                     businessName
	             );
	
	
	     // =========================================================
	     // 홈택스에서 받은 기존 파일명
	     // =========================================================
	
	     String originalFileName =
	             originalFile.getName();
	
	
	     // =========================================================
	     // 최종 파일명
	     //
	     // 상호명_기존파일명
	     // =========================================================
	
	     String newFileName =
	             safeBusinessName
	             + "_"
	             + originalFileName;
	
	
	     // =========================================================
	     // 최종 파일 경로
	     // =========================================================
	
	     File targetFile =
	             new File(
	                     originalFile.getParentFile(),
	                     newFileName
	             );
	
	
	     // =========================================================
	     // 파일 이름 변경
	     //
	     // 동일한 파일명이 이미 존재하면 덮어쓰기
	     // =========================================================
	
	     Path movedPath =
	             Files.move(
	                     originalFile.toPath(),
	                     targetFile.toPath(),
	                     StandardCopyOption.REPLACE_EXISTING
	             );
	
	
	     System.out.println(
	             "[WORK-16] 파일명 변경 완료"
	     );
	
	     System.out.println(
	             "상호명 = "
	             + businessName
	     );
	
	     System.out.println(
	             "기존 파일명 = "
	             + originalFileName
	     );
	
	     System.out.println(
	             "변경 파일명 = "
	             + movedPath.getFileName()
	     );
	
	
	     return movedPath.toFile();
	 }

    // =============================================================
    // 유효성 검증
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
    // 분기별 선택
    // =============================================================

    private void selectQuarterPeriod() {

        List<WebElement> elements =
                driver.findElements(
                    By.xpath(
                        "//*[normalize-space(text())='분기별']"
                    )
                );


        for (WebElement element : elements) {

            try {

                if (!element.isDisplayed()) {
                    continue;
                }


                WebElement clickable =
                        findClickableParent(
                                element
                        );


                if (clickable != null) {

                    scrollTo(clickable);

                    clickable.click();

                    return;
                }

            } catch (Exception ignored) {
            }
        }


        throw new RuntimeException(
                "조회기간 '분기별'을 찾지 못했습니다."
        );
    }


    // =============================================================
    // 콤보박스 선택
    // =============================================================

    private void selectComboByOrder(
            int order,
            String visibleText,
            String alternativeValue) {


        // 일반 SELECT
        List<WebElement> selectElements =
                driver.findElements(
                        By.tagName("select")
                );


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
                        .toList();


        if (visibleSelects.size() > order) {

            Select select =
                    new Select(
                            visibleSelects.get(order)
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


        // WebSquare
        List<WebElement> combos =
                driver.findElements(
                    By.cssSelector(
                        ".w2selectbox, "
                        + "[class*='selectbox'], "
                        + "[role='combobox']"
                    )
                );


        List<WebElement> visibleCombos =
                combos.stream()
                        .filter(element -> {

                            try {

                                return element.isDisplayed();

                            } catch (Exception e) {

                                return false;
                            }
                        })
                        .toList();


        if (visibleCombos.size() <= order) {

            throw new RuntimeException(
                    (order + 1)
                    + "번째 콤보박스를 찾지 못했습니다."
            );
        }


        WebElement combo =
                visibleCombos.get(order);


        scrollTo(combo);

        combo.click();

        sleep(300);


        WebElement option =
                findVisibleTextElement(
                        visibleText
                );


        if (option != null) {

            WebElement clickable =
                    findClickableParent(
                            option
                    );


            if (clickable != null) {

                clickable.click();

                return;
            }
        }


        option =
                findVisibleTextElement(
                        alternativeValue
                );


        if (option != null) {

            WebElement clickable =
                    findClickableParent(
                            option
                    );


            if (clickable != null) {

                clickable.click();

                return;
            }
        }


        throw new RuntimeException(
                visibleText
                + " 값을 찾지 못했습니다."
        );
    }


    // =============================================================
    // 텍스트 요소
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


        for (WebElement element : elements) {

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
    // 버튼 찾기
    // =============================================================

    private WebElement findVisibleButton(
            String text) {

        List<WebElement> elements =
                driver.findElements(
                    By.xpath(
                        "//*["
                        + "self::a "
                        + "or self::button "
                        + "or self::input "
                        + "or self::span"
                        + "]"
                        + "["
                        + "normalize-space(.)='"
                        + text
                        + "' "
                        + "or @value='"
                        + text
                        + "' "
                        + "or @title='"
                        + text
                        + "'"
                        + "]"
                    )
                );


        for (WebElement element : elements) {

            try {

                if (element.isDisplayed()
                        && element.isEnabled()) {

                    return findClickableParent(
                            element
                    );
                }

            } catch (Exception ignored) {
            }
        }


        return null;
    }


    // =============================================================
    // 클릭 가능한 부모
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


            for (int i = 0; i < 5; i++) {

                current =
                        current.findElement(
                                By.xpath("..")
                        );


                String parentTag =
                        current.getTagName();


                String role =
                        current.getAttribute("role");


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
    // 조회 결과 대기
    // =============================================================

    private void waitForSearchResult() {

        sleep(1500);


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


                for (WebElement element : loading) {

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


        sleep(1500);
    }


    // =============================================================
    // Excel 다운로드 완료 확인
    // =============================================================

    private File waitForExcelDownload(
            File folder,
            long startTime,
            int timeoutSeconds) {


        long timeout =
                System.currentTimeMillis()
                + timeoutSeconds * 1000L;


        while (System.currentTimeMillis()
                < timeout) {


            File[] files =
                    folder.listFiles();


            if (files != null) {


                boolean downloading =
                        Arrays.stream(files)
                                .anyMatch(file ->
                                    file.getName()
                                            .toLowerCase()
                                            .endsWith(
                                                ".crdownload"
                                            )
                                );


                if (!downloading) {


                    File result =
                            Arrays.stream(files)

                                    .filter(File::isFile)

                                    .filter(file ->
                                        file.lastModified()
                                                >= startTime
                                    )

                                    .filter(file -> {

                                        String name =
                                                file.getName()
                                                        .toLowerCase();


                                        return name.endsWith(".xlsx")
                                                || name.endsWith(".xls")
                                                || name.endsWith(".csv");
                                    })

                                    .max(
                                        Comparator.comparingLong(
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
    // 스크롤
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


        sleep(300);
    }


    // =============================================================
    // sleep
    // =============================================================

    private void sleep(
            long millis) {

        try {

            Thread.sleep(millis);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }
}