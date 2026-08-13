package vat.home.card.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import egovframework.com.cmm.annotation.IncludedInfo;
import vat.home.card.service.VatCardPurchaseVO;

/**
 * 사업용신용카드 매입세액 공제 확인/변경 Controller
 */
@Controller
public class VatCardPurchaseController {

    /**
     * 사업용신용카드 매입세액 공제 확인/변경 화면
     */
	@IncludedInfo(name = "사업용신용카드", listUrl = "/vat/home/card/selectVatCardPurchaseList.do", order = 11, gid = 1)
    @RequestMapping("/vat/home/card/selectVatCardPurchaseList.do")
    public String selectVatCardPurchaseList(
            @ModelAttribute("searchVO") VatCardPurchaseVO searchVO,
            ModelMap model) throws Exception {

        int currentYear = LocalDate.now().getYear();

        if (searchVO.getSearchYear() == null || "".equals(searchVO.getSearchYear())) {
            searchVO.setSearchYear(String.valueOf(currentYear));
        }

        List<Integer> yearList = new ArrayList<Integer>();
        for (int year = currentYear; year >= currentYear - 5; year--) {
            yearList.add(year);
        }

        /*
         * TODO 후속작업
         * 1. 로그인 사용자 기준 사업자등록번호/상호 조회
         * 2. 조회조건으로 사업용신용카드 매입내역 조회
         * 3. 총 사용금액 계산
         * 4. PaginationInfo 생성
         */
        model.addAttribute("businessNo", "");
        model.addAttribute("businessName", "");
        model.addAttribute("yearList", yearList);
        model.addAttribute("resultList", Collections.emptyList());
        model.addAttribute("totalUseAmount", 0L);
        model.addAttribute("totalCount", 0);

        return "vat/home/card/VatCardPurchaseList";
    }
}
