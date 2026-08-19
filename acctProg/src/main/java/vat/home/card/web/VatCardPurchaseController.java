package vat.home.card.web;

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

        int currentYear = LocalDate.now().getYear();

        if (searchVO.getSearchYear() == null
                || "".equals(searchVO.getSearchYear())) {
            searchVO.setSearchYear(
                String.valueOf(currentYear)
            );
        }

        List<Integer> yearList =
            new ArrayList<Integer>();

        for (int year = currentYear;
             year >= currentYear - 5;
             year--) {

            yearList.add(year);
        }

        PaginationInfo paginationInfo =
            new PaginationInfo();

        paginationInfo.setCurrentPageNo(
            searchVO.getPageIndex()
        );
        paginationInfo.setRecordCountPerPage(
            searchVO.getPageUnit()
        );
        paginationInfo.setPageSize(
            searchVO.getPageSize()
        );

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
            vatCardPurchaseService
                .selectEntrprsMberList(searchVO);

        int totalCount =
            vatCardPurchaseService
                .selectEntrprsMberListTotCnt(searchVO);

        paginationInfo.setTotalRecordCount(totalCount);

        model.addAttribute(
            "yearList",
            yearList
        );
        model.addAttribute(
            "resultList",
            resultList
        );
        model.addAttribute(
            "totalCount",
            totalCount
        );
        model.addAttribute(
            "paginationInfo",
            paginationInfo
        );
        model.addAttribute(
            "totalPageCount",
            paginationInfo.getTotalPageCount()
        );

        return "vat/home/card/VatCardPurchaseList";
    }


    /**
     * 선택된 기업회원 처리 예시.
     *
     * 화면에서는 기업회원 ID만 전달한다.
     * 비밀번호 등 인증정보는 반드시 서버에서 다시 조회한다.
     *
     * 실제 홈택스 자동화 호출은 다음 단계에서 이 메서드 내부에 연결한다.
     */
    @RequestMapping("/vat/home/card/processSelectedEntrprsMber.do")
    public String processSelectedEntrprsMber(
            @RequestParam(
                value = "selectedEntrprsMber",
                required = false
            )
            String[] selectedEntrprsMber,
            @ModelAttribute("searchVO")
            VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        if (selectedEntrprsMber != null) {

            for (String entrprsmberId
                    : selectedEntrprsMber) {

                /*
                 * 핵심:
                 * 비밀번호를 request parameter로 받지 않는다.
                 * 선택된 ID로 DB에서 서버가 다시 조회한다.
                 */
                VatCardPurchaseVO loginInfo =
                    vatCardPurchaseService
                        .selectEntrprsMberLoginInfo(
                            entrprsmberId
                        );

                if (loginInfo == null) {
                    continue;
                }

                /*
                 * 여기에서만 서버 내부 값 사용.
                 *
                 * loginInfo.getEntrprsmberId()
                 * loginInfo.getEntrprsMberPassword()
                 * loginInfo.getApplcntIhidnum2()
                 *
                 * TODO:
                 * 홈택스 로그인/엑셀 다운로드 로직 연결.
                 */
            }
        }

        return "redirect:/vat/home/card/selectVatCardPurchaseList.do";
    }
}
