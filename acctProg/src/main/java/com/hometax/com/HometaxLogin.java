package com.hometax.com;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HometaxLogin {

    private static final String HOMETAX_URL =
            "https://www.hometax.go.kr";


    public static WebDriver login(
            String hometaxId,
            String hometaxPassword,
            String juminFirst6,
            String jumin7th,
            String downloadDir) {

        ChromeDriver driver = null;

        try {

            // =====================================================
            // 다운로드 폴더 생성
            // =====================================================

            File downloadFolder =
                    new File(downloadDir);

            if (!downloadFolder.exists()) {

                boolean created =
                        downloadFolder.mkdirs();

                if (!created
                        && !downloadFolder.exists()) {

                    throw new RuntimeException(
                            "다운로드 폴더 생성 실패: "
                            + downloadDir
                    );
                }
            }


            String absoluteDownloadDir =
                    downloadFolder
                            .getAbsolutePath();


            // =====================================================
            // Chrome 다운로드 설정
            // =====================================================

            Map<String, Object> prefs =
                    new HashMap<>();

            prefs.put(
                    "download.default_directory",
                    absoluteDownloadDir
            );

            prefs.put(
                    "download.prompt_for_download",
                    false
            );

            prefs.put(
                    "download.directory_upgrade",
                    true
            );


            // =====================================================
            // ChromeOptions
            // =====================================================

            ChromeOptions options =
                    new ChromeOptions();

            options.setExperimentalOption(
                    "prefs",
                    prefs
            );


            // =====================================================
            // ★ Headless
            //
            // Chrome 창을 화면에 표시하지 않음
            // =====================================================

            options.addArguments(
                    "--headless=new"
            );

            options.addArguments(
                    "--window-size=1920,1080"
            );

            options.addArguments(
                    "--disable-gpu"
            );

            options.addArguments(
                    "--disable-dev-shm-usage"
            );

            options.addArguments(
                    "--disable-notifications"
            );


            // =====================================================
            // Chrome 실행
            // =====================================================

            driver =
                    new ChromeDriver(options);


            /*
             * Headless Chrome에서도 다운로드 허용
             */
            Map<String, Object> downloadParams =
                    new HashMap<>();

            downloadParams.put(
                    "behavior",
                    "allow"
            );

            downloadParams.put(
                    "downloadPath",
                    absoluteDownloadDir
            );

            driver.executeCdpCommand(
                    "Page.setDownloadBehavior",
                    downloadParams
            );


            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(30)
                    );


            // =====================================================
            // 1. 홈택스 접속
            // =====================================================

            driver.get(HOMETAX_URL);

            System.out.println(
                    "[LOGIN-1] 홈택스 접속 완료"
            );


            // =====================================================
            // 2~4. 로그인 화면 진입 + ID 입력
            //
            // WebSquare 로딩 문제로 ID 입력란이 30초 내 표시되지 않으면
            // 홈택스 첫 화면부터 1회 자동 재시도한다.
            // =====================================================

            WebElement idInput =
                    openIdLoginAndWaitForIdInput(
                            driver,
                            wait
                    );

            idInput.clear();

            idInput.sendKeys(
                    hometaxId
            );


// =====================================================
            // 5. 비밀번호 입력
            // =====================================================

            WebElement passwordInput =
                    wait.until(
                        ExpectedConditions
                            .visibilityOfElementLocated(
                                By.id(
                                    "mf_txppWframe_iptUserPw"
                                )
                            )
                    );

            passwordInput.clear();

            passwordInput.sendKeys(
                    hometaxPassword
            );


            // =====================================================
            // 6. 1차 로그인
            // =====================================================

            WebElement loginButton =
                    wait.until(
                        ExpectedConditions
                            .elementToBeClickable(
                                By.id(
                                    "mf_txppWframe_anchor25"
                                )
                            )
                    );

            loginButton.click();


            System.out.println(
                    "[LOGIN-2] ID/PW 인증 완료"
            );


            // =====================================================
            // 7. 주민번호 앞 6자리
            // =====================================================

            WebElement jumin1 =
                    wait.until(
                        ExpectedConditions
                            .visibilityOfElementLocated(
                                By.id(
                                    "mf_txppWframe_UTXPPABC12_wframe_iptUserJuminNo1"
                                )
                            )
                    );

            jumin1.clear();

            jumin1.sendKeys(
                    juminFirst6
            );


            // =====================================================
            // 8. 주민번호 7번째 숫자
            // =====================================================

            WebElement jumin2 =
                    wait.until(
                        ExpectedConditions
                            .visibilityOfElementLocated(
                                By.id(
                                    "mf_txppWframe_UTXPPABC12_wframe_iptUserJuminNo2"
                                )
                            )
                    );

            jumin2.clear();

            jumin2.sendKeys(
                    jumin7th
            );


            // =====================================================
            // 9. 2차 인증 확인
            // =====================================================

            String originalWindow =
                    driver.getWindowHandle();

            WebElement secondAuth =
                    wait.until(
                        ExpectedConditions
                            .elementToBeClickable(
                                By.id(
                                    "mf_txppWframe_UTXPPABC12_wframe_trigger46"
                                )
                            )
                    );

            secondAuth.click();


            System.out.println(
                    "[LOGIN-3] 2차 인증 요청 완료"
            );


            // =====================================================
            // 10. 로그인 성공 확인
            //
            // ① 일반 로그인: 로그아웃 표시
            // ② 보안카드 발급 팝업:
            //    팝업 취소 -> 지정된 Alert 문구 확인 -> 확인 클릭
            // =====================================================

            boolean loginSuccess =
                    waitForLoginSuccessOrSecurityCardPopup(
                            driver,
                            originalWindow
                    );

            if (!loginSuccess) {

                throw new RuntimeException(
                        "홈택스 로그인 성공 여부를 확인하지 못했습니다."
                );
            }


            System.out.println(
                    "[LOGIN-4] 홈택스 로그인 성공"
            );


            // 로그인된 Chrome 반환
            return driver;


        } catch (Exception e) {

            if (driver != null) {

                try {

                    driver.quit();

                } catch (Exception ignored) {
                }
            }


            throw new RuntimeException(
                    "홈택스 로그인 실패",
                    e
            );
        }
    }


    /**
     * 로그인 성공 또는 보안카드 발급 안내 팝업을 처리한다.
     *
     * 일반 로그인은 '로그아웃' 표시로 성공 처리한다.
     * 보안카드 팝업은 취소 버튼 클릭 후 아래 Alert 문구가 정확히 일치할 때만
     * 확인을 누르고 일반 로그인 성공으로 처리한다.
     */
    private static boolean waitForLoginSuccessOrSecurityCardPopup(
            WebDriver driver,
            String originalWindow) {

        final String expectedAlertText =
                "보안카드 인증을 취소하시고, 일반 로그인을 하시겠습니까?";

        long endTime =
                System.currentTimeMillis()
                + Duration.ofSeconds(30).toMillis();

        while (System.currentTimeMillis() < endTime) {

            // =====================================================
            // 1. 기존 정상 로그인 성공 확인
            // =====================================================
            try {

                boolean logoutVisible =
                        driver.findElements(
                            By.xpath(
                                "//*[normalize-space(text())='로그아웃']"
                            )
                        )
                        .stream()
                        .anyMatch(element -> {

                            try {
                                return element.isDisplayed();
                            } catch (Exception e) {
                                return false;
                            }
                        });

                if (logoutVisible) {

                    System.out.println(
                            "[LOGIN-NORMAL] 일반 로그인 성공"
                    );

                    return true;
                }

            } catch (Exception ignored) {
            }


            // =====================================================
            // 2. 보안카드 발급 팝업 확인
            // =====================================================
            try {

                String securityCardWindow =
                        findSecurityCardPopupWindow(driver);

                if (securityCardWindow != null) {

                    System.out.println(
                            "[LOGIN-SECURITY-CARD] 보안카드 발급 팝업 감지"
                    );

                    driver.switchTo().window(securityCardWindow);

                    WebDriverWait popupWait =
                            new WebDriverWait(
                                    driver,
                                    Duration.ofSeconds(10)
                            );

                    WebElement cancelButton =
                            popupWait.until(d -> {

                                for (WebElement element :
                                        d.findElements(
                                            By.xpath(
                                                "//*["
                                                + "self::button "
                                                + "or self::a "
                                                + "or self::span "
                                                + "or self::input"
                                                + "]["
                                                + "normalize-space(text())='취소' "
                                                + "or @value='취소'"
                                                + "]"
                                            )
                                        )) {

                                    try {

                                        if (element.isDisplayed()
                                                && element.isEnabled()) {

                                            return element;
                                        }

                                    } catch (Exception ignored) {
                                    }
                                }

                                return null;
                            });

                    cancelButton.click();

                    System.out.println(
                            "[LOGIN-SECURITY-CARD] 보안카드 팝업 취소 클릭 완료"
                    );

                    popupWait.until(
                        ExpectedConditions.alertIsPresent()
                    );

                    String actualAlertText =
                            driver.switchTo()
                                  .alert()
                                  .getText();

                    String normalizedActual =
                            normalizeWhitespace(actualAlertText);

                    String normalizedExpected =
                            normalizeWhitespace(expectedAlertText);

                    System.out.println(
                            "[LOGIN-SECURITY-CARD-ALERT] "
                            + normalizedActual
                    );

                    // 지정된 보안카드 취소 Alert 문구와 정확히 일치할 때만 확인
                    if (!normalizedExpected.equals(normalizedActual)) {

                        System.out.println(
                                "[LOGIN-SECURITY-CARD-ALERT-MISMATCH] "
                                + "예상하지 못한 Alert이므로 로그인 성공 처리하지 않음"
                        );

                        try {
                            driver.switchTo().alert().dismiss();
                        } catch (Exception ignored) {
                        }

                        return false;
                    }

                    driver.switchTo()
                          .alert()
                          .accept();

                    System.out.println(
                            "[LOGIN-SECURITY-CARD] 일반 로그인 Alert 확인 완료"
                    );

                    sleep(1000);

                    // Alert 처리 후 원래 홈택스 창으로 복귀
                    try {

                        if (driver.getWindowHandles()
                                  .contains(originalWindow)) {

                            driver.switchTo()
                                  .window(originalWindow);
                        }

                    } catch (Exception ignored) {
                    }

                    System.out.println(
                            "[LOGIN-SECURITY-CARD] "
                            + "보안카드 인증 취소 후 일반 로그인 처리 완료"
                    );

                    return true;
                }

            } catch (Exception e) {

                System.out.println(
                        "[LOGIN-SECURITY-CARD-FAIL] "
                        + firstLine(e.getMessage())
                );
            }

            sleep(500);
        }

        return false;
    }


    /**
     * 보안카드 발급 팝업 창을 찾는다.
     */
    private static String findSecurityCardPopupWindow(
            WebDriver driver) {

        String currentWindow = null;

        try {
            currentWindow = driver.getWindowHandle();
        } catch (Exception ignored) {
        }

        for (String windowHandle :
                driver.getWindowHandles()) {

            try {

                driver.switchTo().window(windowHandle);

                String currentUrl =
                        driver.getCurrentUrl();

                if (currentUrl != null
                        && (
                            currentUrl.contains(
                                "UTECMABA04.xml"
                            )
                            ||
                            currentUrl.contains(
                                "popupID=mf_txppWframe_UTXPPABA08"
                            )
                        )) {

                    return windowHandle;
                }

            } catch (Exception ignored) {
            }
        }

        if (currentWindow != null) {

            try {
                driver.switchTo().window(currentWindow);
            } catch (Exception ignored) {
            }
        }

        return null;
    }


    /**
     * Alert 문구의 줄바꿈/연속 공백 차이만 제거한다.
     * 문구 자체는 완전히 동일해야 성공으로 인정한다.
     */
    private static String normalizeWhitespace(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 홈택스 ID 로그인 화면으로 진입한다.
     *
     * 1차 시도에서 mf_txppWframe_iptUserId가 표시되지 않으면
     * 홈택스 메인으로 다시 이동한 뒤 로그인 화면 진입을 1회 재시도한다.
     */
    private static WebElement openIdLoginAndWaitForIdInput(
            WebDriver driver,
            WebDriverWait wait) {

        Exception lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {

            try {

                if (attempt > 1) {

                    System.out.println(
                            "[LOGIN-RETRY] 로그인 화면 로딩 실패로 1회 재시도"
                    );

                    driver.get(HOMETAX_URL);
                    sleep(1000);
                }

                WebElement mainLogin =
                        wait.until(
                            ExpectedConditions
                                .elementToBeClickable(
                                    By.id(
                                        "mf_wfHeader_group1503"
                                    )
                                )
                        );

                mainLogin.click();

                WebElement idLoginTab =
                        wait.until(
                            ExpectedConditions
                                .elementToBeClickable(
                                    By.id(
                                        "mf_txppWframe_anchor15"
                                    )
                                )
                        );

                idLoginTab.click();

                WebElement idInput =
                        wait.until(
                            ExpectedConditions
                                .visibilityOfElementLocated(
                                    By.id(
                                        "mf_txppWframe_iptUserId"
                                    )
                                )
                        );

                if (attempt > 1) {
                    System.out.println(
                            "[LOGIN-RETRY-SUCCESS] ID 입력란 재로딩 성공"
                    );
                }

                return idInput;

            } catch (Exception e) {

                lastException = e;

                System.out.println(
                        "[LOGIN-LOAD-FAIL] "
                        + attempt
                        + "차 시도 실패 / "
                        + firstLine(e.getMessage())
                );
            }
        }

        throw new RuntimeException(
                "홈택스 로그인 화면 로딩 실패(ID 입력란 미표시)",
                lastException
        );
    }


    private static String firstLine(String value) {

        if (value == null || value.trim().length() == 0) {
            return "";
        }

        String result = value.trim();

        int lineBreak = result.indexOf('\n');

        if (lineBreak >= 0) {
            result = result.substring(0, lineBreak);
        }

        return result.trim();
    }


    private static void sleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}