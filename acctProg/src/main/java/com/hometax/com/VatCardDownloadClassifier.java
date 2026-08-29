package com.hometax.com;

/**
 * 일반 내려받기 전용 조건1+조건2 연결 클래스.
 *
 * 순서:
 *  1) 조건2 prepare()
 *  2) 조건2만으로 종료되면 조건1 조회 없이 반환
 *  3) 조건2가 조건1을 요구할 때만 조건1 조회
 *  4) 조건2가 키워드를 요구한 종류만 키워드 조회
 *  5) 조건2 finish()
 */
public class VatCardDownloadClassifier {

    private final VatCardCondition1Classifier condition1Classifier;

    public VatCardDownloadClassifier(
            VatCardCondition1Classifier condition1Classifier) {

        if (condition1Classifier == null) {
            throw new IllegalArgumentException(
                    "VatCardCondition1Classifier is required."
            );
        }

        this.condition1Classifier =
                condition1Classifier;
    }

    public VatCardCondition2Classifier.Result classify(
            VatCardCondition2Classifier.BusinessContext business,
            VatCardCondition2Classifier.RowContext row) throws Exception {

        // =====================================================
        // 1. 조건2부터 판정
        // =====================================================
        VatCardCondition2Classifier.Plan plan =
                VatCardCondition2Classifier.prepare(
                        business,
                        row
                );

        // 조건2만으로 최종값이 확정되면 조건1을 조회하지 않는다.
        if (plan.isTerminal()) {
            return plan.getTerminalResult();
        }

        // =====================================================
        // 2. 여기부터 조건2가 조건1 등록여부/값을 요구한 경우
        // =====================================================
        if (plan.isNeedCondition1Registration()) {
            validateCondition1Source(row);
        }

        VatCardCondition1Classifier.Result base =
                condition1Classifier.classifyBase(
                        row.getBizcnd(),
                        row.getInduty()
                );

        VatCardCondition2Classifier.Condition1Context condition1 =
                new VatCardCondition2Classifier.Condition1Context()
                        .setRegistered(
                                base != null
                                && base.isMatched()
                        );

        // 등록 X면 조건2 #5에서 바로 공제/57/830으로 확정.
        // 키워드 조회도 하지 않는다.
        if (base == null || !base.isMatched()) {
            return VatCardCondition2Classifier.finish(
                    plan,
                    condition1
            );
        }

        condition1
                .setVatDeduction(
                        base.getVatDeductYn()
                )
                .setVatType(
                        parseInteger(
                                base.getVatTypeCode(),
                                "조건1 부가세유형(2자리)"
                        )
                )
                .setAccountCode(
                        parseInteger(
                                base.getAccountCode(),
                                "조건1 계정과목"
                        )
                );

        // =====================================================
        // 3. 조건2에서 실제로 명시한 키워드만 조회
        // =====================================================

        // #1 사업자-법인(142)
        if (plan.isCorpKeyword()) {
            condition1.setCorpKeywordAccount(
                    parseInteger(
                            condition1Classifier.findKeywordAccount(
                                    VatCardCondition1Classifier.TYPE_CORP,
                                    row.getBizcnd(),
                                    row.getInduty()
                            ),
                            "사업자-법인(142) 키워드 계정과목"
                    )
            );
        }

        // #3 직원O(811)
        if (plan.isEmployeeKeyword()) {
            condition1.setEmployeeKeywordAccount(
                    parseInteger(
                            condition1Classifier.findKeywordAccount(
                                    VatCardCondition1Classifier.TYPE_EMPLOYEE,
                                    row.getBizcnd(),
                                    row.getInduty()
                            ),
                            "직원O(811) 키워드 계정과목"
                    )
            );
        }

        // #4 차량O(822)
        if (plan.isVehicleKeyword()) {
            condition1.setVehicleKeywordAccount(
                    parseInteger(
                            condition1Classifier.findKeywordAccount(
                                    VatCardCondition1Classifier.TYPE_VEHICLE,
                                    row.getBizcnd(),
                                    row.getInduty()
                            ),
                            "차량O(822) 키워드 계정과목"
                    )
            );
        }

        // =====================================================
        // 4. 조건2 우선순위로 최종값 확정
        // =====================================================
        return VatCardCondition2Classifier.finish(
                plan,
                condition1
        );
    }

    private void validateCondition1Source(
            VatCardCondition2Classifier.RowContext row) {

        if (row == null) {
            throw new RuntimeException(
                    "조건1 판정용 거래 ROW 정보가 없습니다."
            );
        }

        if (!row.isBizcndColumnAvailable()
                || !row.isIndutyColumnAvailable()) {

            throw new RuntimeException(
                    "조건2 판정 후 조건1 확인이 필요하지만 "
                    + "홈택스 원본에서 업태/업종 컬럼을 찾을 수 없습니다."
                    + " 업태컬럼=" + row.isBizcndColumnAvailable()
                    + ", 업종컬럼=" + row.isIndutyColumnAvailable()
            );
        }
    }

    private Integer parseInteger(
            String value,
            String fieldName) {

        String normalized =
                trim(value);

        if (normalized.length() == 0) {
            return null;
        }

        try {
            return Integer.valueOf(
                    normalized
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    fieldName
                    + " 값은 숫자여야 합니다. value="
                    + normalized,
                    e
            );
        }
    }

    private String trim(String value) {
        return value == null
                ? ""
                : value.trim();
    }
}
