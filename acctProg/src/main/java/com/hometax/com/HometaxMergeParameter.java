package com.hometax.com;

/**
 * 분류내려받기 대상 1건의 홈택스 로그인/조회 정보.
 */
public class HometaxMergeParameter {

    private final String companyName;
    private final String hometaxId;
    private final String hometaxPassword;
    private final String juminFirst6;
    private final String jumin7th;
    private final String businessNumber;

    public HometaxMergeParameter(
            String companyName,
            String hometaxId,
            String hometaxPassword,
            String juminFirst6,
            String jumin7th,
            String businessNumber) {

        this.companyName = companyName == null ? "" : companyName.trim();
        this.hometaxId = hometaxId;
        this.hometaxPassword = hometaxPassword;
        this.juminFirst6 = juminFirst6;
        this.jumin7th = jumin7th;
        this.businessNumber = onlyNumber(businessNumber);
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getHometaxId() {
        return hometaxId;
    }

    public String getHometaxPassword() {
        return hometaxPassword;
    }

    public String getJuminFirst6() {
        return juminFirst6;
    }

    public String getJumin7th() {
        return jumin7th;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    private static String onlyNumber(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }
}
