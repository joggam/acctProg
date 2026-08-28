package vat.home.card.web;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import egovframework.com.cmm.annotation.IncludedInfo;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import vat.home.card.service.VatCardCondition1KeywordVO;
import vat.home.card.service.VatCardCondition1Service;
import vat.home.card.service.VatCardCondition1VO;

@Controller
public class VatCardCondition1Controller {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final String[] EXCEL_HEADERS = {"업태", "업종", "부가세공제여부", "부가세유형(2자리)", "계정과목"};

    @Resource(name = "vatCardCondition1Service")
    private VatCardCondition1Service service;

    @IncludedInfo(
            name = "조건1관리",
            listUrl = "/vat/home/card/list.do",
            order = 12,
            gid = 1
    )
    @RequestMapping("/vat/home/card/list.do")
    public String list(@ModelAttribute("searchVO") VatCardCondition1VO searchVO, ModelMap model) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        PaginationInfo p = pagination(searchVO.getPageIndex(), searchVO.getPageUnit(), searchVO.getPageSize(), service.selectCondition1ListTotCnt(searchVO));
        searchVO.setFirstIndex(p.getFirstRecordIndex()); searchVO.setRecordCountPerPage(p.getRecordCountPerPage());
        model.addAttribute("resultList", service.selectCondition1List(searchVO)); model.addAttribute("paginationInfo", p);
        return "vat/home/card/VatCardCondition1List";
    }

    @RequestMapping("/vat/home/card/form.do")
    public String form(@RequestParam(value="condition1Seq", required=false) Long seq, ModelMap model) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        VatCardCondition1VO vo = seq == null ? new VatCardCondition1VO() : service.selectCondition1(seq);
        if (vo == null) vo = new VatCardCondition1VO();
        model.addAttribute("condition1VO", vo); return "vat/home/card/VatCardCondition1Form";
    }

    @RequestMapping("/vat/home/card/save.do")
    public String save(@ModelAttribute VatCardCondition1VO vo) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        normalizeAndValidate(vo);
        if (vo.getCondition1Seq() == null) service.insertCondition1(vo); else service.updateCondition1(vo);
        return "redirect:/vat/home/card/list.do";
    }

    @RequestMapping("/vat/home/card/delete.do")
    public String delete(@RequestParam("condition1Seq") Long seq) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        service.deleteCondition1(seq); return "redirect:/vat/home/card/list.do";
    }

    @RequestMapping("/vat/home/card/excelView.do")
    public String excelView(ModelMap model) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        return "vat/home/card/VatCardCondition1ExcelUpload";
    }

    @RequestMapping("/vat/home/card/excelSample.do")
    public void excelSample(HttpServletResponse response) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) { response.sendError(401); return; }
        String fileName = "조건1_대량등록_양식.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"));
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ServletOutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("조건1"); Font font = workbook.createFont(); font.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle(); headerStyle.setFont(font);
            CellStyle textStyle = workbook.createCellStyle(); textStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));
            Row header = sheet.createRow(0);
            for (int i=0; i<EXCEL_HEADERS.length; i++) { Cell c=header.createCell(i); c.setCellValue(EXCEL_HEADERS[i]); c.setCellStyle(headerStyle); sheet.setDefaultColumnStyle(i,textStyle); sheet.setColumnWidth(i, 24*256); }
            workbook.write(out);
        }
    }

    @RequestMapping("/vat/home/card/excelUpload.do")
    public String excelUpload(@RequestParam("excelFile") MultipartFile excelFile, ModelMap model) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        if (excelFile == null || excelFile.isEmpty()) { model.addAttribute("resultMessage","업로드할 엑셀 파일을 선택해 주세요."); return "vat/home/card/VatCardCondition1ExcelUpload"; }
        if (excelFile.getSize() > MAX_FILE_SIZE) { model.addAttribute("resultMessage","엑셀 파일은 10MB 이하만 업로드할 수 있습니다."); return "vat/home/card/VatCardCondition1ExcelUpload"; }
        String name = excelFile.getOriginalFilename() == null ? "" : excelFile.getOriginalFilename().toLowerCase(Locale.KOREA);
        if (!(name.endsWith(".xls") || name.endsWith(".xlsx"))) { model.addAttribute("resultMessage","xls 또는 xlsx 파일만 업로드할 수 있습니다."); return "vat/home/card/VatCardCondition1ExcelUpload"; }

        int total=0, success=0, fail=0; List<String> errors = new ArrayList<String>(); DataFormatter formatter = new DataFormatter(Locale.KOREA);
        try (InputStream in = excelFile.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (!validHeader(sheet.getRow(0), formatter)) { model.addAttribute("resultMessage","엑셀 양식이 올바르지 않습니다. 업태 / 업종 / 부가세공제여부 / 부가세유형(2자리) / 계정과목 순서로 작성해 주세요."); return "vat/home/card/VatCardCondition1ExcelUpload"; }
            for (int r=1; r<=sheet.getLastRowNum(); r++) {
                Row row=sheet.getRow(r); if (row==null || blankRow(row,formatter)) continue; total++;
                VatCardCondition1VO vo = new VatCardCondition1VO();
                vo.setBizcnd(cell(row,0,formatter)); vo.setInduty(cell(row,1,formatter)); vo.setVatDeductYn(cell(row,2,formatter));
                vo.setVatTypeCode(cell(row,3,formatter)); vo.setAccountCode(cell(row,4,formatter)); vo.setUseAt("Y");
                try { normalizeAndValidate(vo); service.saveCondition1Excel(vo); success++; }
                catch (Exception e) { fail++; errors.add((r+1)+"행: "+safeMessage(e)); }
            }
        }
        model.addAttribute("resultMessage", "총 "+total+"건 / 성공 "+success+"건 / 실패 "+fail+"건"); model.addAttribute("errorList", errors);
        return "vat/home/card/VatCardCondition1ExcelUpload";
    }

    @RequestMapping("/vat/home/card/keyword/list.do")
    public String keywordList(@ModelAttribute("searchVO") VatCardCondition1KeywordVO searchVO, ModelMap model) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        PaginationInfo p = pagination(searchVO.getPageIndex(), searchVO.getPageUnit(), searchVO.getPageSize(), service.selectKeywordListTotCnt(searchVO));
        searchVO.setFirstIndex(p.getFirstRecordIndex()); searchVO.setRecordCountPerPage(p.getRecordCountPerPage());
        model.addAttribute("resultList", service.selectKeywordList(searchVO)); model.addAttribute("paginationInfo", p);
        return "vat/home/card/VatCardCondition1KeywordList";
    }

    @RequestMapping("/vat/home/card/keyword/form.do")
    public String keywordForm(@RequestParam(value="keywordSeq", required=false) Long seq, ModelMap model) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        VatCardCondition1KeywordVO vo = seq == null ? new VatCardCondition1KeywordVO() : service.selectKeyword(seq);
        if (vo == null) vo = new VatCardCondition1KeywordVO();
        model.addAttribute("keywordVO", vo); return "vat/home/card/VatCardCondition1KeywordForm";
    }

    @RequestMapping("/vat/home/card/keyword/save.do")
    public String keywordSave(@ModelAttribute VatCardCondition1KeywordVO vo) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        normalizeAndValidate(vo); if (vo.getKeywordSeq()==null) service.insertKeyword(vo); else service.updateKeyword(vo);
        return "redirect:/vat/home/card/keyword/list.do";
    }

    @RequestMapping("/vat/home/card/keyword/delete.do")
    public String keywordDelete(@RequestParam("keywordSeq") Long seq) throws Exception {
        if (!EgovUserDetailsHelper.isAuthenticated()) return "index";
        service.deleteKeyword(seq); return "redirect:/vat/home/card/keyword/list.do";
    }

    private PaginationInfo pagination(int pageIndex, int pageUnit, int pageSize, int total) {
        if (pageUnit <= 0) pageUnit=20; if (pageSize <= 0) pageSize=10;
        PaginationInfo p=new PaginationInfo(); p.setCurrentPageNo(pageIndex); p.setRecordCountPerPage(pageUnit); p.setPageSize(pageSize); p.setTotalRecordCount(total); return p;
    }
    private void normalizeAndValidate(VatCardCondition1VO vo) {
        vo.setBizcnd(trim(vo.getBizcnd())); vo.setInduty(trim(vo.getInduty())); vo.setVatDeductYn(trim(vo.getVatDeductYn())); vo.setVatTypeCode(trim(vo.getVatTypeCode())); vo.setAccountCode(trim(vo.getAccountCode())); if (trim(vo.getUseAt()).length()==0) vo.setUseAt("Y");
        if (vo.getBizcnd().length()==0) throw new IllegalArgumentException("업태는 필수입니다."); if (vo.getInduty().length()==0) throw new IllegalArgumentException("업종은 필수입니다."); if (vo.getVatTypeCode().length()>0 && vo.getVatTypeCode().length()!=2) throw new IllegalArgumentException("부가세유형은 2자리로 입력해 주세요.");
    }
    private void normalizeAndValidate(VatCardCondition1KeywordVO vo) {
        vo.setKeywordType(trim(vo.getKeywordType()).toUpperCase(Locale.ENGLISH)); vo.setTargetType(trim(vo.getTargetType()).toUpperCase(Locale.ENGLISH)); vo.setKeyword(trim(vo.getKeyword())); vo.setAccountCode(trim(vo.getAccountCode())); if (trim(vo.getUseAt()).length()==0) vo.setUseAt("Y");
        if (!("CORP".equals(vo.getKeywordType()) || "EMPLOYEE".equals(vo.getKeywordType()) || "VEHICLE".equals(vo.getKeywordType()))) throw new IllegalArgumentException("키워드구분이 올바르지 않습니다.");
        if (!("BIZCND".equals(vo.getTargetType()) || "INDUTY".equals(vo.getTargetType()))) throw new IllegalArgumentException("적용대상이 올바르지 않습니다.");
        if (vo.getKeyword().length()==0) throw new IllegalArgumentException("키워드는 필수입니다."); if (vo.getAccountCode().length()==0) throw new IllegalArgumentException("계정과목은 필수입니다.");
    }
    private boolean validHeader(Row row, DataFormatter f) { if (row==null) return false; for(int i=0;i<EXCEL_HEADERS.length;i++) if(!EXCEL_HEADERS[i].equals(cell(row,i,f).replaceAll("\\s+",""))) return false; return true; }
    private boolean blankRow(Row row, DataFormatter f) { for(int i=0;i<EXCEL_HEADERS.length;i++) if(cell(row,i,f).length()>0) return false; return true; }
    private String cell(Row row,int idx,DataFormatter f) { Cell c=row.getCell(idx); return c==null?"":trim(f.formatCellValue(c)); }
    private String trim(String s) { return s==null?"":s.trim(); }
    private String safeMessage(Exception e) { String m=e.getMessage(); return (m==null||m.trim().length()==0)?e.getClass().getSimpleName():m; }
}
