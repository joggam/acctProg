package vat.home.card.service.impl;

import java.io.File;
import java.util.List;

import javax.annotation.Resource;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import com.hometax.com.HometaxMain;

import egovframework.com.cmm.web.EgovComUtlController;
import vat.home.card.service.VatCardPurchaseService;
import vat.home.card.service.VatCardPurchaseVO;

@Service("vatCardPurchaseService")
public class VatCardPurchaseServiceImpl extends EgovAbstractServiceImpl
        implements VatCardPurchaseService {

    @Resource(name = "vatCardPurchaseDAO")
    private VatCardPurchaseDAO vatCardPurchaseDAO;

    @Override
    public List<VatCardPurchaseVO> selectEntrprsMberList(
            VatCardPurchaseVO searchVO) throws Exception {

        return vatCardPurchaseDAO.selectEntrprsMberList(searchVO);
    }

    @Override
    public int selectEntrprsMberListTotCnt(
            VatCardPurchaseVO searchVO) throws Exception {

        return vatCardPurchaseDAO.selectEntrprsMberListTotCnt(searchVO);
    }

    @Override
    public VatCardPurchaseVO selectEntrprsMberLoginInfo(
            String entrprsmberId) throws Exception {

        return vatCardPurchaseDAO.selectEntrprsMberLoginInfo(entrprsmberId);
    }

    @Override
    public File downloadHometaxExcel(
            String entrprsmberId,
            int year,
            int quarter) throws Exception {

        VatCardPurchaseVO loginInfo =
                vatCardPurchaseDAO.selectEntrprsMberLoginInfo(
                        entrprsmberId
                );

        if (loginInfo == null) {
            throw new RuntimeException(
                    "기업회원 로그인 정보를 찾을 수 없습니다. ID="
                    + entrprsmberId
            );
        }

        String hometaxId = trim(loginInfo.getEntrprsmberId());
        String businessNumber = onlyNumber(loginInfo.getBizrno());
        String encryptedPassword = trim(loginInfo.getHometaxPassword());
        String ihidnum1 = onlyNumber(loginInfo.getApplcntIhidnum());
        String ihidnum2 = onlyNumber(loginInfo.getApplcntIhidnum2());

        if (hometaxId.length() == 0) {
            throw new RuntimeException("홈택스 아이디가 없습니다.");
        }

        if (businessNumber.length() != 10) {
            throw new RuntimeException(
                    "사업자등록번호가 올바르지 않습니다. ID="
                    + entrprsmberId
            );
        }

        if (encryptedPassword.length() == 0) {
            throw new RuntimeException("홈택스 비밀번호가 없습니다.");
        }

        String hometaxPassword =
                EgovComUtlController.decryptId(
                        encryptedPassword
                );

        if (hometaxPassword == null
                || hometaxPassword.trim().length() == 0) {
            throw new RuntimeException("홈택스 비밀번호 복호화 결과가 없습니다.");
        }

        String juminFirst6;
        String jumin7th;

        /*
         * 홈택스 2차 인증
         * - APPLCNT_IHIDNUM  : 앞 6자리
         * - APPLCNT_IHIDNUM2 : 7번째 숫자
         *
         * APPLCNT_IHIDNUM2가 있으면 반드시 우선 사용한다.
         * APPLCNT_IHIDNUM2가 비어 있는 예전 데이터만
         * APPLCNT_IHIDNUM의 7번째 숫자를 fallback으로 사용한다.
         */
        if (ihidnum1.length() < 6) {
            throw new RuntimeException(
                    "주민등록번호 앞 6자리가 올바르지 않습니다. ID="
                    + entrprsmberId
            );
        }

        juminFirst6 = ihidnum1.substring(0, 6);

        if (ihidnum2.length() > 0) {
            jumin7th = ihidnum2.substring(0, 1);
        } else if (ihidnum1.length() >= 7) {
            jumin7th = ihidnum1.substring(6, 7);
        } else {
            throw new RuntimeException(
                    "주민등록번호 7번째 숫자가 없습니다. ID="
                    + entrprsmberId
            );
        }

        return HometaxMain.execute(
                hometaxId,
                hometaxPassword,
                juminFirst6,
                jumin7th,
                businessNumber,
                year,
                quarter
        );
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String onlyNumber(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }
}
