package com.hometax.com;

import java.io.File;

import org.openqa.selenium.WebDriver;

public class HometaxMain {

    public static void main(
            String[] args) {

        WebDriver driver = null;

        try {

            // =====================================================
            // ★ 홈택스 로그인 정보
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
            // ★ 나중에는 시스템에서 전달받을 값
            //
            // 현재 테스트
            // =====================================================

            int year = 2025;

            int quarter = 1;


            // =====================================================
            // 1. 홈택스 로그인
            //
            // Chrome은 Headless로 실행됨
            // 화면에 브라우저가 뜨지 않음
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
            // 2. 서비스 생성
            // =====================================================

            HometaxService hometax =
                    new HometaxService(
                            driver,
                            downloadDir
                    );


            // =====================================================
            // 3. 조회 + Excel 다운로드
            // =====================================================

            File excelFile =
                    hometax.downloadExcel(
                            year,
                            quarter
                    );


            // =====================================================
            // 4. 최종 결과
            // =====================================================

            System.out.println();

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "전체 작업 완료"
            );

            System.out.println(
                    "======================================"
            );


            System.out.println(
                    "조회기간 = "
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
                    "파일경로 = "
                    + excelFile.getAbsolutePath()
            );


        } catch (Exception e) {

            System.out.println();

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "프로그램 실행 실패"
            );

            System.out.println(
                    "======================================"
            );


            e.printStackTrace();


        } finally {

            // =====================================================
            // 작업 성공/실패 관계없이 Chrome 종료
            //
            // Headless이므로 화면에는 안 보이지만
            // Chrome 프로세스 자체를 종료함
            // =====================================================

            if (driver != null) {

                try {

                    driver.quit();


                    System.out.println(
                            "Chrome 종료 완료"
                    );


                } catch (Exception e) {

                    System.out.println(
                            "Chrome 종료 중 오류 발생"
                    );
                }
            }
        }
    }
}