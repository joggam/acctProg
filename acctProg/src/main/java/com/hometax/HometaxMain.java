package com.hometax;

import java.io.File;

import org.openqa.selenium.WebDriver;

public class HometaxMain {

    public static void main(String[] args) {

        WebDriver driver = null;

        try {

            // =====================================================
            // 로그인 정보
            //
            // 실제 운영에서는 DB/환경변수/암호화 설정 등으로
            // 분리하는 것을 권장
            // =====================================================

            String hometaxId =
                    "";

            String hometaxPassword =
                    "";

            String juminFirst6 =
                    "901204";

            String jumin7th =
                    "2";


            // =====================================================
            // 다운로드 폴더
            // =====================================================

            String downloadDir =
                    "C:\\hometax_download";


            // =====================================================
            // ★ 시스템에서 받을 값
            //
            // 지금은 테스트이므로 직접 지정
            // =====================================================

            int year = 2025;

            int quarter = 1;


            // =====================================================
            // 1. 홈택스 로그인
            // =====================================================

            driver =
                    HometaxLogin.login(
                            hometaxId,
                            hometaxPassword,
                            juminFirst6,
                            jumin7th,
                            downloadDir
                    );


            // =====================================================
            // 2. 홈택스 서비스
            // =====================================================

            HometaxService hometax =
                    new HometaxService(
                            driver,
                            downloadDir
                    );


            // =====================================================
            // 3. 시스템에서 전달받은 연도/분기로 조회
            // =====================================================

            File excelFile =
                    hometax.downloadExcel(
                            year,
                            quarter
                    );


            // =====================================================
            // 4. 결과
            // =====================================================

            System.out.println();
            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "작업 완료"
            );

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "조회 기간 = "
                    + year
                    + "년 "
                    + quarter
                    + "분기"
            );

            System.out.println(
                    "파일명 = "
                    + excelFile.getName()
            );

            System.out.println(
                    "파일 경로 = "
                    + excelFile.getAbsolutePath()
            );


        } catch (Exception e) {

            System.out.println(
                    "프로그램 실행 실패"
            );

            e.printStackTrace();

        } finally {

            /*
             * 개발 중에는 화면 확인을 위해
             * Chrome을 닫지 않습니다.
             *
             * 운영할 때 활성화:
             */

            // if (driver != null) {
            //     driver.quit();
            // }
        }
    }
}