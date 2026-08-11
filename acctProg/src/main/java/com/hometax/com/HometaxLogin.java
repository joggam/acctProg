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
            // 2. 상단 로그인
            // =====================================================

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


            // =====================================================
            // 3. 아이디 로그인 탭
            // =====================================================

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


            // =====================================================
            // 4. 아이디 입력
            // =====================================================

            WebElement idInput =
                    wait.until(
                        ExpectedConditions
                            .visibilityOfElementLocated(
                                By.id(
                                    "mf_txppWframe_iptUserId"
                                )
                            )
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
            // =====================================================

            wait.until(d ->

                d.findElements(
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
                })
            );


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
}