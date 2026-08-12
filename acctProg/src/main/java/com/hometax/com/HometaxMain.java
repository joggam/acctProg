package com.hometax.com;

import java.io.File;

import org.openqa.selenium.WebDriver;

import egovframework.com.cmm.service.EgovProperties;

public class HometaxMain {

    public static void main(
            String[] args) {

        WebDriver driver = null;

        try {

            // =====================================================
            // ★ 홈택스 로그인 정보
            // =====================================================

            String hometaxId =
            		EgovProperties.getProperty("Globals.HometaxId");

            String hometaxPassword =
            		EgovProperties.getProperty("Globals.HometaxPwd");

            String juminFirst6 =
            		EgovProperties.getProperty("Globals.HometaxJuminFirst6");

            String jumin7th =
            		EgovProperties.getProperty("Globals.HometaxJumin7th");


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
            //
            // 반환 파일:
            //
            // 사업자명_사업자번호_최초다운로드파일명
            //
            // 예:
            // 법인설립연구소_679-19-02150_20251231.xls
            // =====================================================

            File excelFile =
                    hometax.downloadExcel(
                            year,
                            quarter
                    );


            System.out.println();

            System.out.println(
                    "[MAIN-1] 홈택스 다운로드 완료"
            );


            System.out.println(
                    "다운로드 파일명 = "
                    + excelFile.getName()
            );


            System.out.println(
                    "다운로드 파일경로 = "
                    + excelFile.getAbsolutePath()
            );


            // =====================================================
            // 4. ExcelTitleCopy 실행
            //
            // HometaxService에서 다운로드 + rename된 파일을
            // 그대로 SOURCE로 전달
            //
            // 여기서
            //
            // - 상호명 폴더 생성
            // - 신용카드매입자료_업로드.xls 복사
            // - TITLE 기준 데이터 복사
            // - 고정값 입력
            // - 최종 파일 생성
            //
            // 을 수행함
            // =====================================================

            File convertedFile =
                    ExcelTitleCopy.copyExcelByTitle(
                            excelFile
                    );


            System.out.println();

            System.out.println(
                    "[MAIN-2] ExcelTitleCopy 완료"
            );


            System.out.println(
                    "변환 파일명 = "
                    + convertedFile.getName()
            );


            System.out.println(
                    "변환 파일경로 = "
                    + convertedFile.getAbsolutePath()
            );


            // =====================================================
            // 5. 최종 결과
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
                    "최종 파일명 = "
                    + convertedFile.getName()
            );


            System.out.println(
                    "최종 파일경로 = "
                    + convertedFile.getAbsolutePath()
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