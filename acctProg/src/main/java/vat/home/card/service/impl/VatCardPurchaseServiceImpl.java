package vat.home.card.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import com.hometax.com.HometaxMain;
import com.hometax.com.HometaxMergeParameter;

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
            Long bizrSeq) throws Exception {
        return vatCardPurchaseDAO.selectEntrprsMberLoginInfo(bizrSeq);
    }

    @Override
    public File downloadHometaxExcel(
            Long bizrSeq,
            int year,
            int quarter) throws Exception {

        VatCardPurchaseVO loginInfo = getLoginInfo(bizrSeq);

        return HometaxMain.execute(
                trim(loginInfo.getEntrprsmberId()),
                decryptPassword(loginInfo),
                getJuminFirst6(loginInfo),
                getJumin7th(loginInfo),
                getBusinessNumber(loginInfo),
                year,
                quarter
        );
    }

    @Override
    public List<File> downloadMergedHometaxExcel(
            String[] selectedBizrSeq,
            int year,
            int quarter) throws Exception {

        if (selectedBizrSeq == null || selectedBizrSeq.length == 0) {
            throw new IllegalArgumentException(
                    "분류내려받기 대상 사업자등록번호가 없습니다."
            );
        }

        List<HometaxMergeParameter> parameters =
                new ArrayList<HometaxMergeParameter>();

        for (String value : selectedBizrSeq) {
            if (value == null || value.trim().length() == 0) {
                continue;
            }

            Long bizrSeq;
            try {
                bizrSeq = Long.valueOf(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "잘못된 사업자 선택값입니다. BIZR_SEQ=" + value
                );
            }

            VatCardPurchaseVO loginInfo = getLoginInfo(bizrSeq);

            parameters.add(
                    new HometaxMergeParameter(
                            trim(loginInfo.getCmpnyNm()),
                            trim(loginInfo.getEntrprsmberId()),
                            decryptPassword(loginInfo),
                            getJuminFirst6(loginInfo),
                            getJumin7th(loginInfo),
                            getBusinessNumber(loginInfo)
                    )
            );
        }

        if (parameters.isEmpty()) {
            throw new RuntimeException(
                    "분류내려받기 가능한 사업자 정보가 없습니다."
            );
        }

        return HometaxMain.executeMerged(parameters, year, quarter);
    }

    private VatCardPurchaseVO getLoginInfo(Long bizrSeq) {
        if (bizrSeq == null) {
            throw new IllegalArgumentException("BIZR_SEQ가 없습니다.");
        }

        VatCardPurchaseVO loginInfo =
                vatCardPurchaseDAO.selectEntrprsMberLoginInfo(bizrSeq);

        if (loginInfo == null) {
            throw new RuntimeException(
                    "사업자 또는 기업회원 로그인 정보를 찾을 수 없습니다. BIZR_SEQ="
                    + bizrSeq
            );
        }

        if (trim(loginInfo.getEntrprsmberId()).length() == 0) {
            throw new RuntimeException(
                    "홈택스 아이디가 없습니다. BIZR_SEQ=" + bizrSeq
            );
        }

        return loginInfo;
    }

    private String getBusinessNumber(VatCardPurchaseVO loginInfo) {
        String businessNumber = onlyNumber(loginInfo.getBizrno());
        if (businessNumber.length() != 10) {
            throw new RuntimeException(
                    "사업자등록번호가 올바르지 않습니다. ID="
                    + trim(loginInfo.getEntrprsmberId())
            );
        }
        return businessNumber;
    }

    private String decryptPassword(VatCardPurchaseVO loginInfo) {
        String encryptedPassword = trim(loginInfo.getHometaxPassword());
        if (encryptedPassword.length() == 0) {
            throw new RuntimeException(
                    "홈택스 비밀번호가 없습니다. ID="
                    + trim(loginInfo.getEntrprsmberId())
            );
        }

        String hometaxPassword =
                EgovComUtlController.decryptId(encryptedPassword);

        if (hometaxPassword == null
                || hometaxPassword.trim().length() == 0) {
            throw new RuntimeException(
                    "홈택스 비밀번호 복호화 결과가 없습니다. ID="
                    + trim(loginInfo.getEntrprsmberId())
            );
        }

        return hometaxPassword.trim();
    }

    private String getJuminFirst6(VatCardPurchaseVO loginInfo) {
        String ihidnum1 = onlyNumber(loginInfo.getApplcntIhidnum());
        if (ihidnum1.length() < 6) {
            throw new RuntimeException(
                    "주민등록번호 앞 6자리가 올바르지 않습니다. ID="
                    + trim(loginInfo.getEntrprsmberId())
            );
        }
        return ihidnum1.substring(0, 6);
    }

    private String getJumin7th(VatCardPurchaseVO loginInfo) {
        String ihidnum1 = onlyNumber(loginInfo.getApplcntIhidnum());
        String ihidnum2 = onlyNumber(loginInfo.getApplcntIhidnum2());

        if (ihidnum2.length() > 0) {
            return ihidnum2.substring(0, 1);
        }
        if (ihidnum1.length() >= 7) {
            return ihidnum1.substring(6, 7);
        }

        throw new RuntimeException(
                "주민등록번호 7번째 숫자가 없습니다. ID="
                + trim(loginInfo.getEntrprsmberId())
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
