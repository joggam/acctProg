package vat.home.card.web;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
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
     * 화면에서는 selectedEntrprsMber 값으로 ENTRPRS_MBER_ID만 전달한다.
     */
    @RequestMapping("/vat/home/card/downloadSelectedEntrprsMber.do")
    public String downloadSelectedEntrprsMber(
            @RequestParam(value = "selectedEntrprsMber", required = false)
            String[] selectedEntrprsMber,
            @ModelAttribute("searchVO") VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        setDefaultSearchCondition(searchVO);

        if (selectedEntrprsMber == null
                || selectedEntrprsMber.length == 0) {

            model.addAttribute(
                    "resultMsg",
                    "내려받을 기업회원을 선택해 주세요."
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

        for (String entrprsmberId : selectedEntrprsMber) {

            if (entrprsmberId == null
                    || entrprsmberId.trim().length() == 0) {
                continue;
            }

            try {
                File resultFile =
                        vatCardPurchaseService.downloadHometaxExcel(
                                entrprsmberId.trim(),
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

                String message = e.getMessage();

                if (message == null
                        || message.trim().length() == 0) {
                    message = e.getClass().getSimpleName();
                }

                failMessages.add(
                        entrprsmberId
                        + " : "
                        + message
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
                    + "개 기업의 홈택스 자료 내려받기가 완료되었습니다."
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
            @RequestParam(value = "selectedEntrprsMber", required = false)
            String[] selectedEntrprsMber,
            @ModelAttribute("searchVO") VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        setDefaultSearchCondition(searchVO);

        if (selectedEntrprsMber == null
                || selectedEntrprsMber.length == 0) {

            model.addAttribute(
                    "resultMsg",
                    "분류내려받기 할 기업회원을 선택해 주세요."
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
                            selectedEntrprsMber,
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
                    "선택한 기업 자료를 한 파일로 분류내려받기 완료했습니다."
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
