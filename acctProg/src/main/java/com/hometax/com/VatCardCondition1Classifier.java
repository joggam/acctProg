package com.hometax.com;

import vat.home.card.service.VatCardCondition1KeywordVO;
import vat.home.card.service.VatCardCondition1Service;
import vat.home.card.service.VatCardCondition1VO;

/**
 * 조건1 판정 전용 클래스.
 * - 업태 + 업종: 완전일치
 * - 키워드: 조건2에서 명시한 세 종류(CORP/EMPLOYEE/VEHICLE)만 사용
 * - 키워드도 업태 또는 업종 값과 완전일치
 *
 * 실제 다운로드 연결은 조건2 최종 우선순위가 확정된 후 기존 흐름에 최소 연결한다.
 */
public class VatCardCondition1Classifier {
    public static final String TYPE_CORP = "CORP";         // 조건2 1번 법인 → 계정과목 142 후보
    public static final String TYPE_EMPLOYEE = "EMPLOYEE"; // 조건2 3번 직원 O → 계정과목 811 후보
    public static final String TYPE_VEHICLE = "VEHICLE";   // 조건2 4번 차량 O → 계정과목 822 후보
    public static final String TARGET_BIZCND = "BIZCND";
    public static final String TARGET_INDUTY = "INDUTY";

    private final VatCardCondition1Service service;

    public VatCardCondition1Classifier(VatCardCondition1Service service) {
        if (service == null) throw new IllegalArgumentException("VatCardCondition1Service is required.");
        this.service = service;
    }

    public Result classifyBase(String bizcnd, String induty) throws Exception {
        VatCardCondition1VO vo = service.selectMatch(trim(bizcnd), trim(induty));
        if (vo == null) return Result.notMatched();
        return new Result(true, vo.getVatDeductYn(), vo.getVatTypeCode(), vo.getAccountCode());
    }

    /** 조건2에서 '일부만:키워드'라고 지정된 경우에만 호출한다. */
    public String findKeywordAccount(String keywordType, String bizcnd, String induty) throws Exception {
        validateKeywordType(keywordType);
        VatCardCondition1KeywordVO byBizcnd = service.selectKeywordMatch(keywordType, TARGET_BIZCND, trim(bizcnd));
        if (byBizcnd != null) return byBizcnd.getAccountCode();
        VatCardCondition1KeywordVO byInduty = service.selectKeywordMatch(keywordType, TARGET_INDUTY, trim(induty));
        return byInduty == null ? null : byInduty.getAccountCode();
    }

    private void validateKeywordType(String type) {
        if (!(TYPE_CORP.equals(type) || TYPE_EMPLOYEE.equals(type) || TYPE_VEHICLE.equals(type))) {
            throw new IllegalArgumentException("Unsupported keyword type: " + type);
        }
    }
    private String trim(String s) { return s == null ? "" : s.trim(); }

    public static class Result {
        private final boolean matched;
        private final String vatDeductYn;
        private final String vatTypeCode;
        private final String accountCode;
        public Result(boolean matched, String vatDeductYn, String vatTypeCode, String accountCode) { this.matched=matched; this.vatDeductYn=vatDeductYn; this.vatTypeCode=vatTypeCode; this.accountCode=accountCode; }
        public static Result notMatched() { return new Result(false, null, null, null); }
        public boolean isMatched() { return matched; }
        public String getVatDeductYn() { return vatDeductYn; }
        public String getVatTypeCode() { return vatTypeCode; }
        public String getAccountCode() { return accountCode; }
    }
}
