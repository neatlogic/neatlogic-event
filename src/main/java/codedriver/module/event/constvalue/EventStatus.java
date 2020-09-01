package codedriver.module.event.constvalue;

public enum EventStatus {
    PENDING("pending", "待处理","#8E949F"),
    RUNNING("running", "进行中","#2d84fb"),
    PAUSED("paused", "已暂停","#8E949F"),
    ABORTED("aborted", "已取消","#F9A825"),
    SUCCEED("succeed", "已成功","#25b865"),
    FAILED("failed", "已失败","#f71010");
    private String status;
    private String text;
    private String color;
 
    private EventStatus(String _status, String _text,String _color) {
        this.status = _status;
        this.text = _text;
        this.color = _color;
    }

    public String getValue() {
        return status;
    }

    public String getText() {
        return text;
    }

    public String getColor() {
        return color;
    }

//    public static String getText(String _status) {
//        for (EventStatus s : EventStatus.values()) {
//            if (s.getValue().equals(_status)) {
//                return s.getText();
//            }
//        }
//        return "";
//    }
//    
//    public static String getColor(String _status) {
//        for (EventStatus s : EventStatus.values()) {
//            if (s.getValue().equals(_status)) {
//                return s.getColor();
//            }
//        }
//        return "";
//    }
}
