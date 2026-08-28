package com.hometax.com;

/**
 * 사업용신용카드 일반 내려받기 2.0 - 조건2 전용 분류 클래스.
 *
 * 조건2(3).xlsx 기준으로 현재 확보된 데이터만 처리한다.
 *
 * 현재 처리 가능:
 *  1. 사업자구분  : COMTNENTRPRSMBER.BIZR_SE_CODE (VAT001)
 *  2. 상대업체    : 홈택스 XLS 가맹점유형
 *  3. 직원여부    : COMTNENTRPRSMBER.EMPL_SE_CODE (VAT002)
 *  4. 차량구분    : COMTNENTRPRSMBER.VHCL_SE_CODE (VAT003)
 *  6. 금액기준    : 홈택스 XLS 공급가액
 *
 * 아직 미구현:
 *  - 조건1 DB 등록 여부/결과
 *  - 조건1 DB의 키워드 예외(142 / 811 / 822)
 *
 * 조건1이 완성되기 전까지 "조건1" 자리는 조건2 5번의 등록 X 값
 * (공제 / 57 / 830)을 임시 기본값으로 사용한다.
 */
public final class VatCardCondition2Classifier {

    private static final String BIZR_CORPORATE = "1";
    private static final String BIZR_PERSONAL = "2";

    private static final String EMPLOYEE_YES = "1";
    private static final String EMPLOYEE_NO = "2";

    private static final String VEHICLE_NONE = "1";
    private static final String VEHICLE_NON_DEDUCTIBLE = "2";
    private static final String VEHICLE_DEDUCTIBLE = "3";

    private VatCardCondition2Classifier() {
    }

    /** 기업회원 단위 조건값. */
    public static final class BusinessContext {
        private String bizrSeCode;
        private String emplSeCode;
        private String vhclSeCode;
        private Boolean condition1Registered;

        public String getBizrSeCode() {
            return bizrSeCode;
        }

        public BusinessContext setBizrSeCode(String bizrSeCode) {
            this.bizrSeCode = trim(bizrSeCode);
            return this;
        }

        public String getEmplSeCode() {
            return emplSeCode;
        }

        public BusinessContext setEmplSeCode(String emplSeCode) {
            this.emplSeCode = trim(emplSeCode);
            return this;
        }

        public String getVhclSeCode() {
            return vhclSeCode;
        }

        public BusinessContext setVhclSeCode(String vhclSeCode) {
            this.vhclSeCode = trim(vhclSeCode);
            return this;
        }

        /**
         * 조건1 DB 등록 여부.
         * 아직 조건1 DB 미구현 상태에서는 null로 유지한다.
         * false일 때만 조건2 표의 5번 등록 X 주황색을 적용한다.
         */
        public Boolean getCondition1Registered() {
            return condition1Registered;
        }

        public BusinessContext setCondition1Registered(Boolean condition1Registered) {
            this.condition1Registered = condition1Registered;
            return this;
        }
    }

    /** 홈택스 거래 1행에서 읽는 조건값. */
    public static final class RowContext {
        private String merchantType;
        private String merchantName;
        private double supplyAmount;

        public String getMerchantType() {
            return merchantType;
        }

        public RowContext setMerchantType(String merchantType) {
            this.merchantType = merchantType;
            return this;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public RowContext setMerchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public double getSupplyAmount() {
            return supplyAmount;
        }

        public RowContext setSupplyAmount(double supplyAmount) {
            this.supplyAmount = supplyAmount;
            return this;
        }
    }

    /** null은 엑셀에서 빈 셀로 기록한다. */
    public static final class Result {
        private String vatDeduction;
        private Integer vatType;
        private Integer accountCode;
        private RowColor rowColor;

        public Result(String vatDeduction, Integer vatType, Integer accountCode) {
            this(vatDeduction, vatType, accountCode, RowColor.NONE);
        }

        public Result(String vatDeduction, Integer vatType, Integer accountCode, RowColor rowColor) {
            this.vatDeduction = vatDeduction;
            this.vatType = vatType;
            this.accountCode = accountCode;
            this.rowColor = rowColor == null ? RowColor.NONE : rowColor;
        }

        public String getVatDeduction() {
            return vatDeduction;
        }

        public Integer getVatType() {
            return vatType;
        }

        public Integer getAccountCode() {
            return accountCode;
        }

        public RowColor getRowColor() {
            return rowColor;
        }

        private void setVatDeduction(String vatDeduction) {
            this.vatDeduction = vatDeduction;
        }

        private void setVatType(Integer vatType) {
            this.vatType = vatType;
        }

        private void setRowColor(RowColor rowColor) {
            this.rowColor = rowColor == null ? RowColor.NONE : rowColor;
        }
    }


    /** 조건2 표의 ROW 색 컬럼을 그대로 표현한다. */
    public enum RowColor {
        NONE,
        YELLOW,
        ORANGE,
        GREEN
    }

    public static Result classify(BusinessContext business, RowContext row) {
        if (business == null) {
            business = new BusinessContext();
        }
        if (row == null) {
            row = new RowContext();
        }

        // 기존 1.0 시그니처를 직접 호출하는 코드와의 호환용.
        // 실제 일반 내려받기 흐름은 ServiceImpl에서 3개 코드를 필수 검증한 뒤
        // 값이 채워진 BusinessContext를 전달한다.
        if (isEmpty(business.getBizrSeCode())
                && isEmpty(business.getEmplSeCode())
                && isEmpty(business.getVhclSeCode())) {
            return new Result("공제", Integer.valueOf(57), Integer.valueOf(830), RowColor.NONE);
        }

        validateBusinessContext(business);

        // -----------------------------------------------------
        // 조건1 임시값
        // 조건2 5번 '등록 X' = 공제 / 57 / 830
        // 조건1 DB 구현 시 이 시작값을 조건1 조회 결과로 교체한다.
        // -----------------------------------------------------
        Result result = new Result(
                "공제",
                Integer.valueOf(57),
                Integer.valueOf(830),
                RowColor.YELLOW
        );

        // -----------------------------------------------------
        // 1번 사업자
        // 법인 : 불공제 / null / 조건1
        // 개인 : null / null / null
        // 개인은 표의 값 그대로 여기서 종료한다.
        // 키워드 142 분기는 조건1 키워드 DB 구현 시 연결한다.
        // -----------------------------------------------------
        if (BIZR_PERSONAL.equals(business.getBizrSeCode())) {
            return new Result(null, null, null, RowColor.YELLOW);
        }

        if (BIZR_CORPORATE.equals(business.getBizrSeCode())) {
            result.setVatDeduction("불공제");
            result.setVatType(null);
        }

        // -----------------------------------------------------
        // 2번 상대(결제)업체
        // 법인/일반 : 조건1 -> 현재값 유지
        // 간이/면세 : 불공제 / null / 조건1
        // -----------------------------------------------------
        if (isSimplifiedOrExempt(row.getMerchantType())) {
            result.setVatDeduction("불공제");
            result.setVatType(null);
        }

        // -----------------------------------------------------
        // 3번 직원
        // 직원 O : 부가세 2개는 조건1, 일부 키워드만 811
        // 직원 X : 조건1
        // 현재는 키워드 DB가 없으므로 직원값 자체로는 변경하지 않는다.
        // 다만 EMPL_SE_CODE 값은 위 validate에서 검증한다.
        // -----------------------------------------------------
        @SuppressWarnings("unused")
        boolean employeePresent = EMPLOYEE_YES.equals(business.getEmplSeCode());

        // -----------------------------------------------------
        // 4번 차량
        // 차량 X       : 조건1
        // 불공차량 O   : 조건1, 일부 키워드만 822
        // 공제차량 O   : 공제 / 57 / 조건1, 일부 키워드만 822
        // 현재 822 키워드 분기는 조건1 DB 구현 시 연결한다.
        // -----------------------------------------------------
        if (VEHICLE_DEDUCTIBLE.equals(business.getVhclSeCode())) {
            result.setVatDeduction("공제");
            result.setVatType(Integer.valueOf(57));
        }

        // -----------------------------------------------------
        // 5번 등록유무 - ROW 색
        // 등록 O : X
        // 등록 X : 주황색
        // 현재 조건1 DB가 아직 없으므로 condition1Registered=null 상태에서는
        // 주황색을 임의 적용하지 않는다. 조건1 연결 시 false인 경우에만 적용한다.
        // -----------------------------------------------------
        if (Boolean.FALSE.equals(business.getCondition1Registered())) {
            result.setRowColor(RowColor.ORANGE);
        }

        // -----------------------------------------------------
        // 6번 금액기준
        // 100만 이하 / 100만 초과 모두 현재 표에서는 조건1이므로
        // 값은 변경하지 않는다. 공급가액은 실제로 읽어 두며,
        // 향후 금액별 예외가 추가되면 여기서 사용한다.
        // -----------------------------------------------------
        boolean overOneMillion = row.getSupplyAmount() > 1000000d;

        // 조건2 표 6번: 공급가액 100만원 초과 = 초록색.
        // 여러 색 조건이 겹치면 표의 뒤 조건(6번)이 최종 ROW 색을 덮어쓴다.
        if (overOneMillion) {
            result.setRowColor(RowColor.GREEN);
        }

        return result;
    }

    private static void validateBusinessContext(BusinessContext business) {
        if (!BIZR_CORPORATE.equals(business.getBizrSeCode())
                && !BIZR_PERSONAL.equals(business.getBizrSeCode())) {
            throw new IllegalArgumentException(
                    "사업자구분 코드가 올바르지 않습니다. BIZR_SE_CODE="
                    + business.getBizrSeCode()
            );
        }

        if (!EMPLOYEE_YES.equals(business.getEmplSeCode())
                && !EMPLOYEE_NO.equals(business.getEmplSeCode())) {
            throw new IllegalArgumentException(
                    "직원여부 코드가 올바르지 않습니다. EMPL_SE_CODE="
                    + business.getEmplSeCode()
            );
        }

        if (!VEHICLE_NONE.equals(business.getVhclSeCode())
                && !VEHICLE_NON_DEDUCTIBLE.equals(business.getVhclSeCode())
                && !VEHICLE_DEDUCTIBLE.equals(business.getVhclSeCode())) {
            throw new IllegalArgumentException(
                    "차량구분 코드가 올바르지 않습니다. VHCL_SE_CODE="
                    + business.getVhclSeCode()
            );
        }
    }

    private static boolean isSimplifiedOrExempt(String merchantType) {
        String value = trim(merchantType).replace(" ", "");
        return value.contains("간이") || value.contains("면세");
    }

    private static boolean isEmpty(String value) {
        return trim(value).length() == 0;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
