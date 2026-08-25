package egovframework.com.uss.umt.web;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.uss.umt.service.EgovEntrprsManageService;
import egovframework.com.uss.umt.service.EntrprsManageVO;

/**
 * 기업회원 엑셀 대량등록 Controller
 *
 * 엑셀 입력 컬럼
 * 1. 기업회원ID
 * 2. 비밀번호
 * 3. 사업자등록번호 (같은 기업회원ID에 여러 사업자번호 허용)
 * 4. 법인등록번호
 * 5. 회사명
 * 6. 주민등록번호 앞 6자리
 * 7. 주민등록번호 2번째 값
 * 8. 대표이사이름
 * 9. 신청자명
 * 10. 대표자 연락처
 */
@Controller
public class EgovEntrprsExcelUploadController {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private static final String[] HEADER_KR = {
            "기업회원ID", "비밀번호", "사업자등록번호", "법인등록번호", "회사명", "주민등록번호 앞 6자리", "주민등록번호 2번째 값", "대표이사이름", "신청자명", "대표자 연락처"
    };

    private static final String[] HEADER_DB = {
            "ENTRPRS_MBER_ID", "ENTRPRS_MBER_PASSWORD", "BIZRNO", "JURIRNO", "CMPNY_NM", "APPLCNT_IHIDNUM", "APPLCNT_IHIDNUM2", "CXFC", "APPLCNT_NM", "REPRESENTATIVE_PHONE"
    };

    @Resource(name = "entrprsManageService")
    private EgovEntrprsManageService entrprsManageService;

    /**
     * 기업회원 엑셀 대량등록 화면
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberExcelUploadView.do")
    public String excelUploadView() throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) {
            return "index";
        }

        return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
    }

    /**
     * 기업회원 엑셀 대량등록 양식 다운로드
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberExcelSample.do")
    public void downloadExcelSample(HttpServletResponse response) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String fileName = "기업회원_대량등록_양식.xlsx";
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream out = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("기업회원대량등록");

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADER_KR.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADER_KR[i]);
                cell.setCellStyle(headerStyle);
            }

            // 사용자 입력값이 숫자로 자동 변환되어 앞자리 0이 사라지는 것을 방지하기 위해
            // 실제 입력 영역은 텍스트 형식으로 지정한다.
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));
            for (int col = 0; col < HEADER_KR.length; col++) {
                sheet.setDefaultColumnStyle(col, textStyle);
            }

            sheet.setColumnWidth(0, 24 * 256);
            sheet.setColumnWidth(1, 22 * 256);
            sheet.setColumnWidth(2, 22 * 256);
            sheet.setColumnWidth(3, 22 * 256);
            sheet.setColumnWidth(4, 30 * 256);
            sheet.setColumnWidth(5, 28 * 256);
            sheet.setColumnWidth(6, 22 * 256);
            sheet.setColumnWidth(7, 22 * 256);
            sheet.setColumnWidth(8, 22 * 256);
            sheet.setColumnWidth(9, 24 * 256);

            workbook.write(out);
            out.flush();
        }
    }

    /**
     * 기업회원 엑셀 대량등록 처리
     */
    @RequestMapping("/uss/umt/EgovEntrprsMberExcelUpload.do")
    public String excelUpload(@RequestParam("excelFile") MultipartFile excelFile,
                              ModelMap model) throws Exception {

        if (!EgovUserDetailsHelper.isAuthenticated()) {
            return "index";
        }

        List<String> errorList = new ArrayList<String>();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        if (excelFile == null || excelFile.isEmpty()) {
            model.addAttribute("resultMessage", "업로드할 엑셀 파일을 선택해 주세요.");
            return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
        }

        if (excelFile.getSize() > MAX_FILE_SIZE) {
            model.addAttribute("resultMessage", "엑셀 파일은 10MB 이하만 업로드할 수 있습니다.");
            return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
        }

        String originalName = excelFile.getOriginalFilename();
        String lowerName = originalName == null ? "" : originalName.toLowerCase(Locale.KOREA);
        if (!(lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls"))) {
            model.addAttribute("resultMessage", "xls 또는 xlsx 파일만 업로드할 수 있습니다.");
            return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
        }

        Set<String> fileIdSet = new HashSet<String>();
        Set<String> fileBizrSet = new HashSet<String>();
        DataFormatter formatter = new DataFormatter(Locale.KOREA);

        try (InputStream in = excelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {

            if (workbook.getNumberOfSheets() < 1) {
                model.addAttribute("resultMessage", "엑셀 시트를 찾을 수 없습니다.");
                return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (!isValidHeader(sheet.getRow(0), formatter)) {
                model.addAttribute("resultMessage",
                        "엑셀 양식이 올바르지 않습니다. 양식 다운로드 후 기업회원ID / 비밀번호 / 사업자등록번호 / 법인등록번호 / 회사명 / 주민등록번호 앞 6자리 / 주민등록번호 2번째 값 / 대표이사이름 / 신청자명 / 대표자 연락처 순서로 작성해 주세요.");
                return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
            }

            // 엑셀 대량등록 시작 전 기존 기업회원 전체 삭제
            entrprsManageService.deleteAllEntrprsmberExcel();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }

                totalCount++;
                int excelRowNo = rowIndex + 1;

                String entrprsmberId = getCellValue(row, 0, formatter);
                String entrprsMberPassword = getCellValue(row, 1, formatter);
                String bizrno = onlyNumber(getCellValue(row, 2, formatter));
                String jurirno = onlyNumber(getCellValue(row, 3, formatter));
                String cmpnyNm = getCellValue(row, 4, formatter);
                String applcntIhidnum = onlyNumber(getCellValue(row, 5, formatter));
                String applcntIhidnum2 = onlyNumber(getCellValue(row, 6, formatter));
                String cxfc = getCellValue(row, 7, formatter);
                String applcntNm = getCellValue(row, 8, formatter);
                String representativePhone = getCellValue(row, 9, formatter);

                String validationMessage = validateRow(
                        entrprsmberId,
                        entrprsMberPassword,
                        bizrno,
                        jurirno,
                        cmpnyNm,
                        applcntIhidnum,
                        applcntIhidnum2,
                        cxfc,
                        applcntNm,
                        representativePhone);
                if (validationMessage != null) {
                    failCount++;
                    errorList.add(excelRowNo + "행: " + validationMessage);
                    continue;
                }

                if (!fileBizrSet.add(bizrno)) {
                    failCount++;
                    errorList.add(excelRowNo + "행: 엑셀 파일 안에서 사업자등록번호가 중복되었습니다. ["
                            + bizrno + "]");
                    continue;
                }

                EntrprsManageVO vo = createExcelEntrprsManageVO(
                        entrprsmberId,
                        entrprsMberPassword,
                        bizrno,
                        jurirno,
                        cmpnyNm,
                        applcntIhidnum,
                        applcntIhidnum2,
                        cxfc,
                        applcntNm,
                        representativePhone);

                boolean firstMemberRow = fileIdSet.add(entrprsmberId);

                try {
                    // 같은 기업회원ID는 COMTNENTRPRSMBER에 최초 1회만 등록
                    if (firstMemberRow) {
                        entrprsManageService.insertEntrprsmberExcel(vo);
                    }

                    // 사업자등록번호는 행마다 COMTNENTRPRSBIZR에 등록
                    entrprsManageService.insertEntrprsmberBizrExcel(vo);
                    successCount++;
                } catch (Exception e) {
                    if (firstMemberRow) {
                        fileIdSet.remove(entrprsmberId);
                    }
                    failCount++;
                    errorList.add(excelRowNo + "행: 등록 처리 중 오류가 발생했습니다. ["
                            + entrprsmberId + " / " + bizrno + "]");
                }
            }

        } catch (Exception e) {
            model.addAttribute("resultMessage", "엑셀 파일을 읽을 수 없습니다. 파일이 손상되었거나 올바른 엑셀 파일이 아닌지 확인해 주세요.");
            return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
        }

        if (totalCount == 0) {
            model.addAttribute("resultMessage", "등록할 데이터가 없습니다.");
        } else {
            model.addAttribute("resultMessage", "엑셀 대량등록 처리가 완료되었습니다.");
        }

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failCount", failCount);
        model.addAttribute("errorList", errorList);

        return "egovframework/com/uss/umt/EgovEntrprsMberExcelUpload";
    }

    private EntrprsManageVO createExcelEntrprsManageVO(String entrprsmberId,
                                                         String entrprsMberPassword,
                                                         String bizrno,
                                                         String jurirno,
                                                         String cmpnyNm,
                                                         String applcntIhidnum,
                                                         String applcntIhidnum2,
                                                         String cxfc,
                                                         String applcntNm,
                                                         String representativePhone) {
        EntrprsManageVO vo = new EntrprsManageVO();

        // 엑셀 입력값
        vo.setEntrprsmberId(entrprsmberId);
        vo.setEntrprsMberPassword(entrprsMberPassword);
        vo.setBizrno(bizrno);
        vo.setJurirno(jurirno);
        vo.setCmpnyNm(cmpnyNm);
        vo.setApplcntIhidnum(applcntIhidnum);
        vo.setApplcntIhidnum2(applcntIhidnum2);
        vo.setCxfc(cxfc);
        vo.setApplcntNm(applcntNm);

        // 대표자 연락처는 선택값이다.
        // DB 컬럼이 NOT NULL이므로 미입력 시 NULL 대신 빈 문자열을 저장한다.
        if (representativePhone == null || representativePhone.trim().isEmpty()) {
            vo.setAreaNo("");
            vo.setEntrprsMiddleTelno("");
            vo.setEntrprsEndTelno("");
        } else {
            String[] phone = representativePhone.split("-", -1);
            vo.setAreaNo(phone[0].trim());
            vo.setEntrprsMiddleTelno(phone[1].trim());
            vo.setEntrprsEndTelno(phone[2].trim());
        }

        // 사용자 지정 고정값
        vo.setEntrprsSeCode("C0000002");
        vo.setZip("123456");
        vo.setAdres("주소");
        vo.setFxnum("12345");
        vo.setIndutyCode("Z");
        vo.setEntrprsMberSttus("P");

        // 비밀번호는 엑셀 입력값을 사용하고,
        // ServiceImpl에서 기존 기업회원 로그인용 단방향 암호화와
        // 홈택스 로그인용 양방향 암호화를 각각 처리한다.
        vo.setEntrprsMberPasswordHint("P01");
        vo.setEntrprsMberPasswordCnsr("대량업로드");
        vo.setGroupId("GROUP_00000000000001");
        vo.setDetailAdres("상세주소");
        vo.setApplcntEmailAdres("test@aa.com");
        vo.setLockAt("N");

        return vo;
    }

    private boolean isValidHeader(Row row, DataFormatter formatter) {
        if (row == null) {
            return false;
        }

        for (int i = 0; i < HEADER_KR.length; i++) {
            String value = getCellValue(row, i, formatter);
            if (!HEADER_KR[i].equals(value) && !HEADER_DB[i].equals(value)) {
                return false;
            }
        }

        return true;
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (int i = 0; i < HEADER_KR.length; i++) {
            if (!getCellValue(row, i, formatter).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null) {
            return "";
        }

        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private String onlyNumber(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }

    private String validateRow(String entrprsmberId,
                               String entrprsMberPassword,
                               String bizrno,
                               String jurirno,
                               String cmpnyNm,
                               String applcntIhidnum,
                               String applcntIhidnum2,
                               String cxfc,
                               String applcntNm,
                               String representativePhone) {
        if (entrprsmberId == null || entrprsmberId.isEmpty()) {
            return "기업회원ID는 필수입니다.";
        }
        if (entrprsmberId.length() > 20) {
            return "기업회원ID는 20자 이하로 입력해 주세요.";
        }
        if (entrprsMberPassword == null || entrprsMberPassword.isEmpty()) {
            return "비밀번호는 필수입니다.";
        }
        if (bizrno == null || bizrno.isEmpty()) {
            return "사업자등록번호는 필수입니다.";
        }
        if (bizrno.length() != 10) {
            return "사업자등록번호는 숫자 10자리여야 합니다.";
        }
        if (jurirno != null && !jurirno.isEmpty() && jurirno.length() != 13) {
            return "법인등록번호는 입력하는 경우 숫자 13자리여야 합니다.";
        }
        if (cmpnyNm == null || cmpnyNm.isEmpty()) {
            return "회사명은 필수입니다.";
        }
        if (cmpnyNm.length() > 60) {
            return "회사명은 60자 이하로 입력해 주세요.";
        }
        if (applcntIhidnum == null || applcntIhidnum.isEmpty()) {
            return "주민등록번호 앞 6자리는 필수입니다.";
        }
        if (!applcntIhidnum.matches("\\d{6}")) {
            return "주민등록번호 앞 6자리는 숫자 6자리로 입력해 주세요.";
        }
        if (applcntIhidnum2 == null || applcntIhidnum2.isEmpty()) {
            return "주민등록번호 2번째 값은 필수입니다.";
        }
        if (!applcntIhidnum2.matches("\\d")) {
            return "주민등록번호 2번째 값은 숫자 1자리로 입력해 주세요.";
        }
        if (cxfc == null || cxfc.isEmpty()) {
            return "대표이사이름은 필수입니다.";
        }
        if (cxfc.length() > 50) {
            return "대표이사이름은 50자 이하로 입력해 주세요.";
        }
        if (applcntNm == null || applcntNm.isEmpty()) {
            return "신청자명은 필수입니다.";
        }
        if (applcntNm.length() > 50) {
            return "신청자명은 50자 이하로 입력해 주세요.";
        }
        // 대표자 연락처는 선택값이다. 입력된 경우에만 형식을 검증한다.
        if (representativePhone != null && !representativePhone.trim().isEmpty()) {
            String[] phone = representativePhone.split("-", -1);
            if (phone.length != 3 || phone[0].trim().isEmpty() || phone[1].trim().isEmpty() || phone[2].trim().isEmpty()) {
                return "대표자 연락처는 지역번호-중간번호-끝번호 형식으로 입력해 주세요. 예: 02-1234-5678";
            }
            if (!phone[0].trim().matches("\\d{1,4}")) {
                return "대표자 연락처의 지역번호는 숫자 1~4자리여야 합니다.";
            }
            if (!phone[1].trim().matches("\\d{1,4}")) {
                return "대표자 연락처의 중간번호는 숫자 1~4자리여야 합니다.";
            }
            if (!phone[2].trim().matches("\\d{1,4}")) {
                return "대표자 연락처의 끝번호는 숫자 1~4자리여야 합니다.";
            }
        }

        return null;
    }
}
