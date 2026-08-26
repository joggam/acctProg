package com.hometax.com;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;

/**
 * 홈택스 내려받기 진행상태 저장소.
 *
 * 브라우저의 진행률 polling 용도로만 사용한다.
 * 비밀번호/주민등록번호 등 민감정보는 저장하지 않는다.
 */
public final class HometaxProgressTracker {

    private static final Map<String, Progress> PROGRESS_MAP =
            new ConcurrentHashMap<String, Progress>();

    /**
     * 일반 내려받기는 기존 Service 시그니처를 바꾸지 않기 위해
     * 현재 요청 Thread의 jobId를 전달하는 용도로만 사용한다.
     */
    private static final ThreadLocal<String> CURRENT_JOB_ID =
            new ThreadLocal<String>();

    private HometaxProgressTracker() {
    }

    public static void start(
            String jobId,
            int totalCount,
            String type) {

        if (jobId == null || jobId.trim().length() == 0) {
            return;
        }

        Progress progress = new Progress();

        progress.totalCount = Math.max(totalCount, 0);
        progress.completedCount = 0;
        progress.currentCompany = "";
        progress.startTime = System.currentTimeMillis();
        progress.lastHeartbeatTime = System.currentTimeMillis();
        progress.finished = false;
        progress.cancelRequested = false;
        progress.cancelSource = "";
        progress.status = "RUNNING";
        progress.message = "";
        progress.type = type == null ? "" : type;

        PROGRESS_MAP.put(
                jobId,
                progress
        );
    }

    /**
     * 진행률/예상 잔여시간 화면 갱신 시각을 기록한다.
     *
     * 중요:
     * 이 heartbeat가 끊겼다는 이유만으로 작업을 취소하지 않는다.
     * 취소는 명시적인 BUTTON 또는 PAGE_CLOSE 요청으로만 수행한다.
     */
    public static void heartbeat(
            String jobId) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null
                || progress.finished) {
            return;
        }

        progress.lastHeartbeatTime =
                System.currentTimeMillis();
    }

    public static void setCurrent(
            String jobId,
            String currentCompany) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.currentCompany =
                currentCompany == null
                ? ""
                : currentCompany.trim();
    }

    public static void completeOne(
            String jobId) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        if (progress.completedCount
                < progress.totalCount) {

            progress.completedCount++;
        }
    }

    /**
     * 사용자가 화면에서 취소 버튼을 누른 상태로 변경한다.
     * 현재 처리 중인 업체는 안전하게 마친 뒤 다음 업체부터 중단한다.
     */
    public static void requestCancel(
            String jobId) {

        requestCancel(
                jobId,
                "BUTTON"
        );
    }

    /**
     * 취소 원인을 함께 기록한다.
     *
     * BUTTON     : 화면의 취소 버튼 클릭
     * PAGE_CLOSE : 처리중인 웹페이지/탭 종료 또는 heartbeat 단절
     */
    public static void requestCancel(
            String jobId,
            String cancelSource) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.cancelRequested = true;
        progress.cancelSource =
                cancelSource == null
                ? ""
                : cancelSource.trim();

        progress.status = "CANCEL_REQUESTED";
        progress.message = "취소 요청 처리중";

        stopActiveDriver(
                jobId,
                progress.cancelSource
        );
    }

    public static void setCurrentJobId(
            String jobId) {

        if (jobId == null
                || jobId.trim().length() == 0) {
            CURRENT_JOB_ID.remove();
            return;
        }

        CURRENT_JOB_ID.set(
                jobId
        );
    }

    public static void clearCurrentJobId() {
        CURRENT_JOB_ID.remove();
    }

    public static String getCurrentJobId() {
        return CURRENT_JOB_ID.get();
    }

    public static boolean isCurrentJobCancelled() {

        String jobId =
                getCurrentJobId();

        return jobId != null
                && isCancelRequested(jobId);
    }

    /**
     * HometaxLogin에서 ChromeDriver가 생성되는 즉시 현재 jobId와 연결한다.
     * 취소 요청이 이미 들어온 상태라면 등록 직후 바로 종료한다.
     */
    public static void registerCurrentDriver(
            WebDriver driver) {

        String jobId =
                getCurrentJobId();

        if (jobId == null
                || driver == null) {
            return;
        }

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.activeDriver = driver;

        if (progress.cancelRequested) {
            stopActiveDriver(
                    jobId,
                    progress.cancelSource
            );
        }
    }

    public static void unregisterCurrentDriver(
            WebDriver driver) {

        String jobId =
                getCurrentJobId();

        unregisterDriver(
                jobId,
                driver
        );
    }

    public static void unregisterDriver(
            String jobId,
            WebDriver driver) {

        if (jobId == null) {
            return;
        }

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        if (driver == null
                || progress.activeDriver == driver) {
            progress.activeDriver = null;
        }
    }

    private static void stopActiveDriver(
            String jobId,
            String cancelSource) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        WebDriver driver =
                progress.activeDriver;

        if (driver == null) {
            return;
        }

        // 중복 quit 방지: 먼저 참조를 비운다.
        progress.activeDriver = null;

        try {

            driver.quit();

            if ("PAGE_CLOSE".equals(cancelSource)) {
                System.out.println(
                        "[CANCEL-DRIVER-PAGE-CLOSE] 현재 Chrome 즉시 종료 완료"
                );
            } else {
                System.out.println(
                        "[CANCEL-DRIVER-BUTTON] 현재 Chrome 즉시 종료 완료"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "[CANCEL-DRIVER] Chrome 종료 중 예외 무시 / "
                    + firstLine(e.getMessage())
            );
        }
    }

    private static String firstLine(
            String value) {

        if (value == null
                || value.trim().length() == 0) {
            return "";
        }

        String result = value.trim();
        int lineBreak = result.indexOf('\n');

        if (lineBreak >= 0) {
            result = result.substring(0, lineBreak);
        }

        return result.trim();
    }


    public static String getCancelSource(
            String jobId) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null
                || progress.cancelSource == null) {

            return "";
        }

        return progress.cancelSource;
    }

    public static boolean isCancelRequested(
            String jobId) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        return progress != null
                && progress.cancelRequested;
    }

    public static void cancelled(
            String jobId,
            String message) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.cancelRequested = true;
        progress.finished = true;
        progress.status = "CANCELLED";
        progress.message =
                message == null
                ? "처리가 취소되었습니다."
                : message;
    }


    public static void finish(
            String jobId,
            String message) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.finished = true;
        progress.status = "SUCCESS";
        progress.message =
                message == null ? "" : message;

        if (progress.completedCount
                < progress.totalCount) {

            progress.completedCount =
                    progress.totalCount;
        }
    }

    public static void fail(
            String jobId,
            String message) {

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.finished = true;
        progress.status = "ERROR";
        progress.message =
                message == null ? "" : message;
    }

    public static Map<String, Object> getSnapshot(
            String jobId) {

        Map<String, Object> result =
                new HashMap<String, Object>();

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {

            result.put("exists", Boolean.FALSE);
            result.put("status", "WAITING");
            return result;
        }

        long now =
                System.currentTimeMillis();

        long elapsedSeconds =
                Math.max(
                        0L,
                        (now - progress.startTime) / 1000L
                );

        long remainSeconds = -1L;

        if (progress.completedCount > 0
                && progress.totalCount
                        > progress.completedCount) {

            double avgSeconds =
                    (double) elapsedSeconds
                    / (double) progress.completedCount;

            remainSeconds =
                    Math.round(
                            avgSeconds
                            * (progress.totalCount
                                    - progress.completedCount)
                    );
        }

        int percent = 0;

        if (progress.totalCount > 0) {

            percent =
                    (int) Math.floor(
                            ((double) progress.completedCount
                                    / (double) progress.totalCount)
                            * 100.0
                    );
        }

        if (progress.finished
                && "SUCCESS".equals(progress.status)) {

            percent = 100;
            remainSeconds = 0L;
        }

        result.put("exists", Boolean.TRUE);
        result.put("type", progress.type);
        result.put("status", progress.status);
        result.put("finished", progress.finished);
        result.put("cancelRequested", progress.cancelRequested);
        result.put("cancelSource", progress.cancelSource);
        result.put("totalCount", progress.totalCount);
        result.put("completedCount", progress.completedCount);
        result.put("currentCompany", progress.currentCompany);
        result.put("percent", percent);
        result.put("elapsedSeconds", elapsedSeconds);
        result.put("remainSeconds", remainSeconds);
        result.put("message", progress.message);

        return result;
    }

    private static class Progress {

        private volatile int totalCount;

        private volatile int completedCount;

        private volatile String currentCompany;

        private volatile long startTime;

        private volatile long lastHeartbeatTime;

        private volatile boolean finished;

        private volatile boolean cancelRequested;

        private volatile String cancelSource;

        private volatile WebDriver activeDriver;

        private volatile String status;

        private volatile String message;

        private volatile String type;
    }
}
