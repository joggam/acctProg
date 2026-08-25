package com.hometax.com;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 홈택스 내려받기 진행상태 저장소.
 *
 * 브라우저의 진행률 polling 용도로만 사용한다.
 * 비밀번호/주민등록번호 등 민감정보는 저장하지 않는다.
 */
public final class HometaxProgressTracker {

    private static final Map<String, Progress> PROGRESS_MAP =
            new ConcurrentHashMap<String, Progress>();

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
        progress.finished = false;
        progress.cancelRequested = false;
        progress.status = "RUNNING";
        progress.message = "";
        progress.type = type == null ? "" : type;

        PROGRESS_MAP.put(
                jobId,
                progress
        );
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

        Progress progress =
                PROGRESS_MAP.get(jobId);

        if (progress == null) {
            return;
        }

        progress.cancelRequested = true;
        progress.status = "CANCEL_REQUESTED";
        progress.message = "취소 요청 처리중";
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

        private volatile boolean finished;

        private volatile boolean cancelRequested;

        private volatile String status;

        private volatile String message;

        private volatile String type;
    }
}
