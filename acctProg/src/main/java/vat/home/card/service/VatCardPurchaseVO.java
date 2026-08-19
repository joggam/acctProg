package vat.home.card.service;

/**
 * 사업용신용카드 매입세액 공제 확인/변경 VO
 */
public class VatCardPurchaseVO extends VatCardPurchaseDefaultVO {

    private static final long serialVersionUID = 1L;

    /** 사용자고유ID */
    private String uniqId;

    /** 사업자등록번호 */
    private String bizrno;

    /** 상호명 */
    private String cmpnyNm;

    /** 기업회원 아이디 */
    private String entrprsmberId;

    /** 신청자 주민등록번호 2번째 값 : APPLCNT_IHIDNUM2 */
    private String applcntIhidnum2;

    /**
     * 기업회원 비밀번호 : ENTRPRS_MBER_PASSWORD
     * 목록 화면에서는 조회/전달하지 않고 선택처리 시 서버에서만 재조회한다.
     */
    private String entrprsMberPassword;

    public String getUniqId() {
        return uniqId;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }

    public String getBizrno() {
        return bizrno;
    }

    public void setBizrno(String bizrno) {
        this.bizrno = bizrno;
    }

    public String getCmpnyNm() {
        return cmpnyNm;
    }

    public void setCmpnyNm(String cmpnyNm) {
        this.cmpnyNm = cmpnyNm;
    }

    public String getEntrprsmberId() {
        return entrprsmberId;
    }

    public void setEntrprsmberId(String entrprsmberId) {
        this.entrprsmberId = entrprsmberId;
    }

    public String getApplcntIhidnum2() {
        return applcntIhidnum2;
    }

    public void setApplcntIhidnum2(String applcntIhidnum2) {
        this.applcntIhidnum2 = applcntIhidnum2;
    }

    public String getEntrprsMberPassword() {
        return entrprsMberPassword;
    }

    public void setEntrprsMberPassword(String entrprsMberPassword) {
        this.entrprsMberPassword = entrprsMberPassword;
    }
}
