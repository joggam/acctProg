package vat.home.card.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.com.cmm.annotation.IncludedInfo;

import vat.home.card.service.VatCardPurchaseService;
import vat.home.card.service.VatCardPurchaseVO;

/**
 * 사업용신용카드 매입세액 공제 확인/변경 Controller
 */
@Controller
public class VatCardPurchaseController {

    @Resource(name = "vatCardPurchaseService")
    private VatCardPurchaseService vatCardPurchaseService;

    @IncludedInfo(
            name = "사업용신용카드",
            listUrl = "/vat/home/card/selectVatCardPurchaseList.do",
            order = 11,
            gid = 1
    )
    @RequestMapping("/vat/home/card/selectVatCardPurchaseList.do")
    public String selectVatCardPurchaseList(
            @ModelAttribute("searchVO") VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        setDefaultSearchCondition(searchVO);

        // 페이지당 조회 건수는 화면에서 10/30/50/100/200/ALL(0)만 허용한다.
        int requestedPageUnit = searchVO.getPageUnit();
        if (requestedPageUnit != 0
                && requestedPageUnit != 10
                && requestedPageUnit != 30
                && requestedPageUnit != 50
                && requestedPageUnit != 100
                && requestedPageUnit != 200) {
            requestedPageUnit = 10;
            searchVO.setPageUnit(10);
        }

        // ALL 처리를 위해 전체 건수를 먼저 구한다.
        int totalCount =
                vatCardPurchaseService.selectEntrprsMberListTotCnt(
                        searchVO
                );

        int recordCountPerPage;
        if (requestedPageUnit == 0) {
            // pageUnit=0은 화면에서 ALL을 의미한다.
            recordCountPerPage = totalCount > 0 ? totalCount : 1;
            searchVO.setPageIndex(1);
        } else {
            recordCountPerPage = requestedPageUnit;
        }

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(recordCountPerPage);
        paginationInfo.setPageSize(searchVO.getPageSize());
        paginationInfo.setTotalRecordCount(totalCount);

        searchVO.setFirstIndex(
                paginationInfo.getFirstRecordIndex()
        );
        searchVO.setLastIndex(
                paginationInfo.getLastRecordIndex()
        );
        searchVO.setRecordCountPerPage(
                paginationInfo.getRecordCountPerPage()
        );

        List<VatCardPurchaseVO> resultList =
                vatCardPurchaseService.selectEntrprsMberList(
                        searchVO
                );

        model.addAttribute("yearList", createYearList());
        model.addAttribute("resultList", resultList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paginationInfo", paginationInfo);

        return "vat/home/card/VatCardPurchaseList";
    }

    /**
     * 체크된 기업회원의 홈택스 자료 내려받기/가공 처리.
     * 화면에서는 selectedBizrSeq 값으로 COMTNENTRPRSBIZR.BIZR_SEQ만 전달한다.
     */
    @RequestMapping("/vat/home/card/downloadSelectedEntrprsMber.do")
    public String downloadSelectedEntrprsMber(
            @RequestParam(value = "selectedBizrSeq", required = false)
            String[] selectedBizrSeq,
            @ModelAttribute("searchVO") VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        setDefaultSearchCondition(searchVO);

        if (selectedBizrSeq == null
                || selectedBizrSeq.length == 0) {

            model.addAttribute(
                    "resultMsg",
                    "내려받을 사업자등록번호를 선택해 주세요."
            );

            return selectVatCardPurchaseList(searchVO, model);
        }

        if (!"QUARTER".equals(searchVO.getSearchPeriodType())) {
            model.addAttribute(
                    "resultMsg",
                    "현재 홈택스 자동 내려받기는 분기별 조회만 지원합니다."
            );

            return selectVatCardPurchaseList(searchVO, model);
        }

        int year = Integer.parseInt(searchVO.getSearchYear());
        int quarter = Integer.parseInt(searchVO.getSearchQuarter());

        int successCount = 0;
        List<String> failMessages = new ArrayList<String>();
        List<String> resultFiles = new ArrayList<String>();

        for (String bizrSeqValue : selectedBizrSeq) {

            if (bizrSeqValue == null
                    || bizrSeqValue.trim().length() == 0) {
                continue;
            }

            VatCardPurchaseVO businessInfo = null;

            try {
                Long bizrSeq = Long.valueOf(bizrSeqValue.trim());

                // 실패하더라도 TXT에 상호명/아이디/사업자등록번호를 남길 수 있도록
                // 다운로드 실행 전에 대상 사업자 정보를 먼저 조회한다.
                businessInfo =
                        vatCardPurchaseService.selectEntrprsMberLoginInfo(
                                bizrSeq
                        );

                File resultFile =
                        vatCardPurchaseService.downloadHometaxExcel(
                                bizrSeq,
                                year,
                                quarter
                        );

                successCount++;

                if (resultFile != null) {
                    resultFiles.add(
                            resultFile.getAbsolutePath()
                    );
                }

            } catch (Exception e) {

                // 상위 RuntimeException 메시지만 사용하면
                // "2026년 1분기 다운로드 실패"처럼 실제 원인이 사라질 수 있다.
                // cause 체인을 끝까지 확인해서 Selenium Alert/Timeout 등의
                // 실제 실패 원인을 TXT에 기록한다.
                String message = getDetailedDownloadFailReason(e);

                failMessages.add(
                        buildDownloadFailMessage(
                                businessInfo,
                                bizrSeqValue,
                                message
                        )
                );
            }
        }

        if (!failMessages.isEmpty()) {
            File failTextFile = writeDownloadFailText(
                    failMessages,
                    year,
                    quarter
            );

            if (failTextFile != null) {
                resultFiles.add(failTextFile.getAbsolutePath());
                model.addAttribute(
                        "downloadFailTextFile",
                        failTextFile.getAbsolutePath()
                );
            }
        }

        model.addAttribute("downloadSuccessCount", successCount);
        model.addAttribute("downloadFailCount", failMessages.size());
        model.addAttribute("downloadFailMessages", failMessages);
        model.addAttribute("downloadResultFiles", resultFiles);

        if (failMessages.isEmpty()) {
            model.addAttribute(
                    "resultMsg",
                    "선택한 "
                    + successCount
                    + "개 사업자의 홈택스 자료 내려받기가 완료되었습니다."
            );
        } else {
            model.addAttribute(
                    "resultMsg",
                    "홈택스 자료 내려받기 완료: 성공 "
                    + successCount
                    + "건, 실패 "
                    + failMessages.size()
                    + "건"
            );
        }

        return selectVatCardPurchaseList(searchVO, model);
    }

    /**
     * 선택된 기업들의 홈택스 자료를 최종 XLS 한 파일로 합쳐 내려받는다.
     * ExcelTitleCopy는 사용하지 않는다.
     */
    @RequestMapping("/vat/home/card/downloadMergedEntrprsMber.do")
    public String downloadMergedEntrprsMber(
            @RequestParam(value = "selectedBizrSeq", required = false)
            String[] selectedBizrSeq,
            @ModelAttribute("searchVO") VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        setDefaultSearchCondition(searchVO);

        if (selectedBizrSeq == null
                || selectedBizrSeq.length == 0) {

            model.addAttribute(
                    "resultMsg",
                    "분류내려받기 할 사업자등록번호를 선택해 주세요."
            );

            return selectVatCardPurchaseList(searchVO, model);
        }

        if (!"QUARTER".equals(searchVO.getSearchPeriodType())) {
            model.addAttribute(
                    "resultMsg",
                    "현재 분류내려받기는 분기별 조회만 지원합니다."
            );

            return selectVatCardPurchaseList(searchVO, model);
        }

        int year = Integer.parseInt(searchVO.getSearchYear());
        int quarter = Integer.parseInt(searchVO.getSearchQuarter());

        try {
            File resultFile =
                    vatCardPurchaseService.downloadMergedHometaxExcel(
                            selectedBizrSeq,
                            year,
                            quarter
                    );

            List<String> resultFiles = new ArrayList<String>();
            if (resultFile != null) {
                resultFiles.add(resultFile.getAbsolutePath());
            }
            model.addAttribute(
                    "downloadResultFiles",
                    resultFiles
            );

            model.addAttribute(
                    "resultMsg",
                    "선택한 사업자 자료를 한 파일로 분류내려받기 완료했습니다."
            );

        } catch (Exception e) {
            String message = e.getMessage();

            if (message == null || message.trim().length() == 0) {
                message = e.getClass().getSimpleName();
            }

            model.addAttribute(
                    "resultMsg",
                    "분류내려받기 실패: " + message
            );
        }

        return selectVatCardPurchaseList(searchVO, model);
    }


    /**
     * 일반 내려받기에서 실패한 사업자 정보를 분류내려받기 실패 TXT와 같은 형식으로 저장한다.
     * 비밀번호와 주민등록번호는 기록하지 않는다.
     */
    private File writeDownloadFailText(
            List<String> failMessages,
            int year,
            int quarter) throws Exception {

        File downloadFolder = new File("C:\\hometax_download");

        if (!downloadFolder.exists()
                && !downloadFolder.mkdirs()
                && !downloadFolder.exists()) {
            throw new RuntimeException(
                    "실패정보 TXT 저장 폴더를 생성할 수 없습니다. "
                    + downloadFolder.getAbsolutePath()
            );
        }

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());

        File outputFile = new File(
                downloadFolder,
                "홈택스_내려받기실패_"
                + year
                + "년_"
                + quarter
                + "분기_"
                + timestamp
                + ".txt"
        );

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(outputFile),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            // Windows 메모장에서 UTF-8 한글을 안정적으로 인식하도록 BOM 기록
            writer.write('\uFEFF');
            writer.write("홈택스 내려받기 실패 업체");
            writer.newLine();
            writer.write(
                    "조회기간 : "
                    + year
                    + "년 "
                    + quarter
                    + "분기"
            );
            writer.newLine();
            writer.write(
                    "실패건수 : "
                    + failMessages.size()
                    + "건"
            );
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();
            writer.write(
                    "상호명 | 아이디 | 사업자등록번호 | 실패사유"
            );
            writer.newLine();
            writer.write(
                    "============================================================"
            );
            writer.newLine();

            for (String failMessage : failMessages) {
                writer.write(failMessage);
                writer.newLine();
            }
        }

        System.out.println(
                "[DOWNLOAD-FAIL-TXT] "
                + outputFile.getAbsolutePath()
        );

        return outputFile;
    }

    private String buildDownloadFailMessage(
            VatCardPurchaseVO businessInfo,
            String bizrSeqValue,
            String message) {

        String companyName = "-";
        String hometaxId = "-";
        String businessNumber = "-";

        if (businessInfo != null) {
            companyName = valueOrDash(businessInfo.getCmpnyNm());
            hometaxId = valueOrDash(businessInfo.getEntrprsmberId());
            businessNumber =
                    formatBusinessNumber(businessInfo.getBizrno());
        } else if (bizrSeqValue != null
                && bizrSeqValue.trim().length() > 0) {
            // 대상 조회 자체가 실패한 경우 최소한 선택값은 실패사유에 남긴다.
            message = "BIZR_SEQ="
                    + bizrSeqValue.trim()
                    + " / "
                    + message;
        }

        return companyName
                + " | "
                + hometaxId
                + " | "
                + businessNumber
                + " | "
                + simplifyDownloadFailReason(message);
    }


    /**
     * 일반 내려받기 실패 원인을 예외 cause 체인까지 확인해서 추출한다.
     *
     * 로그인 실패는 기존처럼 단순한 문구로 유지하고,
     * 그 외 실패는 가장 구체적인 원인을 우선한다.
     */
    private String getDetailedDownloadFailReason(Throwable throwable) {

        if (throwable == null) {
            return "내려받기 실패";
        }

        String fallback = null;
        Throwable current = throwable;
        int depth = 0;

        while (current != null && depth < 20) {

            String message = current.getMessage();
            String className = current.getClass().getName();
            String text = message == null ? "" : message.trim();

            // 로그인 인증 실패는 상세 Selenium 정보 대신 기존 문구를 유지한다.
            if (text.contains(
                    "입력하신 아이디, 비밀번호 또는 주민번호가 정확하지 않습니다")) {
                return "아이디/비밀번호/주민번호 불일치";
            }

            // Selenium UnhandledAlertException의 실제 Alert 문구를 최우선으로 사용한다.
            // 예: unexpected alert open: {Alert text : 조회된 내역이 없습니다.}
            String alertText = extractAlertText(text);
            if (alertText.length() > 0) {
                return alertText;
            }

            if (className.contains("TimeoutException")
                    || text.contains("TimeoutException")
                    || text.contains("Expected condition failed")) {
                return appendDetail(
                        "홈택스 처리시간 초과",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("NoSuchElementException")
                    || text.contains("NoSuchElementException")) {
                return appendDetail(
                        "홈택스 화면 요소를 찾지 못함",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("StaleElementReferenceException")
                    || text.contains("StaleElementReferenceException")) {
                return appendDetail(
                        "홈택스 화면이 갱신되어 요소 참조가 끊어짐",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("ElementClickInterceptedException")
                    || text.contains("ElementClickInterceptedException")) {
                return appendDetail(
                        "홈택스 화면에서 클릭이 다른 요소에 가로막힘",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("NoSuchWindowException")
                    || text.contains("NoSuchWindowException")) {
                return appendDetail(
                        "홈택스 브라우저 창을 찾지 못함",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("SessionNotCreatedException")
                    || text.contains("SessionNotCreatedException")) {
                return appendDetail(
                        "Chrome WebDriver 세션 생성 실패",
                        firstMeaningfulLine(text)
                );
            }

            if (className.contains("WebDriverException")
                    || text.contains("WebDriverException")) {
                fallback = appendDetail(
                        "브라우저 처리 오류",
                        firstMeaningfulLine(text)
                );
            } else if (text.length() > 0
                    && !isGenericDownloadFailMessage(text)) {
                // "2026년 1분기 다운로드 실패" 같은 포장 메시지는 버리고
                // 실제 원인에 가까운 하위 메시지를 후보로 보관한다.
                fallback = firstMeaningfulLine(text);
            }

            current = current.getCause();
            depth++;
        }

        if (fallback != null && fallback.trim().length() > 0) {
            return fallback.trim();
        }

        String topMessage = throwable.getMessage();
        if (topMessage != null && topMessage.trim().length() > 0) {
            return firstMeaningfulLine(topMessage);
        }

        return throwable.getClass().getSimpleName();
    }

    private String extractAlertText(String text) {

        if (text == null || text.trim().length() == 0) {
            return "";
        }

        String value = text.trim();
        String marker = "Alert text :";
        int start = value.indexOf(marker);

        if (start < 0) {
            return "";
        }

        String alertText = value.substring(start + marker.length()).trim();

        int braceIndex = alertText.indexOf('}');
        if (braceIndex >= 0) {
            alertText = alertText.substring(0, braceIndex).trim();
        }

        int lineIndex = alertText.indexOf('\n');
        if (lineIndex >= 0) {
            alertText = alertText.substring(0, lineIndex).trim();
        }

        return alertText;
    }

    private boolean isGenericDownloadFailMessage(String text) {
        if (text == null) {
            return false;
        }

        String value = text.trim();
        return value.matches("\\d{4}년\\s*\\d+분기\\s*다운로드 실패")
                || "다운로드 실패".equals(value)
                || "홈택스 다운로드 실패".equals(value);
    }

    private String firstMeaningfulLine(String text) {

        if (text == null) {
            return "";
        }

        String value = text.trim();
        int newLine = value.indexOf('\n');

        if (newLine >= 0) {
            value = value.substring(0, newLine).trim();
        }

        if (value.length() > 250) {
            value = value.substring(0, 250);
        }

        return value;
    }

    private String appendDetail(String summary, String detail) {

        if (detail == null || detail.trim().length() == 0) {
            return summary;
        }

        String value = detail.trim();

        // 클래스명만 반복되는 등 의미 없는 상세는 붙이지 않는다.
        if (value.equals(summary)) {
            return summary;
        }

        return summary + " - " + value;
    }

    private String simplifyDownloadFailReason(String message) {

        if (message == null || message.trim().length() == 0) {
            return "내려받기 실패";
        }

        String value = message.trim();

        if (value.contains(
                "입력하신 아이디, 비밀번호 또는 주민번호가 정확하지 않습니다")) {
            return "아이디/비밀번호/주민번호 불일치";
        }

        if (value.contains("unexpected alert open")) {
            int alertStart = value.indexOf("Alert text :");

            if (alertStart >= 0) {
                String alertText = value.substring(
                        alertStart + "Alert text :".length()
                );

                int end = alertText.indexOf("}");
                if (end >= 0) {
                    alertText = alertText.substring(0, end);
                }

                alertText = alertText.trim();

                if (alertText.length() > 0) {
                    return alertText;
                }
            }

            return "홈택스 알림 발생으로 처리 실패";
        }

        if (value.contains("TimeoutException")
                || value.contains("Expected condition failed")) {
            return "홈택스 처리시간 초과";
        }

        if (value.contains("NoSuchElementException")) {
            return "홈택스 화면 요소를 찾지 못함";
        }

        if (value.contains("WebDriverException")) {
            return "브라우저 처리 오류";
        }

        int newLine = value.indexOf('\n');
        if (newLine >= 0) {
            value = value.substring(0, newLine).trim();
        }

        if (value.length() > 120) {
            value = value.substring(0, 120);
        }

        return value;
    }

    private String formatBusinessNumber(String businessNumber) {

        if (businessNumber == null) {
            return "-";
        }

        String value = businessNumber.replaceAll("[^0-9]", "");

        if (value.length() == 10) {
            return value.substring(0, 3)
                    + "-"
                    + value.substring(3, 5)
                    + "-"
                    + value.substring(5);
        }

        return value.length() == 0 ? "-" : value;
    }

    private String valueOrDash(String value) {
        if (value == null || value.trim().length() == 0) {
            return "-";
        }
        return value.trim();
    }

    private void setDefaultSearchCondition(
            VatCardPurchaseVO searchVO) {

        int currentYear = LocalDate.now().getYear();

        if (searchVO.getSearchYear() == null
                || searchVO.getSearchYear().trim().length() == 0) {
            searchVO.setSearchYear(
                    String.valueOf(currentYear)
            );
        }

        if (searchVO.getSearchQuarter() == null
                || searchVO.getSearchQuarter().trim().length() == 0) {
            searchVO.setSearchQuarter("1");
        }

        if (searchVO.getSearchPeriodType() == null
                || searchVO.getSearchPeriodType().trim().length() == 0) {
            searchVO.setSearchPeriodType("QUARTER");
        }
    }

    private List<Integer> createYearList() {

        int currentYear = LocalDate.now().getYear();
        List<Integer> yearList = new ArrayList<Integer>();

        for (int year = currentYear;
             year >= currentYear - 5;
             year--) {
            yearList.add(year);
        }

        return yearList;
    }
}
